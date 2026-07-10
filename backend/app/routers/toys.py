"""玩具绑定与查询（实施计划书 §11.3、§11.8）。"""

from fastapi import APIRouter, Depends
from sqlmodel import Session, select

from app.dependencies import get_session
from app.errors import tag_already_bound, toy_not_found
from app.models import Memory, Toy, utcnow
from app.schemas import (
    MemoryOut,
    ToyCreate,
    ToyCreateResponse,
    ToyOut,
    ToyUpdate,
)

router = APIRouter(prefix="/toys")


@router.post("", response_model=ToyCreateResponse, status_code=201)
def create_toy(payload: ToyCreate, session: Session = Depends(get_session)):
    # 先查重，给出明确业务错误（避免直接暴露 IntegrityError）
    existing = session.exec(select(Toy).where(Toy.tag_uid == payload.tag_uid)).first()
    if existing is not None:
        raise tag_already_bound()

    toy = Toy(
        tag_uid=payload.tag_uid,
        name=payload.name,
        toy_type=payload.toy_type,
        trait=payload.trait,
        child_setting=payload.child_setting,
    )
    session.add(toy)
    session.commit()
    session.refresh(toy)
    return ToyCreateResponse(status="ok", toy={"id": toy.id, "name": toy.name})


@router.get("/{toy_id}", response_model=ToyOut)
def get_toy(toy_id: int, session: Session = Depends(get_session)):
    toy = session.get(Toy, toy_id)
    if toy is None or not toy.is_active:
        raise toy_not_found()
    return toy


@router.put("/{toy_id}", response_model=ToyOut)
def update_toy(
    toy_id: int, payload: ToyUpdate, session: Session = Depends(get_session)
):
    toy = session.get(Toy, toy_id)
    if toy is None or not toy.is_active:
        raise toy_not_found()

    data = payload.model_dump(exclude_unset=True)
    for key, value in data.items():
        setattr(toy, key, value)
    toy.updated_at = utcnow()
    session.add(toy)
    session.commit()
    session.refresh(toy)
    return toy


@router.get("/{toy_id}/memories", response_model=list[MemoryOut])
def list_memories(toy_id: int, session: Session = Depends(get_session)):
    toy = session.get(Toy, toy_id)
    if toy is None or not toy.is_active:
        raise toy_not_found()
    stmt = (
        select(Memory)
        .where(Memory.toy_id == toy_id, Memory.is_active == True)  # noqa: E712
        .order_by(Memory.created_at.desc())
    )
    return list(session.exec(stmt))
