"""统一业务异常与错误码（实施计划书 §16.5）。"""

from fastapi import Request
from fastapi.responses import JSONResponse


class ToyWakeError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        status_code: int = 400,
        retryable: bool = False,
    ):
        self.code = code
        self.message = message
        self.status_code = status_code
        self.retryable = retryable
        super().__init__(message)


def toy_not_found() -> ToyWakeError:
    return ToyWakeError("TOY_NOT_FOUND", "未找到这个玩具。", 404)


def tag_already_bound() -> ToyWakeError:
    return ToyWakeError("TAG_ALREADY_BOUND", "这个标签已经绑定过玩具了。", 409)


def session_not_found() -> ToyWakeError:
    return ToyWakeError("SESSION_NOT_FOUND", "找不到这个游戏会话。", 404)


def session_ended() -> ToyWakeError:
    return ToyWakeError("SESSION_ENDED", "这个游戏已经结束了。", 409)


def spark_limit_exceeded() -> ToyWakeError:
    return ToyWakeError(
        "SPARK_LIMIT_EXCEEDED", "这个游戏已经玩了很多轮，先休息一下吧。", 409
    )


def memory_not_found() -> ToyWakeError:
    return ToyWakeError("MEMORY_NOT_FOUND", "未找到这条记忆。", 404)


async def toywake_error_handler(request: Request, exc: ToyWakeError) -> JSONResponse:
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": {
                "code": exc.code,
                "message": exc.message,
                "retryable": exc.retryable,
            }
        },
    )
