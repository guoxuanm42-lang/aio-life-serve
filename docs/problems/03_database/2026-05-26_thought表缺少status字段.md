<!--
-- thought 表缺少 status 字段
-- 创建时间: 2026-05-26
-- 作者: Ethan
-- 更新功能简介:
-- 1) 修复闪念保存/更新时报 Unknown column 'status' 的数据库字段缺失问题
-->

# thought 表缺少 status 字段

## 1. 问题现象
在闪念弹窗点击保存后报错：

- `Unknown column 'status' in 'field list'`

常见出现在：
- MyBatis 更新语句包含 `status=?` 时（例如 `UPDATE thought SET ... status=?, ...`）

## 2. 发生场景
代码已增加闪念状态字段（status）并参与保存/更新，但数据库还没执行新增字段的增量 SQL。

## 3. 原因分析
后端实体字段与数据库表结构不同步：代码已经向 `thought.status` 写入或查询，但表中不存在该列。

## 4. 解决方案
执行增量 SQL，为 `thought` 表补充字段：
- [2026-05-26_add_status_to_thought.sql](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/sql/2026-05-26_add_status_to_thought.sql)

字段约定：
- `status VARCHAR(20) NOT NULL DEFAULT 'pending'`
- 允许值：`pending/ongoing/done/archived`

## 5. 验证方法
- 执行：
  - `SHOW COLUMNS FROM thought LIKE 'status';`
- 再次编辑闪念并保存，确认不再报 `Unknown column 'status'`。

## 6. 注意事项
- 这类问题优先排查：是否执行了对应日期的增量 SQL，且执行到正确的库（`aio-life`）。
