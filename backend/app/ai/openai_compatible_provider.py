"""OpenAI-compatible AI Provider（实施计划书 §5.3、§12.3）。

通过 httpx 调用任意 OpenAI 兼容的 chat completions 接口。
任何失败（网络/超时/非JSON/字段缺失/过长/禁用内容/拒答）均降级为固定内容，
API 仍返回 200，source=fixed，不向儿童暴露错误。
"""

import logging
from typing import Optional

import httpx

from app.ai.base import AiProvider, PlaySparkRequest, PlaySparkResult
from app.ai.output_validator import validate_and_parse
from app.ai.prompt_builder import build_messages
from app.config import settings
from app.services import fallback_service

logger = logging.getLogger("toywake.ai")

# 重试上限：仅对超时/网络错误重试，最多 2 次
_MAX_ATTEMPTS = 2


class OpenAICompatibleProvider(AiProvider):
    async def generate_play_spark(self, request: PlaySparkRequest) -> PlaySparkResult:
        messages = build_messages(request)
        try:
            raw = await self._call_chat(messages)
        except Exception as exc:  # noqa: BLE001 - 任何异常都降级
            logger.warning("AI 调用失败，降级固定内容: %s", type(exc).__name__)
            return self._fallback(request)

        parsed = validate_and_parse(raw)
        if parsed is None:
            logger.warning("AI 输出校验未通过，降级固定内容")
            return self._fallback(request)

        memory_used = (
            request.memory
            if (request.turn_type == "wake" and request.memory)
            else None
        )
        return PlaySparkResult(
            child_speech=parsed["child_speech"],
            parent_hint=parsed["parent_hint"],
            memory_candidate=parsed["memory_candidate"],
            memory_used=memory_used,
            source="ai",
        )

    async def _call_chat(self, messages: list[dict]) -> str:
        """带重试的 chat completions 调用，返回模型内容文本。"""
        last_exc: Optional[Exception] = None
        for attempt in range(_MAX_ATTEMPTS):
            try:
                return await self._single_request(messages)
            except (httpx.TimeoutException, httpx.NetworkError) as exc:
                last_exc = exc
                continue
            # 其他异常（如 HTTPStatusError 401）不重试，直接抛出
        raise last_exc  # type: ignore[misc]

    async def _single_request(self, messages: list[dict]) -> str:
        url = f"{settings.ai_base_url.rstrip('/')}/chat/completions"
        headers = {"Authorization": f"Bearer {settings.ai_api_key}"}
        body = {
            "model": settings.ai_model,
            "messages": messages,
            "temperature": 0.8,
        }
        async with httpx.AsyncClient(timeout=settings.ai_timeout_seconds) as client:
            resp = await client.post(url, headers=headers, json=body)
            resp.raise_for_status()
            data = resp.json()
            return data["choices"][0]["message"]["content"]

    def _fallback(self, request: PlaySparkRequest) -> PlaySparkResult:
        spark = fallback_service.pick_spark(
            request.toy_type, request.toy_name, request.spark_count
        )
        # 固定内容不引用旧记忆
        return PlaySparkResult(
            child_speech=spark.child_speech,
            parent_hint=spark.parent_hint,
            source="fixed",
        )
