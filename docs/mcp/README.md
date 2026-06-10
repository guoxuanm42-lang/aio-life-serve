# MCP 文档说明

本目录存放 AIO-LIFE MCP 接入、调试、Agent 提示词和工具变更相关文档。

## 文档索引

- `streamable-http.md`：MCP Streamable HTTP 接入、鉴权和 curl 调试说明。
- `mcp-principles-and-limits.md`：AIO-LIFE MCP 调用原则、运行链路、工具注册、鉴权、限流、幂等、审计和业务限制说明。
- `food-record-agent-prompt.md`：AIO-LIFE 美食记录 Agent 提示词，可复制到 Trae 智能体配置中使用。
- `food-record-agent-usage.md`：AIO-LIFE 美食记录 Agent 的 Trae“何时调用”配置和用户使用模板。

## Agent 提示词维护规则

每次涉及 MCP 的变动，都需要同步检查相关 Agent 提示词，避免智能体调用过期工具或传入错误字段。

需要同步更新提示词的变更包括：

- 新增、删除或重命名 MCP 工具。
- MCP 工具入参字段新增、删除、重命名或类型变化。
- 字段枚举值变化，例如 `status`、`mealType`、`difficulty`。
- 工具调用语义变化，例如写入从草稿改为完成、图片处理能力变化。
- 鉴权、幂等、限流、审计或权限规则变化。
- 查询能力变化，例如新增日期范围、标签、评分、是否复做等筛选条件。

## 更新记录

### 2026-06-05

- 新增 `food-record-agent-prompt.md`，沉淀美食记录 Agent 提示词。
- 新增 `food-record-agent-usage.md`，沉淀 Trae“何时调用”配置、智能体基础信息建议和用户使用模板。
- 新增本文档，明确 MCP 文档目录用途和 Agent 提示词同步维护规则。
- 约定后续 MCP 工具变更时同步更新对应 Agent 提示词。
