# AIO-LIFE MCP 调用原则与限制

## 1. 文档目的

本文档说明 AIO-LIFE 项目中 MCP 的调用原则、运行链路、工具暴露规则和现有限制，供 Agent 配置、接口调试、工具扩展和问题排查使用。

当前 MCP 服务由后端项目内置实现，不需要单独启动新的 MCP Server，也不要在 Agent 或脚本侧重复实现业务工具逻辑。

## 2. 接入入口

### 2.1 协议端点

- 外部访问端点：`/api/mcp`
- Spring MCP 传输端点：`/mcp`
- 传输协议：`streamable-http`
- MCP Server 名称：`aio-life-serve-mcp`
- MCP Server 版本：`1.0.0`
- 当前暴露能力：`tools`

代码位置：

- `top.aiolife.mcp.config.McpServerConfig`
- `top.aiolife.mcp.handler.McpToolHandlers`

### 2.2 基础请求头

MCP 客户端必须按 Streamable HTTP 协议发起请求：

```http
Authorization: Bearer <token-or-api-key>
Accept: application/json, text/event-stream
Content-Type: application/json
```

如果使用 API Key，值仍放在 `Authorization` 头中：

```http
Authorization: Bearer ak-xxxx
```

### 2.3 Session 要求

客户端需要先调用 `initialize`。服务端会在响应头返回 `mcp-session-id`，后续 `tools/list`、`tools/call` 等请求都应携带该 header。

典型顺序：

1. `initialize`
2. 保存响应头中的 `mcp-session-id`
3. `tools/list`
4. `tools/call`

## 3. 鉴权与用户上下文

### 3.1 支持两类认证

MCP 复用项目现有接口鉴权能力：

- 普通登录 Token：由 Sa-Token 校验。
- API Key：由 `ApiKeyInterceptor` 校验。

API Key 认证规则：

- 只处理 `Authorization` 头中 `Bearer ` 后以 `ak-` 开头的值。
- API Key 不存在、已删除或已过期时拒绝请求。
- 校验成功后，通过 `StpUtil.switchTo(apiKeyEntity.getUserId())` 临时切换为 API Key 所属用户。
- 请求结束后执行 `StpUtil.endSwitch()`，避免污染后续请求上下文。
- API Key 调用会写入 API Key 调用日志。

### 3.2 MCP 工具内部的用户来源

MCP Server 在 `contextExtractor` 中把当前登录用户写入 MCP transport context：

```text
loginId
```

工具调用时由 `McpToolInvoker` 通过 `McpSaTokenScope.runWithContext` 恢复请求上下文和 Sa-Token 登录上下文。因此，业务工具内部仍然可以使用：

```java
StpUtil.getLoginIdAsLong()
```

### 3.3 未登录限制

当前注册的 MCP 工具默认都要求登录。没有登录态或 API Key 时，工具调用会返回 MCP error result：

```text
未登录或登录已过期
```

## 4. 工具注册原则

### 4.1 注册扫描范围

MCP 工具只从以下包前缀扫描：

```text
top.aiolife.mcp.tools
```

工具类必须是 Spring Bean，例如使用 `@Component`。

### 4.2 暴露注解

工具方法通过 `@McpOperation` 暴露，不再依赖 Controller 方法，也不要求使用 `@Tool`。

示例：

```java
@McpOperation(
        name = "thought_save",
        description = "保存一条想法，并可附带多个关联事件"
)
public ApiResponse<Boolean> thoughtSave(ThoughtSaveToolReq req) {
    ...
}
```

### 4.3 方法签名限制

当前注册器只支持单入参方法：

```text
method.getParameterCount() == 1
```

不符合该规则时，项目启动阶段会抛出异常。建议每个 MCP 工具定义独立的 ToolReq DTO，避免复用复杂 Controller 请求体。

### 4.4 工具命名限制

- `@McpOperation.name` 不能为空。
- 工具名全局唯一，重复会导致启动失败。
- 建议使用模块前缀，例如：
- `food_record_save`
- `food_record_query`
- `thought_query`
- `thought_save`
- `time_record_save`
- `time_record_queryByDateRange`

### 4.5 Schema 生成规则

工具输入 Schema 由方法入参 DTO 自动生成。字段说明优先来自 DTO 字段上的 `@McpField`。

`@McpOperation` 支持：

- `name`：工具名。
- `description`：工具说明。
- `ignoreInputFields`：从输入 schema 中排除指定字段路径。
- `unwrapApiResponseData`：是否把 `ApiResponse.data` 解包为 MCP 返回结果，默认 `true`。

## 5. 当前业务工具

当前项目已实现以下 MCP 业务工具：

| 工具名 | 类型 | 说明 |
| --- | --- | --- |
| `food_record_save` | 写入 | 保存美食记录文字内容，支持创建或更新，创建时支持幂等键，第一版不处理图片 |
| `food_record_query` | 查询 | 查询当前用户美食记录历史，返回适合 Agent 阅读的文字摘要，不包含图片 |
| `thought_query` | 查询 | 查询当前用户闪念记录，支持关键词、类型、主题、正文、分类、状态、创建日期范围和事件筛选，返回适合 Agent 阅读的摘要列表 |
| `thought_save` | 写入 | 保存闪念，支持想法行动、情绪心情、复盘沉淀三类记录，可附带结构化详情和多个关联事件 |
| `time_record_save` | 写入 | 保存时间记录，可附带练习记录 |
| `time_record_queryByDateRange` | 查询 | 查询指定日期范围内的时间记录 |

## 6. 调用链路

MCP 协议调用和研发管理页面模拟调用都会进入同一条受控链路：

```text
MCP Client / 管理页模拟调用
  -> McpToolHandlers / McpToolController
  -> McpToolCallServiceImpl
  -> McpToolInvoker
  -> top.aiolife.mcp.tools.* 业务工具
  -> 业务 Service / Facade
```

统一调用服务负责：

- 工具存在性校验。
- 工具启停校验。
- 限流。
- 参数转换。
- 用户上下文恢复。
- `ApiResponse` 解包。
- MCP 错误包装。
- 调用审计记录。

因此，不要绕过 `McpToolCallServiceImpl` 直接反射调用工具方法，也不要在管理页模拟调用中另写一套执行逻辑。

## 7. 工具启停与管理限制

### 7.1 默认启用

如果数据库中没有某个工具的运营配置，该工具默认启用。

判断规则：

```text
config == null || config.enabled != false
```

### 7.2 停用效果

工具被停用后：

- 不出现在 MCP 协议 `tools/list` 结果中。
- 不能通过 MCP 协议调用。
- 不能通过管理页模拟调用。
- 调用会返回错误：`MCP 工具已停用: <toolName>`。

### 7.3 管理接口权限

管理接口路径：

```text
/api/mcp/tools
```

接口说明：

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/mcp/tools` | 登录用户 | 查询运行时工具与运营配置合并结果 |
| `POST` | `/mcp/tools/{name}/call` | 登录用户 | 模拟调用指定 MCP 工具 |
| `PUT` | `/mcp/tools/{name}/config` | `admin` | 保存展示名、分组、描述覆盖、排序、写操作标识、备注 |
| `PUT` | `/mcp/tools/{name}/status` | `admin` | 启用或停用工具 |
| `GET` | `/mcp/tools/{name}/logs` | `admin` | 查询工具最近调用日志 |

## 8. 限流规则

MCP 工具调用存在统一限流：

- 维度：`用户 + 工具名`
- 窗口：`60` 秒
- 上限：`30` 次
- Redis key 前缀：`mcp:tool:rate:`

超过限制时返回 MCP error result：

```text
工具调用过于频繁，请稍后再试
```

注意：

- `tools/call` 即使返回 HTTP 200，也可能在 MCP result 中标记 `isError=true`。
- 批量写入时必须检查 MCP 调用结果中的 `isError`，不能只看 HTTP 状态码。
- 限流检查依赖 Redis；如果 Redis 限流检查异常，当前实现会记录 warn 日志并放行本次调用。

## 9. 幂等规则

### 9.1 写入工具建议传 `idempotencyKey`

写入类工具应尽量传入稳定幂等键，避免 Agent 重试、网络抖动或用户重复提交造成重复数据。

当前支持幂等键的工具：

- `food_record_save`
- `thought_save`
- `time_record_save`

### 9.2 幂等隔离维度

幂等 Redis key 按用户隔离：

```text
mcp:idemp:<toolName>:<userId>:<idempotencyKey>
```

不同用户使用相同 `idempotencyKey` 不会互相影响。

### 9.3 幂等有效期

当前幂等有效期为：

```text
24 小时
```

### 9.4 各工具幂等行为差异

`food_record_save`：

- 创建成功后缓存记录 ID。
- 重复调用时返回已创建记录详情。
- 更新已有记录时不走创建幂等逻辑。

`thought_save`：

- 首次调用成功后缓存占位值。
- 重复调用时直接返回成功，不再创建新闪念。
- 如果写入异常，会删除幂等 key，允许后续重试。

`time_record_save`：

- 首次调用成功后保留占位值。
- 重复调用时直接返回，不再创建新时间记录。
- 如果写入异常，会删除幂等 key，允许后续重试。

## 10. 审计日志规则

每次 MCP 工具调用都会写入工具调用审计日志。

记录内容包括：

- 工具名。
- 用户 ID。
- 参数摘要。
- 是否成功。
- 错误信息。
- 调用耗时。
- 创建时间。

参数摘要限制：

- 最大长度：`2000` 字符。
- 错误信息最大长度：`1000` 字符。

敏感字段会被掩码为 `***`。字段名包含以下关键字时会被认为是敏感字段：

- `password`
- `passwd`
- `token`
- `key`
- `secret`
- `authorization`
- `apikey`
- `accesstoken`
- `refreshtoken`

## 11. 业务调用原则

### 11.1 显式写入原则

Agent 只有在用户明确要求保存、写入、记录时，才调用写入工具。

允许触发写入的典型表达：

- `写入闪念`
- `记一下`
- `保存这个想法`
- `写入美食记录`
- `帮我记录这顿饭`
- `保存时间记录`

不应写入的场景：

- 用户只是讨论想法。
- 用户只是询问建议。
- 用户只是整理草稿。
- 用户明确说“先别记录”“不要入库”“只是聊聊”。

### 11.2 查询优先原则

用户询问历史数据、之前做过什么、最近记录、按条件筛选时，优先使用查询工具，不要创建新记录。

示例：

- 问闪念、想法、灵感历史：使用 `thought_query`。
- 问美食历史：使用 `food_record_query`。
- 问某个日期范围的时间记录：使用 `time_record_queryByDateRange`。

### 11.3 字段确定性原则

写入工具应尽量传结构化字段。无法确定关键字段时，Agent 应先追问，不要编造。

示例：

- 美食记录无法确定菜名时，应先问菜名。
- 时间记录无法确定日期或时间段时，应先问清楚。
- 闪念内容为空或无法提炼主题时，不应调用 `thought_save`。

### 11.4 不处理图片原则

当前美食记录 MCP 第一版不处理图片。Agent 不应尝试把图片、文件或二进制内容写入 `food_record_save`。

## 12. 工具字段限制

### 12.1 `thought_query`

核心字段：

- `keyword`：关键词，同时模糊匹配主题、正文和关联事件内容。
- `subject`：主题关键词，只匹配闪念主题。
- `content`：正文关键词，只匹配闪念正文。
- `thoughtType`：闪念类型，支持 `action` / `emotion` / `reflection`；不传查询全部类型。
- `themeKey`：分类主题色。
- `status`：状态。
- `startDate` / `endDate`：按创建时间筛选，格式 `yyyy-MM-dd`。
- `hasEvents`：是否筛选带有关联事件的闪念；`true` 只查有事件，`false` 只查无事件。
- `page`：当前页码，默认 `1`。
- `pageSize`：每页数量，默认 `10`，最大 `50`。
- `sortBy`：排序字段，支持 `createTime` / `updateTime`，默认 `updateTime`。
- `sortOrder`：排序方向，支持 `desc` / `asc`，默认 `desc`。

输出限制：

- 返回 `page`、`pageSize`、`total`、`hasMore` 和 `items`。
- 单条 `summary` 从正文截断到 120 字。
- 单条 `content` 最多返回 1000 字。
- 每条闪念最多返回 5 条关联事件。
- 单条事件内容最多返回 300 字。
- 返回 `categoryName`、`thoughtTypeName` 和按类型转换后的 `statusName`，供 Agent 直接阅读。

闪念类型：

| 类型 | 含义 |
| --- | --- |
| `action` | 想法行动 |
| `emotion` | 情绪心情 |
| `reflection` | 复盘沉淀 |

### 12.2 `thought_save`

核心字段：

- `subject`：主题，可选；为空时服务端尝试从 `content` 第一行提炼，最长取 60 字符。
- `content`：内容。
- `thoughtType`：闪念类型，支持 `action` / `emotion` / `reflection`；不传或非法时默认 `action`。
- `themeKey`：主题色。
- `status`：状态。
- `changeReason`：状态变化原因，用于写入状态流转日志。
- `createTime`：闪念创建时间/实际发生时间，格式 `yyyy-MM-dd HH:mm:ss`。
- `updateTime`：闪念更新时间，格式 `yyyy-MM-dd HH:mm:ss`。
- `recordTime` / `happenedAt`：`createTime` 的别名。
- `actionDetail`：行动型结构化详情，`thoughtType=action` 时生效。
- `emotionDetail`：情绪型结构化详情，`thoughtType=emotion` 时生效。
- `reflectionDetail`：复盘沉淀型结构化详情，`thoughtType=reflection` 时生效。
- `events[].content`：关联事件内容。
- `events[].createTime`：关联事件发生时间，格式 `yyyy-MM-dd HH:mm:ss`。
- `events[].eventTime` / `events[].recordTime` / `events[].happenedAt`：`events[].createTime` 的别名。
- `idempotencyKey`：幂等键。

主题色限制：

```text
blue, cyan, green, purple, pink, orange, teal, indigo
```

状态限制：

```text
pending, ongoing, done, shelved, archived
```

中文状态会被归一化：

| 中文 | 入库状态 |
| --- | --- |
| 待处理 | `pending` |
| 进行中 | `ongoing` |
| 已完成 | `done` |
| 已搁置 | `shelved` |
| 已归档 | `archived` |

状态为空或非法时默认：

```text
pending
```

闪念类型为空或非法时默认：

```text
action
```

结构化详情字段：

`actionDetail`：

- `resultSummary`：处理结果。
- `reflection`：心得/复盘。
- `nextAction`：后续动作。
- `shelveReason`：搁置原因。
- `shelveReasonTag`：搁置原因标签，支持 `unrealistic` / `no_time` / `low_value` / `blocked` / `duplicate` / `other`。
- `restartPolicy`：是否可重启，支持 `no` / `later` / `conditional`。
- `archiveReason`：归档原因。
- `valueLevel`：价值等级，支持 `normal` / `valuable` / `high`。
- `archiveType`：沉淀类型，支持 `experience` / `lesson` / `method` / `inspiration` / `decision`。

`emotionDetail`：

- `emotionType`：情绪类型，支持 `sad` / `angry` / `anxious` / `stress` / `happy` / `excited` / `moved` / `inspired`。
- `emotionIntensity`：情绪强度，建议 1-5。
- `emotionTrigger`：触发原因。
- `emotionNeed`：背后需求。
- `copingAction`：缓解动作。
- `reflectionSummary`：复盘结论。
- `ignoredReason`：不再关注原因。

`reflectionDetail`：

- `reflectionSummary`：复盘结论。
- `lessonType`：经验/教训/方法/决策，支持 `experience` / `lesson` / `method` / `decision`。
- `archiveType`：沉淀类型，支持 `experience` / `lesson` / `method` / `inspiration` / `decision`。
- `valueLevel`：价值等级，支持 `normal` / `valuable` / `high`。
- `improvementAction`：改进动作。
- `relatedProject`：关联项目。
- `tags`：标签，多个标签可用逗号分隔。

Agent 调用建议：

- 普通想法、行动、待办类记录可以不传 `thoughtType`，默认写为 `action`。
- 记录情绪、心情、感受时必须传 `thoughtType=emotion`，并尽量填写 `emotionDetail`。
- 记录复盘、经验、教训、方法、决策依据时必须传 `thoughtType=reflection`，并尽量填写 `reflectionDetail`。
- 用户明确说“记录昨天/上周/某天的闪念”时，Agent 应将实际发生时间写入 `createTime`、`recordTime` 或 `happenedAt`，不要只依赖系统入库时间。
- 关联事件有独立发生时间时，应写入 `events[].eventTime`。
- 不要同时混填三类 detail；服务端只保存当前 `thoughtType` 对应的 detail。

### 12.3 `food_record_save`

轻量写入字段：

- `id`
- `idempotencyKey`
- `dishName`
- `category`
- `mealType`
- `cookDate`
- `status`
- `tags`
- `rating`
- `ingredients`
- `steps`
- `problems`
- `summary`
- `nextImprove`
- `worthRedo`

核心限制：

- `dishName` 是业务关键字段，无法确定时应追问。
- 创建时建议传 `idempotencyKey`。
- `id` 非空时表示更新指定记录。
- 状态限制为：

```text
draft, done, to_improve, archived
```

- 第一版不处理图片。
- 材料和步骤只保存文字结构化内容。
- 材料项只暴露 `name`、`quantity`、`unit`、`remark`，不暴露 `sortOrder`。
- 步骤项只暴露 `title`、`description`、`durationMinutes`，不暴露 `stepNo` 或 `sortOrder`。
- `difficulty`、`successLevel`、`prepMinutes`、`cookMinutes`、`totalMinutes`、`tasteDescription`、`briefSummary`、`nextTrySuggestion` 仍属于业务记录能力，但不再作为 MCP 写入入参暴露。

### 12.4 `food_record_query`

查询能力包括：

- `keyword`
- `category`
- `mealType`
- `status`
- `tags`
- `startDate`
- `endDate`
- `rating`
- `worthRedo`
- `toImprove`
- `page`
- `pageSize`

分页限制：

- 默认 `pageSize`：`20`
- 最大 `pageSize`：`200`

### 12.5 `time_record_save`

核心字段：

- `categoryId`
- `date`
- `startTime`
- `endTime`
- `title`
- `description`
- `exercises`
- `idempotencyKey`

限制：

- 工具保存时忽略传入 `id`，固定按新增处理。
- `duration` 由业务侧计算或置空，不由 MCP 入参直接决定。
- 关联练习记录保存时也忽略练习 `id`。

### 12.6 `time_record_queryByDateRange`

核心字段：

- `startDate`
- `endDate`

用于查询当前用户指定日期范围内的时间记录。

## 13. 返回结果与错误处理

### 13.1 成功结果

业务工具通常返回 `ApiResponse<T>`。默认情况下，MCP 层会解包 `ApiResponse.data`，并把结果写入 MCP `TextContent` 和 structured content。

### 13.2 业务失败

当 `ApiResponse.rscode` 不是成功码时，MCP 层会把业务错误转成 `isError=true` 的 MCP 结果。

### 13.3 参数错误

参数转换失败时返回：

```text
参数格式错误：<原因>
```

### 13.4 批量调用注意事项

批量调用时必须逐条检查：

- HTTP 状态码。
- JSON-RPC 是否有 error。
- MCP `result.isError` 是否为 `true`。
- MCP 文本内容中的错误信息。

不能只以 HTTP 200 判断写入成功。

## 14. 扩展新 MCP 工具的步骤

1. 在 `top.aiolife.mcp.tools` 包下新增或扩展工具类。
2. 确保工具类是 Spring Bean。
3. 新增一个单入参 public 方法。
4. 为方法添加 `@McpOperation`，配置唯一 `name` 和明确 `description`。
5. 为入参 DTO 字段添加 `@McpField`，写清楚字段用途、枚举、格式和限制。
6. 返回 `ApiResponse<T>` 或普通对象；默认推荐返回 `ApiResponse<T>`。
7. 写入类工具优先支持 `idempotencyKey`。
8. 确认工具内部使用当前登录用户上下文，不允许跨用户读写。
9. 启动后通过 `tools/list` 验证 schema。
10. 通过 `/api/mcp/tools/{name}/call` 或 MCP `tools/call` 验证调用。
11. 如需对外给 Agent 使用，同步更新 `docs/mcp` 下对应 Agent prompt 和 usage 文档。

## 15. 常见排查点

### 15.1 `tools/list` 看不到工具

检查：

- 工具类是否在 `top.aiolife.mcp.tools` 包下。
- 工具类是否是 Spring Bean。
- 方法是否标注 `@McpOperation`。
- 方法是否只有一个入参。
- 工具是否被管理配置停用。
- 工具名是否与已有工具重复导致启动失败。

### 15.2 `tools/call` 返回未登录

检查：

- `Authorization` 头是否存在。
- Bearer Token 是否有效。
- API Key 是否以 `ak-` 开头。
- API Key 是否已删除或过期。
- 后续请求是否携带 `mcp-session-id`。

### 15.3 批量写入部分失败

检查：

- 是否触发每用户每工具 60 秒 30 次限流。
- 是否逐条检查了 `result.isError`。
- 是否传入稳定 `idempotencyKey`，便于失败后安全重试。

### 15.4 审计日志看不到参数

检查：

- 参数摘要是否超过 2000 字符被截断。
- 字段名是否包含敏感关键字，被掩码为 `***`。

## 16. 维护要求

以下变更发生时，必须同步检查并更新 MCP 文档和 Agent 提示词：

- 新增、删除或重命名 MCP 工具。
- 工具入参字段新增、删除、重命名或类型变化。
- 枚举值变化，例如 `status`、`mealType`、`difficulty`、`themeKey`。
- 写入语义变化，例如默认草稿变为完成、支持更新、支持图片。
- 查询能力变化，例如新增筛选条件或分页限制。
- 鉴权、限流、幂等、审计或权限规则变化。
- 管理接口路径或权限变化。
