# Place Insight Agent — Claude Code Plan 요청

## 지시사항
아래 전체 컨텍스트를 읽고 **plan 모드**로 Place Insight Agent를 설계해줘.
코드 작성 전에 계획만 먼저 보여줘. 승인 후 코드 작성.

---

## 기존 프로젝트 구조 (이미 있는 것)

```
scanpang-navigation-agent/   ← 이 폴더를 확장함
├── main.py                  ← 이미 있음, 엔드포인트 추가 예정
├── agents/
│   └── navigation_agent.py  ← 이미 있음
├── tools/
│   └── navigation_tools.py  ← 이미 있음
├── schemas/
│   └── navigation.py        ← 이미 있음
└── .env                     ← 이미 있음
```

## 추가할 파일 구조

```
scanpang-navigation-agent/
├── main.py                        ← /place/query 엔드포인트 추가
├── agents/
│   ├── navigation_agent.py        ← 건드리지 말 것
│   └── place_insight_agent.py     ← 새로 추가
├── tools/
│   ├── navigation_tools.py        ← 건드리지 말 것
│   └── place_tools.py             ← 새로 추가 (Kakao 영업여부 확인)
├── rag/
│   ├── build_place_db.py          ← 새로 추가 (데이터 수집 + Chroma 임베딩)
│   └── data/
│       └── places_manual.json     ← 수동 데이터 (tourist_tip 등)
├── schemas/
│   ├── navigation.py              ← 건드리지 말 것
│   └── place.py                   ← 새로 추가
└── .env                           ← API 키 추가 예정
```

---

## Place Insight Agent 역할

사용자가 카메라로 건물을 비추면 ARCore가 건물을 인식하고 place_id를 전달.
Agent는 두 가지를 동시에 반환:
1. **ar_overlay**: iOS가 AR 화면에 층별 정보를 렌더링할 구조화 데이터 (LLM 없이 RAG에서 직접)
2. **docent**: TTS로 읽어주는 자연어 관광 해설 (LLM 생성)

---

## 데이터 수집 파이프라인 (rag/build_place_db.py)

### 명동 대상 건물 8개

```python
TARGET_PLACES = [
    {"place_id": "myeongdong_cathedral",    "name": "명동성당",          "building_key": None},
    {"place_id": "lotte_dept_myeongdong",   "name": "롯데백화점 명동본점", "building_key": "1114010200100560008012745"},
    {"place_id": "shinsegae_myeongdong",    "name": "신세계백화점 본점",   "building_key": None},  # 키 조사 필요
    {"place_id": "noon_square_myeongdong",  "name": "명동 눈스퀘어",      "building_key": None},
    {"place_id": "cgv_myeongdong",          "name": "CGV 명동",           "building_key": None},
    {"place_id": "myeongdong_art_theater",  "name": "명동예술극장",        "building_key": None},
    {"place_id": "n_seoul_tower",           "name": "N서울타워",           "building_key": None},
    {"place_id": "lotte_city_hotel_myeongdong", "name": "롯데시티호텔 명동", "building_key": None},
]
```

### Step 1: Kakao Local로 기본 정보 수집 (자동)

```
GET https://dapi.kakao.com/v2/local/search/keyword.json
  ?query={place_name}
  &x=126.9822&y=37.5636
  &radius=2000
Headers: Authorization: KakaoAK {KAKAO_REST_API_KEY}

파싱:
  place_name → name_ko
  category_name.split(" > ")[0] → category
  y → lat, x → lng
  road_address_name → addr
  phone → phone
```

### Step 2: TourAPI로 description + image 수집 (자동, 관광지만)

```
# Step 2-1: contentId 검색
GET https://apis.data.go.kr/B551011/KorService2/searchKeyword2
  ?serviceKey={TOUR_API_KEY}
  &keyword={place_name}
  &contentTypeId=12   (관광지), 14(문화시설), 38(쇼핑)
  &areaCode=1
  &MobileOS=ETC&MobileApp=ScanPang&_type=json

파싱: contentId, firstimage → image_url

# Step 2-2: overview 추출
GET https://apis.data.go.kr/B551011/KorService2/detailCommon2
  ?serviceKey={TOUR_API_KEY}
  &contentId={contentId}
  &overviewYN=Y
  &MobileOS=ETC&MobileApp=ScanPang&_type=json

파싱: overview (한국어) → GPT로 영어 번역 → description_en

# Step 2-3: 운영시간 추출
GET https://apis.data.go.kr/B551011/KorService2/detailIntro2
  ?serviceKey={TOUR_API_KEY}
  &contentId={contentId}
  &contentTypeId=12
  &MobileOS=ETC&MobileApp=ScanPang&_type=json

파싱: usetime → open_hours
```

### TourAPI fallback: TourAPI에 없는 건물은 GPT로 description 생성

```python
# TourAPI에 없으면 (롯데백화점 등 상업시설)
prompt = f"""
Write a 2-3 sentence description of {place_name} in Seoul
for a solo foreign traveler.
Focus on what it is, what you can find there, and why it's worth visiting.
Respond in English only.
"""
description_en = await gpt_generate(prompt)
```

### Step 3: 소상공인 API로 floor_info 수집 (자동, building_key 있는 건물만)

```
GET http://apis.data.go.kr/B553077/api/open/sdsc2/storeListInBuilding
  ?serviceKey={STORE_API_KEY}
  &key={building_key}
  &numOfRows=1000
  &pageNo=1
  &type=json

파싱:
  flrInfo → 층 구분
  bizesNm → 매장명
  indsMclsNm → 업종명

→ floor_map으로 그룹핑 후 floor_info 구성:
[
  {"floor": "B1", "stores": ["스타벅스 (커피)", "맥도날드 (패스트푸드)"]},
  {"floor": "1F", "stores": ["화장품 A (화장품)", "화장품 B (화장품)"]},
  ...
]
```

### Step 4: 건물관리번호 없는 경우 — juso.go.kr로 자동 획득

```
GET https://business.juso.go.kr/addrlink/addrLinkApi.do
  ?confmKey={JUSO_API_KEY}
  &keyword={road_address_name}   ← Kakao에서 받은 도로명주소
  &currentPage=1
  &countPerPage=1
  &resultType=json

파싱: bdMgtSn → building_key
```

### Step 5: 수동 데이터 로드 (rag/data/places_manual.json)

```json
{
  "myeongdong_cathedral": {
    "tourist_tip": "야경이 아름다움. 포토스팟은 정문 앞. 입장 무료.",
    "best_for": ["solo", "first-time", "photo", "short-stop"],
    "tags": ["landmark", "free", "quiet", "photo-spot"],
    "floor_info": [
      {"floor": "1F", "stores": ["Main cathedral hall"]},
      {"floor": "B1", "stores": ["Café", "Gift shop"]}
    ]
  }
}
```

> building_key 없는 단순 건물은 floor_info도 여기서 수동으로 관리

### Step 6: Chroma에 임베딩 저장

```python
import chromadb
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("BAAI/bge-m3")
client = chromadb.PersistentClient(path="./chroma_db")
collection = client.get_or_create_collection("place_info")

# 각 장소를 하나의 문서로 임베딩
for place in all_places:
    text = f"{place['name_ko']} {place['category']} {place['description_en']} {' '.join(place['tags'])}"
    embedding = model.encode(text).tolist()
    collection.add(
        embeddings=[embedding],
        documents=[text],
        metadatas=[place],   # 전체 딕셔너리를 메타데이터로 저장
        ids=[place['place_id']]
    )
```

---

## FastAPI — Request / Response (schemas/place.py)

### Request (ARCore → FastAPI)

```json
{
  "place_id": "myeongdong_cathedral",
  "user_message": "What is this building?",
  "user_lat": 37.5628,
  "user_lng": 126.9875,
  "language": "en"
}
```

### Response (FastAPI → iOS)

```json
{
  "ar_overlay": {
    "name": "Myeongdong Cathedral",
    "category": "landmark",
    "floor_info": [
      {"floor": "1F", "stores": ["Main cathedral hall"]},
      {"floor": "B1", "stores": ["Café", "Gift shop"]}
    ],
    "tags": ["landmark", "free", "photo-spot"],
    "tourist_tip": "Best visited in the evening. Free entry.",
    "halal_info": "No halal food inside. Kampungku restaurant 200m away.",
    "image_url": "https://cdn.visitkorea.or.kr/..."
  },
  "docent": {
    "speech": "This is Myeongdong Cathedral, completed in 1898. It's Korea's first Gothic-style Catholic cathedral and one of Seoul's most iconic landmarks. Best visited in the evening for beautiful lighting.",
    "follow_up_suggestions": [
      "Tell me more about the history",
      "What's nearby to eat?",
      "Is there a prayer room nearby?"
    ]
  }
}
```

---

## agents/place_insight_agent.py 로직

```python
async def run_place_insight_agent(req: PlaceRequest) -> dict:
    # 1. Chroma에서 place_id로 직접 조회 (RAG)
    result = collection.get(ids=[req.place_id])
    place_data = result["metadatas"][0]

    # 2. halal_info: Recommendation Agent DB에서 place_id로 교차 조회
    #    (나중에 Recommendation Agent 완성 후 연결. 지금은 place_data에 있으면 사용)
    halal_info = place_data.get("halal_info", "")

    # 3. ar_overlay: LLM 없이 RAG 데이터 그대로 반환
    ar_overlay = {
        "name": place_data["name_ko"],
        "category": place_data["category"],
        "floor_info": place_data.get("floor_info", []),
        "tags": place_data.get("tags", []),
        "tourist_tip": place_data.get("tourist_tip", ""),
        "halal_info": halal_info,
        "image_url": place_data.get("image_url", ""),
    }

    # 4. docent: LLM으로 자연어 해설 생성
    context = f"""
Place: {place_data['name_ko']}
Category: {place_data['category']}
Description: {place_data['description_en']}
Tourist tip: {place_data.get('tourist_tip', '')}
Tags: {', '.join(place_data.get('tags', []))}
Halal info: {halal_info}
User's question: {req.user_message}
Language: {req.language}
"""
    speech = await llm_generate_docent(context, req.language)

    return {
        "ar_overlay": ar_overlay,
        "docent": {
            "speech": speech,
            "follow_up_suggestions": generate_follow_ups(req.user_message, place_data)
        }
    }
```

---

## LLM 프롬프트 조건

- 외국인 혼자 여행자 관점에서 설명
- 2-3문장으로 핵심만 (TTS로 읽힘)
- halal_info 있으면 반드시 포함
- follow_up 질문 2-3개 제안 (대화형 도슨트 유도)
- 사용자 언어 자동 감지 (language 파라미터 기준)
- 아랍어 → 영어로 응답

---

## 환경변수 (.env에 추가)

```
OPENAI_API_KEY=         ← 이미 있음
TMAP_API_KEY=           ← 이미 있음
KAKAO_REST_API_KEY=     ← 추가
TOUR_API_KEY=           ← 추가 (api.visitkorea.or.kr에서 발급)
STORE_API_KEY=          ← 추가 (data.go.kr 소상공인 API)
JUSO_API_KEY=           ← 추가 (business.juso.go.kr에서 발급)
```

---

## 추가 패키지

```bash
pip install chromadb sentence-transformers
```

---

## main.py에 추가할 엔드포인트

```python
from schemas.place import PlaceRequest
from agents.place_insight_agent import run_place_insight_agent

@app.post("/place/query")
async def place_query(req: PlaceRequest):
    return await run_place_insight_agent(req)
```

---

## Postman 테스트 케이스

```
POST http://localhost:8000/place/query

# 영어 질문
{
  "place_id": "myeongdong_cathedral",
  "user_message": "What is this building?",
  "user_lat": 37.5631,
  "user_lng": 126.9879,
  "language": "en"
}

# 한국어 질문
{
  "place_id": "lotte_dept_myeongdong",
  "user_message": "몇 층에 뭐 있어?",
  "user_lat": 37.5631,
  "user_lng": 126.9879,
  "language": "ko"
}

# 할랄 관련 질문
{
  "place_id": "myeongdong_cathedral",
  "user_message": "Is there halal food nearby?",
  "user_lat": 37.5631,
  "user_lng": 126.9879,
  "language": "en"
}
```

---

## 주의사항

1. Navigation Agent 관련 파일 절대 건드리지 말 것
2. `rag/build_place_db.py`는 서버 실행 전 1회만 실행하는 스크립트
3. Chroma DB는 `./chroma_db` 폴더에 persistent하게 저장
4. building_key 없는 건물은 `places_manual.json`의 floor_info 사용
5. TourAPI 없는 상업시설은 GPT fallback으로 description 생성
6. halal_info는 지금은 places_manual.json에 수동으로 넣고, 나중에 Recommendation Agent DB 연결
7. LangGraph 사용 안 함 (지금 단계)
