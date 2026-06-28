-- 2026-06-24 database change
-- CreatedAt: 2026-06-24
-- Author: Ethan
-- Summary:
-- 1) Create article for personal Markdown article management.
-- 2) Create article_category for one-level user custom article categories.
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `article_category` (
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
    INDEX `idx_article_category_user_deleted` (`user_id`, `is_deleted`),
    INDEX `idx_article_category_sort` (`user_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article category';

CREATE TABLE IF NOT EXISTS `article` (
    `id` BIGINT NOT NULL COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `category_id` BIGINT DEFAULT NULL COMMENT 'Article category ID',
    `title` VARCHAR(200) NOT NULL COMMENT 'Article title',
    `summary` VARCHAR(1000) DEFAULT NULL COMMENT 'Article summary',
    `markdown_content` MEDIUMTEXT NOT NULL COMMENT 'Markdown source content',
    `plain_text_content` MEDIUMTEXT NOT NULL COMMENT 'Plain text content for search and AI analysis',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT 'Comma separated tags',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT 'draft/published/archived',
    `word_count` INT NOT NULL DEFAULT 0 COMMENT 'Word count computed from plain text content',
    `create_user` BIGINT DEFAULT NULL COMMENT 'Create user',
    `create_time` DATETIME DEFAULT NULL COMMENT 'Create time',
    `update_user` BIGINT DEFAULT NULL COMMENT 'Update user',
    `update_time` DATETIME DEFAULT NULL COMMENT 'Update time',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    PRIMARY KEY (`id`),
    INDEX `idx_article_user_deleted` (`user_id`, `is_deleted`),
    INDEX `idx_article_category` (`user_id`, `category_id`, `is_deleted`),
    INDEX `idx_article_status` (`user_id`, `status`, `is_deleted`),
    INDEX `idx_article_update_time` (`update_time`),
    INDEX `idx_article_word_count` (`user_id`, `word_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article';

INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`)
VALUES
(1510, 1500, 'article', '/my-hub/article', 'my-hub/article/index', NULL,
 JSON_OBJECT('icon','mdi:file-document-edit-outline','title',CONVERT(0xE69687E7ABA0 USING utf8mb4),'backTop',false,'keepAlive',true),
 NULL, 10, 1, 0)
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
