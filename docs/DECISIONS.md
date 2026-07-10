# 技术决策记录 (DECISIONS)

> 记录架构选择与"不做"事项。每次架构变更更新本文件。

---

## D-001 项目根目录

- **决策**：使用当前目录 `D:\00_personalwork\00_YanShi_toywake` 作为项目根，不额外嵌套 `ToyWake/`。
- **理由**：目录已存在且包含原始规格文档，避免多层嵌套。
- **日期**：阶段 0

## D-002 后端 ORM 选择

- **决策**：采用 SQLModel（基于 SQLAlchemy + Pydantic）。
- **理由**：FastAPI 原生搭配 Pydantic，SQLModel 减少模型与 schema 的重复定义。
- **备选**：纯 SQLAlchemy（更成熟但样板代码更多）。
- **日期**：阶段 1

## D-003 AI Provider 适配器模式

- **决策**：定义统一 `AiProvider` 接口，实现 `MockAiProvider` 与 `OpenAICompatibleProvider`。
- **理由**：业务逻辑不绑定单一模型供应商；Mock 模式保证离线开发与固定测试。
- **日期**：阶段 2/3

## D-004 AI 失败降级策略

- **决策**：AI 任何失败（网络/超时/非JSON/字段缺失/过长/命中禁用规则/空文本/拒答/配置错误）均降级为固定内容库，API 仍返回 200，`source=fixed`。
- **理由**：Demo 不因 AI 问题中断游戏；不向儿童展示技术错误。
- **日期**：阶段 2/3

## D-005 API Key 隔离

- **决策**：API Key 只保存在后端 `.env`，不写入 Android APK，日志不打印完整 Key。
- **理由**：安全；后端负责密钥隔离。
- **日期**：阶段 3

## D-006 默认 AI_MODE=mock

- **决策**：后端默认 `AI_MODE=mock`，无需 API Key 即可运行全部业务闭环。
- **理由**：保证离线开发与 CI 可测试性。
- **日期**：阶段 3

## D-007 Android 版本栈选择（AGP 8.13.2 + 2024 AndroidX）

- **决策**：Android 采用 AGP 8.13.2 + Gradle 9.0.0 + Kotlin 2.1.20 + Compose BOM 2024.12.01 + compileSdk 35，而非 2026 年中最新版。
- **背景**：最新 AndroidX（core-ktx 1.19.0、lifecycle 2.11.0 等）要求 AGP 9.1.0+；AGP 9.0 移除 `org.jetbrains.kotlin.android` 插件改为内置 Kotlin，Compose compiler / serialization 插件配置方式变更，迁移成本高。
- **理由**：AGP 8.x + 显式 Kotlin 插件栈成熟稳定、写法确定；2024 年底 AndroidX 稳定版完全满足 Demo 需求；Gradle 9.0.0 已缓存免下载。
- **验证**：`./gradlew :app:assembleDebug :app:testDebugUnitTest` 构建成功，8 项单元测试通过。
- **日期**：阶段 4

---

## 明确不做（依据计划书 §3.2、§16.2）

- iOS / 微信小程序
- 儿童账号 / 多家庭用户体系
- 儿童自由语音聊天 / 常开麦克风 / 语音转文字
- 独立 NFC 读卡器 / 玩具自动图像识别
- 长篇故事 / 复杂玩具关系图
- 积分、签到、奖励动画 / 云端相册
- 儿童发展评估 / 情绪或健康诊断
- 商业支付 / 应用商店上架
