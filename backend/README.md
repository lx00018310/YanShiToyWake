# ToyWake 后端

FastAPI + SQLite + SQLModel。统一前缀 `/api/v1`，默认 `AI_MODE=mock` 可离线运行。

## 环境要求

- Python 3.11+

## 启动

```bash
python -m venv .venv
source .venv/Scripts/activate    # Windows Git Bash
pip install -r requirements.txt
cp .env.example .env             # 默认 mock 模式
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

- 交互文档：http://localhost:8000/docs
- 健康检查：http://localhost:8000/api/v1/health

## 测试

```bash
pytest
```

## 配置（.env）

| 变量 | 默认 | 说明 |
|---|---|---|
| AI_MODE | mock | mock 离线 / ai 调用真实模型 |
| AI_BASE_URL | | OpenAI-compatible 地址 |
| AI_API_KEY | | 模型密钥（仅后端，不入客户端） |
| AI_MODEL | | 模型名 |
| AI_TIMEOUT_SECONDS | 10 | 超时 |
| DATABASE_URL | sqlite:///./toywake.db | 数据库 |
| MAX_SPARKS_PER_SESSION | 5 | 每会话火花上限 |

## 依赖版本

见 `requirements.txt`，均为稳定版。
