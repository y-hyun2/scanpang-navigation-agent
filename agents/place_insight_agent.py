import json
import os

import chromadb
from openai import AsyncOpenAI
from dotenv import load_dotenv
from shapely.geometry import Point, Polygon

from schemas.place import PlaceRequest
from tools.building_raycast import find_building_by_raycast

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

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


# ── place_info 매칭: "VWorld 폴리곤에 place_info 좌표가 포함되는가" ────────

def _find_in_place_info_by_polygon(collection, vworld_meta: dict, tolerance_m: float = 15.0) -> dict:
    """
    VWorld 건물 폴리곤에 place_info 타겟 건물의 Kakao 좌표가 포함되는지 검사.
    Kakao 좌표가 출입구라 폴리곤 경계 살짝 바깥에 찍힐 수 있으므로
    tolerance_m (기본 15m) 이내 오차는 허용.

    tolerance가 크면 바로 옆 건물도 같은 건물로 오매칭되므로 보수적으로 설정.
    """
    try:
        polygon_coords = json.loads(vworld_meta.get("polygon_2d", "[]"))
        polygon = Polygon(polygon_coords)
        if not polygon.is_valid or polygon.is_empty:
            return {}
    except Exception:
        return {}

    all_docs = collection.get(include=["metadatas"])
    best_meta = None
    best_dist_m = float("inf")

    for meta in all_docs.get("metadatas", []) or []:
        lat = meta.get("lat")
        lng = meta.get("lng")
        if lat is None or lng is None:
            continue
        dist_deg = polygon.distance(Point(lng, lat))
        dist_m = dist_deg * 111_320.0
        if dist_m < best_dist_m:
            best_dist_m = dist_m
            best_meta = meta

    vworld_name = vworld_meta.get("bld_nm") or "(이름 없음)"
    if best_meta and best_dist_m <= tolerance_m:
        tag = "내부" if best_dist_m == 0.0 else f"경계+{best_dist_m:.0f}m"
        print(f"[place_info] 폴리곤 매칭({tag}): {best_meta.get('name_ko', '')} "
              f"← VWorld {vworld_name!r}")
        return best_meta

    print(f"[place_info] VWorld 폴리곤 {vworld_name!r}과 place_info 거리 "
          f"최소 {best_dist_m:.0f}m > tolerance={tolerance_m:.0f}m")
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

    # 1) VWorld 폴리곤에 3D 레이캐스팅 → 바라보는 건물의 중심 좌표
    vworld_meta = find_building_by_raycast(
        user_lat=req.user_lat,
        user_lng=req.user_lng,
        heading=req.heading,
        user_alt=req.user_alt,
        pitch=req.pitch,
    )

    place_data = {}
    bld_name_from_vworld = ""

    if vworld_meta:
        bld_name_from_vworld = vworld_meta.get("bld_nm") or ""
        # 2) place_info(관리 10개 건물) 중 이 폴리곤 내부에 좌표가 있는 건물 매칭
        place_data = _find_in_place_info_by_polygon(collection, vworld_meta)

    # 끝내 데이터 없으면: VWorld 이름만으로 최소 응답 구성
    if not place_data and bld_name_from_vworld:
        return {
            "ar_overlay": {
                "name": bld_name_from_vworld,
                "category": "",
                "floor_info": [],
                "halal_info": "",
                "image_url": "",
                "homepage": "",
                "open_hours": "",
                "closed_days": "",
                "parking_info": "",
                "admission_fee": "",
                "is_estimated": True,
            },
            "docent": {
                "speech": f"{bld_name_from_vworld}입니다. 이 건물에 대한 상세 정보는 아직 준비되지 않았습니다.",
                "follow_up_suggestions": [],
            },
        }

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
