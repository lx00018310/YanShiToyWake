"""ToyWake 后端入口。

统一前缀 /api/v1；启动时建表；统一错误格式。
"""

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from app.database import init_db
from app.errors import ToyWakeError, toywake_error_handler
from app.routers import health, scan, toys


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时创建表（可重复调用）
    init_db()
    yield


app = FastAPI(title="ToyWake API", version="0.1.0", lifespan=lifespan)

# 统一业务异常
app.add_exception_handler(ToyWakeError, toywake_error_handler)


# 输入校验异常 -> 统一格式
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc: RequestValidationError):
    return JSONResponse(
        status_code=422,
        content={
            "error": {
                "code": "VALIDATION_ERROR",
                "message": "输入不合法，请检查后重试。",
                "retryable": False,
            }
        },
    )


API_PREFIX = "/api/v1"
app.include_router(health.router, prefix=API_PREFIX)
app.include_router(scan.router, prefix=API_PREFIX)
app.include_router(toys.router, prefix=API_PREFIX)


@app.get("/")
def root():
    return {"name": "ToyWake", "version": "0.1.0", "docs": "/docs"}
