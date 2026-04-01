from pydantic import BaseModel
from typing import List, Optional


class PlaceRequest(BaseModel):
    place_id: str
    user_message: str
    user_lat: float
    user_lng: float
    language: str = "en"


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
