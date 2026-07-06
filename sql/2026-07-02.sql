-- 2026-07-02 database changes
-- Author: Ethan
-- Photo album phase 1 and phase 2 schema/menu changes.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `photo_folder` (
    `id` BIGINT NOT NULL COMMENT 'primary id',
    `user_id` BIGINT NOT NULL COMMENT 'user id',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'parent folder id, root is 0',
    `name` VARCHAR(100) NOT NULL COMMENT 'folder name',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'sort order',
    `cover_image_id` BIGINT DEFAULT NULL COMMENT 'manual cover image id',
    `create_user` BIGINT DEFAULT NULL COMMENT 'create user',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_user` BIGINT DEFAULT NULL COMMENT 'update user',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT 'deleted flag, 0 no, 1 yes',
    PRIMARY KEY (`id`),
    INDEX `idx_photo_folder_user_parent` (`user_id`, `parent_id`, `is_deleted`),
    INDEX `idx_photo_folder_sort` (`parent_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='photo album folder';

CREATE TABLE IF NOT EXISTS `photo_image` (
    `id` BIGINT NOT NULL COMMENT 'primary id',
    `user_id` BIGINT NOT NULL COMMENT 'user id',
    `folder_id` BIGINT NOT NULL COMMENT 'folder id',
    `bucket_name` VARCHAR(100) NOT NULL COMMENT 'MinIO bucket name',
    `object_key` VARCHAR(500) NOT NULL COMMENT 'MinIO object key',
    `original_filename` VARCHAR(255) DEFAULT NULL COMMENT 'original filename',
    `content_type` VARCHAR(100) DEFAULT NULL COMMENT 'file MIME type',
    `file_size` BIGINT DEFAULT NULL COMMENT 'file size bytes',
    `title` VARCHAR(100) DEFAULT NULL COMMENT 'image title',
    `caption` VARCHAR(255) DEFAULT NULL COMMENT 'image caption',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'sort order',
    `create_user` BIGINT DEFAULT NULL COMMENT 'create user',
    `create_time` DATETIME DEFAULT NULL COMMENT 'create time',
    `update_user` BIGINT DEFAULT NULL COMMENT 'update user',
    `update_time` DATETIME DEFAULT NULL COMMENT 'update time',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT 'deleted flag, 0 no, 1 yes',
    PRIMARY KEY (`id`),
    INDEX `idx_photo_image_folder_time` (`folder_id`, `is_deleted`, `create_time`),
    INDEX `idx_photo_image_user_folder` (`user_id`, `folder_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='photo album image';

SET @add_photo_folder_cover_image_id = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `photo_folder` ADD COLUMN `cover_image_id` BIGINT DEFAULT NULL COMMENT ''manual cover image id'' AFTER `sort_order`',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'photo_folder'
      AND COLUMN_NAME = 'cover_image_id'
);
PREPARE stmt FROM @add_photo_folder_cover_image_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_photo_image_title = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE `photo_image` ADD COLUMN `title` VARCHAR(100) DEFAULT NULL COMMENT ''image title'' AFTER `file_size`',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'photo_image'
      AND COLUMN_NAME = 'title'
);
PREPARE stmt FROM @add_photo_image_title;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`, `create_time`, `update_time`)
VALUES
(1511, 1500, 'photoAlbum', '/my-hub/photo-album', 'my-hub/photo-album/index', NULL,
 JSON_OBJECT('icon','mdi:image-album','title',CONVERT(0xE79BB8E5868C USING utf8mb4),'backTop',false),
 NULL, 9, 1, 0, NOW(), NOW())
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
