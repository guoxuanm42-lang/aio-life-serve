-- 2026-06-29 数据库变更（按天合并）
-- 创建时间: 2026-06-29
-- 作者: Ethan
-- 更新功能简介:
-- 1) 新增 AI Agent 配置表，用于维护系统默认 Agent 和用户覆盖配置。
-- 2) 新增 AI 长期记忆表，用于按用户和 Agent 管理可注入聊天上下文的长期记忆。
-- 3) 初始化默认 AI Agent 配置，兼容当前通用聊天入口。

-- 一、AI Agent 配置表
-- 说明：user_id=0 表示系统默认配置；普通用户可按 user_id + code 保存覆盖配置。
CREATE TABLE IF NOT EXISTS `ai_agent_config` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '用户 ID，0 表示系统默认配置',
  `code` VARCHAR(64) NOT NULL COMMENT 'Agent 编码',
  `name` VARCHAR(128) DEFAULT NULL COMMENT 'Agent 名称',
  `description` VARCHAR(512) DEFAULT NULL COMMENT 'Agent 描述',
  `model_key_id` VARCHAR(64) DEFAULT NULL COMMENT '大模型 Key ID',
  `system_prompt` TEXT DEFAULT NULL COMMENT '系统提示词',
  `enabled_tools` TEXT DEFAULT NULL COMMENT '启用工具 JSON',
  `memory_scope` TEXT DEFAULT NULL COMMENT '记忆范围 JSON',
  `max_context_messages` INT NOT NULL DEFAULT 10 COMMENT '最大上下文消息数',
  `max_memory_items` INT NOT NULL DEFAULT 0 COMMENT '最大记忆条数',
  `temperature` DECIMAL(4,2) DEFAULT NULL COMMENT '模型温度',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_agent_config_user_code` (`user_id`, `code`),
  KEY `idx_ai_agent_config_code` (`code`),
  KEY `idx_ai_agent_config_user_enabled` (`user_id`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Agent 配置表';

-- 二、默认 AI Agent 初始化数据
-- 说明：默认配置不绑定模型 Key，运行时使用当前用户默认大模型 Key。
INSERT IGNORE INTO `ai_agent_config`
(`id`, `user_id`, `code`, `name`, `description`, `model_key_id`, `system_prompt`, `enabled_tools`, `memory_scope`, `max_context_messages`, `max_memory_items`, `temperature`, `enabled`, `is_deleted`)
VALUES
(2060306280000000001, 0, 'life_assistant', '生活总助理', '负责 AIO-LIFE 通用生活助理对话。', NULL, '你是 AIO-LIFE 的生活总助理，回答应简洁、准确，并结合用户的生活管理场景。', '[]', '[]', 10, 0, NULL, 1, 0),
(2060306280000000002, 0, 'thought_assistant', '闪念整理助手', '负责闪念、想法、情绪和复盘整理。', NULL, '你是 AIO-LIFE 的闪念整理助手，帮助用户澄清想法、沉淀复盘，并保持记录结构清晰。', '[]', '[]', 10, 0, NULL, 1, 0),
(2060306280000000003, 0, 'food_assistant', '美食记录助手', '负责美食记录、菜谱和饮食偏好相关对话。', NULL, '你是 AIO-LIFE 的美食记录助手，帮助用户整理菜谱、复盘口味，并避免在未确认时写入记录。', '[]', '[]', 10, 0, NULL, 1, 0),
(2060306280000000004, 0, 'study_assistant', '题库学习助手', '负责题库、刷题、学习复盘相关对话。', NULL, '你是 AIO-LIFE 的题库学习助手，帮助用户分析题目、总结知识点和制定复习计划。', '[]', '[]', 10, 0, NULL, 1, 0),
(2060306280000000005, 0, 'writing_assistant', '文章写作助手', '负责文章写作、结构优化和表达润色。', NULL, '你是 AIO-LIFE 的文章写作助手，帮助用户搭建结构、优化表达，并保留用户原本的观点和风格。', '[]', '[]', 10, 0, NULL, 1, 0),
(2060306280000000006, 0, 'coding_assistant', '研发助手', '负责研发管理、代码问题和技术复盘相关对话。', NULL, '你是 AIO-LIFE 的研发助手，回答应务实、具体，优先结合项目已有架构和工程约束。', '[]', '[]', 10, 0, NULL, 1, 0);

-- 三、AI 长期记忆表
-- 说明：聊天时按 user_id、agent_code 和 enabled 筛选可注入的长期记忆。
CREATE TABLE IF NOT EXISTS `ai_memory` (
  `id` BIGINT NOT NULL COMMENT '主键 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `agent_code` VARCHAR(64) NOT NULL COMMENT 'Agent 编码',
  `memory_type` VARCHAR(32) NOT NULL COMMENT '记忆类型',
  `memory_key` VARCHAR(128) NOT NULL COMMENT '记忆键',
  `memory_value` TEXT NOT NULL COMMENT '记忆内容',
  `source_type` VARCHAR(32) DEFAULT NULL COMMENT '来源类型',
  `source_id` VARCHAR(64) DEFAULT NULL COMMENT '来源业务 ID',
  `importance` INT NOT NULL DEFAULT 50 COMMENT '重要度',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_ai_memory_user_agent` (`user_id`, `agent_code`),
  KEY `idx_ai_memory_type` (`user_id`, `agent_code`, `memory_type`),
  KEY `idx_ai_memory_enabled` (`user_id`, `agent_code`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 长期记忆表';
