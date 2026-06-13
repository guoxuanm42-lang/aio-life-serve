# 美食记录 MCP 保存参数简化

## 背景

`food_record_save` 原先直接暴露接近完整美食记录表单的字段，参数数量偏多。对 Agent 调用来说，很多字段只适合做完后的复盘场景，不适合作为每次写入都需要理解和选择的参数。

## 本次调整

- 将 `food_record_save` 调整为轻量写入协议，只暴露核心记录字段和少量复盘字段。
- 保留完整业务保存模型、数据库字段和前端美食记录页面能力不变。
- MCP 主请求保留 `id`、`idempotencyKey`、`dishName`、`category`、`mealType`、`cookDate`、`status`、`tags`、`rating`、`ingredients`、`steps`、`problems`、`summary`、`nextImprove`、`worthRedo`。
- MCP 主请求不再暴露 `difficulty`、`successLevel`、`prepMinutes`、`cookMinutes`、`totalMinutes`、`tasteDescription`、`briefSummary`、`nextTrySuggestion`。
- 材料项不再暴露 `sortOrder`，步骤项不再暴露 `stepNo` 和 `sortOrder`，排序继续由后端按数组顺序生成。

## 文档同步

- 已同步更新 `docs/mcp/food-record-agent-prompt.md`。
- 已同步更新 `docs/mcp/mcp-principles-and-limits.md`。
- `docs/mcp/streamable-http.md` 当前没有美食记录写入示例，无需同步示例 JSON。

## 验证范围

- Schema 测试会验证 `food_record_save` 不再暴露已移除字段。
- 工具映射测试会验证轻量请求仍能映射为业务保存请求，并将排序字段交给服务层默认生成。
- 幂等门面测试会验证 `idempotencyKey` 已存在时返回已有记录，创建成功后写回记录 ID。
