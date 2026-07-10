# API 文档

统一前缀：`/api/v1`

---

## 1. 健康检查

```http
GET /api/v1/health
```

**响应**

```json
{
  "status": "ok",
  "database": "ok",
  "ai_mode": "mock"
}
```

---

## 2. 扫描标签

```http
POST /api/v1/scan
```

**请求**

```json
{ "tag_uid": "04A1B2C3D4E5F6" }
```

**响应 - 新标签**

```json
{ "status": "new", "tag_uid": "04A1B2C3D4E5F6" }
```

**响应 - 已绑定**

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

---

## 3. 绑定玩具

```http
POST /api/v1/toys
```

**请求**

```json
{
  "tag_uid": "04A1B2C3D4E5F6",
  "name": "大狮子",
  "toy_type": "毛绒动物",
  "trait": "勇敢",
  "child_setting": "住在沙发下面"
}
```

**响应**

```json
{ "status": "ok", "toy": { "id": 1, "name": "大狮子" } }
```

**约束**：`tag_uid` 唯一，重复返回 409；输入长度校验；名字不可空。

---

## 4. 查询/编辑玩具

```http
GET    /api/v1/toys/{toy_id}
PUT    /api/v1/toys/{toy_id}
GET    /api/v1/toys/{toy_id}/memories
```

---

## 5. 开始游戏

```http
POST /api/v1/play/start
```

**请求**

```json
{ "toy_id": 1 }
```

**响应**

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

---

## 6. 下一条游戏火花

```http
POST /api/v1/play/next
```

**请求**

```json
{ "session_id": "uuid", "parent_context": "孩子说要去救小兔。" }
```

**响应**

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

**约束**：会话须为 ACTIVE；每会话建议最多 5 次火花；`parent_context` 可空，最多 100 字。

---

## 7. 结束游戏

```http
POST /api/v1/play/end
```

**请求**

```json
{ "session_id": "uuid", "parent_context": "孩子让大狮子坐消防车去找小兔。" }
```

**响应**

```json
{
  "ending_speech": "大狮子累了，我们送它回家吧。",
  "memory_candidate": "大狮子坐消防车去找小兔。"
}
```

---

## 8. 记忆管理

```http
POST   /api/v1/memories
DELETE /api/v1/memories/{memory_id}
```

**创建记忆请求**

```json
{ "toy_id": 1, "content": "大狮子坐消防车去找小兔。", "memory_type": "event" }
```

`memory_type`：`event` / `setting` / `relationship` / `preference`

---

## 统一错误格式

```json
{
  "error": {
    "code": "TOY_NOT_FOUND",
    "message": "未找到这个玩具。",
    "retryable": false
  }
}
```

| HTTP | code | 说明 |
|---|---|---|
| 404 | TOY_NOT_FOUND | 玩具不存在 |
| 409 | TAG_ALREADY_BOUND | 重复 UID 绑定 |
| 409 | SESSION_ENDED | 会话已结束 |
| 422 | VALIDATION_ERROR | 输入过长或非法 |
| 500 | INTERNAL_ERROR | 数据库异常 |
