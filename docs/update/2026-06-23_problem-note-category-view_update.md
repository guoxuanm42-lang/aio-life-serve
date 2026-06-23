# 题目模块、Markdown 展示与分类首页更新

## 背景

原有“记录”模块下已经新增“题目”入口，用于手动保存题目内容、Java 解法代码和思路备注。随着题目数量增加，直接进入所有题目列表会逐渐拥挤，不利于按算法类型复习，因此本次继续补充一层简单分类，并优化题目正文和思路备注的阅读体验。

## 解决的问题

- 解决题目只能平铺展示，无法按字符串、数组、哈希、排序等类型归类的问题。
- 解决进入题目模块后列表过于拥挤的问题，改为先进入分类首页。
- 解决题目正文和思路备注只能纯文本展示，Markdown 标题、列表、行内代码无法正常渲染的问题。
- 保留 Java 解法代码独立展示、Java 高亮、复制代码和字体缩放能力。

## 功能使用说明

进入“记录 > 题目”后，默认展示分类卡片首页：

- `全部题目`：查看当前用户所有题目。
- `未分类`：查看未设置分类的题目。
- 自定义分类：例如字符串、数组、哈希、排序等。

点击分类卡片后进入该分类下的题目视图：

- 左侧展示当前分类下的题目列表。
- 右侧展示题目详情。
- 顶部可以点击“返回分类”回到分类首页。
- 在普通分类内新增题目时，会默认归属当前分类。
- 在“全部题目”内新增题目时，默认未分类。
- 编辑题目时可以切换所属分类，或清空为未分类。

题目详情支持：

- 题目内容 Markdown 渲染。
- 思路备注 Markdown 渲染。
- Java 代码 highlight.js 高亮。
- 一键复制题目原始 Markdown。
- 一键复制 Java 代码。
- 题目/思路正文字号独立调节。
- Java 代码字号独立调节。

## 本次调整

### 后端

新增题目分类能力：

- 新增 `problem_category` 表。
- `problem_note` 新增 `category_id` 字段。
- 新增 `/problem-category/list` 接口，用于查询分类列表、全部题目数量和未分类题目数量。
- 新增 `/problem-category/save` 接口，用于新增分类。
- 新增 `/problem-category/update` 接口，用于修改分类名称和排序。
- 新增 `/problem-category/delete` 接口，用于逻辑删除分类。
- 删除分类时不删除题目，只把该分类下题目的 `category_id` 置空。
- `problem-note` 查询、新增、更新接口支持 `categoryId`。
- `problem-note` 查询支持 `uncategorized = true`，用于查询未分类题目。

新增和调整的主要后端文件：

- `src/main/java/top/aiolife/record/api/ProblemCategoryController.java`
- `src/main/java/top/aiolife/record/service/IProblemCategoryService.java`
- `src/main/java/top/aiolife/record/service/impl/ProblemCategoryServiceImpl.java`
- `src/main/java/top/aiolife/record/mapper/IProblemCategoryMapper.java`
- `src/main/java/top/aiolife/record/pojo/entity/ProblemCategoryEntity.java`
- `src/main/java/top/aiolife/record/pojo/req/ProblemCategorySaveReq.java`
- `src/main/java/top/aiolife/record/pojo/vo/ProblemCategoryListVO.java`
- `src/main/java/top/aiolife/record/pojo/vo/ProblemCategoryVO.java`
- `src/main/java/top/aiolife/record/pojo/entity/ProblemNoteEntity.java`
- `src/main/java/top/aiolife/record/pojo/req/ProblemNoteQueryReq.java`
- `src/main/java/top/aiolife/record/pojo/req/ProblemNoteSaveReq.java`
- `src/main/java/top/aiolife/record/service/impl/ProblemNoteServiceImpl.java`

### 前端

题目页面从“直接进入题目列表”调整为“分类首页卡片视图 + 分类内题目视图”。

新增和调整的主要前端文件：

- `apps/web-antd/src/api/core/problem-category.ts`
- `apps/web-antd/src/api/core/problem-note.ts`
- `apps/web-antd/src/views/my-hub/problem-note/ProblemNoteCategoryPage.vue`
- `apps/web-antd/src/views/my-hub/problem-note/index.vue`
- `apps/web-antd/src/router/routes/modules/my-hub.ts`

前端行为：

- 页面内部使用状态切换分类首页和题目列表，不新增动态路由。
- `index.vue` 保持为题目模块入口，包装实际页面组件。
- 分类首页使用卡片布局，减少题目列表入口的拥挤感。
- 分类内继续沿用左侧列表、右侧详情的阅读结构。
- 新增/编辑题目弹窗增加“所属分类”下拉选择。
- 题目内容和思路备注使用 `marked` + `dompurify` 渲染 Markdown。
- Java 代码继续使用 `highlight.js`，只注册 Java 语言。

### 数据库

新增普通迁移脚本：

- `sql/2026-06-22_create_problem_note.sql`
- `sql/2026-06-22_create_problem_category.sql`

新增线上执行草稿：

- `sql/executed-online/003_2026-06-22_pending_problem_note.sql`
- `sql/executed-online/004_2026-06-22_pending_problem_category.sql`

本地数据库已执行分类脚本：

```sql
CREATE TABLE IF NOT EXISTS problem_category (...);

ALTER TABLE problem_note
  ADD COLUMN category_id BIGINT DEFAULT NULL COMMENT 'Problem category ID';

CREATE INDEX idx_problem_note_category
ON problem_note (user_id, category_id, is_deleted);
```

实际执行后已确认：

- `problem_category` 表存在。
- `problem_note.category_id` 字段存在。
- `idx_problem_note_category` 索引存在。

## 问题修复

- 修复访问题目模块时报错：

```text
Table 'aio-life.problem_category' doesn't exist
```

原因是代码已经上线到本地运行环境，但数据库还没有执行 `problem_category` 建表脚本。

处理方式：

- 执行 `sql/executed-online/004_2026-06-22_pending_problem_category.sql`。
- 验证表、字段和索引已经创建成功。

## 验证范围

后端验证：

```bash
mvn -DskipTests compile
```

结果：通过。

数据库验证：

```sql
SHOW TABLES LIKE 'problem_category';
SHOW COLUMNS FROM problem_note LIKE 'category_id';
SHOW INDEX FROM problem_note WHERE Key_name = 'idx_problem_note_category';
```

结果：表、字段、索引均存在。

前端验证：

```bash
pnpm --filter @vben/web-antd build
```

结果：失败，仍为既有 `@vben-core/design` 包入口解析问题：

```text
Failed to resolve entry for package "@vben-core/design"
```

该问题与本次题目分类页面代码无关。

```bash
pnpm --filter @vben/web-antd typecheck
```

结果：失败，仍为项目既有大量类型错误；输出中没有新增的 `problem-note` 分类页面错误。

## 后续建议

- 如果题目数量继续增加，可以在分类内增加“按状态/难度快速筛选”的更紧凑工具栏。
- 分类目前只有名称和排序，暂不做描述、颜色、图标和多级分类，避免复杂度过早上升。
- 后续可以增加“题目移动分类”的批量操作，但第一阶段先保持单题编辑归类。
- SQL 草稿正式在线执行后，建议把 `pending` 文件更新为实际执行记录。
