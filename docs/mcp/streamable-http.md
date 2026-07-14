# MCP Streamable HTTP 接入说明

## 端点信息

- MCP 端点：`/api/mcp`
- 传输协议：`streamable-http`
- 认证方式：`Authorization: Bearer <token-or-api-key>`
- 浏览器登录 Token 保持现有接口访问能力。
- API Key 仅允许访问 `/api/mcp`，支持 Streamable HTTP 会话使用的 `POST`、`GET`、`DELETE`。
- `/api/mcp/tools/**` 管理接口和其他普通 REST 接口仅允许浏览器登录 Token；有效 API Key 访问时返回 HTTP 403。
- 无效、已删除或已过期 API Key 返回 HTTP 401。

## 当前已接入工具

- `thought_query`
  - 说明：查询当前用户闪念记录，返回适合 Agent 阅读的摘要列表
  - 入参主体：
    - `keyword`
    - `subject`
    - `content`
    - `thoughtType`
    - `themeKey`
    - `status`
    - `startDate`
    - `endDate`
    - `hasEvents`
    - `page`
    - `pageSize`
    - `sortBy`
    - `sortOrder`
- `thought_save`
  - 说明：保存一条闪念，支持想法行动、情绪心情、复盘沉淀三类记录，并可附带结构化详情和多个关联事件
  - 入参主体：
    - `subject`
    - `content`
    - `thoughtType`
    - `themeKey`
    - `status`
    - `changeReason`
    - `createTime`
    - `updateTime`
    - `recordTime`
    - `happenedAt`
    - `actionDetail`
    - `emotionDetail`
    - `reflectionDetail`
    - `events[].content`
    - `events[].eventTime`
    - `idempotencyKey`

## 调试示例

### 初始化

```bash
curl -X POST 'http://localhost:45678/api/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer <token>' \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2025-03-26",
      "capabilities": {},
      "clientInfo": {
        "name": "curl-client",
        "version": "1.0.0"
      }
    }
  }'
```

### 查询工具列表

```bash
curl -X POST 'http://localhost:45678/api/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer <token>' \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

### 调用想法保存工具

```bash
curl -X POST 'http://localhost:45678/api/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer <token>' \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "thought_save",
      "arguments": {
        "subject": "今天完成了 MCP 接入",
        "content": "今天完成了 MCP 接入",
        "thoughtType": "action",
        "themeKey": "indigo",
        "status": "done",
        "recordTime": "2026-06-13 10:30:00",
        "changeReason": "通过 MCP 工具记录完成事项",
        "actionDetail": {
          "resultSummary": "完成 streamable-http MCP server 接入",
          "reflection": "MCP 工具需要保持字段契约稳定",
          "nextAction": "继续完善 Agent 提示词"
        },
        "events": [
          {
            "content": "新增 streamable-http MCP server",
            "eventTime": "2026-06-13 10:00:00"
          }
        ]
      }
    }
  }'
```

### 调用情绪闪念保存工具

```bash
curl -X POST 'http://localhost:45678/api/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer <token>' \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "tools/call",
    "params": {
      "name": "thought_save",
      "arguments": {
        "subject": "部署失败后的焦虑",
        "content": "看到部署失败后有点焦虑，担心影响后续进度。",
        "thoughtType": "emotion",
        "themeKey": "teal",
        "status": "pending",
        "emotionDetail": {
          "emotionType": "anxious",
          "emotionIntensity": 4,
          "emotionTrigger": "部署失败",
          "emotionNeed": "需要确认问题边界和修复路径"
        }
      }
    }
  }'
```

### 按闪念类型查询

```bash
curl -X POST 'http://localhost:45678/api/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer <token>' \
  -d '{
    "jsonrpc": "2.0",
    "id": 5,
    "method": "tools/call",
    "params": {
      "name": "thought_query",
      "arguments": {
        "thoughtType": "emotion",
        "page": 1,
        "pageSize": 10
      }
    }
  }'
```

## 后续新增接口步骤

1. 在目标 Controller 方法上增加 `@Tool`
2. 在同一方法上增加 `@McpOperation`
3. 配置 `name`、`description` 与 `ignoreInputFields`
4. 保持方法签名为单个请求体对象，返回 `ApiResponse<T>`
5. 编译后通过 `tools/list` 检查新工具是否自动注册
