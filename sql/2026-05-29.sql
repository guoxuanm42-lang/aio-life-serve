-- 2026-05-29 数据库变更（按天合并）
-- 创建时间: 2026-05-29
-- 作者: Ethan
-- 更新功能简介:
-- 1) 简化待办任务字段，补充开始时间、结束时间与完成状态
-- 2) 兼容新版待办清单按时间排序、按完成状态筛选与完成状态切换

SET NAMES utf8mb4;

-- 待办任务表字段扩展
-- 说明：start_time / end_time 用于列表展示、时间筛选与逾期判断；is_completed 用于记录未完成/已完成状态。
ALTER TABLE `task`
    ADD COLUMN `start_time` datetime DEFAULT NULL COMMENT '开始时间' AFTER `due_date`,
    ADD COLUMN `end_time` datetime DEFAULT NULL COMMENT '结束时间' AFTER `start_time`,
    ADD COLUMN `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成 0-未完成 1-已完成' AFTER `end_time`;
