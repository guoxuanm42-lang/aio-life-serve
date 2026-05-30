# 待办清单、复盘配置与 SQL 规范更新（2026-05-31）

## 1. 背景与目标

本次围绕待办模块做了一次完整升级：从原有基础待办列表扩展为“待办清单 / 复盘 / 配置”三段式能力，同时补齐任务类型、失败原因、状态筛选、默认筛选、时间展示和 SQL 维护规范。

目标是让待办模块更适合日常使用：

- 待办清单默认聚焦今天的有效任务。
- 已失败任务进入复盘场景处理，避免混在有效任务里。
- 任务类型可配置主题、颜色和排序。
- SQL 增量脚本按天合并，便于部署和维护。

## 2. 前端更新

### 2.1 待办清单页面

- 文件：`aio-life-front-main/aio-life-front-main/apps/web-antd/src/views/task-center/todo/index.vue`
- 代办列表改为更清晰的列表式布局。
- 新增状态筛选项：`有效任务`。
  - 有效任务 = 未完成 + 已完成。
  - 不包含已失败任务。
- 默认筛选改为：
  - 状态：`有效任务`
  - 日期：`今天`
- 页面刷新、重新进入待办页、点击重置时，都会回到 `有效任务 + 今天`。
- 列表时间展示格式调整为：
  - `周六 05-30 23:59`
- 增加前端兜底过滤：
  - 即使接口异常返回失败任务，只要当前筛选为“有效任务”，页面也不会渲染 `isCompleted = 2` 的记录。

### 2.2 复盘页面

- 新增页面目录：`apps/web-antd/src/views/task-center/todo/review/`
- 用于集中处理已失败待办。
- 支持查看失败任务、填写或更新失败原因。
- 失败任务不再作为普通有效任务参与主列表处理。

### 2.3 配置页面

- 新增页面目录：`apps/web-antd/src/views/task-center/todo/config/`
- 支持维护待办类型：
  - 类型名称
  - 主题
  - 展示颜色
  - 排序
- 颜色选择增加经典色块预设。
  - 可直接点击色块填入颜色值。
  - 当前选中色块会有高亮状态。
  - 移动端做了色块换行适配。

### 2.4 前端 API 与路由

- 文件：`apps/web-antd/src/api/core/todo.ts`
  - 扩展 `Task` 字段：类型、主题、颜色、失败原因等。
  - 新增任务类型相关 API。
  - 查询参数新增 `statusGroup=valid`。
- 文件：`apps/web-antd/src/router/routes/modules/task-center.ts`
  - 待办模块新增复盘、配置子路由。

## 3. 后端更新

### 3.1 待办查询与状态处理

- 文件：`src/main/java/top/aiolife/record/api/TaskController.java`
- `/tasks` 查询接口支持：
  - `typeId`
  - `theme`
  - `isCompleted`
  - `statusGroup=valid`
  - `startDate`
  - `endDate`
  - `hasFailureReason`
- 新增有效任务查询：
  - `statusGroup=valid` 时，仅查询 `is_completed in (0, 1)`。
- 查询前会将已过期未完成任务标记为失败。
- 更新待办时按当前登录用户限定更新范围，避免越权更新。
- 非失败状态会清理失败原因，避免历史失败原因残留。

### 3.2 任务类型能力

- 新增后端文件：
  - `src/main/java/top/aiolife/record/api/TaskTypeController.java`
  - `src/main/java/top/aiolife/record/mapper/ITaskTypeMapper.java`
  - `src/main/java/top/aiolife/record/pojo/entity/TaskTypeEntity.java`
  - `src/main/java/top/aiolife/record/service/ITaskTypeService.java`
  - `src/main/java/top/aiolife/record/service/impl/TaskTypeServiceImpl.java`
- 支持当前用户的任务类型：
  - 查询
  - 新增
  - 更新
  - 删除
- 删除类型为逻辑删除，历史任务仍可展示原类型信息。

### 3.3 实体字段扩展

- 文件：`src/main/java/top/aiolife/record/pojo/entity/TaskEntity.java`
- 新增或补齐字段：
  - `typeId`
  - `failureReason`
  - `typeName`
  - `theme`
  - `typeColor`
  - `typeDeleted`
- 这些展示字段用于前端列表显示，不直接作为任务表持久字段。

## 4. 菜单与 SQL 更新

### 4.1 菜单结构

- 文件：`sql/2026-04-19_create_sys_menu.sql`
- 待办菜单调整为分组结构：
  - 待办
    - 待办清单
    - 复盘
    - 配置

### 4.2 增量 SQL

- 新增按天合并脚本：
  - `sql/2026-05-29.sql`
  - `sql/2026-05-30.sql`
- `2026-05-29.sql`：
  - 为 `task` 表补充开始时间、结束时间、完成状态。
- `2026-05-30.sql`：
  - 为 `task` 表补充 `type_id`、`failure_reason`。
  - 新增 `task_type` 表。
  - 新增任务类型与状态时间索引。
  - 合并待办清单、复盘、配置菜单变更。

### 4.3 SQL 维护规范

- 新增文档：`sql/initial/README.md`
- 明确 SQL 规范：
  - 增量 SQL 统一按 `YYYY-MM-DD.sql` 命名。
  - 同一天多个 SQL 必须合并到同一个日期文件。
  - 文件顶部必须写中文说明。
  - 关键 SQL 段落必须写中文注释。
  - 新增表和字段尽量补充数据库 `COMMENT`。

## 5. 验证情况

- 后端执行：
  - `mvn -q -DskipTests compile`
  - 使用 JDK 21 编译通过。
- 前端执行：
  - `pnpm --filter @vben/web-antd typecheck`
  - 仍被项目已有类型错误阻断，错误集中在其它模块，例如 `bilibili-video.ts`、`crypto.ts`、profile/time 页面等。
  - 本次改动文件未出现在 typecheck 报错列表中。

## 6. 注意事项

- 若线上或本地已执行过拆分 SQL，不要重复执行合并后的同日 SQL，避免重复加字段或重复建索引。
- 当前 `ALTER TABLE ADD COLUMN` 与 `CREATE INDEX` 没有做字段/索引存在性判断，执行前需要确认数据库当前版本。
- 后端运行中的旧进程不会自动加载新代码，涉及 `statusGroup=valid` 等接口行为时需要重启后端服务。
