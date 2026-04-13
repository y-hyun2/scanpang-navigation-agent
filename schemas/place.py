from pydantic import BaseModel
from typing import List


class PlaceRequest(BaseModel):
    heading: float                       # ARCore geospatialPose.heading (0=북, 90=동)
    user_lat: float
    user_lng: float
    user_alt: float = 0.0                # ARCore geospatialPose.altitude (m)
    pitch: float = 0.0                   # 카메라 상하 각도 (도, +위 -아래)
    user_message: str = "이 건물에 대해 알려줘"
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
