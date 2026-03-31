"""
build_place_db.py
서버 실행 전 1회만 실행하는 스크립트.
명동 주요 건물 8개의 정보를 수집하여 ChromaDB에 저장.

실행 방법:
    cd scanpang-navigation-agent
    python -m rag.build_place_db
"""

import asyncio
import json
import os
from pathlib import Path

import httpx
from openai import AsyncOpenAI
from dotenv import load_dotenv
import chromadb
from sentence_transformers import SentenceTransformer

load_dotenv()

KAKAO_REST_API_KEY = os.getenv("KAKAO_REST_API_KEY", "")
TOUR_API_KEY = os.getenv("TOUR_API_KEY", "")
STORE_API_KEY = os.getenv("STORE_API_KEY", "")
JUSO_API_KEY = os.getenv("JUSO_API_KEY", "")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

openai_client = AsyncOpenAI(api_key=OPENAI_API_KEY)

TARGET_PLACES = [
    {"place_id": "myeongdong_cathedral",        "name": "명동성당",            "building_key": None},
    {"place_id": "lotte_dept_myeongdong",       "name": "롯데백화점 명동본점",  "building_key": "1114010200100560008012745"},
    {"place_id": "shinsegae_myeongdong",        "name": "신세계백화점 본점",    "building_key": None},
    {"place_id": "noon_square_myeongdong",      "name": "명동 눈스퀘어",       "building_key": None},
    {"place_id": "cgv_myeongdong",              "name": "CGV 명동",            "building_key": None},
    {"place_id": "myeongdong_art_theater",      "name": "명동예술극장",         "building_key": None},
    {"place_id": "n_seoul_tower",               "name": "N서울타워",            "building_key": None},
    {"place_id": "lotte_city_hotel_myeongdong", "name": "롯데시티호텔 명동",    "building_key": None},
]

MYEONGDONG_LNG = 126.9822
MYEONGDONG_LAT = 37.5636

MANUAL_DATA_PATH = Path(__file__).parent / "data" / "places_manual.json"


# ── Step 1: Kakao Local 기본 정보 ──────────────────────────────────────────────

async def fetch_kakao_info(place_name: str) -> dict:
    url = "https://dapi.kakao.com/v2/local/search/keyword.json"
    headers = {"Authorization": f"KakaoAK {KAKAO_REST_API_KEY}"}
    params = {
        "query": place_name,
        "x": str(MYEONGDONG_LNG),
        "y": str(MYEONGDONG_LAT),
        "radius": 2000,
        "size": 1,
    }
    async with httpx.AsyncClient() as client:
        resp = await client.get(url, headers=headers, params=params)
        resp.raise_for_status()
        data = resp.json()

    docs = data.get("documents", [])
    if not docs:
        return {}
    doc = docs[0]
    return {
        "name_ko": doc.get("place_name", place_name),
        "category": doc.get("category_name", "").split(" > ")[0],
        "lat": float(doc.get("y", MYEONGDONG_LAT)),
        "lng": float(doc.get("x", MYEONGDONG_LNG)),
        "addr": doc.get("road_address_name", ""),
        "phone": doc.get("phone", ""),
    }


# ── Step 2: TourAPI description + image ───────────────────────────────────────

TOUR_CONTENT_TYPES = [12, 14, 38]  # 관광지, 문화시설, 쇼핑

async def fetch_tour_info(place_name: str) -> dict:
    base = "https://apis.data.go.kr/B551011/KorService2"
    common_params = {
        "serviceKey": TOUR_API_KEY,
        "MobileOS": "ETC",
        "MobileApp": "ScanPang",
        "_type": "json",
    }

    content_id = None
    image_url = ""

    async with httpx.AsyncClient() as client:
        for ctype in TOUR_CONTENT_TYPES:
            params = {**common_params, "keyword": place_name, "contentTypeId": ctype, "areaCode": 1}
            resp = await client.get(f"{base}/searchKeyword2", params=params)
            items = resp.json().get("response", {}).get("body", {}).get("items", {}).get("item", [])
            if items:
                first = items[0] if isinstance(items, list) else items
                content_id = first.get("contentid")
                image_url = first.get("firstimage", "")
                break

        if not content_id:
            return {}

        # overview
        resp = await client.get(f"{base}/detailCommon2", params={**common_params, "contentId": content_id, "overviewYN": "Y"})
        detail = resp.json().get("response", {}).get("body", {}).get("items", {}).get("item", {})
        if isinstance(detail, list):
            detail = detail[0]
        overview_ko = detail.get("overview", "")

        # open_hours (관광지 타입만)
        open_hours = ""
        resp2 = await client.get(f"{base}/detailIntro2", params={**common_params, "contentId": content_id, "contentTypeId": 12})
        intro = resp2.json().get("response", {}).get("body", {}).get("items", {}).get("item", {})
        if isinstance(intro, list):
            intro = intro[0] if intro else {}
        open_hours = intro.get("usetime", "")

    # overview → 영어 번역
    description_en = await translate_to_english(overview_ko, place_name) if overview_ko else ""

    return {"description_en": description_en, "image_url": image_url, "open_hours": open_hours}


async def translate_to_english(text_ko: str, place_name: str) -> str:
    response = await openai_client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {"role": "system", "content": "You are a professional translator. Translate Korean to English concisely."},
            {"role": "user", "content": f"Translate this Korean description of {place_name} to English (2-3 sentences max):\n\n{text_ko}"},
        ],
        max_tokens=200,
    )
    return response.choices[0].message.content.strip()


async def gpt_generate_description(place_name: str) -> str:
    response = await openai_client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {
                "role": "user",
                "content": (
                    f"Write a 2-3 sentence description of {place_name} in Seoul "
                    "for a solo foreign traveler. "
                    "Focus on what it is, what you can find there, and why it's worth visiting. "
                    "Respond in English only."
                ),
            }
        ],
        max_tokens=200,
    )
    return response.choices[0].message.content.strip()


# ── Step 3: 소상공인 API floor_info ───────────────────────────────────────────

async def fetch_floor_info(building_key: str) -> list:
    url = "http://apis.data.go.kr/B553077/api/open/sdsc2/storeListInBuilding"
    params = {
        "serviceKey": STORE_API_KEY,
        "key": building_key,
        "numOfRows": 1000,
        "pageNo": 1,
        "type": "json",
    }
    async with httpx.AsyncClient() as client:
        resp = await client.get(url, params=params)
        resp.raise_for_status()
        data = resp.json()

    items = data.get("body", {}).get("items", [])
    floor_map: dict[str, list] = {}
    for item in items:
        floor = item.get("flrInfo", "").strip() or "기타"
        store_name = item.get("bizesNm", "").strip()
        biz_type = item.get("indsMclsNm", "").strip()
        if store_name:
            label = f"{store_name} ({biz_type})" if biz_type else store_name
            floor_map.setdefault(floor, []).append(label)

    return [{"floor": f, "stores": s} for f, s in floor_map.items()]


# ── Step 4: Juso API → building_key 자동 획득 ─────────────────────────────────

async def fetch_building_key(road_addr: str) -> str | None:
    if not road_addr or not JUSO_API_KEY:
        return None
    url = "https://business.juso.go.kr/addrlink/addrLinkApi.do"
    params = {
        "confmKey": JUSO_API_KEY,
        "keyword": road_addr,
        "currentPage": 1,
        "countPerPage": 1,
        "resultType": "json",
    }
    async with httpx.AsyncClient() as client:
        resp = await client.get(url, params=params)
        data = resp.json()
    juso_list = data.get("results", {}).get("juso", [])
    if juso_list:
        return juso_list[0].get("bdMgtSn")
    return None


# ── Main pipeline ─────────────────────────────────────────────────────────────

async def build_all_places() -> list[dict]:
    manual_data: dict = json.loads(MANUAL_DATA_PATH.read_text(encoding="utf-8"))
    all_places = []

    for target in TARGET_PLACES:
        place_id = target["place_id"]
        name = target["name"]
        building_key = target["building_key"]
        print(f"[{place_id}] 수집 중...")

        # Step 1
        kakao = await fetch_kakao_info(name)
        place = {
            "place_id": place_id,
            "name_ko": kakao.get("name_ko", name),
            "category": kakao.get("category", ""),
            "lat": kakao.get("lat", MYEONGDONG_LAT),
            "lng": kakao.get("lng", MYEONGDONG_LNG),
            "addr": kakao.get("addr", ""),
            "phone": kakao.get("phone", ""),
            "description_en": "",
            "image_url": "",
            "open_hours": "",
            "floor_info": [],
            "tags": [],
            "tourist_tip": "",
            "halal_info": "",
        }

        # Step 2: TourAPI
        tour = await fetch_tour_info(name)
        if tour.get("description_en"):
            place["description_en"] = tour["description_en"]
            place["image_url"] = tour.get("image_url", "")
            place["open_hours"] = tour.get("open_hours", "")
        else:
            # Fallback: GPT 생성
            place["description_en"] = await gpt_generate_description(name)

        # Step 3: floor_info (building_key 있으면 자동)
        if not building_key and kakao.get("addr"):
            building_key = await fetch_building_key(kakao["addr"])

        if building_key:
            place["floor_info"] = await fetch_floor_info(building_key)

        # Step 4: 수동 데이터 병합
        manual = manual_data.get(place_id, {})
        place["tags"] = manual.get("tags", [])
        place["tourist_tip"] = manual.get("tourist_tip", "")
        place["halal_info"] = manual.get("halal_info", "")
        # floor_info: 소상공인 API 결과 없으면 수동 데이터 사용
        if not place["floor_info"]:
            place["floor_info"] = manual.get("floor_info", [])

        all_places.append(place)
        print(f"  → 완료: {place['name_ko']}")

    return all_places


def save_to_chroma(all_places: list[dict]):
    print("\nChroma DB 임베딩 저장 중...")
    model = SentenceTransformer("BAAI/bge-m3")
    client = chromadb.PersistentClient(path="./chroma_db")
    collection = client.get_or_create_collection("place_info")

    for place in all_places:
        text = " ".join([
            place["name_ko"],
            place["category"],
            place["description_en"],
            " ".join(place.get("tags", [])),
        ])
        embedding = model.encode(text).tolist()

        # Chroma metadata는 string/int/float/bool만 허용 → floor_info는 JSON 직렬화
        metadata = {k: v for k, v in place.items() if isinstance(v, (str, int, float, bool))}
        metadata["floor_info"] = json.dumps(place.get("floor_info", []), ensure_ascii=False)
        metadata["tags"] = json.dumps(place.get("tags", []), ensure_ascii=False)

        collection.upsert(
            embeddings=[embedding],
            documents=[text],
            metadatas=[metadata],
            ids=[place["place_id"]],
        )
        print(f"  저장: {place['place_id']}")

    print(f"\n총 {len(all_places)}개 장소 저장 완료 → ./chroma_db")


async def main():
    all_places = await build_all_places()
    save_to_chroma(all_places)


if __name__ == "__main__":
    asyncio.run(main())
