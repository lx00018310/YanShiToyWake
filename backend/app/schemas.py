"""API 请求 / 响应 schema（实施计划书 §11）。"""

from datetime import datetime
from typing import Literal, Optional

from pydantic import BaseModel, Field

# 实施计划书 §9.2 玩具类型预设选项
ToyType = Literal["毛绒动物", "玩具车", "人偶", "积木角色", "过家家物品", "其他"]

# 实施计划书 §6.2 记忆类型
MemoryType = Literal["event", "setting", "relationship", "preference"]


# ---------- Scan ----------
class ScanRequest(BaseModel):
    tag_uid: str = Field(min_length=1, max_length=64)


class ToyBrief(BaseModel):
    id: int
    name: str
    toy_type: str
    trait: Optional[str] = None
    child_setting: Optional[str] = None


class ScanResponse(BaseModel):
    status: Literal["new", "known"]
    tag_uid: Optional[str] = None
    toy: Optional[ToyBrief] = None


# ---------- Toy ----------
class ToyCreate(BaseModel):
    tag_uid: str = Field(min_length=1, max_length=64)
    name: str = Field(min_length=1, max_length=20)
    toy_type: ToyType
    trait: Optional[str] = Field(default=None, max_length=20)
    child_setting: Optional[str] = Field(default=None, max_length=50)


class ToyUpdate(BaseModel):
    name: Optional[str] = Field(default=None, min_length=1, max_length=20)
    toy_type: Optional[ToyType] = None
    trait: Optional[str] = Field(default=None, max_length=20)
    child_setting: Optional[str] = Field(default=None, max_length=50)


class ToyOut(BaseModel):
    id: int
    tag_uid: str
    name: str
    toy_type: str
    trait: Optional[str] = None
    child_setting: Optional[str] = None
    is_active: bool


class ToyCreateResponse(BaseModel):
    status: str = "ok"
    toy: dict


# ---------- Memory ----------
class MemoryCreate(BaseModel):
    toy_id: int
    content: str = Field(min_length=1, max_length=50)
    memory_type: MemoryType = "event"


class MemoryOut(BaseModel):
    id: int
    toy_id: int
    content: str
    memory_type: str
    created_at: datetime
    is_active: bool


class MemoryCreateResponse(BaseModel):
    status: str = "ok"
    memory_id: int
