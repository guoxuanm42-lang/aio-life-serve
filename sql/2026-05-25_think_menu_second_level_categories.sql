-- 闪念：一级菜单 + 7个二级分类菜单
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 将“闪念”(id=1503) 调整为父级菜单：/think + BasicLayout + redirect=/think/all
-- 2) 新增 7 个二级菜单（全部 + 工作/生活/学习/社交/创作/旅行），点击后进入同一列表页并按类型筛选
-- 3) 二级菜单 path 为 /think/{categoryKey}，component 统一为 my-hub/think/list

SET NAMES utf8mb4;

UPDATE `sys_menu`
SET
  `parent_id` = 0,
  `name` = 'think',
  `path` = '/think',
  `component` = 'BasicLayout',
  `redirect` = '/think/all',
  `sort` = 3,
  `meta` = JSON_SET(
    COALESCE(`meta`, JSON_OBJECT()),
    '$.title', CONVERT(0xE997AAE5BFB5 USING utf8mb4),
    '$.icon', 'mdi:lightbulb-on-outline',
    '$.order', 3,
    '$.backTop', false,
    '$.keepAlive', true
  )
WHERE `id` = 1503;

INSERT INTO `sys_menu`
(`id`,`parent_id`,`name`,`path`,`component`,`redirect`,`meta`,`roles`,`sort`,`status`,`is_deleted`)
VALUES
(15030, 1503, 'thinkAll', '/think/all', 'my-hub/think/list', NULL,
 JSON_OBJECT('title',CONVERT(0xE585A8E983A8 USING utf8mb4),'keepAlive',true,'backTop',false),
 NULL, 0, 1, 0),
(15031, 1503, 'thinkWork', '/think/work', 'my-hub/think/list', NULL,
 JSON_OBJECT('title',CONVERT(0xE5B7A5E4BD9C USING utf8mb4),'keepAlive',true,'backTop',false),
 NULL, 1, 1, 0),
(15032, 1503, 'thinkLife', '/think/life', 'my-hub/think/list', NULL,
 JSON_OBJECT('title',CONVERT(0xE7949FE6B4BB USING utf8mb4),'keepAlive',true,'backTop',false),
 NULL, 2, 1, 0),
(15033, 1503, 'thinkStudy', '/think/study', 'my-hub/think/list', NULL,
 JSON_OBJECT('title',CONVERT(0xE5ADA6E4B9A0 USING utf8mb4),'keepAlive',true,'backTop',false),
 NULL, 3, 1, 0),
(15034, 1503, 'thinkSocial', '/think/social', 'my-hub/think/list', NULL,
 JSON_OBJECT('title',CONVERT(0xE7A4BEE4BAA4 USING utf8mb4),'keepAlive',true,'backTop',false),
 NULL, 4, 1, 0),
(15035, 1503, 'thinkCreation', '/think/creation', 'my-hub/think/list', NULL,
 JSON_OBJECT('title',CONVERT(0xE5889BE4BD9C USING utf8mb4),'keepAlive',true,'backTop',false),
 NULL, 5, 1, 0),
(15036, 1503, 'thinkTravel', '/think/travel', 'my-hub/think/list', NULL,
 JSON_OBJECT('title',CONVERT(0xE69785E8A18C USING utf8mb4),'keepAlive',true,'backTop',false),
 NULL, 6, 1, 0)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `name` = VALUES(`name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `redirect` = VALUES(`redirect`),
  `meta` = VALUES(`meta`),
  `roles` = VALUES(`roles`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`),
  `is_deleted` = VALUES(`is_deleted`);
