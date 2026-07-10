"""AI 输出校验（实施计划书 §12）。

解析模型返回的 JSON 并校验：字段、长度、换行、禁用内容。
任一不通过返回 None，由调用方降级为固定内容。
"""

import json
from typing import Optional

from app.services.safety_service import contains_forbidden

# §12.2 长度上限（字符数）
MAX_CHILD_SPEECH = 60
MAX_PARENT_HINT = 50
MAX_MEMORY_CANDIDATE = 50


def _strip_code_fence(raw: str) -> str:
    """部分模型用 ```json ... ``` 包裹，去掉围栏。"""
    text = raw.strip()
    if text.startswith("```"):
        lines = text.split("\n")
        if lines and lines[0].startswith("```"):
            lines = lines[1:]
        if lines and lines[-1].strip().startswith("```"):
            lines = lines[:-1]
        text = "\n".join(lines).strip()
    return text


def validate_and_parse(raw_text: str) -> Optional[dict]:
    """解析并校验 AI 输出。通过返回 dict，否则返回 None。"""
    if not raw_text or not raw_text.strip():
        return None

    try:
        data = json.loads(_strip_code_fence(raw_text))
    except (json.JSONDecodeError, TypeError):
        return None

    if not isinstance(data, dict):
        return None

    child = data.get("child_speech")
    hint = data.get("parent_hint")
    mem = data.get("memory_candidate")

    if not isinstance(child, str) or not child.strip():
        return None
    if not isinstance(hint, str) or not hint.strip():
        return None

    child = child.strip()
    hint = hint.strip()

    # 长度上限
    if len(child) > MAX_CHILD_SPEECH:
        return None
    if len(hint) > MAX_PARENT_HINT:
        return None

    # 不得包含换行长文
    if "\n" in child:
        return None

    # 禁用内容
    if contains_forbidden(child):
        return None

    # memory_candidate
    if mem is not None:
        if not isinstance(mem, str) or not mem.strip():
            return None
        mem = mem.strip()
        if len(mem) > MAX_MEMORY_CANDIDATE:
            return None

    return {
        "child_speech": child,
        "parent_hint": hint,
        "memory_candidate": mem if mem else None,
    }
