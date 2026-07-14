# API Key 收缩为 MCP 协议专用凭证

## 背景

API Key 认证成功后会临时切换为凭证所属用户。此前 API Key 与浏览器登录 Token 共用业务身份，外部脚本可以携带 API Key 访问普通 REST 接口及 MCP 管理接口，权限范围超过 MCP / AI Agent 接入所需边界。

本次第一阶段将 API Key 收缩为 MCP 协议专用凭证，不调整生成、过期、删除、日志脱敏和数据库存储方式。

## 访问规则

| 凭证 | 请求范围 | 结果 |
| --- | --- | --- |
| 有效 API Key | `POST /api/mcp` | 允许 |
| 有效 API Key | `GET /api/mcp` | 允许 |
| 有效 API Key | `DELETE /api/mcp` | 允许 |
| 有效 API Key | 普通 REST 接口 | HTTP 403 |
| 有效 API Key | `/api/mcp/tools/**` | HTTP 403 |
| 有效 API Key | `/api/mcp/`、`/api/mcp/**` 或其他相似路径 | HTTP 403 |
| 无效、已删除或已过期 API Key | 任意受认证接口 | HTTP 401 |
| 浏览器登录 Token | 原有可访问接口 | 保持不变 |

越权响应固定为：

```text
API Key 仅允许访问 MCP 协议端点
```

响应和日志均不会输出完整 API Key。

## 本次调整

### 认证上下文

- 新增 `ApiKeyAuthConstants`，统一请求上下文中的 `API_KEY_ID`、`IS_API_KEY_AUTH`、`AUTH_TYPE` 等键和值。
- API Key 认证成功后新增 `AUTH_TYPE=API_KEY` 标记。
- 保留 `StpUtil.switchTo(userId)` 临时用户身份切换，确保 MCP 工具继续按 API Key 所属用户读写数据。

### 访问范围拦截

- 新增 `ApiKeyScopeInterceptor`。
- 拦截器顺序为：`ApiKeyInterceptor` → `ApiKeyScopeInterceptor` → `SaInterceptor` → `UserLastActiveInterceptor`。
- 仅对 `IS_API_KEY_AUTH=true` 的请求执行范围校验，普通 Token 和匿名请求交由原认证链处理。
- 使用 Servlet 内部路径精确匹配 `/mcp`，不使用 `/mcp/**` 前缀放行。
- 精确端点仅允许 `POST`、`GET`、`DELETE`。
- 越权请求在进入 Controller 前抛出独立访问范围异常。

### 状态码与清理

- API Key 越权统一返回 HTTP 403，与无效或过期 Key 的 HTTP 401 区分。
- `ApiKeyInterceptor.afterCompletion()` 继续记录实际 HTTP 状态码，并执行 `StpUtil.endSwitch()`，避免临时身份残留。
- MCP 管理接口 `/api/mcp/tools/**` 明确只接受浏览器登录 Token。

## 兼容性说明

- MCP 客户端通过 `/api/mcp` 建立、读取和删除 Streamable HTTP 会话的方式不变。
- 浏览器登录 Token 的访问能力不变。
- 原先使用 API Key 调用普通 REST 接口或 MCP 管理接口的脚本会收到 HTTP 403，需要迁移为 MCP 工具调用或改用浏览器登录 Token。
- 本次不新增数据库字段，不提供只读/读写范围或工具白名单。

## 验证范围

新增和补充测试覆盖：

- `/mcp` 的 `POST`、`GET`、`DELETE` 对 API Key 放行。
- 普通业务接口、API Key 管理接口和 `/mcp/tools/**` 对 API Key 拒绝。
- `/mcp/`、`/mcp/anything` 等相似路径不会被前缀放行。
- `/mcp` 的 `PUT`、`PATCH` 不会被放行。
- 普通 Token 和无 Authorization 请求跳过 API Key 范围校验。
- 403 响应不携带 API Key。
- 403 请求完成后记录调用日志并结束临时身份切换。
- 无效、已删除、已过期 Key 的 401 行为和有效 Key 的 MCP 身份切换保持不变。

执行命令：

```bash
mvn "-Dtest=ApiKeyMaskUtilTest,ApiKeyInterceptorTest,ApiKeyScopeInterceptorTest,ExceptionHandleTest" test
mvn test
```

验证结果：

- 定向测试：14 个测试全部通过。
- 后端全量测试：149 个测试全部通过。
- 编译与测试使用 JDK 21。
