"""玩具绑定与查询测试（实施计划书 §11.3、§11.8）。"""

import os
import tempfile

from sqlmodel import Session, SQLModel, create_engine, select

from app.models import Toy


def test_create_toy(client):
    r = client.post(
        "/api/v1/toys",
        json={
            "tag_uid": "04A1B2C3D4E5F6",
            "name": "大狮子",
            "toy_type": "毛绒动物",
            "trait": "勇敢",
            "child_setting": "住在沙发下面",
        },
    )
    assert r.status_code == 201
    data = r.json()
    assert data["status"] == "ok"
    assert data["toy"]["id"] == 1
    assert data["toy"]["name"] == "大狮子"


def test_create_toy_duplicate_uid(client, bind_lion):
    bind_lion()
    r = client.post(
        "/api/v1/toys",
        json={"tag_uid": "04A1B2C3D4E5F6", "name": "另一只", "toy_type": "毛绒动物"},
    )
    assert r.status_code == 409
    assert r.json()["error"]["code"] == "TAG_ALREADY_BOUND"


def test_create_toy_empty_name(client):
    r = client.post(
        "/api/v1/toys",
        json={"tag_uid": "AAA", "name": "", "toy_type": "毛绒动物"},
    )
    assert r.status_code == 422
    assert r.json()["error"]["code"] == "VALIDATION_ERROR"


def test_create_toy_invalid_type(client):
    r = client.post(
        "/api/v1/toys",
        json={"tag_uid": "AAA", "name": "小熊", "toy_type": "飞机"},
    )
    assert r.status_code == 422


def test_get_toy(client, bind_lion):
    bind_lion()
    r = client.get("/api/v1/toys/1")
    assert r.status_code == 200
    assert r.json()["name"] == "大狮子"


def test_get_toy_not_found(client):
    r = client.get("/api/v1/toys/999")
    assert r.status_code == 404
    assert r.json()["error"]["code"] == "TOY_NOT_FOUND"


def test_update_toy(client, bind_lion):
    bind_lion()
    r = client.put("/api/v1/toys/1", json={"trait": "非常勇敢"})
    assert r.status_code == 200
    assert r.json()["trait"] == "非常勇敢"


def test_list_memories_empty(client, bind_lion):
    bind_lion()
    r = client.get("/api/v1/toys/1/memories")
    assert r.status_code == 200
    assert r.json() == []


def test_data_persists_across_engines():
    """验收：重启后端（新引擎指向同一文件）后数据不丢失。"""
    with tempfile.TemporaryDirectory() as d:
        db_path = os.path.join(d, "test.db").replace(os.sep, "/")
        url = f"sqlite:///{db_path}"

        eng1 = create_engine(url, connect_args={"check_same_thread": False})
        SQLModel.metadata.create_all(eng1)
        with Session(eng1) as s:
            s.add(Toy(tag_uid="XYZ", name="小熊", toy_type="毛绒动物"))
            s.commit()
        eng1.dispose()

        eng2 = create_engine(url, connect_args={"check_same_thread": False})
        with Session(eng2) as s:
            toy = s.exec(select(Toy).where(Toy.tag_uid == "XYZ")).first()
            assert toy is not None
            assert toy.name == "小熊"
        eng2.dispose()  # 释放文件锁，Windows 下才能删除临时库
