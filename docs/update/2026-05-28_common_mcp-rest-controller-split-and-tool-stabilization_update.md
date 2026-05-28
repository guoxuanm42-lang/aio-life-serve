# 2026-05-28_common_mcp-rest-controller-split-and-tool-stabilization_update

## 1. 背景与目标

为降低 MCP 与业务 REST 的启动/演进耦合，提升 Agent 调用稳定性，本次将 MCP 工具从业务 REST Controller 中剥离，并对 MCP 工具契约（schema）做稳定化治理，避免前端接口变更影响 MCP。

## 2. 变更内容

### 2.1 REST 与 MCP 入口层分离

- 新增 MCP Tool Controller（仅放 `@Tool + @McpOperation`，不新增 REST 路由）：
  - `src/main/java/top/aiolife/mcp/api/ThoughtToolController.java`
  - `src/main/java/top/aiolife/mcp/api/TimeRecordToolController.java`
- 业务 REST Controller 移除 `@Tool/@McpOperation`，保持原 REST 路由不变：
  - `src/main/java/top/aiolife/record/api/ThoughtController.java`
  - `src/main/java/top/aiolife/record/api/TimeRecordController.java`

### 2.2 缩小 MCP 扫描面

- MCP 工具注册逻辑由“扫描全部 `@RestController`”改为“仅扫描 `top.aiolife.mcp.api` 包下的 Tool Controller”，降低启动时全量实例化业务 Controller 的风险：
  - `src/main/java/top/aiolife/mcp/registry/ControllerMcpToolRegistry.java`

### 2.3 MCP Tool 入参稳定化（专用 DTO + schema 描述）

- 新增 MCP Tool 专用入参 DTO（字段补齐 `@McpField` 描述，并保持字段名兼容）：
  - `src/main/java/top/aiolife/mcp/pojo/req/*`
- 当前工具仍为 3 个（工具名保持不变）：
  - `thought_save`
  - `time_record_queryByDateRange`
  - `time_record_save`

### 2.4 移除 Tool 对 REST Controller 的依赖（下沉 Service/Facade）

- 闪念保存下沉至 Service：
  - `src/main/java/top/aiolife/record/service/IThoughtService.java`
  - `src/main/java/top/aiolife/record/service/impl/ThoughtServiceImpl.java`
- 时迹 AI 查询/保存收敛至 Facade：
  - `src/main/java/top/aiolife/record/service/TimeRecordAiFacade.java`
- ToolController 直接调用 Service/Facade，不再委托调用 REST Controller。

### 2.5 写工具幂等（可选）

- `thought_save` 与 `time_record_save` 新增可选字段 `idempotencyKey`（建议 UUID），用于 Agent 重试时避免重复写入。
- Redis best-effort 幂等：
  - `src/main/java/top/aiolife/record/util/RedisUtil.java` 新增 `setIfAbsent`
  - key 规则：
    - `mcp:idemp:thought_save:{userId}:{idempotencyKey}`
    - `mcp:idemp:time_record_save:{userId}:{idempotencyKey}`
  - TTL：24 小时
  - 若业务写入异常会删除幂等占位 key，避免失败占位。

## 3. 兼容性说明

- REST 路由不变：前端使用的 `/thought/save`、`/timeRecord/queryByDateRangeForAI`、`/timeRecord/save` 不受影响。
- MCP 工具名不变：调用端仍使用 `thought_save / time_record_queryByDateRange / time_record_save`。
- MCP 入参字段名保持兼容：新增了 `idempotencyKey` 等字段，但旧字段仍可用。

## 4. 调试与验证

### 4.1 本地启动

- `mvn -DskipTests clean test`
- `mvn -DskipTests spring-boot:run`

### 4.2 MCP 调用要点

- `Accept` 必须包含：`application/json, text/event-stream`
- 需要先 `initialize`，从响应头拿到 `Mcp-Session-Id`，后续请求带上该 header

## 5. 验证结论

- 应用启动成功，MCP 工具仅注册 3 个且来源于 `top.aiolife.mcp.api` 下的 Tool Controller。
- `tools/list` 与 `tools/call` 回归通过，幂等键重复调用可返回成功且避免重复写入。

