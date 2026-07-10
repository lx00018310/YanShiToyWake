"""游戏会话路由：start / next / end（实施计划书 §11.4-11.6）。"""

from fastapi import APIRouter, Depends
from sqlmodel import Session

from app.dependencies import get_session
from app.schemas import (
    PlayEndRequest,
    PlayEndResponse,
    PlayNextRequest,
    PlayNextResponse,
    PlayStartRequest,
    PlayStartResponse,
)
from app.services.play_service import end_play, next_spark, start_play

router = APIRouter(prefix="/play")


@router.post("/start", response_model=PlayStartResponse)
async def play_start(req: PlayStartRequest, session: Session = Depends(get_session)):
    return await start_play(session, req.toy_id)


@router.post("/next", response_model=PlayNextResponse)
async def play_next(req: PlayNextRequest, session: Session = Depends(get_session)):
    return await next_spark(session, req.session_id, req.parent_context)


@router.post("/end", response_model=PlayEndResponse)
async def play_end(req: PlayEndRequest, session: Session = Depends(get_session)):
    return await end_play(session, req.session_id, req.parent_context)
