"""AI Provider 统一接口（实施计划书 §5.3）。

业务逻辑不绑定单一模型供应商；MockAiProvider 用于离线开发，
OpenAICompatibleProvider 在阶段 3 实现。
"""

from abc import ABC, abstractmethod
from typing import Optional

from pydantic import BaseModel


class PlaySparkRequest(BaseModel):
    toy_name: str
    toy_type: str
    trait: Optional[str] = None
    child_setting: Optional[str] = None
    memory: Optional[str] = None  # 至多一条旧记忆
    parent_context: Optional[str] = None  # 家长描述的当前情况
    turn_type: str = "spark"  # wake | spark | end
    spark_count: int = 0  # 当前会话已生成的火花数（供固定内容去重）


class PlaySparkResult(BaseModel):
    child_speech: str
    parent_hint: str
    memory_candidate: Optional[str] = None
    memory_used: Optional[str] = None
    source: str  # ai | fixed | mock


class AiProvider(ABC):
    @abstractmethod
    async def generate_play_spark(self, request: PlaySparkRequest) -> PlaySparkResult:
        ...
