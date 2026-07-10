"""SQLModel 表模型，对应实施计划书 §7 的四张表。"""

from datetime import datetime, timezone
from typing import Optional

from sqlmodel import Field, SQLModel


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


class Toy(SQLModel, table=True):
    __tablename__ = "toys"

    id: Optional[int] = Field(default=None, primary_key=True)
    tag_uid: str = Field(unique=True, index=True, max_length=64)
    name: str = Field(max_length=20)
    toy_type: str = Field(max_length=20)
    trait: Optional[str] = Field(default=None, max_length=20)
    child_setting: Optional[str] = Field(default=None, max_length=50)
    created_at: datetime = Field(default_factory=utcnow)
    updated_at: datetime = Field(default_factory=utcnow)
    last_seen_at: Optional[datetime] = None
    is_active: bool = Field(default=True)


class Memory(SQLModel, table=True):
    __tablename__ = "memories"

    id: Optional[int] = Field(default=None, primary_key=True)
    toy_id: int = Field(foreign_key="toys.id", index=True)
    content: str = Field(max_length=50)
    memory_type: str = Field(max_length=20)  # event | setting | relationship | preference
    created_at: datetime = Field(default_factory=utcnow)
    is_active: bool = Field(default=True)


class PlaySession(SQLModel, table=True):
    __tablename__ = "play_sessions"

    id: str = Field(primary_key=True)  # uuid
    toy_id: int = Field(foreign_key="toys.id", index=True)
    status: str = Field(default="ACTIVE", max_length=20)  # ACTIVE | ENDED | CANCELLED
    started_at: datetime = Field(default_factory=utcnow)
    ended_at: Optional[datetime] = None
    last_parent_context: Optional[str] = None
    spark_count: int = Field(default=0)


class PlayTurn(SQLModel, table=True):
    __tablename__ = "play_turns"

    id: Optional[int] = Field(default=None, primary_key=True)
    session_id: str = Field(foreign_key="play_sessions.id", index=True)
    turn_type: str = Field(max_length=20)  # wake | spark | end
    parent_context: Optional[str] = None
    child_speech: str = Field(max_length=120)
    parent_hint: Optional[str] = Field(default=None, max_length=100)
    source: str = Field(max_length=10)  # ai | fixed | mock
    created_at: datetime = Field(default_factory=utcnow)
