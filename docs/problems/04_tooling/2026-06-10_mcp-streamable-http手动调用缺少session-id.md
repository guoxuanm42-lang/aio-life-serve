# MCP Streamable HTTP 手动调用缺少 session-id

## 1. 现象

AIO-LIFE MCP 配置在 Trae Solo 中可以正常使用，但直接用 `curl`、Postman 或其它 HTTP 工具手动调用时，可能出现连接失败、工具列表获取失败、工具调用失败等问题。

常见场景：

- MCP 地址、`Authorization`、`Accept` 配置都正确，但手动调用仍失败。
- `initialize` 可以返回结果，后续 `tools/list` 或 `tools/call` 失败。
- Trae Solo 页面显示 MCP 可用，但自己用命令行复现时不通。

## 2. 根因

`streamable-http` MCP 协议需要 session 管理。

客户端必须先调用 `initialize`，服务端会在响应头中返回 `mcp-session-id`。后续同一个 MCP 会话的 `tools/list`、`tools/call` 等请求，需要继续带上这个 `mcp-session-id` header。

Trae Solo 作为 MCP 客户端会自动处理这件事，所以普通 Trae MCP JSON 配置里不需要手动写 `mcp-session-id`。但使用 `curl`、Postman 等工具手动调试时，需要自己保存并传递这个 header。

## 3. 正确配置

Trae Solo MCP 配置只需要写端点和鉴权 header，例如云端：

```json
{
  "mcpServers": {
    "aio-life": {
      "type": "streamable-http",
      "url": "http://150.158.105.184:8080/api/mcp",
      "headers": {
        "Authorization": "Bearer <api-key>",
        "Accept": "application/json, text/event-stream"
      }
    }
  }
}
```

本地调试端点示例：

```text
http://172.20.10.2:5666/api/mcp
```

云端调试端点示例：

```text
http://150.158.105.184:8080/api/mcp
```

注意：真实 API Key 不要写入文档或提交到仓库。

## 4. curl 调试流程

第一步，调用 `initialize`：

```bash
curl -i -X POST 'http://150.158.105.184:8080/api/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer <api-key>' \
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

从响应头里找到并保存：

```text
mcp-session-id: <session-id>
```

第二步，后续请求带上 `mcp-session-id`：

```bash
curl -X POST 'http://150.158.105.184:8080/api/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer <api-key>' \
  -H 'mcp-session-id: <session-id>' \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

调用工具时同样需要带 `mcp-session-id`：

```bash
curl -X POST 'http://150.158.105.184:8080/api/mcp' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'Authorization: Bearer <api-key>' \
  -H 'mcp-session-id: <session-id>' \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "thought_save",
      "arguments": {
        "content": "测试 MCP 手动调用 session 流程"
      }
    }
  }'
```

## 5. 排查结论

如果 Trae Solo 可以正常连接，但手动 HTTP 调用失败，优先检查：

1. `Authorization` 是否为 `Bearer <api-key>`。
2. `Accept` 是否包含 `application/json, text/event-stream`。
3. 是否先调用了 `initialize`。
4. 后续请求是否带上了 `mcp-session-id`。
5. URL 是否包含后端上下文路径 `/api/mcp`，不要误写成 `/mcp` 或 `/mcp/tools`。

`/mcp/tools` 是后台管理和模拟调用接口，不是 MCP 协议端点。MCP 客户端应连接 `/api/mcp`。
