-- 2026-08-14 AI 活动总结生成与消息幂等
-- 作者：Ethan

ALTER TABLE `chat_message`
    ADD COLUMN `source_type` VARCHAR(64) NULL COMMENT '消息来源类型' AFTER `model_name`,
    ADD COLUMN `idempotency_key` VARCHAR(64) NULL COMMENT '幂等键' AFTER `source_type`,
    ADD UNIQUE KEY `uk_chat_message_activity_summary`
        (`user_id`, `conversation_id`, `source_type`, `idempotency_key`, `role`);

CREATE TABLE IF NOT EXISTS `ai_activity_summary_generation` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话 ID',
    `idempotency_key` VARCHAR(64) NOT NULL COMMENT '请求幂等键',
    `period` VARCHAR(16) NOT NULL COMMENT '统计周期：week/month/year',
    `status` VARCHAR(16) NOT NULL COMMENT 'PROCESSING/GENERATED/SUCCESS/FAILED',
    `user_message` TEXT NULL COMMENT '保存到会话的用户总结指令',
    `content` LONGTEXT NULL COMMENT '模型生成的原始总结',
    `model_name` VARCHAR(100) NULL COMMENT '模型名称',
    `agent_code` VARCHAR(64) NULL COMMENT 'Agent 编码',
    `agent_name` VARCHAR(100) NULL COMMENT 'Agent 名称',
    `user_message_id` BIGINT NULL COMMENT '用户消息 ID',
    `assistant_message_id` BIGINT NULL COMMENT '助手消息 ID',
    `error_message` VARCHAR(1000) NULL COMMENT '最近失败原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_summary_request` (`user_id`, `conversation_id`, `idempotency_key`),
    KEY `idx_activity_summary_conversation` (`conversation_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 活动总结生成任务';
