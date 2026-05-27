-- 变更目的：为 thought 表新增 subject 字段，用于记录闪念卡片的主题内容（卡片标题）
-- 变更人：Ethan
-- 创建时间：2026-05-23
-- 变更点：新增字段 subject

ALTER TABLE `thought`
    ADD COLUMN `subject` VARCHAR(200) NULL COMMENT '主题内容' AFTER `theme_key`;

