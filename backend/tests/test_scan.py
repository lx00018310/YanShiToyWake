"""扫描标签测试（实施计划书 §11.2）。"""


def test_scan_new_tag(client):
    r = client.post("/api/v1/scan", json={"tag_uid": "04A1B2C3D4E5F6"})
    assert r.status_code == 200
    data = r.json()
    assert data["status"] == "new"
    assert data["tag_uid"] == "04A1B2C3D4E5F6"
    # exclude_none：新标签不应包含 toy 字段
    assert "toy" not in data


def test_scan_known_tag(client, bind_lion):
    bind_lion()
    r = client.post("/api/v1/scan", json={"tag_uid": "04A1B2C3D4E5F6"})
    assert r.status_code == 200
    data = r.json()
    assert data["status"] == "known"
    # exclude_none：已知标签不应包含 tag_uid 字段
    assert "tag_uid" not in data
    toy = data["toy"]
    assert toy["name"] == "大狮子"
    assert toy["toy_type"] == "毛绒动物"
    assert toy["trait"] == "勇敢"
    assert toy["child_setting"] == "住在沙发下面"


def test_scan_missing_field(client):
    r = client.post("/api/v1/scan", json={})
    assert r.status_code == 422
    assert r.json()["error"]["code"] == "VALIDATION_ERROR"
