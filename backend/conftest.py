"""pytest 配置：为每个测试提供隔离的内存数据库 + TestClient。

通过替换 database.engine，让 init_db（lifespan 启动时调用）与 get_session 都使用内存引擎，
不触碰真实 toywake.db 文件。StaticPool 保证同一内存库被所有连接共享。
"""

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.pool import StaticPool
from sqlmodel import SQLModel, create_engine

from app import database
from app.main import app

LION_PAYLOAD = {
    "tag_uid": "04A1B2C3D4E5F6",
    "name": "大狮子",
    "toy_type": "毛绒动物",
    "trait": "勇敢",
    "child_setting": "住在沙发下面",
}


@pytest.fixture(name="client")
def client_fixture():
    test_engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    SQLModel.metadata.create_all(test_engine)

    original_engine = database.engine
    database.engine = test_engine
    try:
        with TestClient(app) as c:
            yield c
    finally:
        database.engine = original_engine


@pytest.fixture
def bind_lion(client):
    """绑定大狮子玩具，返回响应 JSON。"""

    def _bind(payload=LION_PAYLOAD):
        r = client.post("/api/v1/toys", json=payload)
        assert r.status_code == 201, r.text
        return r.json()

    return _bind
