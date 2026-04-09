import json
import os

import chromadb
from openai import AsyncOpenAI
from dotenv import load_dotenv

from schemas.place import PlaceRequest
from tools.osm_tools import find_building_by_osm

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
openai_client = AsyncOpenAI(api_key=OPENAI_API_KEY)

_chroma_client = None
_collection = None

# Kakao/OSM 건물명 → place_id 매핑
BUILDING_NAME_MAP = {
    "명동대성당": "myeongdong_cathedral",
    "명동성당": "myeongdong_cathedral",
    "롯데백화점 본점": "lotte_dept_myeongdong",
    "롯데백화점 명동본점": "lotte_dept_myeongdong",
    "롯데시네마 에비뉴엘": "lotte_dept_myeongdong",
    "롯데영플라자 본점": "lotte_dept_myeongdong",
    "롯데백화점 롯데문화홀": "lotte_dept_myeongdong",
    "에비뉴엘 명동": "lotte_dept_myeongdong",
    "국립극단 명동예술극장": "myeongdong_art_theater",
    "명동난타극장": "myeongdong_art_theater",
    "신세계백화점 본점": "shinsegae_myeongdong",
    "신세계백화점": "shinsegae_myeongdong",
    "신세계백화점 본점 더 리저브": "shinsegae_myeongdong",
    "신세계백화점 본점 디 에스테이트": "shinsegae_myeongdong",
    "회현지하쇼핑센터": "shinsegae_myeongdong",
    "눈스퀘어": "noon_square_myeongdong",
    "명동 눈스퀘어": "noon_square_myeongdong",
    "명동예술극장": "myeongdong_art_theater",
    "N서울타워": "n_seoul_tower",
    "남산서울타워": "n_seoul_tower",
    "서울타워": "n_seoul_tower",
    "롯데시티호텔 명동": "lotte_city_hotel_myeongdong",
    "유네스코회관": "unesco_hall_myeongdong",
    "유네스코회관빌딩": "unesco_hall_myeongdong",
    "포스트타워": "post_tower_myeongdong",
    "서울중앙우체국": "post_tower_myeongdong",
    "대신파이낸스센터": "daishin_finance_center",
    "Daishin343": "daishin_finance_center",
}


def _get_collection():
    global _chroma_client, _collection
    if _collection is None:
        from chromadb.utils.embedding_functions import DefaultEmbeddingFunction
        _chroma_client = chromadb.PersistentClient(path="./chroma_db")
        _collection = _chroma_client.get_or_create_collection(
            "place_info", embedding_function=DefaultEmbeddingFunction()
        )
    return _collection


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


# ── place_id 해석: 이름 → place_id 매핑 ───────────────────────────────────────

def _resolve_place_id_from_name(name: str) -> str:
    """BUILDING_NAME_MAP에서 exact → partial 순으로 place_id 조회."""
    if not name:
        return ""
    pid = BUILDING_NAME_MAP.get(name, "")
    if pid:
        return pid
    for map_key, pid in BUILDING_NAME_MAP.items():
        if map_key in name or name in map_key:
            return pid
    return ""


# ── Main agent ────────────────────────────────────────────────────────────────

async def run_place_insight_agent(req: PlaceRequest) -> dict:
    collection = _get_collection()

    # 1. place_id 결정
    #    우선순위: 직접 전달 > building_name 매핑 > OSM 레이캐스팅 > Chroma 벡터 유사도
    place_id = req.place_id

    if not place_id and req.building_name:
        place_id = _resolve_place_id_from_name(req.building_name)

    if not place_id and req.heading is not None:
        osm_name = await find_building_by_osm(req.user_lat, req.user_lng, req.heading)
        if osm_name:
            place_id = _resolve_place_id_from_name(osm_name)
            # BUILDING_NAME_MAP에 없으면 osm_name 자체로 Chroma 벡터 검색
            if not place_id:
                req = req.model_copy(update={"building_name": osm_name})

    # 2. Chroma에서 place_id로 직접 조회
    result = collection.get(ids=[place_id]) if place_id else {"metadatas": []}

    # 3. 매핑 실패 시 → Chroma 벡터 유사도 검색
    building_name_for_search = req.building_name
    if not result["metadatas"] and building_name_for_search:
        try:
            query_result = collection.query(
                query_texts=[building_name_for_search],
                n_results=1,
            )
            if query_result["metadatas"] and query_result["metadatas"][0]:
                best_dist = query_result["distances"][0][0]
                if best_dist < 0.6:
                    result = {"metadatas": [query_result["metadatas"][0][0]]}
                    place_id = query_result["ids"][0][0]
        except Exception:
            pass

    # 4. 끝내 데이터 없으면 미인식 응답
    if not result["metadatas"]:
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

    place_data = result["metadatas"][0]
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
