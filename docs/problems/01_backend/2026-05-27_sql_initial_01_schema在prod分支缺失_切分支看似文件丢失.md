# 2026-05-27_sql_initial_01_schema在prod分支缺失_切分支看似文件丢失

## 1. 现象

- 本地曾存在 `sql/initial/01_schema.sql`，但切换到 `prod-serve` 后文件“消失”。
- 执行 `git ls-files | findstr 01_schema.sql` 找不到该文件。

---

## 2. 根因

- `prod-serve` 分支当前的提交历史中未包含 `sql/initial/01_schema.sql`（未被该分支跟踪）。
- 该文件可能只在其它分支/提交中存在，切换分支时工作区会被检出为目标分支版本，因此表现为“文件不见了”。

---

## 3. 解决方法

（待补充）

