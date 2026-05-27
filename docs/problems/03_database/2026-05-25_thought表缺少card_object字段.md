<!--
-- thought 表缺少 card_object 字段
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 修复闪念列表/详情查询时报 Unknown column 'card_object' 的数据库字段缺失问题
-->

# thought 表缺少 card_object 字段

## 1. 问题现象
页面打开闪念模块或查询闪念列表时报错（后端日志/前端提示均可见）：

- `Unknown column 'card_object' in 'field list'`

## 2. 发生场景
后端实体/查询已开始读取 `thought.card_object`，但数据库还未执行对应的增量脚本。

相关字段映射：
- [ThoughtEntity.java](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/src/main/java/top/aiolife/record/pojo/entity/ThoughtEntity.java)

## 3. 原因分析
代码先改了（实体/查询包含 `card_object`），但数据库表结构未同步更新，导致 SQL 执行失败。

## 4. 解决方案
执行增量 SQL，为 `thought` 表补充字段：
- [2026-05-25_add_card_object_to_thought.sql](file:///d:/my_document/program_product/life_os/aio-life-serve-main/aio-life-serve-main/sql/2026-05-25_add_card_object_to_thought.sql)

核心语句：
- `ALTER TABLE thought ADD COLUMN card_object VARCHAR(512) NULL ...`

## 5. 验证方法
- 执行：
  - `SHOW COLUMNS FROM thought LIKE 'card_object';`
- 重新打开闪念页面/刷新列表，确认不再报 `Unknown column 'card_object'`。

## 6. 注意事项
- 本项目闪念相关字段新增通常会先改实体/接口，再补 SQL；升级时要保证 SQL 已在目标库执行完成。
