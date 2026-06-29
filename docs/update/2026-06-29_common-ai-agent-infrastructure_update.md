# AI Agent 基建、长期记忆与前端接入更新

## 背景

原有 AI 能力主要围绕 `/llm/chat` 单一聊天入口展开，前端会拼接上下文，后端直接调用大模型并保存聊天记录。随着后续需要支持不同场景的 AI 助手、长期记忆、MCP 工具调用和用户自定义模型参数，原有结构已经难以继续扩展。

本次更新将原来的通用 LLM 聊天能力升级为一套可配置、可扩展、可测试的 AI Agent 基建。

## 解决的问题

- 解决单一 `/llm/chat` 入口难以区分不同 AI 助手职责的问题。
- 解决 system prompt、数据库历史、前端 context 和用户消息容易重复注入的问题。
- 解决 AI 配置写死、无法按用户覆盖配置的问题。
- 解决长期偏好、事实、规则等记忆无法结构化管理和按 Agent 注入的问题。
- 为后续 Agent 调用 MCP 工具预留统一适配层。
- 保留旧 `/llm/chat`、`/llm/chat/stream` 兼容入口，降低前端迁移风险。

## 功能使用说明

### AI 聊天

前端主聊天流程改为调用新 AI 接口：

- 非流式：`POST /ai/chat`
- 流式：`POST /ai/chat/stream`

请求核心字段：

- `agentCode`：选择要使用的 Agent，不传时后端默认使用 `life_assistant`。
- `conversationId`：会话 id，用于加载短期历史和保存聊天记录。
- `message`：用户本轮原始输入。
- `context`：兼容旧入口的本轮系统上下文，不作为 user 消息保存。

### Agent 配置

个人中心新增 `AI 设置`，包含 `Agent 配置` 页签。用户可以查看和编辑 Agent：

- 名称
- 描述
- system prompt
- 模型 Key
- temperature
- 最大短期上下文条数
- 最大长期记忆条数
- 启用状态

### 长期记忆

个人中心 `AI 设置` 中新增 `长期记忆` 页签，支持：

- 按 Agent 筛选记忆
- 按记忆类型筛选记忆
- 新增记忆
- 编辑记忆
- 启用 / 禁用记忆

长期记忆字段包括：

- Agent 编码
- 记忆类型
- 记忆 Key
- 记忆内容
- 来源类型
- 来源业务 id
- 重要性
- 启用状态

## 本次调整

### 数据库

新增按天合并 SQL：

- `sql/2026-06-29.sql`

新增表：

- `ai_agent_config`
  - 保存系统默认 Agent 和用户覆盖配置。
  - `user_id=0` 表示系统默认配置。
  - 通过 `user_id + code` 保证同一用户同一 Agent 只有一份配置。

- `ai_memory`
  - 保存用户长期记忆。
  - 按 `user_id + agent_code + enabled` 查询聊天时可注入的记忆。

默认 Agent：

- `life_assistant`：生活总助理
- `thought_assistant`：闪念整理助手
- `food_assistant`：美食记录助手
- `study_assistant`：题库学习助手
- `writing_assistant`：文章写作助手
- `coding_assistant`：研发助手

同步调整：

- `sql/initial/01_schema.sql` 已同步 AI 表结构和默认 Agent 初始化数据。
- `sql/initial/01_schema.sql` 中 AI 表来源注释调整为 `2026-06-29.sql`。
- 已移除旧拆分增量文件引用：`2026-06-28_create_ai_agent_config.sql`、`2026-06-28_create_ai_memory.sql`。

### 后端 AI 编排

新增统一 AI 入口：

- `AiChatController`
  - `/ai/chat`
  - `/ai/chat/stream`

新增 Agent 配置接口：

- `AiAgentConfigController`
  - `GET /ai/agents`
  - `GET /ai/agents/{code}`
  - `POST /ai/agents/{code}`
  - `PUT /ai/agents/{code}/status`

新增长期记忆接口：

- `AiMemoryController`
  - `GET /ai/memories`
  - `GET /ai/memories/{id}`
  - `POST /ai/memories`
  - `PUT /ai/memories/{id}`
  - `PUT /ai/memories/{id}/status`

核心编排服务：

- `AiChatServiceImpl`
  - 解析默认 Agent 和指定 Agent。
  - 校验 Agent 启用状态。
  - 解析默认模型 Key 或指定模型 Key。
  - 加载长期记忆。
  - 构建 system message。
  - 加载短期聊天历史。
  - 构建 Agent 可用工具。
  - 创建 LangChain4j runtime。
  - 保存 user 原始输入和 assistant 回复。

上下文策略调整：

- `AiPromptContextBuilder` 只构建系统上下文，包括 system prompt、长期记忆和旧接口兼容 context。
- `AiChatMemoryFactory` 从 `chat_message` 加载短期历史。
- 模型调用时只传用户本轮原始 `message`。
- `chat_message` 的 user 消息只保存用户原始输入，不保存拼接后的完整 prompt。
- 流式和非流式保存口径保持一致。

### MCP 工具适配

新增 AI 工具适配层：

- `AiToolService`
- `AiToolServiceImpl`
- `AiMcpToolExecutor`
- `AiMcpToolProviderFactory`
- `AiToolSchemaConverter`

Agent 可通过 `enabledTools` 配置可用 MCP 工具，后端会将 MCP 工具转换为 LangChain4j `ToolSpecification` 和 `ToolExecutor`。

### 旧 LLM 入口兼容

旧入口保留：

- `/llm/chat`
- `/llm/chat/stream`

旧入口内部转发到新的 `AiChatService`，保持前端旧协议的返回结构：

- `/llm/chat` 继续返回 `String`。
- `/llm/chat/stream` 继续使用原有 SSE token、`[DONE]`、`[ERROR]` 协议。

### 前端接入

前端新增 AI API 封装：

- `apps/web-antd/src/api/core/ai.ts`

聊天页调整：

- `apps/web-antd/src/views/message/index.vue`
  - 进入 AI 聊天时加载 Agent 列表。
  - 默认选择 `life_assistant`，不可用时选择第一个启用 Agent。
  - 聊天页支持选择 Agent。
  - 发送消息改用 `/ai/chat/stream`。
  - 请求只携带 `agentCode`、`conversationId`、原始 `message`。
  - 不再发送前端拼接的历史 context，避免上下文重复。

个人中心新增 AI 设置：

- `apps/web-antd/src/views/_core/profile/ai-setting.vue`
- `apps/web-antd/src/views/_core/profile/index.vue`

包含两个页签：

- `Agent 配置`
- `长期记忆`

## 主要文件

### 后端

- `src/main/java/top/aiolife/ai/api/AiChatController.java`
- `src/main/java/top/aiolife/ai/api/AiAgentConfigController.java`
- `src/main/java/top/aiolife/ai/service/impl/AiChatServiceImpl.java`
- `src/main/java/top/aiolife/ai/service/impl/AiAgentConfigServiceImpl.java`
- `src/main/java/top/aiolife/ai/langchain4j/AiChatMemoryFactory.java`
- `src/main/java/top/aiolife/ai/langchain4j/AiPromptContextBuilder.java`
- `src/main/java/top/aiolife/ai/langchain4j/AiServiceFactory.java`
- `src/main/java/top/aiolife/ai/memory/service/impl/AiMemoryServiceImpl.java`
- `src/main/java/top/aiolife/ai/tool/impl/AiToolServiceImpl.java`
- `src/main/java/top/aiolife/llm/api/LLMController.java`
- `src/main/java/top/aiolife/config/MybatisPlusConfig.java`

### SQL

- `sql/2026-06-29.sql`
- `sql/initial/01_schema.sql`

### 测试

- `src/test/java/top/aiolife/ai/service/impl/AiAgentConfigServiceImplTest.java`
- `src/test/java/top/aiolife/ai/memory/service/impl/AiMemoryServiceImplTest.java`
- `src/test/java/top/aiolife/ai/service/impl/AiChatServiceImplTest.java`
- `src/test/java/top/aiolife/ai/tool/impl/AiToolServiceImplTest.java`
- `src/test/java/top/aiolife/llm/service/impl/LLMServiceTest.java`

### 前端

- `apps/web-antd/src/api/core/ai.ts`
- `apps/web-antd/src/api/core/index.ts`
- `apps/web-antd/src/views/message/index.vue`
- `apps/web-antd/src/views/_core/profile/index.vue`
- `apps/web-antd/src/views/_core/profile/ai-setting.vue`

## 验证范围

### 后端编译

已执行：

```powershell
$env:JAVA_HOME="D:\my_app\jdk\java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -q -DskipTests compile
```

结果：通过。

### AI 单元测试

已执行：

```powershell
$env:JAVA_HOME="D:\my_app\jdk\java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -q "-Dtest=AiAgentConfigServiceImplTest,AiMemoryServiceImplTest,AiChatServiceImplTest,AiToolServiceImplTest" test
```

结果：通过。

### SQL 执行

已执行：

```cmd
mysql --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u aio-life -proot aio-life < sql\2026-06-29.sql
```

执行结果：

- `ai_agent_config` 已创建。
- `ai_memory` 已创建。
- 系统默认 Agent 已写入 6 条。
- `ai_memory` 初始为空，符合预期。

说明：

- 第一次通过 PowerShell 管道执行 SQL 时，默认 Agent 中文字段被转成 `????`。
- 已删除 `user_id=0` 的默认 Agent 后，使用原始 SQL 文件重跑。
- 已通过 `HEX(name)` 确认中文字段按 UTF-8 正常写入。

### 前端验证

已启动前端开发服务：

```powershell
pnpm --filter @vben/web-antd dev
```

访问地址：

```text
http://localhost:5666/
```

前端生产构建验证：

```powershell
pnpm --filter @vben/web-antd build
```

当前未通过，失败点在现有 Vite/Tailwind 依赖链的 `jiti` 浏览器打包问题：

```text
"createRequire" is not exported by "__vite-browser-external"
```

该问题未在本阶段处理，建议单独作为前端构建配置问题收口。

## 技术栈

后端：

- Java 21
- Spring Boot
- MyBatis-Plus
- Sa-Token
- LangChain4j
- SseEmitter
- JUnit 5
- Mockito
- Maven
- MySQL

前端：

- Vue 3
- TypeScript
- Vite
- Ant Design Vue
- Vben Admin
- Fetch Stream
- pnpm

AI / 工具：

- LangChain4j ChatModel
- LangChain4j ChatMemory
- LangChain4j ToolSpecification
- LangChain4j ToolExecutor
- MCP 工具注册与执行适配

## 注意事项

- 当前前后端相关新增文件仍需纳入 git 提交。
- `/llm/chat` 兼容入口当前主要读取旧字段 `prompt`，如果后续旧入口也要兼容 `message` 字段，建议补充 `prompt` 优先、`message` 兜底的转换逻辑。
- 前端生产构建存在既有构建链路问题，需要单独修复后才能确认生产包可发布。
- 当前长期记忆为手工维护能力，暂未实现自动抽取和自动沉淀。
- MCP 工具能力已预留到 Agent 配置，但前端暂未做工具选择 UI。

## 后续建议

- 修复前端 `jiti` 打包问题，恢复 `pnpm --filter @vben/web-antd build` 生产构建验证。
- 补充旧 `/llm/chat` 对 `message` 字段的兼容。
- 后续增加 Agent 工具选择 UI，让用户能配置每个 Agent 可使用的 MCP 工具。
- 增加长期记忆自动抽取策略，例如从聊天总结、时迹总结、闪念复盘中沉淀记忆。
- 增加 AI 模块集成测试，覆盖真实 Spring 容器、数据库和 SSE 流式响应。
