import json
import math
import os

import chromadb
import httpx
from openai import AsyncOpenAI
from dotenv import load_dotenv

from schemas.place import PlaceRequest
from tools.osm_tools import find_building_by_osm

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
KAKAO_REST_API_KEY = os.getenv("KAKAO_REST_API_KEY", "")

openai_client = AsyncOpenAI(api_key=OPENAI_API_KEY)

_chroma_client = None
_collection = None


def _get_collection():
    global _chroma_client, _collection
    if _collection is None:
        from chromadb.utils.embedding_functions import DefaultEmbeddingFunction
        _chroma_client = chromadb.PersistentClient(path="./chroma_db")
        _collection = _chroma_client.get_or_create_collection(
            "place_info", embedding_function=DefaultEmbeddingFunction()
        )
    return _collection


# ── 거리 계산 (Haversine) ───────────────────────────────────────────────────

def _haversine(lat1: float, lng1: float, lat2: float, lng2: float) -> float:
    """두 GPS 좌표 사이의 거리(미터) 반환."""
    R = 6_371_000
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lng2 - lng1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))


# ── 1순위: 좌표 기반 Chroma 조회 ────────────────────────────────────────────

def _find_by_coords(collection, bld_lat: float, bld_lng: float, threshold_m: float = 200.0) -> dict:
    """
    Chroma DB에 저장된 건물들과 교차점 좌표의 거리를 계산해
    가장 가까운 건물(threshold 이내)의 metadata 반환.
    이름 매핑 없이 좌표만으로 건물을 식별하므로 OSM/Kakao 이름 불일치 문제 없음.
    """
    all_docs = collection.get(include=["metadatas", "ids"])
    best_meta = None
    best_dist = float("inf")

    for meta, doc_id in zip(all_docs.get("metadatas", []), all_docs.get("ids", [])):
        lat = meta.get("lat")
        lng = meta.get("lng")
        if lat is None or lng is None:
            continue
        d = _haversine(bld_lat, bld_lng, lat, lng)
        if d < best_dist:
            best_dist = d
            best_meta = meta

    if best_meta and best_dist <= threshold_m:
        print(f"[Chroma] 좌표 매칭: {best_meta.get('name_ko', '')} (거리={best_dist:.0f}m)")
        return best_meta

    print(f"[Chroma] 좌표 매칭 실패 (최근접={best_dist:.0f}m, threshold={threshold_m}m)")
    return {}


# ── 2순위: Kakao coord2address → Chroma 벡터 검색 ───────────────────────────

async def _kakao_coord2building_name(lat: float, lng: float) -> str:
    """Kakao coord2address API로 좌표 → 건물명 조회."""
    if not KAKAO_REST_API_KEY:
        return ""
    url = "https://dapi.kakao.com/v2/local/geo/coord2address.json"
    headers = {"Authorization": f"KakaoAK {KAKAO_REST_API_KEY}"}
    params = {"x": lng, "y": lat, "input_coord": "WGS84"}
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.get(url, headers=headers, params=params)
            data = resp.json()
        docs = data.get("documents", [])
        if docs:
            road = docs[0].get("road_address") or {}
            return road.get("building_name", "")
    except Exception as e:
        print(f"[Kakao] coord2address 오류: {e}")
    return ""


async def _find_by_kakao_coords(collection, bld_lat: float, bld_lng: float) -> dict:
    """Kakao로 건물명 조회 후 Chroma 벡터 유사도 검색."""
    kakao_name = await _kakao_coord2building_name(bld_lat, bld_lng)
    if not kakao_name:
        return {}
    print(f"[Kakao] coord2address 결과: {kakao_name!r}")
    try:
        q = collection.query(query_texts=[kakao_name], n_results=1)
        if q["metadatas"] and q["metadatas"][0] and q["distances"][0][0] < 0.6:
            print(f"[Chroma] 벡터 검색 매칭: {q['metadatas'][0][0].get('name_ko', '')} "
                  f"(거리={q['distances'][0][0]:.3f})")
            return q["metadatas"][0][0]
    except Exception:
        pass
    return {}


# ── LLM: docent 해설 생성 ──────────────────────────────────────────────────────

async def llm_generate_docent(context: str, language: str) -> str:
    lang_map = {"ko": "Korean", "en": "English", "ar": "Arabic", "ja": "Japanese", "zh": "Chinese"}
    response_lang_label = lang_map.get(language, language)

    system_prompt = (
        "You are a friendly AR tour guide for foreign visitors in Seoul. "
        "Respond in 2-3 short sentences suitable for text-to-speech. "
        f"Always respond in {response_lang_label}. "
        "If halal_info is provided, always mention it. "
        "Be warm, concise, and helpful for a solo traveler."
    )

    response = await openai_client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": context},
        ],
        max_tokens=300,
    )
    return response.choices[0].message.content.strip()


# ── Follow-up 질문 생성 ────────────────────────────────────────────────────────

def generate_follow_ups(user_message: str, place_data: dict) -> list[str]:
    suggestions = []
    msg_lower = user_message.lower()

    has_floor_info = bool(place_data.get("floor_info"))
    has_halal = bool(place_data.get("halal_info"))
    has_admission = bool(place_data.get("admission_fee"))
    has_parking = bool(place_data.get("parking_info"))

    if "floor" not in msg_lower and has_floor_info:
        suggestions.append("What's on each floor?")
    if "halal" not in msg_lower and has_halal:
        suggestions.append("Where can I find halal food nearby?")
    if "fee" not in msg_lower and "price" not in msg_lower and has_admission:
        suggestions.append("How much is the admission fee?")
    if "park" not in msg_lower and has_parking:
        suggestions.append("Is there parking available?")
    if "eat" not in msg_lower and "restaurant" not in msg_lower:
        suggestions.append("What's nearby to eat?")
    if "prayer" not in msg_lower:
        suggestions.append("Is there a prayer room nearby?")

    return suggestions[:3]


# ── Main agent ────────────────────────────────────────────────────────────────

async def run_place_insight_agent(req: PlaceRequest) -> dict:
    collection = _get_collection()
    place_data = {}

    # OSM 레이캐스팅 → 좌표 기반 매칭 → Kakao fallback
    if req.heading is not None:
        osm_name, bld_lat, bld_lng = await find_building_by_osm(
            req.user_lat, req.user_lng, req.heading
        )

        if bld_lat != 0.0:
            # 1순위: 폴리곤 중심 좌표 → Chroma 거리 기반 조회
            place_data = _find_by_coords(collection, bld_lat, bld_lng)

            # 2순위: Kakao coord2address → 건물명 → Chroma 벡터 검색
            if not place_data:
                place_data = await _find_by_kakao_coords(collection, bld_lat, bld_lng)

    # 끝내 데이터 없으면 미인식 응답
    if not place_data:
        return {
            "ar_overlay": {
                "name": "",
                "category": "",
                "floor_info": [],
                "halal_info": "",
                "image_url": "",
                "homepage": "",
                "open_hours": "",
                "closed_days": "",
                "parking_info": "",
                "admission_fee": "",
                "is_estimated": False,
            },
            "docent": {
                "speech": "죄송합니다, 이 건물에 대한 정보가 아직 없습니다.",
                "follow_up_suggestions": [],
            },
        }

    floor_info = json.loads(place_data.get("floor_info", "[]"))
    halal_info = place_data.get("halal_info", "")

    ar_overlay = {
        "name":          place_data.get("name_ko", ""),
        "category":      place_data.get("category", ""),
        "floor_info":    floor_info,
        "halal_info":    halal_info,
        "image_url":     place_data.get("image_url", ""),
        "homepage":      place_data.get("homepage", ""),
        "open_hours":    place_data.get("open_hours", ""),
        "closed_days":   place_data.get("closed_days", ""),
        "parking_info":  place_data.get("parking_info", ""),
        "admission_fee": place_data.get("admission_fee", ""),
        "is_estimated":  False,
    }

    context = f"""
Place: {place_data.get('name_ko', '')}
Category: {place_data.get('category', '')}
Description: {place_data.get('description_en', '')}
Open hours: {place_data.get('open_hours', '')}
Closed days: {place_data.get('closed_days', '')}
Admission fee: {place_data.get('admission_fee', '')}
Halal info: {halal_info}
User's question: {req.user_message}
Language: {req.language}
""".strip()

    speech = await llm_generate_docent(context, req.language)
    follow_ups = generate_follow_ups(req.user_message, {
        "floor_info":    floor_info,
        "halal_info":    halal_info,
        "admission_fee": place_data.get("admission_fee", ""),
        "parking_info":  place_data.get("parking_info", ""),
    })

    return {
        "ar_overlay": ar_overlay,
        "docent": {
            "speech": speech,
            "follow_up_suggestions": follow_ups,
        },
    }
