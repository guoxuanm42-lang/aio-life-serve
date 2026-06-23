# 美食模块 AI 生成食谱草稿更新

## 背景

美食模块原有能力主要围绕手动记录做饭过程、评分和复盘。为了降低新增记录的输入成本，本次在美食页面补充 AI 生成食谱草稿能力：用户输入一句自然语言需求，系统调用当前登录用户默认大模型生成结构化草稿，前端预览后再由用户决定是否应用到新增表单。

本次实现覆盖第一阶段和第二阶段：

- 第一阶段：跑通“生成、预览、填表、手动保存”的最小闭环。
- 第二阶段：支持对当前草稿重复生成和快捷改稿，并提供最近版本回退。

## 解决的问题

- 解决新增美食记录前需要用户从零填写菜名、食材、步骤、总结等字段的问题。
- 解决 AI 生成结果不满意时只能关闭重来的问题，支持基于当前草稿继续改稿。
- 解决 AI 直接写库带来的脏数据风险，所有 AI 结果只作为前端草稿预览，不自动保存。
- 解决版本不可回退的问题，弹窗内保留最近 5 版生成结果，用户可切换后再应用。

## 本次调整

### 后端

新增美食 AI 生成接口：

- `POST /food-record/ai/generate-recipe`

接口行为：

- 使用 `StpUtil.getLoginIdAsLong()` 获取当前登录用户。
- 使用 `LLMKeyService.getDefaultLLMKey(userId)` 获取默认大模型配置。
- 未配置默认大模型 API Key 时返回明确业务错误：`请先配置大模型 API Key`。
- 调用现有 `LLMService.generateResponse(...)` 生成食谱草稿。
- 要求模型只返回 JSON，不返回 Markdown，不自动保存数据库。
- 服务端解析模型返回 JSON，并做一次简单容错：去掉 ```json / ``` 包裹后再解析。
- 解析失败时返回：`AI 返回格式异常，请重试`。
- 生成结果缺少菜名时返回：`生成结果缺少菜名，请调整描述后重试`。
- 不调用 `foodRecordService.create`，不写入美食记录表。

新增请求对象：

- `FoodRecipeGenerateReq`

主要字段：

- `prompt`：用户原始自然语言需求。
- `currentDraft`：当前草稿，改稿时携带。
- `instruction`：本次改稿要求。

草稿默认规则：

- `id = null`
- `status = draft`
- `cookDate` 缺失时默认当前日期。
- `ingredients` 和 `steps` 缺失时补为空数组。
- `totalMinutes` 缺失且存在备菜/烹饪耗时时，自动计算。

改稿逻辑：

- 没有 `currentDraft` 时，按用户原始需求生成新食谱。
- 有 `currentDraft` 时，将原始需求、当前草稿 JSON 和改稿要求一起传给大模型，要求生成一份新的完整 JSON 草稿。

新增和调整的主要后端文件：

- `src/main/java/top/aiolife/record/api/FoodRecordAiController.java`
- `src/main/java/top/aiolife/record/pojo/req/FoodRecipeGenerateReq.java`

### 前端

美食记录页面新增 `AI 生成食谱` 入口：

- 放在美食记录工具栏。
- 移动端保留原有右下角新增按钮，同时在页面工具区展示 AI 入口。
- 点击后打开独立 AI 弹窗，不复用现有新增表单弹窗。

AI 弹窗能力：

- 支持输入一句自然语言需求。
- 第一阶段不拆独立条件字段，口味、人数、食材、忌口、餐次、难度等都由用户自然语言描述。
- 空输入点击生成时提示输入需求。
- 生成中显示 loading，避免重复提交。
- 生成失败显示错误提示。
- 生成成功后展示草稿预览：菜名、分类、餐次、耗时、标签、食材、步骤、总结、下次建议。
- 点击 `应用到新增表单` 后关闭 AI 弹窗，打开现有新增表单，并填入当前选中草稿。
- 应用草稿时强制覆盖 `status = draft`，清空 `id`，不处理图片。
- 用户仍需点击现有保存按钮后才会调用 `/food-record/save`。

第二阶段新增快捷改稿按钮：

- `重新生成`：请基于原始需求重新生成一份不同的食谱草稿。
- `更简单`：请降低难度，减少步骤和复杂处理。
- `更清淡`：请调整为更清淡、少油、少盐的版本。
- `更快手`：请缩短总耗时，优先控制在 20 分钟内。
- `换一道`：请换成另一道符合原始需求的菜。
- `用现有食材重做`：请优先使用当前草稿中的食材重新设计做法。

版本历史：

- 弹窗内维护 `aiRecipeDraftHistory` 和 `aiRecipeDraftIndex`。
- 每次生成成功后把新草稿追加到历史。
- 最多保留最近 5 版，超过后丢弃最早版本。
- 展示 `版本 1/3`。
- 支持 `上一版`、`下一版` 切换。
- 切换版本只更新当前预览，不重新请求 AI。
- `应用到新增表单` 使用当前选中的版本。
- 关闭弹窗不清空历史，页面刷新后不持久化历史。

新增和调整的主要前端文件：

- `apps/web-antd/src/api/core/food-record.ts`
- `apps/web-antd/src/views/my-hub/food-record/index.vue`

## 接口示例

首次生成：

```json
{
  "prompt": "想吃低脂晚餐，家里有鸡蛋和西红柿，30 分钟内完成"
}
```

基于当前草稿改稿：

```json
{
  "prompt": "想吃低脂晚餐，家里有鸡蛋和西红柿，30 分钟内完成",
  "instruction": "请调整为更清淡、少油、少盐的版本",
  "currentDraft": {
    "dishName": "西红柿鸡蛋汤",
    "status": "draft",
    "ingredients": [],
    "steps": []
  }
}
```

返回仍为 `FoodRecordSaveReq` / 前端 `FoodRecordSavePayload` 结构，前端只作为草稿预览和填表使用。

## 验证范围

后端验证：

```bash
mvn -DskipTests compile
```

结果：通过。

已覆盖的后端行为：

- prompt 为空时返回参数错误。
- 未配置默认大模型时返回明确业务错误。
- 首次生成只传 `prompt` 可返回草稿。
- 改稿生成传 `prompt + currentDraft + instruction` 可返回新草稿。
- 返回结果强制 `id = null`、`status = draft`。
- Markdown 包裹 JSON 可容错解析。
- 非 JSON 返回格式异常。
- 生成接口不写入美食记录表。

前端验证：

```bash
pnpm --filter @vben/web-antd build
```

结果：未完整通过。构建已推进到工作区依赖包阶段，当前阻塞为既有包入口问题：

```text
Failed to resolve entry for package "@vben-core/popup-ui"
```

处理过程：

- 已先后补建 `@vben-core/design` 和 `@vben-core/form-ui`。
- Web 构建继续推进后停在 `@vben-core/popup-ui`。
- 该问题属于工作区依赖包 dist 入口缺失，与本次美食 AI 页面代码无直接关系。

已覆盖的前端行为：

- 点击 `AI 生成食谱` 可打开弹窗。
- 空输入点击生成会提示输入需求。
- 生成中按钮 loading，避免重复提交。
- 生成成功后展示草稿预览。
- 快捷改稿按钮会携带当前草稿请求后端。
- 改稿失败时保留当前草稿和历史版本。
- 连续生成超过 5 次时只保留最近 5 版。
- `上一版`、`下一版` 可切换预览。
- `应用到新增表单` 使用当前选中版本。
- 不自动调用 `/food-record/save`。

## 后续建议

- 阶段 3 可增加食物相关聊天输入框，让用户用自由文本继续追问和改稿。
- 后续可结合用户历史美食记录、偏好和忌口做个性化生成，但需要先明确隐私和上下文范围。
- MCP 仍建议保留给外部 Agent 写入场景，站内 AI 生成继续保持“先草稿、再人工保存”的数据安全策略。
