"""固定内容降级路径测试（实施计划书 §14、§12.3）。

阶段 2 在 mock 模式下验证固定内容库行为；真实 AI 失败降级在阶段 3 补充。
"""

from app.services import fallback_service


def test_fallback_picks_by_type():
    s = fallback_service.pick_spark("毛绒动物", "小熊", 0)
    assert "小熊" in s.child_speech
    assert s.parent_hint


def test_fallback_no_immediate_repeat():
    sparks = [
        fallback_service.pick_spark("玩具车", "消防车", i).child_speech for i in range(5)
    ]
    for a, b in zip(sparks, sparks[1:]):
        assert a != b


def test_fallback_cycles_after_exhaustion():
    """超过条目数后取模循环，不越界。"""
    s0 = fallback_service.pick_spark("毛绒动物", "小熊", 0)
    s5 = fallback_service.pick_spark("毛绒动物", "小熊", 5)  # 5 % 5 == 0
    assert s0.child_speech == s5.child_speech


def test_fallback_unknown_type_uses_generic():
    s = fallback_service.pick_spark("不存在的类型", "物件", 0)
    assert "物件" in s.child_speech


def test_mock_spark_source_is_mock(client, bind_lion):
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.json()["spark"]["source"] == "mock"
