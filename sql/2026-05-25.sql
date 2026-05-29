-- 2026-05-25 数据库变更（按天合并）

-- 来源：
-- - 2026-05-25_add_card_object_to_thought.sql
-- - 2026-05-25_migrate_cbti_image_object_prefix.sql
-- - 2026-05-25_move_think_menu_to_root.sql
-- - 2026-05-25_add_think_list_menu_route.sql
-- - 2026-05-25_think_menu_second_level_categories.sql

-- 思考表闪念卡图片对象键扩展
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 为 thought 表新增 card_object 字段，用于记录闪念卡图片的 MinIO 对象键（think-card/...）
ALTER TABLE `thought`
    ADD COLUMN `card_object` VARCHAR(512) NULL COMMENT 'MinIO 对象键（think-card/...）' AFTER `theme_key`;

-- CBTI 人格图片对象键前缀迁移
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 将 cbti_personality.image_object 从历史前缀（如 images/cbti/characters/SUDO.png）迁移为方案A前缀（cbti/SUDO.png）
-- 2) 保留文件名与扩展名，仅替换前缀目录
UPDATE `cbti_personality`
SET `image_object` = CONCAT('cbti/', SUBSTRING_INDEX(`image_object`, '/', -1))
WHERE `image_object` IS NOT NULL
  AND `image_object` <> ''
  AND `image_object` NOT LIKE 'cbti/%';

-- 闪念菜单提升为一级菜单（从“记录”分组中移出）
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 将 sys_menu 中“闪念”(id=1503) 从 parent_id=1500（记录）提升为 parent_id=0（一级菜单）
-- 2) 将菜单 path 从 /my-hub/think 改为 /think（避免继续归类到 /my-hub 分组）
-- 3) 调整 sort 与 meta.order，保证在侧边栏位置稳定（紧跟“记录”之后）
UPDATE `sys_menu`
SET
  `parent_id` = 0,
  `path` = '/think',
  `sort` = 4,
  `meta` = JSON_SET(
    COALESCE(`meta`, JSON_OBJECT()),
    '$.title', '闪念',
    '$.icon', 'mdi:lightbulb-on-outline',
    '$.order', 4,
    '$.backTop', false,
    '$.keepAlive', true
  )
WHERE `id` = 1503;

-- 闪念列表路由（隐藏菜单项）
-- 创建时间: 2026-05-25
-- 作者: Ethan
-- 更新功能简介:
-- 1) 在后端菜单模式下补充 /think/list 动态路由
-- 2) 作为隐藏路由（parent_id=0 + meta.hideInMenu=true），侧边栏不显示
INSERT INTO `sys_menu`
(`id`,`parent_id`,`name`,`path`,`component`,`redirect`,`meta`,`roles`,`sort`,`status`,`is_deleted`)
VALUES
(15031, 0, 'thinkList', '/think/list', 'my-hub/think/list', NULL,
 JSON_OBJECT('title','闪念列表','hideInMenu',true,'keepAlive',false),
 NULL, 0, 1, 0)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `name` = VALUES(`name`),
  `component` = VALUES(`component`),
  `redirect` = VALUES(`redirect`),
  `meta` = VALUES(`meta`),
  `roles` = VALUES(`roles`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`),
  `is_deleted` = VALUES(`is_deleted`);

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

