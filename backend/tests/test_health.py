"""健康检查测试（实施计划书 §11.1）。"""


def test_health_ok(client):
    r = client.get("/api/v1/health")
    assert r.status_code == 200
    data = r.json()
    assert data["status"] == "ok"
    assert data["database"] == "ok"
    assert data["ai_mode"] == "mock"


def test_root(client):
    r = client.get("/")
    assert r.status_code == 200
    assert r.json()["name"] == "ToyWake"
