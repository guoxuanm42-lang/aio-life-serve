-- 2026-06-12 database changes
-- author: Ethan
-- Add thought type compatibility field.

SET NAMES utf8mb4;

ALTER TABLE `thought`
    ADD COLUMN `thought_type` VARCHAR(32) NOT NULL DEFAULT 'action'
    COMMENT '闪念类型：action行动、emotion情绪、reflection复盘'
    AFTER `status`;

CREATE TABLE IF NOT EXISTS `thought_action_detail`
(
    `id`                 bigint(20) NOT NULL AUTO_INCREMENT,
    `thought_id`         bigint(20) NOT NULL COMMENT '闪念ID',
    `user_id`            bigint(20) NOT NULL COMMENT '用户ID',
    `result_summary`     text COMMENT '处理结果',
    `reflection`         text COMMENT '心得/复盘',
    `next_action`        text COMMENT '后续动作',
    `shelve_reason`      text COMMENT '搁置原因',
    `shelve_reason_tag`  varchar(64) DEFAULT NULL COMMENT '搁置原因标签',
    `restart_policy`     varchar(64) DEFAULT NULL COMMENT '是否可重启',
    `archive_reason`     text COMMENT '归档原因',
    `value_level`        varchar(64) DEFAULT NULL COMMENT '价值等级',
    `archive_type`       varchar(64) DEFAULT NULL COMMENT '沉淀类型',
    `create_time`        datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user`        bigint(20) DEFAULT NULL,
    `update_time`        datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `update_user`        bigint(20) DEFAULT NULL,
    `is_deleted`         int(11) DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_thought_action_detail_thought_user` (`thought_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪念行动详情表';

CREATE TABLE IF NOT EXISTS `thought_emotion_detail`
(
    `id`                 bigint(20) NOT NULL AUTO_INCREMENT,
    `thought_id`         bigint(20) NOT NULL COMMENT '闪念ID',
    `user_id`            bigint(20) NOT NULL COMMENT '用户ID',
    `emotion_type`       varchar(64) DEFAULT NULL COMMENT '情绪类型',
    `emotion_intensity`  int(11) DEFAULT NULL COMMENT '情绪强度1-5',
    `emotion_trigger`    text COMMENT '触发原因',
    `emotion_need`       text COMMENT '背后需求',
    `coping_action`      text COMMENT '缓解动作',
    `reflection_summary` text COMMENT '复盘结论',
    `ignored_reason`     text COMMENT '不再关注原因',
    `create_time`        datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user`        bigint(20) DEFAULT NULL,
    `update_time`        datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `update_user`        bigint(20) DEFAULT NULL,
    `is_deleted`         int(11) DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_thought_emotion_detail_thought_user` (`thought_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪念情绪详情表';

CREATE TABLE IF NOT EXISTS `thought_reflection_detail`
(
    `id`                 bigint(20) NOT NULL AUTO_INCREMENT,
    `thought_id`         bigint(20) NOT NULL COMMENT '闪念ID',
    `user_id`            bigint(20) NOT NULL COMMENT '用户ID',
    `reflection_summary` text COMMENT '复盘结论',
    `lesson_type`        varchar(64) DEFAULT NULL COMMENT '经验/教训/方法/决策',
    `archive_type`       varchar(64) DEFAULT NULL COMMENT '沉淀类型',
    `value_level`        varchar(64) DEFAULT NULL COMMENT '价值等级',
    `improvement_action` text COMMENT '改进动作',
    `related_project`    varchar(255) DEFAULT NULL COMMENT '关联项目',
    `tags`               varchar(500) DEFAULT NULL COMMENT '标签',
    `create_time`        datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user`        bigint(20) DEFAULT NULL,
    `update_time`        datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `update_user`        bigint(20) DEFAULT NULL,
    `is_deleted`         int(11) DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_thought_reflection_detail_thought_user` (`thought_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闪念复盘详情表';
