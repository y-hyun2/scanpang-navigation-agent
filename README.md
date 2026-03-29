# ScanPang Navigation Agent

**ScanPang** AR 내비게이션 앱의 백엔드 서버입니다.
사용자의 자연어 메시지를 받아 목적지 POI 검색, 보행자 경로 계산, 턴별 TTS 안내 문구 생성을 처리합니다.

---

## 기술 스택

| 항목 | 내용 |
|---|---|
| 프레임워크 | FastAPI |
| LLM | OpenAI gpt-4o (LangChain) |
| 지도 API | TMAP (SK Telecom) |
| 언어 | Python 3.10+ |

---

## 프로젝트 구조

```
scanpang-navigation-agent/
├── main.py                   # FastAPI 진입점 (엔드포인트 2개)
├── agents/
│   └── navigation_agent.py   # LLM 오케스트레이션 (검색 + 경로 에이전트)
├── tools/
│   └── navigation_tools.py   # TMAP API 래퍼 (POI 검색, 보행자 경로)
├── schemas/
│   └── navigation.py         # Pydantic 요청/응답 모델
└── .env                      # API 키 (아래 환경변수 설정 참고)
```

---

## 환경변수 설정

프로젝트 루트에 `.env` 파일을 생성하세요.

```
OPENAI_API_KEY=sk-...
TMAP_API_KEY=...
```

---

## 실행 방법

```bash
pip install fastapi uvicorn langchain langchain-openai httpx pydantic python-dotenv

uvicorn main:app --reload --port 8000
```

Swagger UI: http://localhost:8000/docs

---

## 전체 동작 흐름

**2단계 엔드포인트** 구조로 동작합니다. 앱이 사용자에게 목적지를 확인받은 후 경로를 요청하는 흐름입니다.

```
[1단계] POST /navigation/search
  사용자: "캄풍쿠 어떻게 가?"
        ↓
  Step 0: LLM → 키워드 추출 + 의도 분류 + 언어 감지
          { "keyword": "캄풍쿠", "intent": "specific_place", "language": "ko" }
        ↓
  Step 1: TMAP POI 검색 (5km 반경 → 없으면 전국 fallback)
          상위 5개 후보 반환
        ↓
  Step 2: LLM → 최적 POI 인덱스 선택 (specific_place일 때만)
          category_search: 거리 가장 가까운 pois[0]를 recommended로
        ↓
  응답: speech + candidates[] + recommended 플래그
        ↓
  [앱] 사용자에게 후보 목록 보여주고 최종 확인 받음

[2단계] POST /navigation/route
  사용자가 확정한 POI 정보 전달
        ↓
  Step 3: TMAP 보행자 경로 계산 (GeoJSON 파싱)
        ↓
  Step 4: LLM → 모든 턴포인트에 대해 TTS 문구 배열 생성 (1회 호출)
        ↓
  응답: departure_speech + ar_command { route_line, turn_points(각 speech 포함) }
```

---

## LLM 사용 포인트

총 3곳에서 gpt-4o를 호출합니다.

### Step 0: 의도 + 키워드 + 언어 추출

| intent 값 | 설명 | 예시 |
|---|---|---|
| `specific_place` | 특정 장소명 검색 | "캄풍쿠 어디야", "이마트 찾아줘" |
| `category_search` | 주변 카테고리 검색 | "할랄 식당 찾아줘", "근처 카페" |

```
"주변 할랄 식당 알려줘"  →  { "keyword": "할랄 식당", "intent": "category_search", "language": "ko" }
"مطعم حلال قريب"       →  { "keyword": "할랄 식당", "intent": "category_search", "language": "ar" }
```

### Step 2: 최적 POI 인덱스 선택

`specific_place`일 때만 실행. 상위 5개 POI 중 가장 적합한 것의 인덱스(0-based)를 반환합니다.
`category_search`는 거리순 정렬 후 pois[0]가 자동으로 recommended 됩니다.

### Step 4: 턴포인트별 TTS 문구 생성

모든 turn_points를 한 번에 전달하여 JSON 문자열 배열로 반환받습니다.

TTS 문구 생성 규칙:
- **SP (출발)**: 총 거리 + 소요 시간 포함 출발 안내
- **GP (안내점)**: nearPoiName(랜드마크) 있으면 우선 활용 → 없으면 intersectionName(교차로명) → 없으면 description만
- **EP (도착)**: 도착 안내
- facilityType 125(육교) / 126(지하보도) / 127(계단)은 반드시 언급
- 입력 언어(ko/en/ar)와 동일한 언어로 응답

---

## API 명세

### POST /navigation/search

**Request**
```json
{
  "message": "캄풍쿠 어떻게 가?",
  "lat": 37.5636,
  "lng": 126.9822
}
```

**Response**
```json
{
  "speech": "'캄풍쿠' 찾았어요. 서울 중구 남산동2가. 이 곳으로 안내할까요?",
  "candidates": [
    {
      "poi_id": "374469",
      "name": "캄풍쿠",
      "address": "서울 중구 남산동2가 16-4",
      "pns_lat": 37.55820,
      "pns_lon": 126.98490,
      "recommended": true
    }
  ],
  "intent": "specific_place",
  "language": "ko"
}
```

### POST /navigation/route

**Request** (1단계 응답에서 사용자가 선택한 candidate 그대로 전달)
```json
{
  "lat": 37.5636,
  "lng": 126.9822,
  "destination": {
    "poi_id": "374469",
    "pns_lat": 37.55820,
    "pns_lon": 126.98490,
    "name": "캄풍쿠"
  },
  "language": "ko"
}
```

**Response**
```json
{
  "speech": "출발합니다. 캄풍쿠까지 350m, 약 7분 소요됩니다.",
  "ar_command": {
    "type": "start_navigation",
    "route_line": [
      { "lat": 37.563, "lng": 126.982 }
    ],
    "turn_points": [
      {
        "lat": 37.563, "lng": 126.982,
        "pointType": "SP", "turnType": 200,
        "description": "출발",
        "nearPoiName": "", "intersectionName": "",
        "facilityType": "", "segment_distance_m": 0,
        "speech": "출발합니다. 캄풍쿠까지 350m, 약 7분 소요됩니다."
      },
      {
        "lat": 37.561, "lng": 126.983,
        "pointType": "GP", "turnType": 13,
        "description": "우회전",
        "nearPoiName": "GS25 명동점", "intersectionName": "명동사거리",
        "facilityType": "", "segment_distance_m": 150,
        "speech": "GS25 명동점에서 우회전하세요."
      },
      {
        "lat": 37.558, "lng": 126.985,
        "pointType": "EP", "turnType": 201,
        "description": "도착",
        "nearPoiName": "", "intersectionName": "",
        "facilityType": "", "segment_distance_m": 200,
        "speech": "목적지 캄풍쿠에 도착했습니다."
      }
    ],
    "destination": { "lat": 37.5582, "lng": 126.9849, "name": "캄풍쿠" },
    "total_distance_m": 350,
    "total_time_min": 7
  }
}
```

---

## turnType 코드표

AR 앱에서 화살표 방향 매핑에 사용합니다.

| turnType | 의미 |
|---|---|
| 11 | 직진 |
| 12 | 좌회전 |
| 13 | 우회전 |
| 14 | 유턴 |
| 16 | 8시 방향 (좌측 사선) |
| 17 | 10시 방향 (우측 사선) |
| 125 | 육교 |
| 126 | 지하보도 |
| 127 | 계단 |
| 200 | 출발 (SP) |
| 201 | 도착 (EP) |

---

## TMAP POI 검색 전략

```
1차: 현재 위치 기준 5km 반경
      ↓ 결과 없으면
2차: 전국 거리순 fallback
```

---

## 다국어 지원

한국어 / 영어 / 아랍어 입력을 자동 감지하며, 감지된 언어와 동일한 언어로 모든 응답(speech, TTS 문구)을 반환합니다.

| language | 입력 예시 | 응답 언어 |
|---|---|---|
| `ko` | "캄풍쿠 어떻게 가?" | 한국어 |
| `en` | "How do I get to Kampungku?" | 영어 |
| `ar` | "مطعم حلال قريب" | 아랍어 |
