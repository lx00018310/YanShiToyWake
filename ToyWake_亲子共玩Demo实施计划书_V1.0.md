# ToyWake 亲子共玩 Demo 实施计划书 V1.0

> 面向对象：本地 AI 开发代理、产品开发者、测试人员  
> Demo 目标年龄：3 岁左右儿童及其父母  
> 核心技术形态：Android App + NFC 贴纸 + FastAPI 极简后端 + AI API + Android TTS  
> 核心产品原则：**设备负责点火，父母和孩子负责真正的游戏。**

---

# 0. 文档用途

本计划书用于指导本地 AI 从零实现一个可在真实家庭中测试的 ToyWake Demo。

本 Demo 不是完整商业产品，也不是儿童 AI 聊天机器人。它只验证一个核心假设：

> 当一个现实玩具被手机识别，并通过一句带有少量记忆的语音发起现实动作时，孩子是否会拿起玩具，并继续和父母玩。

实施过程中必须始终遵守以下约束：

1. 不扩展为长篇故事机；
2. 不扩展为儿童自由聊天机器人；
3. 不追求复杂 UI；
4. 不开发独立硬件；
5. 不开发云端多用户体系；
6. 不让儿童持续注视手机；
7. AI 每次只提供一句简短、可执行的“游戏火花”；
8. AI 出错或网络不可用时，固定内容仍然可以完成演示；
9. 手机由家长操作，儿童只接触现实玩具；
10. 每完成一个阶段，都必须实际运行、测试、记录结果，再进入下一阶段。

---

# 1. Demo 最终效果

## 1.1 第一次绑定

家长把 NFC 贴纸贴在一只现实玩具上，例如“大狮子”。

家长打开 ToyWake App，用手机靠近 NFC 贴纸。

App 读取标签 UID，发现尚未绑定，进入绑定页面。

家长填写：

```text
名字：大狮子
类型：毛绒动物
特点：勇敢
孩子设定：住在沙发下面
```

保存后，系统将 NFC UID 与“大狮子”绑定。

---

## 1.2 再次唤醒

家长再次用手机碰触大狮子的 NFC 贴纸。

App 立即发出一个短提示音，并通过本地 TTS 说：

> “大狮子醒啦。”

随后后端根据玩具资料和最近一条记忆调用 AI，返回一句游戏火花：

> “大狮子想坐小红车，你放它上去好吗？”

App 通过 TTS 播放这句话。

随后手机保持安静，父母和孩子开始现实游戏。

---

## 1.3 游戏继续

当游戏停滞时，家长点击：

```text
再来一个点子
```

家长可以选择不输入，也可以输入孩子刚才的行为：

```text
孩子说要去救小兔。
```

后端调用 AI，返回：

> “快让大狮子坐上消防车，我们去哪里找小兔？”

App 播放后再次保持安静。

---

## 1.4 游戏结束

家长点击：

```text
准备结束
```

系统返回一条结束提示：

> “大狮子累了，我们送它回家吧。”

家长可选择保存一条本次记忆：

```text
今天大狮子坐消防车去找小兔。
```

第二天再次扫描时，系统可以说：

> “大狮子还记得昨天坐过消防车。”

---

# 2. Demo 成功标准

本 Demo 的成功不以代码量、AI 对话轮数或 UI 精美程度判断。

## 2.1 产品成功标准

真实测试时，以下现象出现至少 3 项，说明方向值得继续：

1. 孩子听到语音后主动拿起玩具；
2. 手机收起后，孩子继续玩超过 2 分钟；
3. 孩子执行了语音提出的动作；
4. 孩子主动加入另一个玩具；
5. 孩子纠正或改变玩具设定；
6. 孩子第二天要求再次唤醒；
7. 引用昨天记忆后，孩子有明显反应；
8. 父母认为比自己临时编故事更轻松；
9. AI 不再说话后，游戏仍然继续。

## 2.2 技术成功标准

1. Android 手机稳定读取 NFC 标签 UID；
2. 新标签可以绑定玩具；
3. 已知标签可以识别玩具；
4. App 能调用后端；
5. 后端能调用 AI API；
6. App 能通过系统 TTS 播放中文；
7. AI 返回失败时能自动使用固定内容；
8. 玩具资料和最近记忆重启后不丢失；
9. 所有关键接口有自动化测试；
10. Demo 可以按文档在另一台开发电脑上重新启动。

---

# 3. 系统边界

## 3.1 本次必须实现

- Android NFC Reader Mode；
- 读取标签 UID；
- 新玩具绑定；
- 已绑定玩具识别；
- 玩具资料存储；
- 最近记忆存储；
- AI 游戏火花生成；
- 固定内容降级；
- Android 系统 TTS；
- 家长输入一条上下文；
- “再来一个点子”；
- “准备结束”；
- 简单设置页；
- 后端健康检查；
- 基础日志；
- 单元测试与接口测试。

## 3.2 本次明确不实现

- iOS；
- 微信小程序；
- 儿童账号；
- 多家庭用户体系；
- 儿童自由语音聊天；
- 常开麦克风；
- 语音转文字；
- 独立 NFC 读卡器；
- 玩具自动图像识别；
- 长篇故事；
- 复杂玩具关系图；
- 积分、签到、奖励动画；
- 云端相册；
- 儿童发展评估；
- 情绪或健康诊断；
- 商业支付；
- App Store 或应用商店上架。

---

# 4. 总体架构

```text
┌─────────────────────────────────────┐
│ Android App                         │
│                                     │
│ NFC读取                             │
│ 标签UID标准化                       │
│ 家长界面                            │
│ 本地提示音                          │
│ Android TextToSpeech                │
│ 后端地址与配置                      │
└──────────────────┬──────────────────┘
                   │ HTTP / JSON
                   ▼
┌─────────────────────────────────────┐
│ FastAPI Backend                     │
│                                     │
│ 玩具绑定与查询                      │
│ 游戏会话状态                        │
│ SQLite / SQLModel                   │
│ AI Provider Adapter                 │
│ 提示词与儿童内容约束                │
│ 固定内容降级                        │
│ API Key 隔离                        │
└──────────────────┬──────────────────┘
                   │ HTTPS
                   ▼
┌─────────────────────────────────────┐
│ AI API                              │
│ 任意支持文本生成的服务              │
└─────────────────────────────────────┘
```

## 4.1 为什么保留后端

AI API Key 不应直接写入 Android APK。

后端负责：

- 保存 API Key；
- 保存玩具资料和记忆；
- 组装提示词；
- 强制输出长度；
- 校验结构化返回；
- 内容安全检查；
- AI 异常降级；
- 统一日志。

## 4.2 开发期运行方式

第一阶段默认：

```text
Android 手机
   ↓ 同一局域网
开发电脑 FastAPI
   ↓
AI API
```

例如：

```text
后端地址：http://192.168.1.100:8000
```

App 设置页允许家长修改后端地址。

后续有需要再部署到云端，本 Demo 不强制。

---

# 5. 技术选型

## 5.1 Android

建议：

```text
语言：Kotlin
UI：Jetpack Compose
架构：单 Activity + Navigation Compose
状态管理：ViewModel + StateFlow
网络：Retrofit + OkHttp，或等价稳定方案
序列化：kotlinx.serialization，或等价稳定方案
本地配置：DataStore
最低 Android 版本：由本地 AI 根据测试手机确认
```

版本号不要在计划书中硬编码。

执行时由本地 AI：

1. 查询当前稳定版本；
2. 统一写入 Version Catalog；
3. 避免使用 alpha、beta、preview 版本；
4. 在 README 记录实际版本。

## 5.2 后端

建议：

```text
Python：3.11+
框架：FastAPI
运行：Uvicorn
数据库：SQLite
ORM：SQLModel 或 SQLAlchemy
配置：pydantic-settings
HTTP 客户端：httpx
测试：pytest + FastAPI TestClient
迁移：Demo 可暂不引入 Alembic
```

## 5.3 AI Provider

必须采用适配器，不把业务逻辑绑定到某一家模型。

统一接口：

```python
class AiProvider:
    async def generate_play_spark(self, request: PlaySparkRequest) -> PlaySparkResult:
        ...
```

实现至少两个 Provider：

```text
MockAiProvider
OpenAICompatibleProvider
```

其中：

- `MockAiProvider` 用于离线开发和固定测试；
- `OpenAICompatibleProvider` 通过环境变量配置 Base URL、API Key、模型名；
- 其他模型供应商后续只需增加 Adapter。

---

# 6. 核心业务对象

## 6.1 Toy：玩具

字段：

```text
id
tag_uid
name
toy_type
trait
child_setting
created_at
updated_at
last_seen_at
is_active
```

示例：

```json
{
  "id": 1,
  "tag_uid": "04AABBCCDD1122",
  "name": "大狮子",
  "toy_type": "毛绒动物",
  "trait": "勇敢",
  "child_setting": "住在沙发下面",
  "is_active": true
}
```

## 6.2 Memory：记忆

字段：

```text
id
toy_id
content
memory_type
created_at
is_active
```

`memory_type` 只允许：

```text
event
setting
relationship
preference
```

Demo 中每个玩具最多取最近 3 条记忆参与后台处理，但每次生成只允许引用其中 1 条。

## 6.3 PlaySession：游戏会话

字段：

```text
id
toy_id
status
started_at
ended_at
last_parent_context
spark_count
```

状态：

```text
ACTIVE
ENDED
CANCELLED
```

## 6.4 PlayTurn：游戏轮次

字段：

```text
id
session_id
turn_type
parent_context
child_speech
parent_hint
source
created_at
```

`source`：

```text
ai
fixed
mock
```

Demo 可保留 PlayTurn，便于后续评估，但不保存儿童原始语音。

---

# 7. 数据库设计

建议四张表。

## 7.1 toys

```text
id INTEGER PRIMARY KEY
tag_uid TEXT UNIQUE NOT NULL
name TEXT NOT NULL
toy_type TEXT NOT NULL
trait TEXT
child_setting TEXT
created_at DATETIME NOT NULL
updated_at DATETIME NOT NULL
last_seen_at DATETIME
is_active BOOLEAN NOT NULL DEFAULT TRUE
```

## 7.2 memories

```text
id INTEGER PRIMARY KEY
toy_id INTEGER NOT NULL
content TEXT NOT NULL
memory_type TEXT NOT NULL
created_at DATETIME NOT NULL
is_active BOOLEAN NOT NULL DEFAULT TRUE
FOREIGN KEY toy_id REFERENCES toys(id)
```

## 7.3 play_sessions

```text
id TEXT PRIMARY KEY
toy_id INTEGER NOT NULL
status TEXT NOT NULL
started_at DATETIME NOT NULL
ended_at DATETIME
last_parent_context TEXT
spark_count INTEGER NOT NULL DEFAULT 0
FOREIGN KEY toy_id REFERENCES toys(id)
```

## 7.4 play_turns

```text
id INTEGER PRIMARY KEY
session_id TEXT NOT NULL
turn_type TEXT NOT NULL
parent_context TEXT
child_speech TEXT NOT NULL
parent_hint TEXT
source TEXT NOT NULL
created_at DATETIME NOT NULL
FOREIGN KEY session_id REFERENCES play_sessions(id)
```

---

# 8. NFC 识别设计

## 8.1 MVP 标识方式

MVP 默认使用 Android `Tag.id` 转换后的十六进制字符串作为 `tag_uid`。

标准化规则：

1. 读取字节数组；
2. 转换为大写十六进制；
3. 不带冒号；
4. 不带空格；
5. 作为数据库唯一键。

示例：

```text
原始：04 A1 B2 C3 D4 E5 F6
保存：04A1B2C3D4E5F6
```

## 8.2 可选 NDEF 支持

本 Demo 不要求向 NFC 标签写入数据。

可选增强：

- 若标签内包含 NDEF 文本 `toywake:<uuid>`，优先使用该值；
- 若不存在，则使用 Tag UID；
- 该增强不得阻塞 UID 方案。

## 8.3 NFC 页面行为

进入扫描页时：

- 检查设备是否支持 NFC；
- 检查 NFC 是否开启；
- 不支持时显示明确提示；
- 未开启时引导家长打开系统 NFC 设置；
- 页面处于前台时开启 Reader Mode；
- 页面退出或 Activity 暂停时关闭 Reader Mode；
- 500 至 1000 毫秒内重复读到同一标签时去重；
- 正在处理标签时禁止重复提交。

## 8.4 NFC 状态

```text
UNAVAILABLE
DISABLED
READY
TAG_DETECTED
RESOLVING
ERROR
```

---

# 9. App 页面设计

采用单 Activity，建议 6 个页面。

## 9.1 启动与扫描页

显示：

- ToyWake 标识；
- 当前 NFC 状态；
- “请用手机轻触玩具”；
- 后端连接状态；
- 设置入口。

状态文案：

```text
等待玩具
检测到玩具
正在叫醒……
NFC 未开启
无法连接服务
```

扫描到标签后自动调用：

```text
POST /api/v1/scan
```

## 9.2 新玩具绑定页

字段：

```text
玩具名字：必填，1—20 字
玩具类型：必填，预设选项
简单特点：选填，最多 20 字
孩子设定：选填，最多 50 字
```

玩具类型：

```text
毛绒动物
玩具车
人偶
积木角色
过家家物品
其他
```

按钮：

```text
保存并叫醒
取消
```

## 9.3 唤醒与共玩页

页面只保留必要信息：

```text
大狮子醒啦

[正在播放语音 / 已播放]

[再来一个点子]
[告诉它刚才发生了什么]
[准备结束]
```

不显示长聊天记录。

可以显示最近一句，但不允许页面形成传统聊天气泡流。

## 9.4 家长上下文输入页

输入框标题：

```text
孩子刚才说了什么，或做了什么？
```

示例提示：

```text
例如：孩子说要去救小兔。
例如：孩子把大狮子放进了消防车。
```

限制：

- 最多 100 字；
- 输入由家长操作；
- 不做儿童自由聊天；
- 提交后生成下一条游戏火花。

## 9.5 游戏结束页

显示：

```text
今天发生了什么？
```

默认可由后端给出一个记忆候选：

```text
大狮子坐消防车去找小兔。
```

家长可以：

```text
保存这条记忆
修改后保存
不保存
```

保存后返回扫描页。

## 9.6 设置页

包含：

- 后端 Base URL；
- 连接测试；
- AI 模式 / 固定内容模式；
- TTS 语速；
- TTS 音量提示；
- 是否自动播放；
- 清理本地缓存；
- App 版本。

不要在 App 中配置或保存 AI API Key。

---

# 10. Android 状态机

## 10.1 顶层状态

```text
BOOTING
READY_TO_SCAN
READING_TAG
RESOLVING_TAG
BINDING_NEW_TOY
STARTING_PLAY
PLAYING
REQUESTING_NEXT_SPARK
ENDING_PLAY
ERROR
```

## 10.2 核心流转

```text
BOOTING
  ↓
READY_TO_SCAN
  ↓ NFC标签
READING_TAG
  ↓ UID标准化
RESOLVING_TAG
  ├─ 新标签 → BINDING_NEW_TOY
  ├─ 已知标签 → STARTING_PLAY
  └─ 失败 → ERROR
```

绑定后：

```text
BINDING_NEW_TOY
  ↓ 保存成功
STARTING_PLAY
  ↓
PLAYING
```

继续游戏：

```text
PLAYING
  ↓ 再来一个点子
REQUESTING_NEXT_SPARK
  ↓
PLAYING
```

结束：

```text
PLAYING
  ↓ 准备结束
ENDING_PLAY
  ↓ 保存或跳过记忆
READY_TO_SCAN
```

## 10.3 状态机约束

- 任意时刻只能有一个网络请求控制页面主状态；
- 重复 NFC 事件不得重复创建会话；
- App 切后台时停止 TTS；
- App 恢复前台后不自动重播旧内容；
- AI 超时后自动进入固定内容，不进入死循环；
- 会话结束后不能继续调用 next；
- 用户可随时返回扫描页并取消当前会话。

---

# 11. 后端 API

统一前缀：

```text
/api/v1
```

## 11.1 健康检查

```http
GET /api/v1/health
```

返回：

```json
{
  "status": "ok",
  "database": "ok",
  "ai_mode": "mock"
}
```

## 11.2 扫描标签

```http
POST /api/v1/scan
```

请求：

```json
{
  "tag_uid": "04A1B2C3D4E5F6"
}
```

新标签：

```json
{
  "status": "new",
  "tag_uid": "04A1B2C3D4E5F6"
}
```

已绑定：

```json
{
  "status": "known",
  "toy": {
    "id": 1,
    "name": "大狮子",
    "toy_type": "毛绒动物",
    "trait": "勇敢",
    "child_setting": "住在沙发下面"
  }
}
```

## 11.3 绑定玩具

```http
POST /api/v1/toys
```

请求：

```json
{
  "tag_uid": "04A1B2C3D4E5F6",
  "name": "大狮子",
  "toy_type": "毛绒动物",
  "trait": "勇敢",
  "child_setting": "住在沙发下面"
}
```

返回：

```json
{
  "status": "ok",
  "toy": {
    "id": 1,
    "name": "大狮子"
  }
}
```

要求：

- `tag_uid` 唯一；
- 重复绑定返回 409；
- 输入必须做长度校验；
- 不接受空名字。

## 11.4 开始游戏

```http
POST /api/v1/play/start
```

请求：

```json
{
  "toy_id": 1
}
```

返回：

```json
{
  "session_id": "uuid",
  "wake_phrase": "大狮子醒啦。",
  "spark": {
    "child_speech": "大狮子想坐小红车，你放它上去好吗？",
    "parent_hint": "等待孩子行动，不要替孩子决定坐在哪里。",
    "source": "ai",
    "memory_used": null
  }
}
```

说明：

- `wake_phrase` 可以由后端模板生成；
- App 可立即本地播放 wake phrase；
- `child_speech` 是游戏火花；
- `parent_hint` 只给家长看；
- 每次只能引用一条记忆。

## 11.5 下一条游戏火花

```http
POST /api/v1/play/next
```

请求：

```json
{
  "session_id": "uuid",
  "parent_context": "孩子说要去救小兔。"
}
```

返回：

```json
{
  "spark": {
    "child_speech": "快让大狮子坐上消防车，我们去哪里找小兔？",
    "parent_hint": "让孩子决定地点，不要替他回答。",
    "source": "ai",
    "memory_used": null
  }
}
```

限制：

- 会话必须为 ACTIVE；
- 每个会话建议最多 5 次 AI 火花；
- 超过上限返回可理解的业务错误；
- `parent_context` 可为空；
- 输入最多 100 字。

## 11.6 结束游戏

```http
POST /api/v1/play/end
```

请求：

```json
{
  "session_id": "uuid",
  "parent_context": "孩子让大狮子坐消防车去找小兔。"
}
```

返回：

```json
{
  "ending_speech": "大狮子累了，我们送它回家吧。",
  "memory_candidate": "大狮子坐消防车去找小兔。"
}
```

结束提示优先固定模板，保证稳定。

## 11.7 保存记忆

```http
POST /api/v1/memories
```

请求：

```json
{
  "toy_id": 1,
  "content": "大狮子坐消防车去找小兔。",
  "memory_type": "event"
}
```

返回：

```json
{
  "status": "ok",
  "memory_id": 10
}
```

## 11.8 查询与编辑玩具

```http
GET /api/v1/toys/{toy_id}
PUT /api/v1/toys/{toy_id}
GET /api/v1/toys/{toy_id}/memories
DELETE /api/v1/memories/{memory_id}
```

这些接口主要用于测试与家长维护，可以在 Android Demo 后期补充简单入口。

---

# 12. AI 输出协议

AI 不直接返回任意文本，必须返回结构化 JSON。

## 12.1 输出 Schema

```json
{
  "child_speech": "大狮子想坐小红车，你放它上去好吗？",
  "parent_hint": "等待孩子行动，不要替孩子选择。",
  "memory_candidate": null
}
```

## 12.2 校验规则

`child_speech`：

- 必填；
- 1 至 2 句；
- 建议不超过 30 个汉字；
- 最多 60 个字符；
- 必须包含可执行动作或简单选择；
- 不得包含换行长文；
- 不得要求继续与 AI 对话。

`parent_hint`：

- 必填；
- 最多 50 个汉字；
- 必须是给父母的操作建议；
- 不对儿童进行诊断和评价。

`memory_candidate`：

- 可为空；
- 最多 50 个汉字；
- 只描述本次明确发生的事件；
- 不推断儿童人格。

## 12.3 AI 失败处理

以下任一情况视为失败：

- 网络错误；
- 超时；
- 返回非 JSON；
- 字段缺失；
- 内容过长；
- 命中禁用规则；
- 返回空文本；
- 模型拒答；
- Provider 配置错误。

失败后：

1. 记录错误日志；
2. 不向儿童展示错误详情；
3. 从固定内容库选择一条适合玩具类型的火花；
4. 返回 `source=fixed`；
5. App 正常播放，不中断游戏。

---

# 13. AI 系统提示词

```text
你是一个面向3岁儿童家庭的亲子陪玩提示生成器。

你不是儿童聊天机器人。
你不是故事机。
你不是老师。
你不是儿童心理评估工具。

你的任务是根据一个现实玩具、最多一条过去记忆，以及家长描述的当前情况，生成一句可以立即引发现实动作的“游戏火花”。

目标：
1. 让孩子看向和拿起现实玩具；
2. 让父母更容易加入游戏；
3. 说完后立即把主动权交给孩子和父母；
4. 不要求孩子继续看屏幕或和AI对话。

儿童语音规则：
1. 优先只说1句，最多2句；
2. 使用3岁儿童能理解的简单词语；
3. 每次只提出一个主要动作；
4. 优先使用：抱、找、藏、推、搬、搭、喂、盖、排队、送、坐；
5. 可以提出一个简单问题，但不能连续追问；
6. 不讲完整故事；
7. 不讲道理；
8. 不评价孩子对错；
9. 接受孩子改变玩具设定；
10. 不制造遗弃、死亡、受伤、怪物追逐或永久消失；
11. 不要求儿童保守秘密；
12. 不引导儿童离开父母视线；
13. 不使用“你不陪我，我会伤心”等负罪表达；
14. 不使用成人化关系表达；
15. 不提供危险动作；
16. 不要求儿童继续向AI回答。

家长提示规则：
1. 只给一条简短建议；
2. 建议父母观察、等待、模仿或提一个简单问题；
3. 不替孩子决定剧情；
4. 不进行儿童诊断。

请严格返回 JSON：
{
  "child_speech": "一句儿童语音",
  "parent_hint": "一句家长提示",
  "memory_candidate": null
}
```

---

# 14. 固定内容库

后端必须内置固定内容库，至少覆盖以下类型。

## 14.1 毛绒动物

```text
{name}有点冷，给它找块小被子吧。
{name}肚子饿了，你想给它吃什么？
{name}找不到家了，你带它去哪里？
{name}想坐小车，你放它上去好吗？
{name}困了，我们给它找个睡觉的地方。
```

## 14.2 玩具车

```text
{name}要运两块积木。
{name}前面的路被挡住了。
{name}听见桌子下面有人叫它。
{name}要过一座桥，我们搭一座吧。
{name}累了，给它找个停车的地方。
```

## 14.3 人偶或积木角色

```text
{name}今天没有家，我们搭一个吧。
{name}想去找一位朋友。
{name}藏起来了，我们找找看。
{name}要把这个东西送过去。
{name}想邀请一个玩具来做客。
```

选择规则：

- 避免同一会话重复；
- 与上一条动作不同；
- 固定内容也必须返回家长提示；
- 支持按玩具类型选择。

---

# 15. TTS 设计

## 15.1 播放顺序

开始游戏：

```text
短提示音
↓
本地 wake phrase
↓
等待 TTS 播放完成
↓
AI / 固定游戏火花
```

避免两个 TTS 同时播放。

## 15.2 TTS 生命周期

- 初始化完成前不得调用 speak；
- 初始化失败时显示家长提示；
- 页面退出时停止播放；
- Activity 销毁时 shutdown；
- App 切后台时 stop；
- 同一内容不自动重复播放；
- 提供“再听一次”小按钮，但不突出。

## 15.3 TTS 文本

只播放：

```text
wake_phrase
child_speech
ending_speech
```

不播放：

- parent_hint；
- 网络错误；
- 技术状态；
- 模型信息；
- 数据保存结果。

---

# 16. 错误与降级策略

## 16.1 后端不可达

App：

- 显示“暂时连接不上服务”；
- 允许进入固定内容本地模式；
- 不显示技术堆栈；
- 提供“重新连接”；
- 设置页可以重新填写 Base URL。

## 16.2 AI 不可用

后端：

- 自动固定内容降级；
- API 仍返回 200；
- `source=fixed`；
- 日志记录真实原因。

## 16.3 NFC 不可用

- 设备无 NFC：明确说明该手机不支持；
- NFC 未开启：引导进入系统设置；
- 标签读取失败：提示重新靠近；
- 重复读取：静默去重；
- 标签未绑定：进入绑定流程。

## 16.4 数据异常

- 重复 UID：返回 409；
- 玩具不存在：404；
- 会话已结束：409；
- 输入过长：422；
- 数据库异常：500，并返回统一错误码。

## 16.5 统一错误格式

```json
{
  "error": {
    "code": "TOY_NOT_FOUND",
    "message": "未找到这个玩具。",
    "retryable": false
  }
}
```

---

# 17. 安全与隐私

## 17.1 API Key

- 只保存在后端 `.env`；
- `.env` 不提交 Git；
- 提供 `.env.example`；
- Android 中不得出现真实 Key；
- 日志不得打印完整 Key。

## 17.2 儿童数据

Demo 默认只保存：

- 玩具名称；
- 玩具设定；
- 家长输入的简短游戏事件；
- AI 生成的短句；
- 时间戳。

不保存：

- 儿童姓名；
- 儿童照片；
- 原始录音；
- 家庭地址；
- 儿童健康信息；
- 对儿童性格或能力的推断。

## 17.3 物理安全

本计划只实现软件 Demo，但测试时必须在文档中提示：

- NFC 贴纸应由家长检查；
- 不贴在儿童容易撕咬的位置；
- 脱落后立即收走；
- 未通过正式儿童产品安全测试前，不作为无人看护玩具使用。

---

# 18. 项目目录

```text
ToyWake/
├─ README.md
├─ .gitignore
├─ docs/
│  ├─ DEMO_IMPLEMENTATION_PLAN.md
│  ├─ API.md
│  ├─ TEST_PLAN.md
│  ├─ FAMILY_OBSERVATION.md
│  └─ DECISIONS.md
│
├─ backend/
│  ├─ app/
│  │  ├─ main.py
│  │  ├─ config.py
│  │  ├─ database.py
│  │  ├─ models.py
│  │  ├─ schemas.py
│  │  ├─ dependencies.py
│  │  ├─ routers/
│  │  │  ├─ health.py
│  │  │  ├─ scan.py
│  │  │  ├─ toys.py
│  │  │  ├─ play.py
│  │  │  └─ memories.py
│  │  ├─ services/
│  │  │  ├─ play_service.py
│  │  │  ├─ memory_service.py
│  │  │  ├─ safety_service.py
│  │  │  └─ fallback_service.py
│  │  └─ ai/
│  │     ├─ base.py
│  │     ├─ mock_provider.py
│  │     ├─ openai_compatible_provider.py
│  │     ├─ prompt_builder.py
│  │     └─ output_validator.py
│  ├─ tests/
│  │  ├─ test_health.py
│  │  ├─ test_scan.py
│  │  ├─ test_toys.py
│  │  ├─ test_play.py
│  │  ├─ test_memories.py
│  │  └─ test_ai_fallback.py
│  ├─ requirements.txt
│  ├─ .env.example
│  └─ README.md
│
└─ android/
   ├─ app/
   │  └─ src/main/java/.../
   │     ├─ MainActivity.kt
   │     ├─ navigation/
   │     ├─ data/
   │     │  ├─ remote/
   │     │  ├─ preferences/
   │     │  └─ repository/
   │     ├─ domain/
   │     │  ├─ model/
   │     │  └─ usecase/
   │     ├─ nfc/
   │     │  ├─ NfcReaderManager.kt
   │     │  └─ TagUidFormatter.kt
   │     ├─ tts/
   │     │  └─ ToyWakeTtsManager.kt
   │     └─ ui/
   │        ├─ scan/
   │        ├─ bind/
   │        ├─ play/
   │        ├─ context/
   │        ├─ ending/
   │        └─ settings/
   ├─ gradle/
   ├─ gradle.properties
   └─ README.md
```

---

# 19. 分阶段实施计划

# 阶段 0：仓库与规范初始化

## 目标

建立可持续开发的基础结构。

## 任务

1. 初始化 Git 仓库；
2. 创建上述目录；
3. 写根 README；
4. 创建 `.gitignore`；
5. 创建 `docs/DECISIONS.md`；
6. 记录技术选择和不做事项；
7. 建立基础提交规范；
8. 禁止一次性生成全部代码后不测试。

## 验收

- 根目录清晰；
- Android 和 backend 可独立启动；
- README 说明项目目标；
- 首次提交完成。

---

# 阶段 1：后端骨架与数据库

## 目标

后端可启动，数据库可保存玩具。

## 任务

1. 初始化 FastAPI；
2. 添加配置加载；
3. 创建 SQLite；
4. 创建四张表；
5. 实现 health；
6. 实现 scan；
7. 实现 toys 创建和查询；
8. 编写测试。

## 验收脚本

```text
启动后端
↓
GET /health 返回 ok
↓
扫描陌生 tag_uid 返回 new
↓
绑定“大狮子”
↓
再次扫描返回 known
↓
重启后端
↓
再次扫描仍返回 known
```

## Git 提交建议

```text
feat(backend): initialize FastAPI and database
feat(backend): add toy binding and scan APIs
test(backend): cover health scan and toy binding
```

---

# 阶段 2：Mock 游戏火花闭环

## 目标

不接真实 AI，先跑通完整业务。

## 任务

1. 实现 MockAiProvider；
2. 实现固定内容库；
3. 实现 start；
4. 实现 next；
5. 实现 end；
6. 实现 memories；
7. 增加会话状态检查；
8. 增加自动化测试。

## 验收

可通过 API 完成：

```text
扫描
绑定
开始游戏
获得一句火花
输入家长上下文
获得下一句火花
结束游戏
保存记忆
第二次开始时读取记忆
```

---

# 阶段 3：真实 AI Provider

## 目标

接入一个 OpenAI-compatible 文本 API。

## 任务

1. 实现 provider adapter；
2. 从环境变量读取配置；
3. 实现 Prompt Builder；
4. 要求 JSON 输出；
5. 实现字段校验；
6. 实现超时；
7. 实现重试上限；
8. 实现固定内容降级；
9. 补充测试；
10. README 写明配置。

## 环境变量示例

```text
AI_MODE=mock
AI_BASE_URL=
AI_API_KEY=
AI_MODEL=
AI_TIMEOUT_SECONDS=10
DATABASE_URL=sqlite:///./toywake.db
```

## 验收

- Mock 模式无网络可运行；
- AI 模式返回结构化结果；
- 故意填错 Key 时自动降级；
- AI 返回长文时被拒绝并降级；
- 日志不出现完整 Key。

---

# 阶段 4：Android 基础与后端连接

## 目标

Android App 可启动、保存 Base URL、连接 health。

## 任务

1. 创建 Kotlin Compose 项目；
2. 配置导航；
3. 建立网络层；
4. 实现 DataStore；
5. 实现设置页；
6. 实现连接测试；
7. 处理 HTTP 错误；
8. 添加基础 UI 测试或 ViewModel 测试。

## 验收

- App 启动；
- 输入局域网后端地址；
- 点击测试连接；
- 成功显示“服务可用”；
- 后端关闭时显示明确错误。

---

# 阶段 5：Android NFC 闭环

## 目标

真实手机读取 NFC UID，并调用 scan。

## 任务

1. Manifest 添加 NFC feature 和 permission；
2. 实现 NfcReaderManager；
3. 实现 Reader Mode 生命周期；
4. 实现 UID 格式化；
5. 实现去重；
6. 实现扫描页状态；
7. 调用 scan；
8. new 跳绑定页；
9. known 跳共玩页。

## 验收

使用至少 3 张真实 NFC 贴纸：

- 每张 UID 不同；
- 连续扫描识别稳定；
- 重复靠近不重复跳转；
- 未开启 NFC 时提示正确；
- 无 NFC 设备提示正确。

---

# 阶段 6：绑定、TTS 与共玩页面

## 目标

完成用户可见的核心 Demo。

## 任务

1. 实现绑定表单；
2. 提交 toys；
3. 实现 TTS Manager；
4. 实现 start；
5. 播放 wake phrase；
6. 播放 child_speech；
7. 显示 parent_hint；
8. 实现“再来一个点子”；
9. 实现家长上下文输入；
10. 实现“准备结束”；
11. 实现记忆确认页。

## 验收

完整运行：

```text
第一次扫描
↓
绑定大狮子
↓
再次扫描
↓
手机说“大狮子醒啦”
↓
手机说一条游戏火花
↓
家长输入孩子行为
↓
手机说下一条
↓
结束并保存记忆
```

---

# 阶段 7：异常处理与离线降级

## 目标

Demo 不因网络或 AI 问题中断。

## 任务

1. 后端不可达时本地固定模式；
2. AI 失败时后端固定降级；
3. TTS 初始化失败提示；
4. 页面返回和会话取消；
5. 防重复点击；
6. 请求 loading 状态；
7. 统一错误文案；
8. 日志清理。

## 验收

主动制造：

- 后端关闭；
- AI Key 错误；
- 网络断开；
- 重复扫标签；
- 重复点击下一条；
- TTS 播放中离开页面；
- 会话结束后再次请求。

所有情况均不得崩溃。

---

# 阶段 8：真实家庭 Demo 准备

## 目标

形成可以交给家长使用的测试包。

## 任务

1. 准备 3 个玩具；
2. 每个玩具贴 NFC；
3. 预先完成绑定；
4. 确认音量；
5. 准备固定内容模式；
6. 准备 AI 模式；
7. 写家庭测试观察表；
8. 打包 Debug APK；
9. 写启动后端脚本；
10. 录制一段完整演示视频。

## 最终交付

```text
Android Debug APK
后端完整源码
启动脚本
.env.example
测试数据
README
API 文档
家庭观察表
已知问题列表
演示视频
```

---

# 20. 自动化测试要求

## 20.1 后端必测

- health 正常；
- 新标签扫描；
- 已知标签扫描；
- 重复 UID；
- 创建玩具输入校验；
- 开始会话；
- 下一条火花；
- 已结束会话不能继续；
- 保存记忆；
- AI 超时降级；
- AI 非 JSON 降级；
- AI 长文本降级；
- 固定内容不重复基本逻辑。

## 20.2 Android 必测

至少为以下纯逻辑写单元测试：

- Tag UID 格式化；
- NFC 去重逻辑；
- API DTO 映射；
- ViewModel 状态流转；
- Base URL 校验；
- TTS 队列状态；
- 结束会话后清理状态。

NFC 硬件本身必须真实设备手测。

---

# 21. 手工验收用例

## 用例 A：新玩具

1. 使用未绑定标签；
2. 扫描；
3. 进入绑定；
4. 名字留空，确认无法提交；
5. 填写大狮子；
6. 保存；
7. 开始游戏。

预期：数据保存，语音正常。

## 用例 B：已知玩具

1. 返回扫描页；
2. 再扫大狮子；
3. 自动识别；
4. 播放唤醒语；
5. 播放游戏火花。

预期：无需再次绑定。

## 用例 C：AI 失败

1. 将 AI Key 改错；
2. 扫描大狮子；
3. 开始游戏。

预期：固定内容正常播放，App 不报技术错误。

## 用例 D：保存记忆

1. 输入“孩子让大狮子坐消防车”；
2. 结束游戏；
3. 保存记忆；
4. 第二天重新开始。

预期：后端存在该记忆，AI 可引用最多一条。

## 用例 E：重复 NFC

1. 手机持续贴着标签；
2. 观察 5 秒。

预期：只创建一次扫描处理和一个会话。

---

# 22. 家庭测试观察表

每次测试记录：

```text
日期：
孩子年龄：
玩具：
模式：父母固定台词 / 固定内容 / AI+记忆
开始时间：
结束时间：

1. 语音后5秒内是否拿起玩具：
2. 是否看向手机多于玩具：
3. 是否执行动作：
4. 手机收起后继续玩多久：
5. 是否加入其他玩具：
6. 是否纠正系统：
7. 是否主动要求下一条：
8. 是否反复扫描但不玩：
9. 家长操作是否麻烦：
10. 第二天是否记得：
11. 原始观察记录：
```

只记录事实，不急于解释儿童行为。

---

# 23. 本地 AI 执行规则

执行本计划的本地 AI 必须遵守：

1. 先检查现有仓库，不覆盖已有有效内容；
2. 每阶段开始前阅读本计划；
3. 每阶段只完成本阶段范围；
4. 不自行添加语音识别、账号、动画等额外功能；
5. 不把 API Key 写入客户端；
6. 不以“代码已生成”作为完成；
7. 必须实际运行测试；
8. 测试失败时先修复，不带病进入下一阶段；
9. 每阶段更新 README 和进度记录；
10. 每阶段独立 Git 提交；
11. 不提交数据库、密钥、构建产物；
12. 所有不确定版本查官方文档并选择稳定版本；
13. 所有架构变更记录到 `docs/DECISIONS.md`；
14. 无法确定的硬件行为必须在真实 Android 设备上验证；
15. 最终报告必须区分“已实现”“已测试”“未验证”。

---

# 24. 完成定义

只有同时满足以下条件，Demo 才算完成：

- [ ] Android 真机可以读取 NFC；
- [ ] 3 张标签可绑定 3 个玩具；
- [ ] App 可以识别已绑定玩具；
- [ ] 后端数据库重启后数据不丢；
- [ ] App 可以播放中文 TTS；
- [ ] AI 可以生成一句结构化游戏火花；
- [ ] AI 失败时固定内容可用；
- [ ] 家长可以输入当前情况；
- [ ] 家长可以结束游戏并确认记忆；
- [ ] 重复 NFC 不重复创建会话；
- [ ] API Key 不在 APK 中；
- [ ] 后端测试全部通过；
- [ ] Android 核心逻辑测试通过；
- [ ] 有完整 README；
- [ ] 有家庭测试观察表；
- [ ] 已在真实家庭场景完成至少一次完整演示。

---

# 25. 最终产品判断

本 Demo 不是为了证明：

> NFC 能读取，AI 能回复。

这些技术本身已经不是核心风险。

真正要验证的是：

> 当手机只说一句话并安静下来后，孩子是否会拿起现实玩具，父母是否更容易加入，而游戏是否能够继续。

因此，实施过程中必须始终使用以下判断：

> **当设备安静下来，而孩子和父母仍在继续玩，ToyWake Demo 才算成功。**
