<!--
-- AI 活动总结生成失败：HTTP 环境 UUID 与雪花 ID 精度问题
-- 创建时间: 2026-08-15
-- 作者: Ethan
-- 更新功能简介:
-- 1) 记录局域网 HTTP 环境下 crypto.randomUUID 不可用导致生成请求未发出的问题
-- 2) 记录会话雪花 ID 转为 JavaScript Number 后精度丢失导致后端提示会话不存在的问题
-- 3) 记录对应修复方式、排查证据和验证方法
-->

# AI 活动总结生成失败：HTTP 环境 UUID 与雪花 ID 精度问题

## 1. 问题背景

消息中心新增“本周复盘 / 本月总结 / 本年回顾”功能后，在局域网地址
`http://172.20.10.2:5666` 点击“生成本年回顾”，页面连续出现：

```text
生成活动总结失败，请稍后重试
```

排查过程中发现这是两个先后出现、相互独立的前端问题。

## 2. 问题一：生成接口没有发出

### 2.1 现象

- 点击总结后创建了“本年活动总结”会话。
- 浏览器网络面板中没有 `/api/ai/activity-summary/generate` 请求。
- `ai_activity_summary_generation` 表中没有对应任务记录。
- 前端只显示通用失败提示。

### 2.2 根因

前端直接使用：

```ts
crypto.randomUUID();
```

`crypto.randomUUID()` 依赖安全上下文，通常只在 HTTPS 或 localhost 中可用。
项目通过局域网普通 HTTP 地址访问时，该方法可能不存在，前端在创建会话后、调用生成接口前便抛出异常。

原执行顺序为：

```text
创建会话
→ 调用 crypto.randomUUID()
→ 保存幂等请求
→ 调用 generate 接口
```

因此会出现“空会话已经创建，但生成请求没有发出”的现象。

### 2.3 解决方案

新增统一 UUID v4 工具：

```text
apps/web-antd/src/utils/uuid.ts
```

实现策略：

- 安全上下文优先使用 `crypto.randomUUID()`。
- 不支持 `randomUUID()` 时，使用 `crypto.getRandomValues()` 生成 UUID v4。
- 浏览器完全不支持安全随机数时抛出明确异常。
- 幂等键在创建会话前生成，避免 UUID 生成失败后遗留空会话。

调整后的执行顺序为：

```text
生成幂等键
→ 创建或确认会话
→ 保存待重试请求
→ 调用 generate 接口
```

### 2.4 验证

- UUID 定向单元测试通过，共 `2/2`。
- UUID 工具及消息中心定向 ESLint 通过。
- 修复后浏览器网络面板能够看到 `/api/ai/activity-summary/generate` 请求。

## 3. 问题二：后端返回“会话不存在”

### 3.1 现象

修复 UUID 后，生成请求已经正常发出，HTTP 状态为 `200`，但业务响应为：

```json
{
  "rscode": "100400",
  "result": "会话不存在",
  "data": null
}
```

数据库中实际存在该会话，例如：

```text
2088475559578959873
```

### 3.2 根因

前端发送请求前执行了以下转换：

```ts
const numericId = Number(conversationId);
return Number.isFinite(numericId) ? numericId : conversationId;
```

JavaScript 的最大安全整数是：

```text
9007199254740991
```

项目的 19 位雪花 ID 远大于该值。转换为 `Number` 后会发生精度丢失，导致前端发送的 ID 与数据库中的真实 ID 不一致，后端按错误 ID 查询时便返回“会话不存在”。

### 3.3 解决方案

雪花 ID 在前端全链路使用字符串：

```ts
const result = await generateActivitySummaryApi({
  period: summaryPeriod.value,
  conversationId: targetConversationId,
  idempotencyKey,
});
```

同时收紧活动总结 API 类型：

```ts
export interface ActivitySummaryGenerateRequest {
  period: ActivitySummaryPeriod;
  conversationId: string;
  idempotencyKey: string;
}
```

请求体必须保留字符串形式：

```json
{
  "period": "year",
  "conversationId": "2088475559578959873",
  "idempotencyKey": "UUID"
}
```

后端的 `Long conversationId` 可以正常接收数字字符串，无需前端转换为数值。

普通 AI 流式对话中的会话 ID 也同步改为直接传递字符串，避免同类问题。

## 4. 排查过程中的关键证据

### 4.1 后端年度统计本身正常

使用同一用户调用年度活动预览接口，时迹、闪念、美食、待办、题目、笔记、相册、文章和 MCP 九个模块均成功返回，因此可以排除：

- 年度时间范围解析错误。
- 业务统计 SQL 错误。
- 数据库字段缺失。
- 空模块组装错误。

### 4.2 第一次失败发生在请求前

- 网络面板没有 `generate` 请求。
- 数据库没有生成任务。
- 会话已经创建。

这些证据将问题定位到“创建会话之后、调用接口之前”的 UUID 生成步骤。

### 4.3 第二次失败发生在会话归属校验

- 网络面板已有 `generate` 请求。
- 后端明确返回“会话不存在”。
- 数据库存在浏览器 URL 中的原始会话 ID。

这些证据将问题定位到请求参数中的雪花 ID 精度丢失。

## 5. 通用规范

项目所有雪花 ID 在前端统一遵循以下规则：

- TypeScript 类型使用 `string`。
- JSON 请求和响应使用字符串。
- 路由参数和组件属性使用字符串。
- 禁止对雪花 ID 调用 `Number()`、`parseInt()` 或 `parseFloat()`。
- 排序、比较和 Map key 操作不要依赖数值转换。
- 后端继续使用 `Long`，由 JSON 框架完成数字字符串到 `Long` 的转换。

适用范围包括：

- 会话 ID。
- 消息 ID。
- 用户 ID。
- 分类 ID。
- 业务记录 ID。
- 其他 MyBatis-Plus 雪花主键。

## 6. 部署注意事项

- 生产环境应使用 HTTPS，原生 `crypto.randomUUID()` 在安全上下文中可直接使用。
- 即使生产环境使用 HTTPS，也应保留 UUID 回退逻辑，兼容局域网 HTTP、测试环境和部分 WebView。
- 此次修改仅涉及前端，开发环境通常刷新页面即可，无需重启后端。
- 正式环境需要重新构建并发布前端静态资源。

## 7. 后续优化建议

- 前端建立统一的 `SnowflakeId` 字符串类型或类型别名。
- 对 API DTO 中仍存在的 `number | string` 主键逐步收紧为 `string`。
- 增加超长雪花 ID 的请求序列化测试。
- 前端错误提示优先展示后端业务信息，避免所有异常都显示同一个通用提示。

