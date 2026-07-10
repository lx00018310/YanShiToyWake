"""MockAiProvider：离线开发用，基于固定内容库生成火花。

mock 模式下保证完整业务闭环可运行，无需网络与 API Key。
"""

from app.ai.base import AiProvider, PlaySparkRequest, PlaySparkResult
from app.services import fallback_service


class MockAiProvider(AiProvider):
    async def generate_play_spark(self, request: PlaySparkRequest) -> PlaySparkResult:
        # 唤醒轮：若存在旧记忆，引用一条（§10.6 每次最多引用一条）
        if request.turn_type == "wake" and request.memory:
            memory_text = request.memory.rstrip("。.，,！!？?")
            return PlaySparkResult(
                child_speech=f"{request.toy_name}还记得{memory_text}。",
                parent_hint="引用昨天的事，观察孩子反应，不要催促。",
                memory_used=request.memory,
                source="mock",
            )

        spark = fallback_service.pick_spark(
            request.toy_type, request.toy_name, request.spark_count
        )
        return PlaySparkResult(
            child_speech=spark.child_speech,
            parent_hint=spark.parent_hint,
            source="mock",
        )
