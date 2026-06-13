-- 2026-06-12 thought status log
-- author: Ethan

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `thought_status_log`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT,
    `thought_id`    bigint(20) NOT NULL COMMENT 'thought id',
    `user_id`       bigint(20) NOT NULL COMMENT 'user id',
    `thought_type`  varchar(32) DEFAULT NULL COMMENT 'thought type',
    `from_status`   varchar(32) DEFAULT NULL COMMENT 'from status',
    `to_status`     varchar(32) NOT NULL COMMENT 'to status',
    `change_reason` text COMMENT 'change reason',
    `create_time`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `create_user`   bigint(20) DEFAULT NULL COMMENT 'create user',
    `update_time`   datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `update_user`   bigint(20) DEFAULT NULL COMMENT 'update user',
    `is_deleted`    int(11) DEFAULT '0' COMMENT 'deleted flag',
    PRIMARY KEY (`id`),
    KEY `idx_thought_status_log_thought_user` (`thought_id`, `user_id`),
    KEY `idx_thought_status_log_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='thought status log';
