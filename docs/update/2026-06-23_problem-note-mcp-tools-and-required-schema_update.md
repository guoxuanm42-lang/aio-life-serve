# 题目模块 MCP 工具与必填 Schema 更新

## 背景

题目模块已经支持前端手动维护题目记录，包括题目标题、题目内容、Java 解法代码、解题思路、难度、标签、状态和分类。

为了让外部 AI 能通过 AIO-LIFE 现有 MCP Streamable HTTP 入口读取题库、写入新题目，本次为题目模块新增 MCP 读写工具，并补充 MCP 参数必填标记能力，避免 AI 写入缺少核心字段的题目记录。

## 解决的问题

- 解决外部 AI 无法通过 MCP 查询当前用户题库的问题。
- 解决外部 AI 无法通过 MCP 新增题目记录的问题。
- 解决 `problem_note_save` 虽然字段描述写了“必填”，但 MCP 工具参数面板仍显示“必填：否”的问题。
- 保留业务层强校验，避免绕过 MCP Schema 时写入空标题或空题面。
- 支持写入幂等键，避免外部 AI 重试导致重复新增题目。

## 功能使用说明

新增 MCP 工具：

- `problem_note_query`：查询当前登录用户题库。
- `problem_note_save`：新增当前登录用户题目记录。

推荐 MCP 工具配置：

`problem_note_save`

- 展示名称：`保存题目记录`
- 分组：`题目`
- 写操作：开启
- 排序：`10`
- 描述覆盖：

```text
新增当前用户题目记录。用于把题目标题、题目内容、Java 解法代码和解题思路保存到题库；title 和 problemContent 必填，solutionCode 可选；支持 idempotencyKey 防止重复写入。
```

`problem_note_query`

- 展示名称：`查询题库`
- 分组：`题目`
- 写操作：关闭
- 排序：`20`
- 描述覆盖：

```text
查询当前用户题库。支持按关键词、难度、状态、标签、分类 ID 或未分类筛选，返回题目内容、Java 解法代码和解题思路。
```

## 本次调整

### MCP 工具

新增题目记录 MCP 工具：

- `problem_note_query`
  - 支持 `page`、`pageSize`、`keyword`、`difficulty`、`status`、`tags`、`categoryId`、`uncategorized`。
  - 返回 `id`、`categoryId`、`title`、`problemContent`、`solutionCode`、`ideaNote`、`difficulty`、`tags`、`status`、`createTime`、`updateTime`。
  - 复用当前用户上下文，只查询当前登录用户题库。
- `problem_note_save`
  - 支持 `idempotencyKey`、`categoryId`、`title`、`problemContent`、`solutionCode`、`ideaNote`、`difficulty`、`tags`、`status`。
  - 仅支持新增题目，不支持更新已有题目。
  - `categoryId` 为空时保存为未分类题目。
  - `categoryId` 非空时复用业务层分类归属校验。
  - 创建成功后按 `mcp:idemp:problem_note_save:<userId>:<idempotencyKey>` 缓存题目记录 ID。

### 必填字段

扩展 MCP 字段注解：

- `@McpField` 新增 `required` 属性，默认 `false`。
- `McpFieldSchemaResolver` 支持读取 `@McpField(required = true)` 并写入 JSON Schema `required`。
- 保留 Java primitive 字段自动 required 的原有逻辑。

`problem_note_save` 必填字段：

- `title`
- `problemContent`

`solutionCode` 继续保持可选，允许先保存题面，后续再从前端补充 Java 解法代码。

### 文档

同步更新 MCP 文档：

- `docs/mcp/mcp-principles-and-limits.md`
  - 新增题目 MCP 工具说明。
  - 新增 MCP 字段必填规则说明。
  - 新增 `problem_note_query`、`problem_note_save` 字段限制。
- `docs/mcp/problem-note-agent-usage.md`
  - 新增题目记录 Agent 使用说明。
  - 明确查询和写入工具的触发条件。
  - 明确 `title`、`problemContent` 必填，`solutionCode` 可选。

## 主要文件

后端新增和调整：

- `src/main/java/top/aiolife/mcp/tools/ProblemNoteMcpTools.java`
- `src/main/java/top/aiolife/record/service/ProblemNoteAiFacade.java`
- `src/main/java/top/aiolife/mcp/pojo/req/ProblemNoteQueryToolReq.java`
- `src/main/java/top/aiolife/mcp/pojo/req/ProblemNoteSaveToolReq.java`
- `src/main/java/top/aiolife/mcp/pojo/vo/ProblemNoteQueryToolVO.java`
- `src/main/java/top/aiolife/mcp/annotation/McpField.java`
- `src/main/java/top/aiolife/mcp/schema/McpFieldSchemaResolver.java`

测试新增和调整：

- `src/test/java/top/aiolife/mcp/tools/ProblemNoteMcpToolsTest.java`
- `src/test/java/top/aiolife/record/service/ProblemNoteAiFacadeTest.java`
- `src/test/java/top/aiolife/record/service/impl/ProblemNoteServiceImplTest.java`
- `src/test/java/top/aiolife/mcp/McpSchemaGeneratorTest.java`
- `src/test/java/top/aiolife/mcp/McpToolCompatibilityTest.java`
- `src/test/java/top/aiolife/mcp/McpToolRegistryTest.java`

文档新增和调整：

- `docs/mcp/problem-note-agent-usage.md`
- `docs/mcp/mcp-principles-and-limits.md`
- `docs/mcp/README.md`

## 验证范围

已执行 MCP 题目工具和必填字段相关测试：

```bash
mvn "-Dtest=McpToolRegistryTest,McpSchemaGeneratorTest,McpToolCompatibilityTest,ProblemNoteMcpToolsTest,ProblemNoteAiFacadeTest" test
```

结果：通过。

已执行必填字段双层限制相关测试：

```bash
mvn "-Dtest=McpSchemaGeneratorTest,McpToolCompatibilityTest,ProblemNoteServiceImplTest,ProblemNoteAiFacadeTest,ProblemNoteMcpToolsTest" test
```

结果：通过。

已执行后端编译验证：

```bash
mvn -DskipTests compile
```

结果：通过。

验证时需要使用 JDK 21：

```powershell
$env:JAVA_HOME="D:\my_app\jdk\java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## 后续建议

- 如果后续希望 AI 直接按分类名称写入题目，可以增加分类名匹配或自动创建分类能力。
- 如果题目内容和代码变长，可以新增 `problem_note_detail`，让 `problem_note_query` 只返回摘要。
- 如果后续需要 MCP 更新题目，再新增独立 `problem_note_update`，不要复用当前只新增的 `problem_note_save`。
