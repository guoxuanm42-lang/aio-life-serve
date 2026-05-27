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
