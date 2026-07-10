"""记忆管理测试（实施计划书 §11.7、§11.8）。"""


def test_create_memory(client, bind_lion):
    bind_lion()
    r = client.post(
        "/api/v1/memories",
        json={"toy_id": 1, "content": "坐消防车去找小兔。", "memory_type": "event"},
    )
    assert r.status_code == 201
    assert r.json()["memory_id"] == 1


def test_create_memory_toy_not_found(client):
    r = client.post(
        "/api/v1/memories",
        json={"toy_id": 999, "content": "x", "memory_type": "event"},
    )
    assert r.status_code == 404
    assert r.json()["error"]["code"] == "TOY_NOT_FOUND"


def test_create_memory_invalid_type(client, bind_lion):
    bind_lion()
    r = client.post(
        "/api/v1/memories",
        json={"toy_id": 1, "content": "x", "memory_type": "bad_type"},
    )
    assert r.status_code == 422


def test_list_memories(client, bind_lion):
    bind_lion()
    client.post(
        "/api/v1/memories", json={"toy_id": 1, "content": "事件A", "memory_type": "event"}
    )
    client.post(
        "/api/v1/memories", json={"toy_id": 1, "content": "设定B", "memory_type": "setting"}
    )
    r = client.get("/api/v1/toys/1/memories")
    assert r.status_code == 200
    assert len(r.json()) == 2


def test_delete_memory(client, bind_lion):
    bind_lion()
    client.post(
        "/api/v1/memories", json={"toy_id": 1, "content": "事件A", "memory_type": "event"}
    )
    r = client.delete("/api/v1/memories/1")
    assert r.status_code == 200
    # 软删除后列表应为空
    r = client.get("/api/v1/toys/1/memories")
    assert r.json() == []


def test_delete_memory_not_found(client):
    r = client.delete("/api/v1/memories/999")
    assert r.status_code == 404
    assert r.json()["error"]["code"] == "MEMORY_NOT_FOUND"
