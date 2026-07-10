"""游戏会话服务：开始 / 继续 / 结束（实施计划书 §11.4-11.6、§10.6）。

- 每次最多引用一条旧记忆；
- 每会话火花上限（MAX_SPARKS_PER_SESSION）；
- 会话须为 ACTIVE 才能继续；
- 固定模板生成 wake_phrase 与 ending_speech，保证稳定。
"""

import uuid
from typing import Optional

from sqlmodel import Session, select

from app.ai.base import AiProvider, PlaySparkRequest, PlaySparkResult
from app.ai.mock_provider import MockAiProvider
from app.config import settings
from app.errors import (
    session_ended,
    session_not_found,
    spark_limit_exceeded,
    toy_not_found,
)
from app.models import Memory, PlaySession, PlayTurn, Toy, utcnow


def _get_provider() -> AiProvider:
    """阶段 3 将在此根据 settings.ai_mode 返回真实 Provider。"""
    return MockAiProvider()


def _latest_memory(session: Session, toy_id: int) -> Optional[Memory]:
    stmt = (
        select(Memory)
        .where(Memory.toy_id == toy_id, Memory.is_active == True)  # noqa: E712
        .order_by(Memory.created_at.desc())
    )
    return session.exec(stmt).first()


def _wake_phrase(name: str) -> str:
    return f"{name}醒啦。"


def _ending_speech(name: str) -> str:
    return f"{name}累了，我们送它回家吧。"


def _record_turn(
    session: Session,
    session_id: str,
    turn_type: str,
    result: PlaySparkResult,
    parent_context: Optional[str] = None,
) -> None:
    turn = PlayTurn(
        session_id=session_id,
        turn_type=turn_type,
        parent_context=parent_context,
        child_speech=result.child_speech,
        parent_hint=result.parent_hint,
        source=result.source,
    )
    session.add(turn)


def _spark_dict(result: PlaySparkResult) -> dict:
    return {
        "child_speech": result.child_speech,
        "parent_hint": result.parent_hint,
        "source": result.source,
        "memory_used": result.memory_used,
    }


async def start_play(session: Session, toy_id: int) -> dict:
    toy = session.get(Toy, toy_id)
    if toy is None or not toy.is_active:
        raise toy_not_found()

    play_session = PlaySession(id=str(uuid.uuid4()), toy_id=toy_id, status="ACTIVE")
    session.add(play_session)
    session.commit()
    session.refresh(play_session)

    memory = _latest_memory(session, toy_id)
    req = PlaySparkRequest(
        toy_name=toy.name,
        toy_type=toy.toy_type,
        trait=toy.trait,
        child_setting=toy.child_setting,
        memory=memory.content if memory else None,
        turn_type="wake",
        spark_count=play_session.spark_count,
    )
    result = await _get_provider().generate_play_spark(req)

    _record_turn(session, play_session.id, "wake", result)
    play_session.spark_count += 1
    session.add(play_session)
    session.commit()

    return {
        "session_id": play_session.id,
        "wake_phrase": _wake_phrase(toy.name),
        "spark": _spark_dict(result),
    }


async def next_spark(
    session: Session, session_id: str, parent_context: Optional[str]
) -> dict:
    play_session = session.get(PlaySession, session_id)
    if play_session is None:
        raise session_not_found()
    if play_session.status != "ACTIVE":
        raise session_ended()
    if play_session.spark_count >= settings.max_sparks_per_session:
        raise spark_limit_exceeded()

    toy = session.get(Toy, play_session.toy_id)
    req = PlaySparkRequest(
        toy_name=toy.name,
        toy_type=toy.toy_type,
        trait=toy.trait,
        child_setting=toy.child_setting,
        memory=None,
        parent_context=parent_context,
        turn_type="spark",
        spark_count=play_session.spark_count,
    )
    result = await _get_provider().generate_play_spark(req)

    _record_turn(session, play_session.id, "spark", result, parent_context)
    play_session.spark_count += 1
    play_session.last_parent_context = parent_context
    session.add(play_session)
    session.commit()

    return {"spark": _spark_dict(result)}


async def end_play(
    session: Session, session_id: str, parent_context: Optional[str]
) -> dict:
    play_session = session.get(PlaySession, session_id)
    if play_session is None:
        raise session_not_found()
    if play_session.status != "ACTIVE":
        raise session_ended()

    toy = session.get(Toy, play_session.toy_id)

    play_session.status = "ENDED"
    play_session.ended_at = utcnow()
    play_session.last_parent_context = parent_context
    session.add(play_session)

    # 结束提示优先固定模板；记忆候选取自家长上下文（mock 模式）
    ending_result = PlaySparkResult(
        child_speech=_ending_speech(toy.name),
        parent_hint="送玩具回家，让孩子决定放哪里。",
        source="fixed",
    )
    _record_turn(session, play_session.id, "end", ending_result, parent_context)

    memory_candidate = (
        parent_context.strip() if parent_context and parent_context.strip() else None
    )

    session.commit()

    return {
        "ending_speech": _ending_speech(toy.name),
        "memory_candidate": memory_candidate,
    }
