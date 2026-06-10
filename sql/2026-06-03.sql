-- 2026-06-03 database changes
-- author: Ethan
-- 1) Add MCP tool management menu under /coding.
-- 2) Add MCP tool operation config and call audit log tables.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `mcp_tool_config` (
  `id` BIGINT NOT NULL COMMENT 'Primary key',
  `tool_name` VARCHAR(128) NOT NULL COMMENT 'MCP tool name',
  `display_name` VARCHAR(128) DEFAULT NULL COMMENT 'Display name',
  `group_name` VARCHAR(64) DEFAULT NULL COMMENT 'Group name',
  `description_override` VARCHAR(1024) DEFAULT NULL COMMENT 'Description override',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Enabled flag',
  `write_operation` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Write operation flag',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT 'Remark',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mcp_tool_config_tool_name` (`tool_name`),
  KEY `idx_mcp_tool_config_group_sort` (`group_name`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP tool operation config';

ALTER TABLE `mcp_tool_config`
  ADD COLUMN IF NOT EXISTS `write_operation` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Write operation flag' AFTER `enabled`;

CREATE TABLE IF NOT EXISTS `mcp_tool_call_log` (
  `id` BIGINT NOT NULL COMMENT 'Primary key',
  `tool_name` VARCHAR(128) NOT NULL COMMENT 'MCP tool name',
  `user_id` BIGINT DEFAULT NULL COMMENT 'Caller user id',
  `arguments_summary` VARCHAR(2000) DEFAULT NULL COMMENT 'Masked argument summary',
  `success` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Success flag',
  `error_message` VARCHAR(1000) DEFAULT NULL COMMENT 'Error message',
  `duration_ms` BIGINT NOT NULL DEFAULT 0 COMMENT 'Duration milliseconds',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_mcp_tool_call_log_tool_time` (`tool_name`, `create_time`),
  KEY `idx_mcp_tool_call_log_user_time` (`user_id`, `create_time`),
  KEY `idx_mcp_tool_call_log_success_time` (`success`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP tool call audit log';

UPDATE `sys_menu`
SET
    `status` = 1,
    `meta` = JSON_OBJECT('icon', 'lucide:wrench', 'title', '研发管理', 'order', 3),
    `update_time` = NOW()
WHERE `id` = 1400;

INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`, `create_time`, `update_time`)
VALUES
(1403, 1400, 'McpTools', '/coding/mcp-tools', 'coding/mcp-tools/index', NULL,
 JSON_OBJECT('icon', 'lucide:box', 'title', 'MCP 工具列表', 'order', 2),
 NULL, 2, 1, 0, NOW(), NOW())
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
