"""真实 AI Provider 降级与校验测试（实施计划书 §3 验收、§12.3）。

通过 monkeypatch _call_chat 模拟各种 AI 返回/失败，验证均降级为 source=fixed。
"""

import json

import httpx

from app.ai.openai_compatible_provider import OpenAICompatibleProvider
from app.ai.output_validator import validate_and_parse
from app.ai.prompt_builder import build_messages
from app.ai.base import PlaySparkRequest
from app.config import settings


def _use_ai(monkeypatch):
    monkeypatch.setattr(settings, "ai_mode", "ai")


def _patch_call(monkeypatch, fn):
    """替换 OpenAICompatibleProvider._call_chat。"""
    monkeypatch.setattr(OpenAICompatibleProvider, "_call_chat", fn)


# ---------- 降级场景 ----------


def test_ai_network_error_falls_back(client, bind_lion, monkeypatch):
    _use_ai(monkeypatch)

    async def boom(self, messages):
        raise httpx.ConnectError("no network")

    _patch_call(monkeypatch, boom)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.status_code == 200
    assert r.json()["spark"]["source"] == "fixed"


def test_ai_timeout_falls_back(client, bind_lion, monkeypatch):
    _use_ai(monkeypatch)

    async def boom(self, messages):
        raise httpx.TimeoutException("timeout")

    _patch_call(monkeypatch, boom)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.json()["spark"]["source"] == "fixed"


def test_ai_http_error_falls_back(client, bind_lion, monkeypatch):
    """错 Key 等返回 401 -> 降级。"""
    _use_ai(monkeypatch)
    req = httpx.Request("POST", "http://x/chat/completions")
    resp = httpx.Response(401, request=req)

    async def bad_key(self, messages):
        raise httpx.HTTPStatusError("unauthorized", request=req, response=resp)

    _patch_call(monkeypatch, bad_key)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.json()["spark"]["source"] == "fixed"


def test_ai_non_json_falls_back(client, bind_lion, monkeypatch):
    _use_ai(monkeypatch)

    async def not_json(self, messages):
        return "这不是 JSON，是一段普通文字。"

    _patch_call(monkeypatch, not_json)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.json()["spark"]["source"] == "fixed"


def test_ai_long_text_falls_back(client, bind_lion, monkeypatch):
    _use_ai(monkeypatch)

    async def long_text(self, messages):
        return json.dumps(
            {"child_speech": "一" * 100, "parent_hint": "提示", "memory_candidate": None}
        )

    _patch_call(monkeypatch, long_text)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.json()["spark"]["source"] == "fixed"


def test_ai_forbidden_content_falls_back(client, bind_lion, monkeypatch):
    _use_ai(monkeypatch)

    async def forbidden(self, messages):
        return json.dumps(
            {"child_speech": "不要告诉爸爸妈妈", "parent_hint": "提示"}
        )

    _patch_call(monkeypatch, forbidden)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.json()["spark"]["source"] == "fixed"


def test_ai_missing_fields_falls_back(client, bind_lion, monkeypatch):
    _use_ai(monkeypatch)

    async def incomplete(self, messages):
        return json.dumps({"child_speech": "大狮子想坐车。"})  # 缺 parent_hint

    _patch_call(monkeypatch, incomplete)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.json()["spark"]["source"] == "fixed"


# ---------- 成功路径 ----------


def test_ai_valid_response_used(client, bind_lion, monkeypatch):
    _use_ai(monkeypatch)

    async def good(self, messages):
        return json.dumps(
            {
                "child_speech": "大狮子想坐小红车，你放它上去好吗？",
                "parent_hint": "等孩子行动，不要替他放。",
                "memory_candidate": None,
            }
        )

    _patch_call(monkeypatch, good)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    spark = r.json()["spark"]
    assert spark["source"] == "ai"
    assert spark["child_speech"] == "大狮子想坐小红车，你放它上去好吗？"


def test_ai_code_fence_stripped(client, bind_lion, monkeypatch):
    """模型用 ```json 围裹时仍能解析。"""
    _use_ai(monkeypatch)

    async def fenced(self, messages):
        return '```json\n{"child_speech":"大狮子有点冷。","parent_hint":"等孩子行动。","memory_candidate":null}\n```'

    _patch_call(monkeypatch, fenced)
    bind_lion()
    r = client.post("/api/v1/play/start", json={"toy_id": 1})
    assert r.json()["spark"]["source"] == "ai"


# ---------- 校验器 / 提示词 单元测试 ----------


def test_validator_valid():
    out = validate_and_parse(
        json.dumps({"child_speech": "抱抱我。", "parent_hint": "等孩子。"})
    )
    assert out is not None
    assert out["child_speech"] == "抱抱我。"


def test_validator_rejects_long():
    out = validate_and_parse(
        json.dumps({"child_speech": "一" * 80, "parent_hint": "x"})
    )
    assert out is None


def test_validator_rejects_forbidden():
    out = validate_and_parse(
        json.dumps({"child_speech": "你不陪我我会很伤心", "parent_hint": "x"})
    )
    assert out is None


def test_validator_rejects_newline():
    out = validate_and_parse(
        json.dumps({"child_speech": "第一句\n第二句", "parent_hint": "x"})
    )
    assert out is None


def test_validator_rejects_non_json():
    assert validate_and_parse("not json") is None
    assert validate_and_parse("") is None


def test_prompt_builder_includes_context():
    req = PlaySparkRequest(
        toy_name="大狮子",
        toy_type="毛绒动物",
        trait="勇敢",
        memory="坐过消防车",
        parent_context="孩子想救小兔",
        turn_type="spark",
    )
    msgs = build_messages(req)
    assert msgs[0]["role"] == "system"
    user = msgs[1]["content"]
    assert "大狮子" in user
    assert "坐过消防车" in user
    assert "孩子想救小兔" in user
