# MCP 工具重构、调用链标准化与闪念导入更新（2026-05-31）

## 1. 背景与目标

本次更新围绕后端 MCP 工具体系做了一轮分阶段重构，并通过 MCP 工具完成了一批闪念写入验证。

目标是让 MCP 工具从“借用 Controller 注册”逐步演进为独立的工具基础设施：

- 工具命名和边界更清晰。
- 工具定义、Schema 生成、调用执行职责分离。
- 工具类不再依赖 `@RestController` 和 LangChain4j `@Tool`。
- 调用链具备统一的登录校验、参数转换、返回解包和异常处理。
- 补齐可回归的 MCP 单元测试，减少后续新增工具时的手测成本。

## 2. MCP 后端重构

### 2.1 命名与边界整理

- `ControllerMcpToolRegistry` 重命名为 `McpToolRegistry`。
- `ControllerMcpToolInvoker` 重命名为 `McpToolInvoker`。
- `McpToolHandlers` 改为注入新的注册器和调用器。
- 工具名、入参、返回结构保持兼容。

### 2.2 统一工具描述模型

- 新增 `top.aiolife.mcp.definition.McpToolDefinition`。
- 注册器扫描工具方法后，先构造内部统一定义，再供 Handler 和 Invoker 使用。
- 工具定义包含：
  - 工具名
  - 工具描述
  - Bean
  - Method
  - 输入类型
  - 输出类型
  - MCP Schema
  - 是否需要登录
  - 是否解包 `ApiResponse.data`

### 2.3 Schema 生成独立化

- 新增 `McpSchemaGenerator`。
- 新增 `McpFieldSchemaResolver`。
- `LangChain4jToolSchemaAdapter` 保留为兼容层，内部委托新的 Schema 生成器。
- Schema 解析支持基础类型、集合、数组、枚举、嵌套 DTO、父类字段和 `@McpField.description`。

### 2.4 工具注册与 Controller 解耦

- 新增 MCP 工具组件包：`top.aiolife.mcp.tools`。
- 新增 `ThoughtMcpTools`，承接 `thought_save`。
- 新增 `TimeRecordMcpTools`，承接：
  - `time_record_queryByDateRange`
  - `time_record_save`
- 删除旧的 `ThoughtToolController` 和 `TimeRecordToolController`。
- 注册器改为扫描 `top.aiolife.mcp.tools` 下 Spring Bean 的 `@McpOperation` 方法。
- 不再依赖 `@RestController` 和 LangChain4j `@Tool` 注册 MCP 工具。

### 2.5 调用链标准化

- `McpToolInvoker` 增加统一调用管道：
  - 未登录时直接返回 MCP error：`未登录或登录已过期`。
  - 使用 `ObjectMapper.convertValue` 统一完成 `Map<String, Object>` 到 MCP DTO 的转换。
  - 参数转换失败返回 MCP error。
  - 保留 `McpSaTokenScope` 注入登录上下文。
  - `ApiResponse` 默认解包 `data`。
  - 非成功 `ApiResponse` 转 MCP error，错误文案优先使用 `result`。
  - 字符串结果直接返回。
  - 普通对象继续转 JSON 返回。
  - 未知异常写日志，客户端只收到安全错误信息。
- `McpOperation.unwrapApiResponseData()` 默认值调整为 `true`。

## 3. MCP 测试补齐

新增 MCP 回归测试：

- `McpToolRegistryTest`
  - 验证工具注册数量。
  - 验证工具名不重复。
  - 验证 `thought_save` 存在。
- `McpSchemaGeneratorTest`
  - 验证 `ThoughtSaveToolReq` Schema。
  - 验证 `events` 嵌套结构。
  - 验证字段描述来源于 `@McpField`。
  - 验证 `TimeRecordSaveToolReq.exercises` 嵌套结构。
- `McpToolInvokerTest`
  - 验证 `ApiResponse<Boolean>` 解包。
  - 验证字符串直接返回。
  - 验证失败 `ApiResponse` 转 MCP error。
  - 验证参数错误、未登录、业务异常和未知异常处理。
- `McpToolCompatibilityTest`
  - 锁定 `thought_save`、`time_record_save`、`time_record_queryByDateRange`。
  - 锁定关键入参字段，防止后续重构破坏客户端兼容。

## 4. 通过 MCP 写入闪念

使用本地 MCP endpoint `/api/mcp` 和 API Key 调用 `thought_save`，将截图中的闪念内容写入闪念模块。

本次写入时按语义做了去重和分类：

| 闪念主题 | 分类 | themeKey | 状态 |
|---|---|---|---|
| 代办类型和失败原因字段设计 | AIO-LIFE开发 | `indigo` | `pending` |
| 代办功能开发完整并提交 GitHub | AIO-LIFE开发 | `indigo` | `pending` |
| 研究 Codex 版本回溯 | AIO-LIFE开发 | `indigo` | `pending` |
| 收拾房间 | 生活 | `green` | `pending` |
| 整理购买清单 | 生活 | `green` | `pending` |

说明：

- 截图中重复的“代办类型和失败原因字段设计”只保留完整版本。
- 截图中重复的“代办功能开发完整并提交 GitHub”只写入一条。
- 通过 `/thought/query` 查询确认 5 条记录已落库。

## 5. 验证情况

后端执行：

```bash
mvn -q "-Dtest=McpToolRegistryTest,McpSchemaGeneratorTest,McpToolInvokerTest,McpToolCompatibilityTest" test
mvn -q -DskipTests compile
```

结果：

- MCP 指定单元测试通过。
- 后端编译通过。
- `checkedError` 测试会输出一条预期内的 error 日志，用于验证未知异常会被服务端记录，同时客户端只收到安全错误信息。

## 6. 注意事项

- 当前本地运行中的后端服务如果未重启，仍会使用旧版 MCP 调用链行为；要验证 `ApiResponse.data` 默认解包，需要重启后端。
- 本阶段没有引入 Bean Validation，也没有批量为 MCP DTO 增加 `@NotNull` / `@NotBlank`。
- 后续新增 MCP 工具时，建议优先放到 `top.aiolife.mcp.tools`，方法使用 `@McpOperation` 暴露。
