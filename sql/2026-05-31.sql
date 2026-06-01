-- 2026-05-31 数据库变更（按天合并）
-- 创建时间: 2026-05-31
-- 作者: Ethan
-- 更新功能简介:
-- 1) 新增美食记录主表 food_record，用于保存做饭基础信息、时间信息、口味评价和总结复盘
-- 2) 新增美食记录材料表 food_record_ingredient，用于保存每次做饭的材料清单
-- 3) 新增美食记录步骤表 food_record_step，用于保存每次做饭的步骤流程
SET NAMES utf8mb4;

-- 一、美食记录主表
-- 说明：第一阶段只保存文字结构化数据，图片能力后续阶段单独建表扩展。
CREATE TABLE IF NOT EXISTS `food_record` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `dish_name` VARCHAR(100) NOT NULL COMMENT '菜名',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类，如家常菜/早餐/甜品等',
    `meal_type` VARCHAR(50) DEFAULT NULL COMMENT '餐次，如早餐/午餐/晚餐/夜宵',
    `cook_date` DATE DEFAULT NULL COMMENT '做饭日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft-草稿，done-已完成，to_improve-待优化，archived-已归档',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签，第一阶段以逗号分隔或 JSON 字符串保存',
    `difficulty` VARCHAR(30) DEFAULT NULL COMMENT '难度',
    `rating` DECIMAL(3,1) DEFAULT NULL COMMENT '评分',
    `success_level` VARCHAR(30) DEFAULT NULL COMMENT '成功程度',
    `prep_minutes` INT DEFAULT NULL COMMENT '备菜时间（分钟）',
    `cook_minutes` INT DEFAULT NULL COMMENT '烹饪时间（分钟）',
    `total_minutes` INT DEFAULT NULL COMMENT '总耗时（分钟）',
    `taste_description` TEXT DEFAULT NULL COMMENT '口味描述',
    `problems` TEXT DEFAULT NULL COMMENT '问题或不足',
    `summary` TEXT DEFAULT NULL COMMENT '本次总结',
    `brief_summary` VARCHAR(255) DEFAULT NULL COMMENT '一句话总结，用于列表展示',
    `next_improve` TEXT DEFAULT NULL COMMENT '下次改进',
    `worth_redo` TINYINT(1) DEFAULT NULL COMMENT '是否值得复做：1-是，0-否',
    `next_try_suggestion` TEXT DEFAULT NULL COMMENT '下次尝试建议',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    INDEX `idx_food_record_user_deleted` (`user_id`, `is_deleted`),
    INDEX `idx_food_record_status` (`status`),
    INDEX `idx_food_record_cook_date` (`cook_date`),
    INDEX `idx_food_record_user_date` (`user_id`, `cook_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美食记录主表';

-- 二、美食记录材料表
-- 说明：每条美食记录可包含多条材料，材料随主记录按用户隔离并支持逻辑删除。
CREATE TABLE IF NOT EXISTS `food_record_ingredient` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_id` BIGINT NOT NULL COMMENT '美食记录ID',
    `name` VARCHAR(100) NOT NULL COMMENT '材料名称',
    `quantity` VARCHAR(50) DEFAULT NULL COMMENT '数量',
    `unit` VARCHAR(30) DEFAULT NULL COMMENT '单位',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    INDEX `idx_food_ingredient_record` (`record_id`, `is_deleted`),
    INDEX `idx_food_ingredient_user` (`user_id`, `is_deleted`),
    INDEX `idx_food_ingredient_sort` (`record_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美食记录材料表';

-- 三、美食记录步骤表
-- 说明：每条美食记录可包含多条步骤，编辑时第一阶段采用先逻辑删除旧步骤再插入新步骤的方式。
CREATE TABLE IF NOT EXISTS `food_record_step` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_id` BIGINT NOT NULL COMMENT '美食记录ID',
    `step_no` INT DEFAULT NULL COMMENT '步骤序号',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '步骤标题',
    `description` TEXT DEFAULT NULL COMMENT '步骤描述',
    `duration_minutes` INT DEFAULT NULL COMMENT '步骤耗时（分钟）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    INDEX `idx_food_step_record` (`record_id`, `is_deleted`),
    INDEX `idx_food_step_user` (`user_id`, `is_deleted`),
    INDEX `idx_food_step_sort` (`record_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美食记录步骤表';

-- 四、美食记录图片表
-- 说明：保存美食记录图片元数据，图片文件本体存储在 MinIO，第一版删除仅做逻辑删除。
CREATE TABLE IF NOT EXISTS `food_record_image` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_id` BIGINT NOT NULL COMMENT '美食记录ID',
    `bucket_name` VARCHAR(100) NOT NULL COMMENT 'MinIO 桶名',
    `object_key` VARCHAR(500) NOT NULL COMMENT 'MinIO 对象键',
    `image_type` VARCHAR(30) NOT NULL DEFAULT 'other' COMMENT '图片类型：ingredient-食材图，process-过程图，finished-成品图，failed-失败图，other-其他',
    `caption` VARCHAR(255) DEFAULT NULL COMMENT '图片说明',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    INDEX `idx_food_image_record` (`record_id`, `is_deleted`),
    INDEX `idx_food_image_user` (`user_id`, `is_deleted`),
    INDEX `idx_food_image_type_sort` (`record_id`, `image_type`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美食记录图片表';

-- 五、美食菜单
-- 说明：当前前端使用后端菜单模式，将美食作为一级菜单展示，页面仍复用美食记录模块。
INSERT INTO `sys_menu`
(`id`, `parent_id`, `name`, `path`, `component`, `redirect`, `meta`, `roles`, `sort`, `status`, `is_deleted`)
VALUES
(1508, 0, 'foodRecord', '/my-hub/food-record', 'my-hub/food-record/index', NULL,
 JSON_OBJECT('icon','mdi:food-fork-drink','title',CAST(UNHEX('E7BE8EE9A39F') AS CHAR CHARACTER SET utf8mb4),'backTop',false),
 NULL, 5, 1, 0)
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
