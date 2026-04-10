import math
import httpx
from shapely.geometry import LineString, Point, Polygon

OVERPASS_URL = "https://overpass-api.de/api/interpreter"


async def find_building_by_osm(
    lat: float, lng: float, heading: float
) -> tuple[str, float, float]:
    """
    ARCore VPS가 제공하는 GPS 위치(lat, lng)와 카메라 방위각(heading)으로
    Overpass API에서 OSM 건물 폴리곤을 가져와 레이캐스팅.

    Returns:
        (osm_name, centroid_lat, centroid_lng)
        - osm_name: OSM tag의 건물명 (없으면 빈 문자열)
        - centroid_lat/lng: 교차 건물 폴리곤 중심 좌표 (교차 없으면 0.0, 0.0)
    """
    buildings = await _fetch_osm_buildings(lat, lng, radius=300)
    if not buildings:
        return ("", 0.0, 0.0)

    return _raycast(lat, lng, heading, buildings)


async def _fetch_osm_buildings(lat: float, lng: float, radius: int) -> list:
    query = f"""
[out:json][timeout:15];
way["building"](around:{radius},{lat},{lng});
out geom tags;
"""
    try:
        async with httpx.AsyncClient(timeout=20.0) as client:
            resp = await client.post(OVERPASS_URL, data={"data": query})
            resp.raise_for_status()
            return resp.json().get("elements", [])
    except Exception as e:
        print(f"[OSM] Overpass API 오류: {e}")
        return []


def _raycast(
    lat: float, lng: float, heading: float, buildings: list
) -> tuple[str, float, float]:
    """
    heading(0=북, 90=동, 180=남, 270=서) 방향으로 300m 레이를 쏴서
    가장 먼저 교차하는 OSM 건물의 (이름, 폴리곤 중심 lat, 폴리곤 중심 lng) 반환.
    """
    rad = math.radians(heading)
    dist_deg = 0.003  # ~300m (위도 기준)

    end_lng = lng + dist_deg * math.sin(rad)
    end_lat = lat + dist_deg * math.cos(rad)
    ray = LineString([(lng, lat), (end_lng, end_lat)])
    user_point = Point(lng, lat)

    best_name = ""
    best_dist = float("inf")
    best_centroid = (0.0, 0.0)

    for building in buildings:
        if building.get("type") != "way":
            continue
        geometry = building.get("geometry", [])
        if len(geometry) < 3:
            continue

        coords = [(node["lon"], node["lat"]) for node in geometry]
        try:
            polygon = Polygon(coords)
        except Exception:
            continue

        if not ray.intersects(polygon):
            continue

        intersection = ray.intersection(polygon)
        dist = user_point.distance(intersection)
        if dist < best_dist:
            best_dist = dist
            tags = building.get("tags", {})
            best_name = (
                tags.get("name:ko")
                or tags.get("name")
                or tags.get("name:en")
                or ""
            )
            centroid = polygon.centroid
            best_centroid = (centroid.y, centroid.x)  # (lat, lng)

    if best_centroid != (0.0, 0.0):
        print(f"[OSM] 레이캐스팅 결과: {best_name!r} "
              f"(heading={heading:.1f}°, centroid={best_centroid[0]:.5f},{best_centroid[1]:.5f})")
    else:
        print(f"[OSM] 교차 건물 없음 (heading={heading:.1f}°, 후보={len(buildings)}개)")

    return (best_name, best_centroid[0], best_centroid[1])
