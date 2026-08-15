# 2026-08-15 AI 活动总结生成与复盘交互更新

## 提交建议

```bash
git commit -m "feat(ai): add activity summary generation and review interaction"
```

## 背景

消息中心原有“总结今日 / 总结本周”主要围绕时迹记录，无法完整反映用户在 AIO-LIFE 中新增或执行的活动，也存在前端收到总结后再次调用聊天接口保存、可能重复生成和重复落库的问题。

本次更新建设统一的 AI 活动总结链路，以后端业务表的准确统计为事实来源，再由会话绑定的 Agent 生成自然语言复盘。功能仅支持本周、本月和本年，不提供今日总结。

## 解决的问题

- 将单一时迹总结升级为覆盖九个业务模块的活动总结。
- 内部业务数据由后端 Service 直接统计，不通过 MCP 临时逐项查询。
- 提供不调用模型的预览能力，便于先核对统计数据。
- 一次生成请求完成统计、模型调用和双边消息保存，避免前端二次调用聊天接口。
- 通过幂等任务防止网络重试造成重复模型调用或重复消息。
- 空模块不进入模型上下文，减少无意义内容和 Token 消耗。
- 修复局域网 HTTP 环境无法生成 UUID，导致生成请求未发出的问题。
- 修复 19 位会话雪花 ID 转为 JavaScript `Number` 后精度丢失，导致后端提示“会话不存在”的问题。

## 功能使用说明

### 总结入口

消息中心的 Agent 选择器右侧提供紧凑的活动总结组合按钮，默认显示：

```text
✨ 生成本周复盘
```

下拉菜单支持：

- 本周复盘：本周一 00:00 至当前时刻。
- 本月总结：本月 1 日 00:00 至当前时刻。
- 本年回顾：本年 1 月 1 日 00:00 至当前时刻。

最后选择的周期保存在 `localStorage`，非法值自动回退到 `week`。

### 生成流程

- 已有会话时，使用会话绑定的 Agent 生成总结。
- 没有会话时，先按当前选择的 Agent 创建活动总结会话。
- 生成期间按钮进入 loading 状态并禁止重复点击。
- 生成成功后重新查询当前会话历史，以数据库消息作为唯一展示来源。
- 空活动只显示页面提示，不调用模型、不写入聊天消息。
- 网络失败后可以重试，同一次重试复用原幂等键。
- 切换周期、会话或重新发起总结时使用新的幂等键。

## 统计范围与口径

### 统计周期

- 仅支持 `week`、`month`、`year`。
- 时区固定为 `Asia/Shanghai`。
- 日期时间查询使用半开区间 `[startTime, endTime)`。
- 时迹按业务日期查询，日期范围包含开始日期和当前日期。
- 同一次聚合只读取一次当前时间，九个模块共享相同结束时刻。

### 九个统计模块

| 模块 | 主要统计内容 |
|---|---|
| 时迹 | 记录数、总分钟数、分类耗时、分类占比、主要活动 |
| 闪念 | 新增数、类型分布、主题分布、最近标题 |
| 美食 | 新增记录数、最近菜名 |
| 待办 | 新增数、最近待办内容 |
| 题目 | 新增数、标题、分类分布、难度分布 |
| 笔记 | 新增数、最近标题，不读取正文 |
| 相册 | 新建相册数、最近相册名称 |
| 文章 | 新增数、更新数、标题、以新增文章为准的分类分布 |
| MCP | 调用数、成功数、失败数、平均耗时、工具排行 |

总数基于完整数据，标题、名称和排行明细按策略截断。无数据模块在统一 Context 中设为 `null`，序列化时自动省略。

## 本次调整

### 后端统计协议

- 新增 `AiActivitySummaryReq`，调用方必须明确传入周期。
- 新增 `AiActivitySummaryPeriod`，统一解析 `week/month/year`。
- 新增 `AiActivityDateRange` 和 `AiActivityDateRangeResolver`。
- 新增 `AiActivitySummaryContext` 及九个模块 Summary DTO。
- 统一集合非空、空模块省略、总数不受明细截断影响等序列化约定。

### 模块统计服务

在 `ai.activity` 活动统计层建立九个独立统计 Service，直接读取各业务 Mapper：

- `TimeRecordActivitySummaryService`
- `ThoughtActivitySummaryService`
- `FoodActivitySummaryService`
- `TodoActivitySummaryService`
- `ProblemActivitySummaryService`
- `NoteActivitySummaryService`
- `AlbumActivitySummaryService`
- `ArticleActivitySummaryService`
- `McpActivitySummaryService`

所有查询使用当前登录用户 ID 隔离，并遵循业务表的逻辑删除规则。统计层只读取生成总结所需字段，不读取笔记正文和工具参数等无关内容。

### 统一聚合与预览

新增统一聚合服务：

```text
AiActivitySummaryService
AiActivitySummaryServiceImpl
```

聚合服务按固定顺序同步调用九个模块，任一模块失败时整次聚合失败，不返回可能被误认为完整结果的残缺数据。

新增预览接口：

```http
POST /api/ai/activity-summary/preview
```

该接口只返回结构化统计，不调用模型、不保存会话消息，主要用于数据核验和故障定位。

### AI 生成与消息持久化

新增生成接口：

```http
POST /api/ai/activity-summary/generate
```

执行链路：

```text
校验用户和会话归属
→ 聚合结构化活动统计
→ 创建或认领幂等任务
→ 构建受控提示词
→ 调用会话 Agent 一次
→ 原子保存用户消息和助手消息
→ 更新任务状态和会话时间
→ 返回数据库中的消息 ID 与内容
```

模型上下文只包含周期、数量、分类分布、有限标题、时迹耗时和 MCP 排行，不加载历史对话、长期记忆或 MCP 工具。

提示词要求模型只能引用提供的数据，不能修改数字、补充未提供事实，也不能把“新增、记录、调用”统一描述为“完成”。

### 幂等与数据库

`chat_message` 新增：

```text
source_type
idempotency_key
```

活动总结消息使用：

```text
source_type = activity_summary
```

并通过用户、会话、来源、幂等键和角色组成的唯一索引防止重复消息。

新增 `ai_activity_summary_generation` 表，状态包括：

```text
PROCESSING
GENERATED
SUCCESS
FAILED
```

数据库迁移脚本：

```text
sql/2026-07-20.sql
sql/2026-08-14_activity_summary_generation.sql
```

同时补齐会话 Agent、活动总结消息来源、幂等任务和相关索引字段。

### 前端交互

- 使用组合下拉按钮收拢周、月、年三个入口，减少顶部操作区拥挤。
- 桌面端显示完整文案，移动端显示紧凑总结入口。
- 已有会话时锁定 Agent 选择器，避免界面选择与会话实际 Agent 不一致。
- 不再本地拼接用户消息和助手消息，生成成功后统一刷新历史。
- 删除“生成后再次调用普通聊天接口记录总结”的旧逻辑。
- 请求失败或空活动时不插入假消息。

## 本次故障修复

### HTTP 环境 UUID 兼容

原代码直接调用 `crypto.randomUUID()`。通过局域网普通 HTTP 地址访问时，该方法可能不可用，导致前端在创建会话后、发起生成请求前抛出异常。

新增 `createUuid()`：

- 优先使用 `crypto.randomUUID()`。
- 不可用时通过 `crypto.getRandomValues()` 生成 UUID v4。
- 将幂等键生成提前到创建会话之前，避免遗留空会话。

### 雪花 ID 精度保护

原代码将会话 ID 执行 `Number(conversationId)`。19 位雪花 ID 超过 JavaScript 最大安全整数，转换后精度丢失，后端按错误 ID 查询并返回“会话不存在”。

修复后：

- 活动总结请求的 `conversationId` 类型固定为 `string`。
- 普通 AI 流式对话也直接传递字符串会话 ID。
- 删除会话 ID 的 `Number()` 转换。
- 请求 JSON 中雪花 ID 始终使用带引号的字符串形式。

详细故障复盘见：

```text
docs/problems/02_frontend/2026-08-15_AI活动总结生成失败_HTTP环境UUID与雪花ID精度.md
```

## 兼容性与影响范围

- 后端周期协议仅支持 `week/month/year`，不支持 `today`。
- 前端默认周期为 `week`，后端不提供默认周期。
- 旧时迹总结接口暂时保留，但消息中心不再使用。
- 普通聊天消息的来源和幂等字段为空，不受活动总结唯一索引影响。
- 后端 `Long` 类型可以接收 JSON 数字字符串，前端无需转换雪花 ID。
- 开发环境只修改前端时通常刷新页面即可，无需重启后端。
- 正式发布需要重新构建并部署前端静态资源。
- 生产环境仍建议使用 HTTPS，同时保留 HTTP 环境 UUID 回退能力。

## 验证范围

### 数据库

- 已确认 `conversation/chat_session` 可读取 `agent_code`。
- 已确认 `chat_message` 包含消息来源和幂等字段及唯一索引。
- 已确认 `ai_activity_summary_generation` 表及索引存在。
- 迁移前已有会话和消息数据未被删除。

### 后端统计

- 使用真实年度数据调用预览接口成功。
- 九个模块均返回结构化统计，周期为 `year`。
- 由此排除年度时间范围、统计 SQL 和模块组装异常。

### 前端

执行：

```powershell
pnpm exec vitest run apps/web-antd/src/utils/uuid.test.ts
pnpm exec eslint apps/web-antd/src/views/message/index.vue apps/web-antd/src/api/core/llm.ts apps/web-antd/src/utils/uuid.ts apps/web-antd/src/utils/uuid.test.ts
```

验证结果：

- UUID 测试 `2/2` 通过，覆盖原生 `randomUUID()` 和 HTTP 环境回退路径。
- 本次涉及文件的定向 ESLint 检查通过。
- 已确认活动总结和普通流式对话不再将会话 ID 转为 `Number`。
- 修复 UUID 后，浏览器能够正常发出 `/activity-summary/generate` 请求。

### 已知验证限制

项目全量 `vue-tsc` 类型检查仍被多个既有 TypeScript 错误阻断，本次检查输出中未发现 UUID 和活动总结 ID 修复新增的类型错误。

最终的真实模型生成、消息双边落库和刷新后展示一致性，仍需在当前模型配置可用的环境中完成一次端到端验收。

## 主要文件

### 后端

- `src/main/java/top/aiolife/ai/activity/`
- `src/main/java/top/aiolife/llm/pojo/entity/ChatMessageEntity.java`
- `src/main/java/top/aiolife/llm/pojo/entity/ConversationEntity.java`
- `sql/2026-07-20.sql`
- `sql/2026-08-14_activity_summary_generation.sql`

### 前端

- `apps/web-antd/src/views/message/index.vue`
- `apps/web-antd/src/views/message/components/ActivitySummaryAction.vue`
- `apps/web-antd/src/api/core/llm.ts`
- `apps/web-antd/src/utils/uuid.ts`
- `apps/web-antd/src/utils/uuid.test.ts`

### 文档

- `docs/problems/02_frontend/2026-08-15_AI活动总结生成失败_HTTP环境UUID与雪花ID精度.md`
- `docs/update/2026/08/2026-08-15_ai-activity-summary-generation_update.md`

## 后续建议

- 完成一次真实模型端到端验收，核对一次点击只调用一次模型并保存一组消息。
- 为活动总结生成接口增加浏览器端组件测试和请求序列化测试。
- 建立统一的 `SnowflakeId = string` 类型，逐步清理前端 API 中的 `number | string` 主键定义。
- 优化前端错误提取逻辑，优先展示后端 `result` 中的业务错误信息。
- 在生产部署中配置 HTTPS，并增加活动总结接口耗时、模型失败率和幂等任务状态监控。
