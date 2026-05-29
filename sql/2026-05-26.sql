-- 2026-05-26 数据库变更（按天合并）

-- 来源：
-- - 2026-05-26_add_status_to_thought.sql
-- - 2026-05-26_swap_think_theme_key_for_life_study_work.sql

-- 思考表状态扩展
-- 创建时间: 2026-05-26
-- 作者: Ethan
-- 更新功能简介:
-- 1) 为 thought 表新增 status 字段，用于记录闪念状态（pending/ongoing/done/archived）
ALTER TABLE `thought`
    ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态(pending/ongoing/done/archived)' AFTER `subject`;

-- 可选：加索引（列表页常用 user_id + status 过滤时更快）
-- CREATE INDEX `idx_thought_user_status` ON `thought` (`user_id`, `status`);

UPDATE thought
SET theme_key = CASE theme_key
  WHEN 'blue' THEN 'cyan'
  WHEN 'cyan' THEN 'green'
  WHEN 'green' THEN 'blue'
  ELSE theme_key
END;

