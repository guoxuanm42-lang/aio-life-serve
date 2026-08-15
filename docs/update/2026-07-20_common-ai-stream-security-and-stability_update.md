# git commit -m "2026-07-20 AI 流式通信安全与稳定性更新"


## 背景

原有 AI 聊天流式接口使用原始 token、`[DONE]` 和 `[ERROR]` 特殊字符串区分响应状态，前端通过查找字符串、全局删除 `data:` 和执行 `trim()` 处理响应。这种方式无法准确区分协议标记与模型正文，并可能改变代码缩进、换行和连续空格。

同时，AI 返回的 Markdown 会解析为 HTML 并通过 `v-html` 渲染，缺少对模型输出中原始 HTML 的限制。当用户停止生成、离开页面或连接异常时，原流程也没有统一的请求终止与不完整回复落库控制。

本次更新在不调整数据库和 Agent 会话模型的前提下，完成 AI 输出安全、结构化 SSE 协议、主动停止和断线状态控制。

## 解决的问题

- 解决流式正文包含 `data:`、`[DONE]` 或 `[ERROR]` 时可能被误识别为协议标记的问题。
- 解决代码缩进、连续空格、换行、中文和 Emoji 在增量解析中可能被改变的问题。
- 降低模型输出原始 HTML 或危险属性后通过 `v-html` 引发 XSS 的风险。
- 解决用户无法主动停止生成，以及页面离开后请求继续更新页面状态的问题。
- 避免流式请求异常、超时或断线后将不完整的 assistant 回复写入聊天记录。
- 在升级新 AI 接口的同时保留旧流式协议，避免影响现有调用方。

## 功能使用说明

### 新 AI 流式协议

`POST /ai/chat/stream` 改为输出标准 SSE 命名事件，事件数据使用 JSON。

#### Token 事件

```text
event: token
data: {"content":"模型增量文本"}
```

`content` 会按模型原始输出追加，前端不会对其执行 `trim()` 或全局字符串替换。

#### 完成事件

```text
event: done
data: {"conversationId":123,"modelName":"model-name"}
```

后端只在模型正常生成完成后保存完整 assistant 回复，随后发送 `done` 事件。

#### 错误事件

```text
event: error
data: {"code":"AI_STREAM_FAILED","message":"AI 生成失败，请稍后重试"}
```

新接口不向前端返回模型供应商或后端内部异常原文，详细错误仅记录在后端日志中。

### 停止生成

- AI 生成期间，发送按钮切换为“停止生成”。
- 用户点击停止后，前端通过 `AbortController` 中止响应读取，恢复输入状态，不显示异常提示。
- 切换菜单、切换聊天会话或卸载当前页面时，自动中止当前前端请求。
- 停止前已接收的部分回复保留在当前页面，但不写入数据库，刷新页面后不再显示。

### Markdown 安全渲染

- Marked 解析阶段不接受模型输出中的原始 HTML。
- 解析后的 HTML 再通过 DOMPurify 白名单净化。
- 保留标题、列表、引用、表格、链接和代码块等常用 Markdown 结构。
- 不允许 `script`、`style`、`iframe`、`object`、`embed`、`form` 等危险结构和内联事件属性。

## 本次调整

### 后端流式编排

- `AiChatService` 新增旧协议兼容入口，新旧协议共用 Agent 配置、模型调用、记忆、MCP 工具和消息保存链路。
- `/ai/chat/stream` 输出 `token`、`done`、`error` 命名事件及 JSON 数据。
- `/llm/chat/stream` 调用兼容入口，继续输出原始 token、`[DONE]` 和 `[ERROR]`。
- 使用原子终止状态统一控制正常完成、生成错误、SSE 发送失败、超时和客户端断线，避免同一流重复执行终止回调。
- 使用线程安全的完整回复缓冲区，只在正常完成时保存 assistant 消息。
- 客户端断线或超时后忽略后续 token 与完成回调。

### 前端流式解析与生命周期

- 新增独立 SSE 分帧解析器，支持 CRLF、LF、跨网络 chunk、单 chunk 内多事件和多行 `data`。
- AI 流式 API 返回包含 `start()` 和 `abort()` 的控制对象。
- 使用 `TextDecoder` 的流式解码能力处理拆分的 UTF-8 字符。
- 终止请求触发独立的取消回调，不进入普通错误处理流程。
- 将 Markdown 解析与安全净化抽取为独立工具，便于测试安全边界。

## 兼容性说明

- `/ai/chat` 非流式接口与请求结构保持不变。
- `/ai/chat/stream` 已切换为新协议，仅支持旧字符串解析的客户端需要同步升级。
- `/llm/chat/stream` 的旧流式协议保持不变，现有旧客户端可继续使用。
- 本次不新增依赖，使用前端已安装的 DOMPurify。
- 本次不新增或修改数据库表、字段和 SQL 脚本。
- 用户消息仍在模型调用前保存；主动停止或生成失败后，对应用户消息会保留，不保存不完整 assistant 消息。

## 已知边界

- 当前 LangChain4j `TokenStream` 没有提供公开的生成取消方法。
- 用户点击“停止生成”后，系统能够停止前端读取、后端 SSE 写出和 assistant 结果落库，但不能保证模型供应商侧的推理立即终止。
- 本次不新增 `pending`、`cancelled` 等消息状态，不完整 assistant 回复仅保留在当前页面内存中。

## 验证范围

### 后端

执行：

```powershell
$env:JAVA_HOME='D:\my_app\jdk\java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -q -DskipTests compile
mvn -q "-Dtest=AiAgentConfigServiceImplTest,AiMemoryServiceImplTest,AiChatServiceImplTest,AiToolServiceImplTest,LLMServiceTest" test
```

验证结果：

- JDK 21 后端编译通过。
- AI Agent 配置、长期记忆、流式聊天、MCP 工具适配和旧 LLM 服务定向测试通过。
- 新增流式异常测试，验证正常完成只保存完整回复，异常时不保存部分 assistant 消息。

### 前端

执行：

```powershell
pnpm exec vitest run apps/web-antd/src/api/core/ai.test.ts apps/web-antd/src/api/core/ai-stream.test.ts apps/web-antd/src/utils/safe-markdown.test.ts
pnpm exec eslint apps/web-antd/src/api/core/ai.ts apps/web-antd/src/api/core/ai-stream.ts apps/web-antd/src/api/core/ai.test.ts apps/web-antd/src/api/core/ai-stream.test.ts apps/web-antd/src/utils/safe-markdown.ts apps/web-antd/src/utils/safe-markdown.test.ts apps/web-antd/src/views/message/index.vue
pnpm --filter @vben/web-antd build
```

验证结果：

- 3 个前端测试文件、7 个测试用例全部通过。
- 测试覆盖 SSE 跨 chunk 分割、同 chunk 多事件、CRLF、多行 `data`、中文、Emoji、连续空格、`data:` 和 `[DONE]` 正文。
- 测试覆盖主动取消不触发错误回调，以及原始 HTML、事件属性、危险链接的净化。
- 本次涉及文件的 ESLint 检查通过。
- `@vben/web-antd` 生产构建通过。

### 已有验证限制

`pnpm --filter @vben/web-antd typecheck` 仍会因项目内既有 TypeScript 错误失败。已对本次涉及文件的错误输出进行过滤，未发现本次修改新增的类型错误。

## 主要文件

### 后端

- `src/main/java/top/aiolife/ai/service/impl/AiChatServiceImpl.java`
- `src/main/java/top/aiolife/ai/service/AiChatService.java`
- `src/main/java/top/aiolife/ai/api/AiChatController.java`
- `src/main/java/top/aiolife/llm/api/LLMController.java`
- `src/test/java/top/aiolife/ai/service/impl/AiChatServiceImplTest.java`

### 前端

- `apps/web-antd/src/api/core/ai.ts`
- `apps/web-antd/src/api/core/ai-stream.ts`
- `apps/web-antd/src/views/message/index.vue`
- `apps/web-antd/src/utils/safe-markdown.ts`
- `apps/web-antd/src/api/core/ai.test.ts`
- `apps/web-antd/src/api/core/ai-stream.test.ts`
- `apps/web-antd/src/utils/safe-markdown.test.ts`

## 后续建议

- 如后续模型 SDK 提供可靠的上游取消接口，将当前停止生成机制扩展为供应商侧推理取消。
- 结合消息状态字段补充 `pending`、`completed`、`failed` 和 `cancelled` 状态，进一步完善请求恢复和重试语义。
- 增加真实登录态下的浏览器端到端测试，覆盖发送、停止、切换会话和页面离开场景。
