# 美食记录 Agent MCP 文档更新（2026-06-05）

## 1. Git 检索结果

本次检索后端仓库 Git 状态，当前已跟踪文件没有未提交 diff，工作区新增了 3 个 MCP 文档文件：

- `docs/mcp/README.md`
- `docs/mcp/food-record-agent-prompt.md`
- `docs/mcp/food-record-agent-usage.md`

最近一次提交为：

- `e0468b8 增加闪念查询功能`

该提交已记录在 `2026-06-05_thought-subject-fuzzy-search_update.md`，本篇记录聚焦当前工作区新增的 MCP / 美食记录 Agent 文档。

## 2. 更新背景

AIO-LIFE 已具备 MCP 工具管理和美食记录相关工具能力，但 Agent 配置文档此前没有形成独立沉淀。为了让 Trae 智能体在美食模块中稳定区分“普通规划对话”“查询历史记录”和“明确写入美食记录”，本次新增 MCP 文档目录说明与美食记录 Agent 专用提示词、调用规则和使用模板。

## 3. 文档更新

### 3.1 MCP 文档目录说明

新增文件：

- `docs/mcp/README.md`

主要内容：

- 说明 `docs/mcp` 目录用于存放 MCP 接入、调试、Agent 提示词和工具变更相关文档。
- 建立文档索引，关联 Streamable HTTP、Food Record Agent 提示词和 Food Record Agent 使用说明。
- 增加 Agent 提示词维护规则，要求 MCP 工具名称、字段、枚举、写入语义、查询能力、鉴权和限流规则变化时，同步检查并更新相关提示词。

### 3.2 美食记录 Agent 提示词

新增文件：

- `docs/mcp/food-record-agent-prompt.md`

主要内容：

- 定义 AIO-LIFE 美食模块专用 Agent 的职责：做饭规划、菜品建议、食材清单、烹饪步骤、口味调整、复盘总结和记录写入。
- 明确日常对话阶段只能规划和建议，不允许主动写入系统。
- 约定只有用户明确表达“美食记录”“写入美食记录”“帮我记录到美食记录”“记录一下这顿饭”等写入口令时，才允许调用 `food_record_save`。
- 明确普通聊天、菜谱咨询、购物清单整理，以及用户声明“先别记录”“不要入库”时禁止调用 `food_record_save`。
- 规定写入默认字段：
  - `status` 默认为 `draft`
  - `cookDate` 默认为当天，格式为 `YYYY-MM-DD`
  - `idempotencyKey` 按日期、餐次、菜名和核心信息生成，避免重复写入
  - 当前 MCP 第一版不处理图片
- 规范写入字段整理规则，包括菜名、分类、餐次、食材、步骤、标签、难度、耗时、口味描述、问题、摘要、复做建议等。
- 补充历史查询能力：用户询问历史美食记录、之前做过什么、某类菜做过哪些时，可调用 `food_record_query`。

### 3.3 Trae 调用规则与用户模板

新增文件：

- `docs/mcp/food-record-agent-usage.md`

主要内容：

- 提供可复制到 Trae 智能体“何时调用”配置中的文本。
- 明确用户围绕美食模块进行规划、建议、食材清单、烹饪步骤、口味调整、复盘总结、历史记录查询时可调用该 Agent。
- 再次区分：
  - 明确写入美食记录时调用 `food_record_save`
  - 查询历史美食记录时调用 `food_record_query`
  - 普通聊天、菜谱咨询、购物清单整理或明确不要入库时禁止调用 `food_record_save`
- 提供智能体基础信息建议：
  - 名称：`AIO-LIFE 美食记录助手`
  - 英文标识名：`aio-life-food-record-agent`
- 提供用户使用模板：
  - 直接生成并写入 5 份家常菜
  - 指定 5 道菜并写入
  - 先预览再写入
  - 查询历史美食记录

## 4. 行为约束

本次更新重点强化 Agent 的工具调用边界：

- 未收到明确写入口令前，不调用 `food_record_save`。
- 不把普通菜谱咨询、聊天建议或购物清单自动入库。
- 用户明确“先别记录”“不要入库”时，即使已有完整菜品信息，也不写入。
- 查询历史记录、复做建议和改进记录时使用 `food_record_query`，避免误触发写入。
- 写入记录默认保持草稿状态，避免将计划阶段内容误标记为已完成。

## 5. 影响范围

- 本次没有修改 Java 业务代码。
- 本次没有新增 SQL。
- 本次没有修改 MCP 工具实现和工具 Schema。
- 本次主要影响 Trae 智能体配置、Agent 使用说明和后续 MCP 工具变更时的文档维护流程。

## 6. 验证情况

已执行 Git 检索：

```bash
git status --short
git diff --stat
git log -1 --stat --oneline
```

结果：

- `git status --short` 显示 3 个新增未跟踪 MCP 文档文件。
- `git diff --stat` 没有输出，说明当前已跟踪文件无未提交 diff。
- 最近一次提交为 `e0468b8 增加闪念查询功能`，该提交已另有更新记录。

注意：执行 `git status --short` 时出现 `.git/index.lock` 删除失败提示，可能是 Windows 文件占用导致。当前命令仍返回了有效状态；如后续 Git 操作异常，可检查是否存在残留的 `.git/index.lock` 或是否有其他 Git 进程占用仓库。
