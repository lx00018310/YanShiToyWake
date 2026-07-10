"""扫描标签：POST /api/v1/scan

读取 tag_uid，返回 new（未绑定）或 known（已绑定玩具资料）。
"""

from fastapi import APIRouter, Depends
from sqlmodel import Session, select

from app.dependencies import get_session
from app.models import Toy, utcnow
from app.schemas import ScanRequest, ScanResponse, ToyBrief

router = APIRouter()


@router.post("/scan", response_model=ScanResponse, response_model_exclude_none=True)
def scan(req: ScanRequest, session: Session = Depends(get_session)):
    toy = session.exec(select(Toy).where(Toy.tag_uid == req.tag_uid)).first()
    if toy is None:
        return ScanResponse(status="new", tag_uid=req.tag_uid)

    # 更新最近扫描时间
    toy.last_seen_at = utcnow()
    session.add(toy)
    session.commit()

    return ScanResponse(
        status="known",
        toy=ToyBrief(
            id=toy.id,
            name=toy.name,
            toy_type=toy.toy_type,
            trait=toy.trait,
            child_setting=toy.child_setting,
        ),
    )
