-- 2026-06-22 database change
-- CreatedAt: 2026-06-22
-- Author: Ethan
-- Summary:
-- 1) Create problem_category for one-level problem categories.
-- 2) Add category_id to problem_note.
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `problem_category` (
    `id` BIGINT NOT NULL COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `name` VARCHAR(100) NOT NULL COMMENT 'Category name',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    `create_user` BIGINT DEFAULT NULL COMMENT 'Create user',
    `create_time` DATETIME DEFAULT NULL COMMENT 'Create time',
    `update_user` BIGINT DEFAULT NULL COMMENT 'Update user',
    `update_time` DATETIME DEFAULT NULL COMMENT 'Update time',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    PRIMARY KEY (`id`),
    INDEX `idx_problem_category_user_deleted` (`user_id`, `is_deleted`),
    INDEX `idx_problem_category_sort` (`user_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Problem category';

SET @problem_note_category_column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'problem_note'
      AND COLUMN_NAME = 'category_id'
);
SET @problem_note_category_column_sql = IF(
    @problem_note_category_column_exists = 0,
    'ALTER TABLE `problem_note` ADD COLUMN `category_id` BIGINT DEFAULT NULL COMMENT ''Problem category ID'' AFTER `user_id`',
    'SELECT 1'
);
PREPARE stmt FROM @problem_note_category_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @problem_note_category_index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'problem_note'
      AND INDEX_NAME = 'idx_problem_note_category'
);
SET @problem_note_category_index_sql = IF(
    @problem_note_category_index_exists = 0,
    'CREATE INDEX `idx_problem_note_category` ON `problem_note` (`user_id`, `category_id`, `is_deleted`)',
    'SELECT 1'
);
PREPARE stmt FROM @problem_note_category_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
