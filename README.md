# YanShiToyWake

> **设备负责点火，父母和孩子负责真正的游戏。**

ToyWake 是一个面向 3 岁左右儿童家庭的现实玩具共玩辅助系统。它通过极短的声音、少量记忆和简单仪式，帮助父母与孩子围绕已有玩具开始、延续并结束一场现实中的想象游戏。

它不是儿童聊天机器人，不是故事机，不是早教机，不是电子宠物，不是手机游戏。它的任务是在适当的时候给出一句话，然后安静下来。

> **当设备安静下来，而孩子仍在继续玩，产品才算成功。**

---

## 架构

```text
┌─────────────────────────────────────┐
│ Android App                         │
│ NFC读取 / 家长界面 / 本地提示音      │
│ Android TextToSpeech                │
└──────────────────┬──────────────────┘
                   │ HTTP / JSON
                   ▼
┌─────────────────────────────────────┐
│ FastAPI Backend                     │
│ 玩具绑定与查询 / 游戏会话状态        │
│ SQLite / SQLModel                   │
│ AI Provider Adapter / 固定内容降级   │
└──────────────────┬──────────────────┘
                   │ HTTPS
                   ▼
┌─────────────────────────────────────┐
│ AI API (OpenAI-compatible)          │
└─────────────────────────────────────┘
```

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Python 3.11+ · FastAPI · Uvicorn · SQLite · SQLModel · httpx · pytest |
| Android | Kotlin · Jetpack Compose · Retrofit · kotlinx.serialization · DataStore |
| AI | OpenAI-compatible 适配器，默认 Mock 模式可离线运行 |

## 项目结构

```text
.
├─ README.md
├─ .gitignore
├─ docs/                       # 决策记录、API、测试计划、家庭观察表
├─ backend/                    # FastAPI 后端
│  ├─ app/
│  ├─ tests/
│  ├─ requirements.txt
│  └─ .env.example
└─ android/                    # Kotlin Compose App
```

## 快速开始（后端）

```bash
cd backend
python -m venv .venv
source .venv/Scripts/activate    # Windows Git Bash
pip install -r requirements.txt
cp .env.example .env             # 默认 AI_MODE=mock，无需 API Key
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

访问 `http://localhost:8000/docs` 查看交互式 API 文档。

## 开发阶段

依据《ToyWake 亲子共玩 Demo 实施计划书》分 8 个阶段推进：

- [x] 阶段 0：仓库与规范初始化
- [ ] 阶段 1：后端骨架与数据库
- [ ] 阶段 2：Mock 游戏火花闭环
- [ ] 阶段 3：真实 AI Provider
- [ ] 阶段 4：Android 基础与后端连接
- [ ] 阶段 5：Android NFC 闭环
- [ ] 阶段 6：绑定、TTS 与共玩页面
- [ ] 阶段 7：异常处理与离线降级

## 核心文档

- [产品说明书](ToyWake_3岁亲子共玩产品说明书_V1.0.md)
- [Demo 实施计划书](ToyWake_亲子共玩Demo实施计划书_V1.0.md)
- [技术决策记录](docs/DECISIONS.md)
- [API 文档](docs/API.md)
- [测试计划](docs/TEST_PLAN.md)
- [家庭测试观察表](docs/FAMILY_OBSERVATION.md)
