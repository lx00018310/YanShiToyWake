"""健康检查：GET /api/v1/health"""

from fastapi import APIRouter, Depends
from sqlmodel import Session, select

from app.config import settings
from app.dependencies import get_session

router = APIRouter()


@router.get("/health")
def health(session: Session = Depends(get_session)):
    db_status = "ok"
    try:
        session.exec(select(1))
    except Exception:
        db_status = "error"
    return {"status": "ok", "database": db_status, "ai_mode": settings.ai_mode}
