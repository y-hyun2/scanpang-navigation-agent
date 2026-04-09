from pydantic import BaseModel
from typing import List, Optional


class PlaceRequest(BaseModel):
    place_id: str = ""
    building_name: Optional[str] = None  # 직접 건물명 지정 시 (레거시/테스트용)
    heading: Optional[float] = None      # ARCore geospatialPose.heading (0=북, 90=동)
    image_base64: Optional[str] = None   # GPT-4V fallback용 카메라 이미지
    user_message: str = "이 건물에 대해 알려줘"
    user_lat: float
    user_lng: float
    language: str = "ko"


class FloorInfo(BaseModel):
    floor: str
    stores: List[str]


class ArOverlay(BaseModel):
    name: str
    category: str
    floor_info: List[FloorInfo]
    halal_info: str
    image_url: str
    homepage: str
    open_hours: str
    closed_days: str
    parking_info: str
    admission_fee: str


class Docent(BaseModel):
    speech: str
    follow_up_suggestions: List[str]


class PlaceResponse(BaseModel):
    ar_overlay: ArOverlay
    docent: Docent
