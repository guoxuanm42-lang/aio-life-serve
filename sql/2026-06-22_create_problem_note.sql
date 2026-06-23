-- 2026-06-22 database change
-- CreatedAt: 2026-06-22
-- Author: Ethan
-- Summary:
-- 1) Create problem_note for manually saved problems, Java code, and idea notes.
-- 2) Add the Problem menu under the Record module.
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `problem_note` (
    `id` BIGINT NOT NULL COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `title` VARCHAR(200) NOT NULL COMMENT 'Problem title',
    `problem_content` MEDIUMTEXT NOT NULL COMMENT 'Problem content',
    `solution_code` MEDIUMTEXT DEFAULT NULL COMMENT 'Java solution code',
    `idea_note` MEDIUMTEXT DEFAULT NULL COMMENT 'Idea note',
    `difficulty` VARCHAR(30) DEFAULT NULL COMMENT 'Difficulty',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT 'Comma separated tags',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT 'draft/solved/reviewing/archived',
    `create_user` BIGINT DEFAULT NULL COMMENT 'Create user',
    `create_time` DATETIME DEFAULT NULL COMMENT 'Create time',
    `update_user` BIGINT DEFAULT NULL COMMENT 'Update user',
    `update_time` DATETIME DEFAULT NULL COMMENT 'Update time',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    PRIMARY KEY (`id`),
    INDEX `idx_problem_note_user_deleted` (`user_id`, `is_deleted`),
    INDEX `idx_problem_note_status` (`status`),
    INDEX `idx_problem_note_difficulty` (`difficulty`),
    INDEX `idx_problem_note_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Problem note';

INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`)
VALUES
(1509, 1500, 'problemNote', '/my-hub/problem-note', 'my-hub/problem-note/index', NULL,
 JSON_OBJECT('icon','mdi:code-tags','title',CONVERT(0xE9A298E79BAE USING utf8mb4),'backTop',false,'keepAlive',true),
 NULL, 9, 1, 0)
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
