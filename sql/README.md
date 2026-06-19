# SQL 文件维护规范

## 一、增量 SQL 按天归档

新增数据库变更脚本统一放在 `sql/` 目录下，并按日期命名：

```text
YYYY-MM-DD.sql
```

示例：

```text
2026-05-29.sql
2026-05-30.sql
```

同一天产生的多个数据库变更必须合并到同一个日期文件中，禁止再创建类似下面这种拆分文件：

```text
2026-05-30_xxx_menu.sql
2026-05-30_xxx_field.sql
2026-05-30_xxx_index.sql
```

## 二、同一天 SQL 必须合并

如果同一天新增了表结构、索引、菜单、初始化数据等多类变更，都写入当天同一个 `YYYY-MM-DD.sql` 文件。

推荐按功能分段组织：

```sql
-- 一、表字段扩展

-- 二、新增业务表

-- 三、索引优化

-- 四、菜单或初始化数据
```

这样可以保证执行顺序清晰，也避免部署时漏执行某个同日脚本。

## 三、必须写中文注释

每个按天 SQL 文件顶部必须包含中文说明：

```sql
-- 2026-05-30 数据库变更（按天合并）
-- 创建时间: 2026-05-30
-- 作者: Ethan
-- 更新功能简介:
-- 1) 新增 xxx
-- 2) 调整 xxx
-- 3) 优化 xxx
```

每个关键 SQL 段落也必须写中文注释，说明用途和影响范围：

```sql
-- 待办任务表字段扩展
-- 说明：type_id 关联代办类型；failure_reason 仅用于已失败代办的复盘说明。
ALTER TABLE `task`
    ADD COLUMN `type_id` BIGINT DEFAULT NULL COMMENT '代办类型ID';
```

## 四、字段和表注释要求

新增表、字段、索引时，应尽量补充数据库 `COMMENT`，便于后续排查和维护：

```sql
CREATE TABLE IF NOT EXISTS `task_type` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(64) NOT NULL COMMENT '类型名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代办类型表';
```

## 五、初始化脚本说明

`sql/initial/` 目录用于维护完整初始化脚本：

- `01_schema.sql`：完整表结构
- `02_init_data.sql`：初始化数据

日常开发新增变更时，优先写入 `sql/YYYY-MM-DD.sql` 增量脚本。确认功能稳定后，再按需要同步整理到 `sql/initial/` 的初始化脚本中。
## 六、线上已执行 SQL 本地归档

`sql/executed-online/` 目录用于记录已经在服务器数据库执行过的 SQL 脚本副本，仅作为本地执行流水，不提交到 Git 仓库。

该目录已加入 `.gitignore`：

```gitignore
/sql/executed-online/
```

每次线上数据库执行 SQL 后，必须复制一份最终执行脚本到 `sql/executed-online/`，并使用递增编号区分执行顺序：

```text
001_2026-06-16_cloud_deploy_to_prod_thought_detail.sql
002_2026-06-20_xxx.sql
003_2026-07-01_xxx.sql
```

编号表示线上执行顺序，不表示业务版本号。脚本一旦标记为 `EXECUTED`，禁止修改正文；如需修正，新增下一个编号脚本。

每个已执行脚本顶部必须包含执行标记区：

```sql
-- Execute Record
-- Version: 001
-- Status: EXECUTED
-- Environment: production
-- Database: aio-life
-- ExecutedAt: 2026-06-16 13:30:00
-- ExecutedBy: Ethan
-- SourceFiles:
--   sql/2026-06-12.sql
--   sql/2026-06-12_thought_status_log.sql
-- GitBranch: prod-serve
-- GitCommit: e35b048
-- Remark: cloud-deploy-serve upgrade to prod-serve
```

`Status` 仅允许使用以下值：

```text
PENDING   已整理，未执行
EXECUTED  已在线上执行
FAILED    执行失败，需要处理
REPLACED  被后续脚本替代
SKIPPED   确认无需执行
```
