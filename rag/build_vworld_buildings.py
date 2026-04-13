"""
build_vworld_buildings.py
VWorld WFS lt_c_bldginfo 레이어에서 명동 반경 2km 건물을 전부 수집해
rag/data/vworld_buildings.json 에 적재한다.

런타임에는 이 JSON을 메모리로 로드해 3D 레이캐스팅으로 건물을 인식한다.
벡터 검색이 필요 없으므로 Chroma 임베딩은 사용하지 않음 (수천 건 임베딩은 과도).

실행:
    cd scanpang-navigation-agent
    python -m rag.build_vworld_buildings
"""

import json
import math
import os
import time
from typing import Optional

import httpx
from dotenv import load_dotenv

load_dotenv()

VWORLD_API_KEY = os.getenv("VWORLD_API_KEY", "").strip()
VWORLD_DOMAIN = os.getenv("VWORLD_DOMAIN", "http://localhost")
VWORLD_URL = "http://api.vworld.kr/req/wfs"

# 명동 중심 좌표 (한국은행 근처)
CENTER_LAT = 37.5636
CENTER_LNG = 126.9822
RADIUS_M = 2000  # 반경 2km

# VWorld WFS 1요청당 최대 1000건 제한 → 영역을 격자로 쪼갬
# 명동 1.1km × 1.1km 박스에 약 1400개 건물 → 0.55km × 0.55km 격자가 적당
GRID_SIZE_DEG = 0.005  # 약 550m

# 층당 평균 높이 (m) — height 필드가 0일 때 grnd_flr로 fallback
FLOOR_HEIGHT_M = 3.3

# 출력 JSON 경로
OUTPUT_PATH = "rag/data/vworld_buildings.json"


# ── 좌표 변환 ────────────────────────────────────────────────────────────────

def meters_to_deg_lat(m: float) -> float:
    """미터 → 위도 도 (지구 반지름 고정값 사용)."""
    return m / 111_320.0


def meters_to_deg_lng(m: float, lat: float) -> float:
    """미터 → 경도 도 (위도에 따른 보정)."""
    return m / (111_320.0 * math.cos(math.radians(lat)))


def polygon_centroid(coords: list) -> tuple:
    """(lng, lat) 꼭짓점 리스트 → centroid (lat, lng)."""
    if not coords:
        return 0.0, 0.0
    # 단순 평균 (작은 폴리곤에서는 충분)
    lngs = [c[0] for c in coords]
    lats = [c[1] for c in coords]
    return sum(lats) / len(lats), sum(lngs) / len(lngs)


# ── VWorld WFS 쿼리 ──────────────────────────────────────────────────────────

def fetch_vworld_bbox(min_lat: float, min_lng: float, max_lat: float, max_lng: float) -> list:
    """
    단일 BBOX로 VWorld 건물 조회. WFS 1.1.0 + EPSG:4326은 lat,lng 순서.
    최대 1000건 반환. totalFeatures > 1000이면 격자를 더 쪼개야 함.
    """
    bbox = f"{min_lat},{min_lng},{max_lat},{max_lng},EPSG:4326"
    params = {
        "key": VWORLD_API_KEY,
        "domain": VWORLD_DOMAIN,
        "SERVICE": "WFS",
        "VERSION": "1.1.0",
        "REQUEST": "GetFeature",
        "TYPENAME": "lt_c_bldginfo",
        "OUTPUTFORMAT": "application/json",
        "SRSNAME": "EPSG:4326",
        "BBOX": bbox,
        "MAXFEATURES": 1000,
    }
    try:
        with httpx.Client(timeout=30.0) as client:
            resp = client.get(VWORLD_URL, params=params)
            resp.raise_for_status()
            data = resp.json()
    except Exception as e:
        print(f"  [ERROR] VWorld 쿼리 실패 ({bbox}): {e}")
        return []

    features = data.get("features", [])
    total = data.get("totalFeatures", len(features))
    if total > len(features):
        print(f"  [WARN] totalFeatures={total} > returned={len(features)} "
              f"— 일부 누락. 격자를 더 쪼개세요.")
    return features


# ── Feature 파싱 ─────────────────────────────────────────────────────────────

def extract_polygon_2d(geometry: dict) -> list:
    """
    VWorld geometry(MultiPolygon/Polygon) → 외곽 꼭짓점 리스트 [[lng, lat], ...]
    MultiPolygon일 경우 첫 번째 폴리곤만 사용 (가장 큰 부분).
    """
    gtype = geometry.get("type", "")
    coords = geometry.get("coordinates", [])
    if not coords:
        return []

    if gtype == "Polygon":
        return coords[0]  # outer ring
    elif gtype == "MultiPolygon":
        # coords = [[[outer], [hole], ...], ...]
        # 첫 번째 폴리곤의 outer ring
        return coords[0][0] if coords and coords[0] else []
    return []


def compute_height(props: dict) -> float:
    """
    건물 높이(미터) 계산.
    1순위: height 필드 (이미 미터 단위)
    2순위: grnd_flr × FLOOR_HEIGHT_M
    3순위: 기본값 10m (인식 후보에서 제외되지 않도록 최소값)
    """
    h = props.get("height") or 0
    if h and h > 0:
        return float(h)
    g = props.get("grnd_flr") or 0
    if g and g > 0:
        return float(g) * FLOOR_HEIGHT_M
    return 10.0


def parse_feature(feature: dict) -> Optional[dict]:
    """WFS feature → Chroma metadata dict. 유효하지 않으면 None."""
    geom = feature.get("geometry") or {}
    props = feature.get("properties") or {}

    polygon_2d = extract_polygon_2d(geom)
    if len(polygon_2d) < 3:
        return None

    center_lat, center_lng = polygon_centroid(polygon_2d)
    if center_lat == 0.0 or center_lng == 0.0:
        return None

    height = compute_height(props)
    ufid = props.get("ufid") or feature.get("id") or f"bld_{center_lat:.6f}_{center_lng:.6f}"

    return {
        "ufid": str(ufid),
        "bld_nm": props.get("bld_nm") or "",
        "center_lat": center_lat,
        "center_lng": center_lng,
        "height": height,
        "grnd_flr": int(props.get("grnd_flr") or 0),
        "ugrnd_flr": int(props.get("ugrnd_flr") or 0),
        "usability": props.get("usability") or "",
        "polygon_2d": json.dumps(polygon_2d, ensure_ascii=False),  # [[lng, lat], ...]
    }


# ── 격자 스윕 ────────────────────────────────────────────────────────────────

def generate_grid(center_lat: float, center_lng: float, radius_m: int, cell_deg: float) -> list:
    """중심 좌표 주변 반경을 cell_deg 격자로 쪼개서 BBOX 리스트 반환."""
    half_lat = meters_to_deg_lat(radius_m)
    half_lng = meters_to_deg_lng(radius_m, center_lat)

    min_lat = center_lat - half_lat
    max_lat = center_lat + half_lat
    min_lng = center_lng - half_lng
    max_lng = center_lng + half_lng

    boxes = []
    lat = min_lat
    while lat < max_lat:
        lng = min_lng
        while lng < max_lng:
            boxes.append((
                lat,
                lng,
                min(lat + cell_deg, max_lat),
                min(lng + cell_deg, max_lng),
            ))
            lng += cell_deg
        lat += cell_deg
    return boxes


# ── JSON 저장 ────────────────────────────────────────────────────────────────

def save_to_json(buildings: list):
    # 중복 ufid 제거 (격자 경계에 걸친 건물)
    seen = set()
    unique = []
    for b in buildings:
        if b["ufid"] in seen:
            continue
        seen.add(b["ufid"])
        unique.append(b)

    print(f"\n중복 제거: {len(buildings)} → {len(unique)}건")

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)
    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump({
            "center_lat": CENTER_LAT,
            "center_lng": CENTER_LNG,
            "radius_m": RADIUS_M,
            "count": len(unique),
            "buildings": unique,
        }, f, ensure_ascii=False)

    size_mb = os.path.getsize(OUTPUT_PATH) / 1024 / 1024
    print(f"✅ {len(unique)}건 저장 완료 → {OUTPUT_PATH} ({size_mb:.1f} MB)")


# ── Main ─────────────────────────────────────────────────────────────────────

def main():
    if not VWORLD_API_KEY:
        print("[ERROR] VWORLD_API_KEY 환경변수가 설정되지 않았습니다. .env 확인.")
        return

    print(f"VWorld 건물 수집 시작")
    print(f"  중심: ({CENTER_LAT}, {CENTER_LNG})")
    print(f"  반경: {RADIUS_M}m")
    print(f"  격자 셀: {GRID_SIZE_DEG}° (약 {GRID_SIZE_DEG * 111320:.0f}m)")

    boxes = generate_grid(CENTER_LAT, CENTER_LNG, RADIUS_M, GRID_SIZE_DEG)
    print(f"  총 격자 수: {len(boxes)}")

    all_buildings = []
    for idx, (min_lat, min_lng, max_lat, max_lng) in enumerate(boxes, 1):
        print(f"\n[{idx}/{len(boxes)}] BBOX: {min_lat:.4f},{min_lng:.4f},{max_lat:.4f},{max_lng:.4f}")
        features = fetch_vworld_bbox(min_lat, min_lng, max_lat, max_lng)
        print(f"  수신: {len(features)}건")

        parsed = 0
        for f in features:
            meta = parse_feature(f)
            if meta:
                all_buildings.append(meta)
                parsed += 1
        print(f"  파싱 성공: {parsed}건")

        time.sleep(0.3)  # VWorld rate limit 예방

    print(f"\n━━━ 전체 수집 결과 ━━━")
    print(f"  총 건물: {len(all_buildings)}")
    named = sum(1 for b in all_buildings if b["bld_nm"])
    with_height = sum(1 for b in all_buildings if b["height"] > 10.0)
    print(f"  이름 있음: {named}")
    print(f"  실제 높이값 있음: {with_height}")

    save_to_json(all_buildings)


if __name__ == "__main__":
    main()
