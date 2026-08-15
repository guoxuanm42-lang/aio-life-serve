-- 2026-07-20 数据库变更（按天合并）
-- 创建时间: 2026-07-20
-- 作者: Ethan
-- 更新功能简介:
-- 1) AI 对话会话绑定 Agent，已有会话默认归属生活总助理。
-- 2) 更新 conversation 兼容视图以暴露 agent_code 字段。

-- 一、会话表增加 Agent 绑定字段
SET @chat_session_agent_code_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_session'
      AND COLUMN_NAME = 'agent_code'
);

SET @chat_session_agent_code_sql = IF(
    @chat_session_agent_code_exists = 0,
    'ALTER TABLE `chat_session` ADD COLUMN `agent_code` VARCHAR(64) NOT NULL DEFAULT ''life_assistant'' COMMENT ''绑定的 AI Agent 编码'' AFTER `title`',
    'SELECT 1'
);

PREPARE stmt FROM @chat_session_agent_code_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `chat_session`
SET `agent_code` = 'life_assistant'
WHERE `agent_code` IS NULL OR TRIM(`agent_code`) = '';

-- 二、更新兼容视图
CREATE OR REPLACE VIEW `conversation` AS
SELECT
    `chat_session`.`id` AS `id`,
    `chat_session`.`user_id` AS `user_id`,
    `chat_session`.`title` AS `title`,
    `chat_session`.`agent_code` AS `agent_code`,
    `chat_session`.`create_user` AS `create_user`,
    `chat_session`.`create_time` AS `create_time`,
    `chat_session`.`update_user` AS `update_user`,
    `chat_session`.`update_time` AS `update_time`,
    `chat_session`.`is_deleted` AS `is_deleted`
FROM `chat_session`;
