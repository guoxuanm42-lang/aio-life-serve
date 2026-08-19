-- 2026-08-15 活动总结结构化报告快照
-- 作者：Ethan

ALTER TABLE `ai_activity_summary_generation`
    ADD COLUMN `context_json` LONGTEXT NULL COMMENT '生成报告使用的结构化统计快照' AFTER `user_message`,
    ADD KEY `idx_activity_summary_assistant_message` (`user_id`, `assistant_message_id`);
