-- 思考表闪念卡图片对象键扩展
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 为 thought 表新增 card_object 字段，用于记录闪念卡图片的 MinIO 对象键（think-card/...）

ALTER TABLE `thought`
    ADD COLUMN `card_object` VARCHAR(512) NULL COMMENT 'MinIO 对象键（think-card/...）' AFTER `theme_key`;

