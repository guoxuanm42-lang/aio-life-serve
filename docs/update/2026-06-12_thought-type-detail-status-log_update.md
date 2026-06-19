# 闪念多类型、结构化详情与状态流转日志

## 背景

原有闪念模块主要按单一“想法/行动”记录设计，所有记录共用同一套状态语义。随着使用场景扩展，闪念开始承载行动事项、情绪心情、复盘沉淀等不同内容。如果继续只依赖 `status`、`content` 和 `theme_key`，会导致两类问题：

- 情绪、复盘类记录被错误计入行动积压。
- 后续 AI 分析只能读取原始正文，缺少可解释、可聚合的结构化字段。

本次更新将闪念升级为统一入口下的多类型记录体系，并补充结构化详情、状态日志和类型统计能力。

## 解决的问题

本次更新主要解决闪念模块从“单一行动记录”扩展到“多类型记录”后的使用和分析问题：

- 解决情绪、复盘类内容被误算进行动积压的问题。
- 解决不同类型闪念共用同一套状态文案，导致语义不准确的问题。
- 解决 AI 后续分析只能依赖原始正文，缺少结构化字段的问题。
- 解决用户编辑闪念时字段过多、一次性铺开、填写负担大的问题。
- 解决状态变化只保存最终状态，无法追踪生命周期的问题。
- 解决手机端编辑弹窗过长、按钮不易点击、保存操作不方便的问题。

## 功能使用说明

### 选择闪念类型

新建或编辑闪念时，可以选择类型：

- 想法行动：用于待办、任务、想法执行。
- 情绪心情：用于记录沮丧、生气、焦虑、开心、灵感等情绪。
- 复盘沉淀：用于记录经验、教训、方法、决策依据等长期内容。

旧记录默认归为“想法行动”。

### 按类型查看记录

闪念页面支持切换：

- 全部记录
- 想法行动
- 情绪心情
- 复盘沉淀
- 统计洞察

进入三类业务视图时，系统会自动只展示对应类型的闪念。

### 使用状态动作流转

编辑已有闪念时，不再直接选择所有状态，而是根据当前状态显示可执行动作。

以想法行动为例：

- 待处理时可以：开始处理、标记完成、搁置
- 已完成时可以：归档、重新打开
- 已归档时可以：重新处理

点击动作后，系统会显示“将变更为”的目标状态，并允许填写状态变化原因。

### 按流程填写字段

编辑弹窗不会再一次性展示全部结构化字段。

未选择动作时，只填写基础内容：

- 想法行动：主题、内容、分类
- 情绪心情：感受、情绪类型、情绪强度
- 复盘沉淀：复盘主题、复盘内容

选择具体动作后，才展示当前流程需要的字段。

例如：

- 标记完成：填写处理结果、心得/复盘、后续动作
- 搁置：填写搁置原因、搁置标签、是否可重启
- 沉淀为经验：填写复盘结论、背后需求、缓解动作

### 查看状态时间线

详情接口会返回状态流转日志，后续详情页可以展示：

- 从什么状态变更到什么状态
- 变更原因
- 变更时间

这为后续生命周期分析和 AI 总结提供依据。

### 统计洞察变化

统计中的积压数现在只统计“想法行动”类型的待处理和进行中记录，情绪和复盘不会再污染行动积压。

统计页也增加三类状态分区，可以分别查看行动、情绪、复盘的状态分布。

## 本次调整

### 类型兼容层

- `thought` 表新增 `thought_type` 字段，支持三类闪念：
  - `action`：想法行动
  - `emotion`：情绪心情
  - `reflection`：复盘沉淀
- 旧数据默认归为 `action`，不需要人工迁移。
- 后端 `ThoughtEntity`、保存、更新、查询接口支持 `thoughtType`。
- 前端列表增加类型筛选：
  - 全部
  - 想法行动
  - 情绪心情
  - 复盘沉淀
- 新增/编辑弹窗支持选择闪念类型。

### 类型化状态文案

底层状态值仍保持：

```text
pending / ongoing / done / shelved / archived
```

前端根据 `thoughtType` 展示不同语义：

- `action`：待处理 / 进行中 / 已完成 / 已搁置 / 已归档
- `emotion`：已记录 / 待观察 / 已缓解 / 不再关注 / 已沉淀
- `reflection`：待整理 / 整理中 / 已沉淀 / 暂不整理 / 已归档

这样保持数据库状态兼容，同时避免情绪类记录显示“已完成”这类任务化文案。

### 结构化详情表

新增三张详情表：

- `thought_action_detail`
- `thought_emotion_detail`
- `thought_reflection_detail`

行动型详情字段包括：

- 处理结果
- 心得/复盘
- 后续动作
- 搁置原因
- 搁置原因标签
- 是否可重启
- 归档原因
- 价值等级
- 沉淀类型

情绪型详情字段包括：

- 情绪类型
- 情绪强度
- 触发原因
- 背后需求
- 缓解动作
- 复盘结论
- 不再关注原因

复盘沉淀型详情字段包括：

- 复盘结论
- 经验/教训/方法/决策
- 沉淀类型
- 价值等级
- 改进动作
- 关联项目
- 标签

详情读取新增：

```text
GET /thought/{id}/detail
```

列表查询仍保持轻量，不返回三类详情。

### 状态流转日志

新增 `thought_status_log` 表，用于记录闪念生命周期：

- `thought_id`
- `user_id`
- `thought_type`
- `from_status`
- `to_status`
- `change_reason`
- `create_time`

写入规则：

- 新建闪念时写入初始状态日志，`from_status = null`。
- 更新闪念时，只有 `status` 发生变化才写入日志。
- 删除闪念时，同步逻辑删除当前用户对应的状态日志。

详情接口返回 `statusLogs`，为后续展示生命周期和 AI 分析保留数据基础。

### 统计口径调整

原有总览保留大部分全量口径，但积压数调整为行动积压：

```text
thought_type = action
AND status IN (pending, ongoing)
```

避免情绪心情、复盘沉淀污染行动积压数据。

`ThoughtStatisticsVO` 新增 `typeSummaries`，按三类闪念分别返回：

- 类型总数
- 状态分布
- 行动型积压数
- 完成数
- 搁置数
- 归档数
- 转化率

趋势接口新增可选 `thoughtType` 参数；不传时保持原全量趋势行为。

### 前端统计页

统计洞察页新增“三类状态分区”：

- 行动型展示总数、积压、完成、搁置、归档、转化率和状态分布。
- 情绪型展示已记录、待观察、已缓解、不再关注、已沉淀。
- 复盘型展示待整理、整理中、已沉淀、暂不整理、已归档。

原有总览卡片、状态分布图、分类分布图和趋势分析继续保留。

### 前端流程化编辑体验

闪念编辑弹窗从“按类型展示全部字段”调整为“按当前状态动作展示必要字段”。

动作按钮视觉调整：

- 未选中动作统一使用白底、浅灰边框、深灰文字。
- 只有当前选中的动作高亮。
- 风险动作未选中时不显示红色底色，选中后才显示红色浅底。
- 当前状态保持只读展示，例如：`当前状态：待处理`。
- 选择动作后展示目标状态和状态变化原因，例如：`将变更为：已完成`。

行动型编辑器调整：

- 未选择动作时只显示基础字段：
  - 主题
  - 内容
  - 分类
- 关联事件默认折叠，只显示 `关联事件 N 个`，展开后才编辑事件流。
- 点击不同动作后只显示当前流程字段：
  - 开始处理 / 重新打开 / 重新处理：后续动作
  - 标记完成：处理结果、心得/复盘、后续动作
  - 搁置：搁置原因、搁置原因标签、是否可重启
  - 归档：归档原因、价值等级、沉淀类型

情绪型编辑器调整：

- 未选择动作时只显示基础字段：
  - 我现在的感受
  - 情绪类型
  - 情绪强度
- 点击不同动作后只显示当前流程字段：
  - 继续观察：发生了什么、背后需求
  - 已缓解：缓解动作、复盘结论
  - 不再关注：不再关注原因
  - 沉淀为经验：复盘结论、背后需求、缓解动作

复盘型编辑器调整：

- 未选择动作时只显示基础字段：
  - 复盘主题
  - 复盘内容
- 点击不同动作后只显示当前流程字段：
  - 开始整理：经验/教训/方法/决策
  - 标记沉淀：复盘结论、沉淀类型、价值等级、改进动作
  - 暂不整理：只使用父级状态变化原因，不额外展示结构化字段
  - 归档：价值等级、沉淀类型、标签
  - 重新整理：改进动作

移动端编辑体验调整：

- 编辑弹窗在小屏幕下使用近似全屏体验：
  - 宽度 `100vw`
  - 高度 `100dvh`
  - 内容区域独立滚动
  - 底部操作区固定
- 表单在移动端全部改为单列布局。
- 表单 label、输入内容、类型按钮、动作按钮、保存按钮放大点击区域。
- 低频字段不再默认铺满页面，按动作出现或折叠展示。

### MCP 工具同步升级

闪念 MCP 工具同步适配多类型和结构化详情。

`thought_save` 新增入参：

- `thoughtType`：闪念类型，支持 `action` / `emotion` / `reflection`，不传默认 `action`。
- `changeReason`：状态变化原因，用于状态流转日志。
- `createTime` / `recordTime` / `happenedAt`：闪念创建时间或实际发生时间。
- `updateTime`：闪念更新时间。
- `events[].eventTime`：关联事件发生时间。
- `actionDetail`：行动型结构化详情。
- `emotionDetail`：情绪型结构化详情。
- `reflectionDetail`：复盘沉淀型结构化详情。

`thought_query` 新增能力：

- 支持按 `thoughtType` 查询。
- 返回 `thoughtType` 和 `thoughtTypeName`。
- `statusName` 按闪念类型返回不同中文文案。

Agent 调用规则：

- 普通想法、行动、待办类记录可以不传 `thoughtType`，默认保存为 `action`。
- 情绪心情类记录必须传 `thoughtType=emotion`，并尽量填写 `emotionDetail`。
- 复盘沉淀类记录必须传 `thoughtType=reflection`，并尽量填写 `reflectionDetail`。
- 服务端只保存当前 `thoughtType` 对应的 detail，避免多类型结构化字段混写。

## 数据库执行记录

本次本地数据库已补充执行：

```sql
ALTER TABLE thought
  ADD COLUMN thought_type VARCHAR(32) NOT NULL DEFAULT 'action'
  COMMENT 'thought type: action emotion reflection'
  AFTER status;

CREATE INDEX idx_thought_user_type_status
ON thought (user_id, thought_type, status);
```

并已创建：

- `thought_action_detail`
- `thought_emotion_detail`
- `thought_reflection_detail`
- `thought_status_log`

其中旧 `thought` 数据已全部默认归为 `action`。

## 问题修复

- 修复 `Unknown column 'thought_type' in 'field list'`：
  - 原因是后端代码已查询 `thought_type`，但数据库未执行第一阶段字段迁移。
  - 已补执行 `thought_type` 字段和联合索引。
- 修复 `No static resource thought/{id}/detail`：
  - 原因是前端已调用详情接口，但后端 Controller 缺少 `GET /thought/{id}/detail` 映射，且服务未重启到新代码。
  - 已补充 Controller 映射并重启后端。
- 修复详情接口后续潜在缺表问题：
  - 已补建三张结构化详情表。

## 验证范围

- 后端执行：

```bash
mvn -DskipTests compile
```

结果：通过。

- 数据库验证：
  - `thought.thought_type` 字段存在。
  - `idx_thought_user_type_status` 索引存在。
  - 三张详情表存在。
  - `thought_status_log` 表和索引存在。
- 接口验证：
  - `GET /api/thought/{id}/detail` 已映射到 `ThoughtController#detail(Long)`。
  - 未登录直接请求返回 401，说明接口已进入 Controller 鉴权流程，不再是静态资源 404。
- 前端验证：
  - `git diff --check` 通过。
  - `pnpm --filter @vben/web-antd typecheck` 仍因仓库既有文件失败，失败项不包含 `my-hub/think` 本次新增或修改文件。
- MCP 验证：
  - 使用 JDK 21 执行 `mvn -DskipTests compile` 通过。
  - 执行 `mvn -Dtest=McpSchemaGeneratorTest test` 通过。
  - 执行 `mvn -Dtest=McpToolRegistryTest test` 通过。
  - `thought_save` 已支持 `thoughtType/changeReason/createTime/updateTime/recordTime/happenedAt/actionDetail/emotionDetail/reflectionDetail`。
  - `thought_save` 事件已支持 `eventTime`。
  - `thought_query` 已支持 `thoughtType` 筛选，并返回类型和类型化状态文案。

## 后续建议

- 将当前损坏的 `sql/2026-06-12.sql` 中文注释修复为 UTF-8，避免后续整文件执行失败。
- 后续可以在状态动作上补充更细的必填校验，例如归档时要求填写价值等级，搁置时要求填写搁置原因。
- 后续如果 Agent 需要读取完整结构化详情，可以新增 `thought_detail` MCP 工具。
- AI 分析优先读取 `thought_type`、三类 detail 表和 `thought_status_log`，再辅助读取原始正文。
