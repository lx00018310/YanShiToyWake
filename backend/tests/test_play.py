"""游戏会话测试（实施计划书 §11.4-11.6、§10.6、§16.4）。"""


def test_start_play(client, bind_lion):
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.status_code == 200
    data = r.json()
    assert "session_id" in data
    assert data["wake_phrase"] == "大狮子醒啦。"
    spark = data["spark"]
    assert spark["child_speech"]
    assert spark["parent_hint"]
    assert spark["source"] == "mock"


def test_start_play_toy_not_found(client):
    r = client.post("/api/v1/play/start", json={"toy_id": 999})
    assert r.status_code == 404
    assert r.json()["error"]["code"] == "TOY_NOT_FOUND"


def test_next_spark(client, bind_lion):
    bind_lion()
    sid = client.post("/api/v1/play/start", json={"toy_id": 1}).json()["session_id"]
    r = client.post(
        "/api/v1/play/next",
        json={"session_id": sid, "parent_context": "孩子说要去救小兔。"},
    )
    assert r.status_code == 200
    spark = r.json()["spark"]
    assert spark["source"] == "mock"
    assert spark["child_speech"]


def test_next_spark_no_immediate_repeat(client, bind_lion):
    bind_lion()
    sid = client.post("/api/v1/play/start", json={"toy_id": 1}).json()["session_id"]
    sparks = [
        client.post("/api/v1/play/next", json={"session_id": sid})
        .json()["spark"]["child_speech"]
        for _ in range(4)
    ]
    for a, b in zip(sparks, sparks[1:]):
        assert a != b


def test_end_play(client, bind_lion):
    bind_lion()
    sid = client.post("/api/v1/play/start", json={"toy_id": 1}).json()["session_id"]
    r = client.post(
        "/api/v1/play/end",
        json={"session_id": sid, "parent_context": "坐消防车去找小兔。"},
    )
    assert r.status_code == 200
    data = r.json()
    assert data["ending_speech"] == "大狮子累了，我们送它回家吧。"
    assert data["memory_candidate"] == "坐消防车去找小兔。"


def test_end_play_no_context(client, bind_lion):
    bind_lion()
    sid = client.post("/api/v1/play/start", json={"toy_id": 1}).json()["session_id"]
    r = client.post("/api/v1/play/end", json={"session_id": sid})
    assert r.status_code == 200
    assert r.json()["memory_candidate"] is None


def test_next_after_end_rejected(client, bind_lion):
    bind_lion()
    sid = client.post("/api/v1/play/start", json={"toy_id": 1}).json()["session_id"]
    client.post("/api/v1/play/end", json={"session_id": sid})
    r = client.post("/api/v1/play/next", json={"session_id": sid})
    assert r.status_code == 409
    assert r.json()["error"]["code"] == "SESSION_ENDED"


def test_next_unknown_session(client):
    r = client.post("/api/v1/play/next", json={"session_id": "nonexistent"})
    assert r.status_code == 404
    assert r.json()["error"]["code"] == "SESSION_NOT_FOUND"


def test_spark_limit(client, bind_lion, monkeypatch):
    from app.config import settings

    monkeypatch.setattr(settings, "max_sparks_per_session", 2)
    bind_lion()
    sid = client.post("/api/v1/play/start", json={"toy_id": 1}).json()["session_id"]
    client.post("/api/v1/play/next", json={"session_id": sid})  # 第 2 条，允许
    r = client.post("/api/v1/play/next", json={"session_id": sid})  # 第 3 条，超限
    assert r.status_code == 409
    assert r.json()["error"]["code"] == "SPARK_LIMIT_EXCEEDED"


def test_second_day_references_memory(client, bind_lion):
    """验收：第二天唤醒引用昨天的一条记忆（§12.4）。"""
    bind_lion()
    sid = client.post("/api/v1/play/start", json={"toy_id": 1}).json()["session_id"]
    client.post(
        "/api/v1/play/end", json={"session_id": sid, "parent_context": "坐消防车去找小兔。"}
    )
    client.post(
        "/api/v1/memories",
        json={"toy_id": 1, "content": "坐消防车去找小兔。", "memory_type": "event"},
    )

    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    spark = r.json()["spark"]
    assert spark["memory_used"] == "坐消防车去找小兔。"
    assert "坐消防车去找小兔" in spark["child_speech"]
