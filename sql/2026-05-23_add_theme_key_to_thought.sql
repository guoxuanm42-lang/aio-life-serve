-- 思考表主题色扩展
-- 创建时间: 2026-05-23
-- 作者: Ethan
-- 更新功能简介:
-- 1) 为 thought 表新增 theme_key 字段，用于记录每条闪念卡片的主题色（预设 key）

ALTER TABLE `thought`
    ADD COLUMN `theme_key` VARCHAR(20) NULL COMMENT '主题色 key (blue/cyan/green/purple/pink/orange)' AFTER `user_id`;

