# 美食记录后端、MCP、前端页面与图片预览更新（2026-06-01）

## 1. 背景与目标

本次更新围绕“美食记录”模块补齐一套可落地的前后端能力：用户可以记录做饭信息、材料清单、步骤流程、复盘总结和图片；系统可以按当前登录用户隔离数据，并提供统计视图与 MCP/AI 工具入口。

目标包括：

- 新增美食记录主数据、材料、步骤、图片元数据表和菜单入口。
- 后端提供记录 CRUD、详情、图片上传预览、图片排序和统计接口。
- 前端新增“美食”页面，支持卡片/表格列表、筛选、详情、编辑、图片管理和统计图表。
- MCP 工具新增美食记录保存与查询能力，支持 Agent 写入结构化做饭记录。
- 补充单元测试和 MCP 工具注册/Schema 兼容性测试。

## 2. 前端更新

### 2.1 路由与 API

- 仓库：`aio-life-front-main/aio-life-front-main`
- 路由文件：`apps/web-antd/src/router/routes/modules/my-hub.ts`
  - 在 `my-hub` 下新增一级页面 `/my-hub/food-record`。
  - 菜单图标使用 `mdi:food-fork-drink`，标题为“美食”。
- API 文件：`apps/web-antd/src/api/core/food-record.ts`
  - 新增美食记录、材料、步骤、图片、统计相关 TypeScript 类型。
  - 新增接口封装：
    - `/food-record/query`
    - `/food-record/detail`
    - `/food-record/save`
    - `/food-record/update`
    - `/food-record/delete`
    - `/food-record/image/upload`
    - `/food-record/image/update`
    - `/food-record/image/sort`
    - `/food-record/image/delete`
    - `/food-record/image/preview`
    - `/food-record/statistics`

### 2.2 美食记录页面

- 新增页面目录：`apps/web-antd/src/views/my-hub/food-record/`
- 页面文件：`index.vue`
- 功能点：
  - 顶部概览卡展示本月做饭、平均评分、平均耗时、待优化菜品。
  - 支持按关键字、分类、餐次、状态、标签和日期范围筛选。
  - 支持卡片视图和表格视图切换。
  - 支持新增、编辑、删除美食记录。
  - 表单支持维护基础信息、时间成本、材料清单、步骤流程、口味评价和复盘字段。
  - 详情弹窗展示主记录、材料、步骤、图片和复盘内容。
  - 统计页使用 ECharts 展示做饭频次、分类分布、餐次分布、菜品排行、材料排行、待优化和复做提醒。

### 2.3 图片管理与预览

- 编辑模式下支持上传图片，上传前要求先保存美食记录。
- 图片类型支持：
  - 食材图：`ingredient`
  - 过程图：`process`
  - 成品图：`finished`
  - 失败图：`failed`
  - 其他：`other`
- 前端通过 `/food-record/image/preview` 下载 Blob，并使用 `URL.createObjectURL` 生成本地预览地址。
- 页面在切换详情、编辑记录和组件卸载时会回收 Object URL，避免预览资源泄漏。
- 支持图片说明编辑、类型调整、上移下移排序和逻辑删除。

## 3. 后端更新

### 3.1 美食记录接口

- 仓库：`aio-life-serve-main/aio-life-serve-main`
- 新增控制器：`src/main/java/top/aiolife/record/api/FoodRecordController.java`
- 接口：
  - `POST /food-record/query`：分页查询当前用户美食记录。
  - `GET /food-record/detail`：查询当前用户美食记录详情。
  - `POST /food-record/save`：新增美食记录。
  - `POST /food-record/update`：编辑美食记录。
  - `POST /food-record/delete`：逻辑删除美食记录。
- 服务实现：`FoodRecordServiceImpl`
  - 按当前登录用户隔离数据。
  - 保存主记录时同步维护材料和步骤子项。
  - 编辑时采用先逻辑删除旧材料/步骤、再插入新子项的方式。
  - 支持状态：`draft`、`done`、`to_improve`、`archived`。
  - 当总耗时为空时，自动由备菜时间和烹饪时间计算。

### 3.2 图片接口

- 新增控制器：`src/main/java/top/aiolife/record/api/FoodRecordImageController.java`
- 接口：
  - `POST /food-record/image/upload`：上传图片到 MinIO 并保存元数据。
  - `POST /food-record/image/update`：更新图片类型、说明和排序。
  - `POST /food-record/image/sort`：批量更新图片排序。
  - `POST /food-record/image/delete`：逻辑删除图片元数据。
  - `GET /food-record/image/preview`：校验图片归属后输出 MinIO 图片流。
- 服务实现：`FoodRecordImageServiceImpl`
  - 上传前校验当前用户拥有目标美食记录。
  - 仅允许 `image/*` 文件。
  - 对象键格式为 `food-record/{userId}/{recordId}/{uuid}.{ext}`。
  - 支持按图片类型和排序值展示。
  - 删除只更新元数据 `is_deleted`，不物理删除 MinIO 文件。

### 3.3 统计接口

- 新增控制器：`src/main/java/top/aiolife/record/api/FoodRecordStatisticsController.java`
- 接口：
  - `GET /food-record/statistics`
- 服务实现：`FoodRecordStatisticsServiceImpl`
  - 聚合总记录数、本月记录数、平均评分、平均耗时、值得复做数量和待优化数量。
  - 统计周频次趋势、分类分布、餐次分布、状态分布。
  - 生成菜品排行、材料排行、待优化记录和复做提醒。

### 3.4 数据模型与 Mapper

- 新增实体：
  - `FoodRecordEntity`
  - `FoodRecordIngredientEntity`
  - `FoodRecordStepEntity`
  - `FoodRecordImageEntity`
- 新增请求对象：
  - `FoodRecordQueryReq`
  - `FoodRecordSaveReq`
  - `FoodRecordIngredientSaveReq`
  - `FoodRecordStepSaveReq`
  - `FoodRecordImageUpdateReq`
  - `FoodRecordImageSortReq`
- 新增返回对象：
  - `FoodRecordDetailVO`
  - `FoodRecordStatisticsVO`
- 新增 Mapper：
  - `IFoodRecordMapper`
  - `IFoodRecordIngredientMapper`
  - `IFoodRecordStepMapper`
  - `IFoodRecordImageMapper`

## 4. MCP / AI 工具更新

- 新增工具：`src/main/java/top/aiolife/mcp/tools/FoodRecordMcpTools.java`
- 新增工具请求与返回对象：
  - `FoodRecordSaveToolReq`
  - `FoodRecordQueryToolReq`
  - `FoodRecordIngredientToolReq`
  - `FoodRecordStepToolReq`
  - `FoodRecordQueryToolVO`
- 新增 MCP 工具：
  - `food_record_save`：保存美食记录文字内容，支持创建或更新。
  - `food_record_query`：查询当前用户美食记录历史摘要。
- 新增适配门面：`FoodRecordAiFacade`
  - 将 MCP 请求转换为业务保存请求。
  - 创建记录时支持 `idempotencyKey`，Redis 幂等有效期为 24 小时。
  - 查询返回适合 Agent 阅读的摘要字段，不包含图片。

## 5. SQL 更新

- 新增 SQL：`sql/2026-05-31.sql`
- 新增表：
  - `food_record`：美食记录主表。
  - `food_record_ingredient`：美食记录材料表。
  - `food_record_step`：美食记录步骤表。
  - `food_record_image`：美食记录图片元数据表。
- 新增索引：
  - 用户与删除状态索引。
  - 记录日期、状态、排序相关索引。
  - 图片按记录、用户、类型和排序查询的索引。
- 新增菜单：
  - `sys_menu` 中新增“美食”一级菜单，路径为 `/my-hub/food-record`。

## 6. 测试更新

- 新增测试：
  - `FoodRecordServiceImplTest`
  - `FoodRecordStatisticsServiceImplTest`
- 更新 MCP 测试：
  - `McpSchemaGeneratorTest`
  - `McpToolCompatibilityTest`
  - `McpToolRegistryTest`
- 覆盖点：
  - 美食记录保存校验。
  - 材料和步骤写入。
  - 主记录与子项逻辑删除。
  - 统计空数据、概览、排行和提醒聚合。
  - MCP 工具注册数量从 3 个扩展为 5 个。
  - `food_record_save` 嵌套材料和步骤 Schema 生成。
  - `food_record_query` 查询字段暴露。

## 7. 注意事项

- 当前 Git 工作区中前后端改动均未提交，本文档依据当前工作区变更整理。
- 需要先执行 `sql/2026-05-31.sql`，否则后端美食记录接口无法落库。
- 图片能力依赖 MinIO 配置和桶可用性；本次后端上传使用配置桶名，缺省值为 `aiolife`。
- MCP 保存工具的幂等依赖 Redis；Redis 不可用时会影响带 `idempotencyKey` 的重复写入保护。
- 图片删除当前只删除元数据，不清理 MinIO 对象，后续如需释放存储空间需要补充物理清理任务。
- 本次只整理文档，未额外执行编译或测试命令。
