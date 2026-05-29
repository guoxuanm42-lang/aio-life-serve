-- 2026-05-23 数据库变更（按天合并）

-- 来源：
-- - 2026-05-23_add_theme_key_to_thought.sql
-- - 2026-05-23_add_subject_to_thought.sql

-- 思考表主题色扩展
-- 创建时间: 2026-05-23
-- 作者: Ethan
-- 更新功能简介:
-- 1) 为 thought 表新增 theme_key 字段，用于记录每条闪念卡片的主题色（预设 key）
ALTER TABLE `thought`
    ADD COLUMN `theme_key` VARCHAR(20) NULL COMMENT '主题色 key (blue/cyan/green/purple/pink/orange)' AFTER `user_id`;

-- 变更目的：为 thought 表新增 subject 字段，用于记录闪念卡片的主题内容（卡片标题）
-- 变更人：Ethan
-- 创建时间：2026-05-23
-- 变更点：新增字段 subject
ALTER TABLE `thought`
    ADD COLUMN `subject` VARCHAR(200) NULL COMMENT '主题内容' AFTER `theme_key`;

