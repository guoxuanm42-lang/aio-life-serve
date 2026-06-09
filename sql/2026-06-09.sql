-- 2026-06-09 database changes
-- author: Ethan
-- Split coding dashboards from research management menu.

SET NAMES utf8mb4;

UPDATE `sys_menu`
SET
    `component` = 'BasicLayout',
    `redirect` = '/coding/mcp-tools',
    `meta` = JSON_OBJECT('icon', 'lucide:wrench', 'title', '研发管理', 'order', 3, 'keepAlive', true),
    `sort` = 3,
    `status` = 1,
    `is_deleted` = 0,
    `update_time` = NOW()
WHERE `id` = 1400;

INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`, `create_time`, `update_time`)
VALUES
(1450, 0, 'CodingDashboard', '/coding-dashboard', 'BasicLayout', '/coding/github',
 JSON_OBJECT('icon', 'lucide:code-2', 'title', '编程看板', 'order', 4, 'keepAlive', true),
 NULL, 4, 1, 0, NOW(), NOW())
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
    `is_deleted` = VALUES(`is_deleted`),
    `update_time` = NOW();

INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`, `create_time`, `update_time`)
VALUES
(1403, 1400, 'McpTools', '/coding/mcp-tools', 'coding/mcp-tools/index', NULL,
 JSON_OBJECT('icon', 'lucide:box', 'title', 'MCP 工具列表', 'order', 0),
 NULL, 0, 1, 0, NOW(), NOW())
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
    `is_deleted` = VALUES(`is_deleted`),
    `update_time` = NOW();

UPDATE `sys_menu`
SET
    `parent_id` = 1450,
    `sort` = CASE `id`
        WHEN 1401 THEN 0
        WHEN 1402 THEN 1
        WHEN 1404 THEN 2
        ELSE `sort`
    END,
    `update_time` = NOW()
WHERE `id` IN (1401, 1402, 1404);
