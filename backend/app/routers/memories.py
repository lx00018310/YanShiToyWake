"""记忆管理路由：创建 / 删除（实施计划书 §11.7、§11.8）。"""

from fastapi import APIRouter, Depends
from sqlmodel import Session

from app.dependencies import get_session
from app.errors import memory_not_found, toy_not_found
from app.models import Memory, Toy
from app.schemas import MemoryCreate, MemoryCreateResponse

router = APIRouter(prefix="/memories")


@router.post("", response_model=MemoryCreateResponse, status_code=201)
def create_memory(payload: MemoryCreate, session: Session = Depends(get_session)):
    toy = session.get(Toy, payload.toy_id)
    if toy is None or not toy.is_active:
        raise toy_not_found()

    memory = Memory(
        toy_id=payload.toy_id,
        content=payload.content,
        memory_type=payload.memory_type,
    )
    session.add(memory)
    session.commit()
    session.refresh(memory)
    return MemoryCreateResponse(status="ok", memory_id=memory.id)


@router.delete("/{memory_id}")
def delete_memory(memory_id: int, session: Session = Depends(get_session)):
    memory = session.get(Memory, memory_id)
    if memory is None or not memory.is_active:
        raise memory_not_found()
    memory.is_active = False
    session.add(memory)
    session.commit()
    return {"status": "ok"}
