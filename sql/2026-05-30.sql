-- 2026-05-30 数据库变更（按天合并）
-- 创建时间: 2026-05-30
-- 作者: Ethan
-- 更新功能简介:
-- 1) 新增代办类型表 task_type，支持类型名称、主题、颜色、排序与逻辑删除
-- 2) 为 task 表新增 type_id 与 failure_reason 字段，支持类型筛选与失败复盘
-- 3) 调整待办菜单结构，待办下包含“待办清单 / 复盘 / 配置”
-- 4) 新增常用查询索引，优化待办类型、状态与截止时间筛选

SET NAMES utf8mb4;

-- 一、待办任务表字段扩展
-- 说明：type_id 关联代办类型；failure_reason 仅用于已失败代办的复盘说明。
ALTER TABLE `task`
    ADD COLUMN `type_id` BIGINT DEFAULT NULL COMMENT '代办类型ID' AFTER `column_id`,
    ADD COLUMN `failure_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因，仅失败代办使用' AFTER `is_completed`;

-- 二、代办类型表
-- 说明：用户可自定义类型，并为类型配置主题、颜色与排序值。
CREATE TABLE IF NOT EXISTS `task_type` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(64) NOT NULL COMMENT '类型名称',
    `theme` VARCHAR(64) DEFAULT NULL COMMENT '所属主题',
    `color` VARCHAR(32) DEFAULT NULL COMMENT '展示颜色',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除(0-否,1-是)',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_task_type_user_deleted` (`user_id`, `is_deleted`),
    INDEX `idx_task_type_theme` (`theme`),
    INDEX `idx_task_type_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代办类型表';

-- 三、待办相关索引
-- 说明：type_id 用于类型筛选；is_completed + end_time 用于有效任务、失败任务和日期范围查询。
CREATE INDEX `idx_task_type_id` ON `task` (`type_id`);
CREATE INDEX `idx_task_completed_end_time` ON `task` (`is_completed`, `end_time`);

-- 四、待办菜单结构调整
-- 说明：恢复“待办”为分组菜单，子菜单包含待办清单、复盘、配置。
UPDATE `sys_menu`
SET
    `parent_id` = 0,
    `name` = 'TaskCenter',
    `path` = '/task-center',
    `component` = 'BasicLayout',
    `redirect` = '/task-center/todo',
    `meta` = JSON_OBJECT('icon', 'mdi:format-list-checks', 'title', '待办', 'order', 1),
    `sort` = 1,
    `status` = 1,
    `update_time` = NOW()
WHERE `id` = 1200
  AND `is_deleted` = 0;

-- 待办清单子菜单
UPDATE `sys_menu`
SET
    `parent_id` = 1200,
    `name` = 'TaskCenterTodo',
    `path` = '/task-center/todo',
    `component` = 'task-center/todo/index',
    `redirect` = NULL,
    `meta` = JSON_OBJECT('icon', 'mdi:format-list-checks', 'title', '待办清单', 'order', 0),
    `sort` = 0,
    `status` = 1,
    `update_time` = NOW()
WHERE `id` = 1201
  AND `is_deleted` = 0;

-- 复盘子菜单
INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`, `create_time`, `update_time`)
VALUES
(1202, 1200, 'TaskCenterTodoReview', '/task-center/todo/review', 'task-center/todo/review/index', NULL,
 JSON_OBJECT('icon', 'mdi:clipboard-alert-outline', 'title', '复盘', 'order', 1),
 NULL, 1, 1, 0, NOW(), NOW())
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

-- 配置子菜单
INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`, `create_time`, `update_time`)
VALUES
(1203, 1200, 'TaskCenterTodoConfig', '/task-center/todo/config', 'task-center/todo/config/index', NULL,
 JSON_OBJECT('icon', 'mdi:cog-outline', 'title', '配置', 'order', 2),
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
