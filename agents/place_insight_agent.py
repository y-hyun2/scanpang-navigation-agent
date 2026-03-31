import json
import os

import chromadb
from openai import AsyncOpenAI
from dotenv import load_dotenv

from schemas.place import PlaceRequest

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
openai_client = AsyncOpenAI(api_key=OPENAI_API_KEY)

_chroma_client = None
_collection = None


def _get_collection():
    global _chroma_client, _collection
    if _collection is None:
        _chroma_client = chromadb.PersistentClient(path="./chroma_db")
        _collection = _chroma_client.get_or_create_collection("place_info")
    return _collection


# ── LLM: docent 해설 생성 ──────────────────────────────────────────────────────

async def llm_generate_docent(context: str, language: str) -> str:
    # 아랍어 요청은 영어로 응답
    response_lang = "English" if language == "ar" else language

    lang_map = {"ko": "Korean", "en": "English", "ja": "Japanese", "zh": "Chinese"}
    response_lang_label = lang_map.get(response_lang, "English")

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
    tags = place_data.get("tags", [])

    if "history" not in msg_lower and "historic" in tags:
        suggestions.append("Tell me more about the history")
    if "floor" not in msg_lower and has_floor_info:
        suggestions.append("What's on each floor?")
    if "halal" not in msg_lower and has_halal:
        suggestions.append("Where can I find halal food nearby?")
    if "eat" not in msg_lower and "restaurant" not in msg_lower:
        suggestions.append("What's nearby to eat?")
    if "photo" not in msg_lower and "photo-spot" in tags:
        suggestions.append("What's the best photo spot here?")
    if "prayer" not in msg_lower:
        suggestions.append("Is there a prayer room nearby?")

    return suggestions[:3]


# ── Main agent ────────────────────────────────────────────────────────────────

async def run_place_insight_agent(req: PlaceRequest) -> dict:
    collection = _get_collection()

    # 1. Chroma에서 place_id로 직접 조회
    result = collection.get(ids=[req.place_id])
    if not result["metadatas"]:
        return {
            "ar_overlay": {
                "name": req.place_id,
                "category": "",
                "floor_info": [],
                "tags": [],
                "tourist_tip": "",
                "halal_info": "",
                "image_url": "",
            },
            "docent": {
                "speech": "Sorry, I don't have information about this place yet.",
                "follow_up_suggestions": [],
            },
        }

    place_data = result["metadatas"][0]

    # Chroma에서 꺼낸 JSON 문자열 → 파이썬 객체 역직렬화
    floor_info = json.loads(place_data.get("floor_info", "[]"))
    tags = json.loads(place_data.get("tags", "[]"))

    # 2. halal_info
    halal_info = place_data.get("halal_info", "")

    # 3. ar_overlay (LLM 없이 RAG 데이터 그대로)
    ar_overlay = {
        "name": place_data.get("name_ko", ""),
        "category": place_data.get("category", ""),
        "floor_info": floor_info,
        "tags": tags,
        "tourist_tip": place_data.get("tourist_tip", ""),
        "halal_info": halal_info,
        "image_url": place_data.get("image_url", ""),
    }

    # 4. docent: LLM 자연어 해설 생성
    context = f"""
Place: {place_data.get('name_ko', '')}
Category: {place_data.get('category', '')}
Description: {place_data.get('description_en', '')}
Tourist tip: {place_data.get('tourist_tip', '')}
Tags: {', '.join(tags)}
Halal info: {halal_info}
User's question: {req.user_message}
Language: {req.language}
""".strip()

    speech = await llm_generate_docent(context, req.language)
    follow_ups = generate_follow_ups(req.user_message, {"floor_info": floor_info, "halal_info": halal_info, "tags": tags})

    return {
        "ar_overlay": ar_overlay,
        "docent": {
            "speech": speech,
            "follow_up_suggestions": follow_ups,
        },
    }
