-- AIO Life full database schema
-- Generated on 2026-05-07
-- Source directory: sql
-- Original SQL files are kept unchanged.
-- Execute this file once on a fresh MySQL database environment.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ============================================================
-- Source: 2026-03-11_create_table.sql
-- ============================================================


-- 创建库
create database if not exists `aio-life`;

-- 切换库
use  `aio-life`;

CREATE TABLE IF NOT EXISTS `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `username` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(255) NOT NULL COMMENT '密码',
                        `nickname` varchar(50) NOT NULL COMMENT '昵称',
                        `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
                        `avatar` varchar(100) DEFAULT NULL,
                        `password_salt` varchar(32) DEFAULT NULL COMMENT '密码盐值',
                        `role` varchar(50) DEFAULT 'user' COMMENT '角色类型',
                        `introduction` varchar(255) DEFAULT NULL COMMENT '个人简介',
                        `is_deleted` tinyint(4) NOT NULL DEFAULT '0',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `user_bind` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `user_id` bigint(20) NOT NULL COMMENT '本系统用户ID',
                             `platform` varchar(32) NOT NULL COMMENT '平台类型：github, leetcode, shanbay',
                             `platform_username` varchar(128) DEFAULT NULL COMMENT '第三方平台的用户名/账号',
                             `access_token` text COMMENT '访问令牌',
                             `meta_fields` json DEFAULT NULL COMMENT '额外配置(JSON)',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
                             `create_user` bigint(20) NOT NULL COMMENT '创建人',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `update_user` bigint(20) NOT NULL COMMENT '更新人',
                             `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户第三方账号绑定表';

CREATE TABLE IF NOT EXISTS `b_video` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `title` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频标题',
                           `url` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'B站视频URL',
                           `cover` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频封面URL',
                           `duration` int(11) NOT NULL COMMENT '视频时长（秒）',
                           `watched_duration` int(11) NOT NULL DEFAULT '0' COMMENT '观看时长',
                           `episodes` int(11) DEFAULT '1' COMMENT '总集数',
                           `current_episode` int(11) DEFAULT '1' COMMENT '当前观看集数',
                           `progress` decimal(5,2) DEFAULT '0.00' COMMENT '观看进度（百分比）',
                           `status` int(11) DEFAULT NULL COMMENT '学习状态',
                           `last_watched` datetime DEFAULT NULL COMMENT '最后观看时间',
                           `added_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
                           `notes` text COLLATE utf8mb4_unicode_ci COMMENT '学习笔记',
                           `bvid` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'BV号',
                           `aid` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'AV号',
                           `description` text COLLATE utf8mb4_unicode_ci COMMENT '视频描述',
                           `owner_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                           `pages_info` json DEFAULT NULL COMMENT '分集信息：cid-分集ID, page-页码, part-分集标题, duration-分集时长',
                           `user_id` bigint(20) DEFAULT NULL COMMENT '用户 ID',
                           `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `is_deleted` int(11) DEFAULT '0' COMMENT '是否删除',
                           PRIMARY KEY (`id`),
                           KEY `idx_status` (`status`),
                           KEY `idx_progress` (`progress`),
                           KEY `idx_added_at` (`added_at`),
                           KEY `idx_bvid` (`bvid`),
                           KEY `idx_aid` (`aid`),
                           KEY `idx_title` (`title`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='B 站视频记录表';

CREATE TABLE IF NOT EXISTS `device` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                          `name` varchar(255) NOT NULL COMMENT '设备名称',
                          `type` varchar(255) DEFAULT NULL COMMENT '设备类型',
                          `status` varchar(255) DEFAULT NULL COMMENT '设备状态',
                          `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                          `purchase_date` varchar(255) DEFAULT NULL COMMENT '购买日期',
                          `purchase_price` decimal(10,2) DEFAULT NULL COMMENT '购买价格',
                          `purchase_place` varchar(255) DEFAULT NULL COMMENT '购买地点',
                          `purchase_company` varchar(255) DEFAULT NULL COMMENT '购买公司',
                          `image` varchar(255) DEFAULT NULL COMMENT '图片',
                          `order_number` varchar(255) DEFAULT NULL COMMENT '订单号',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

CREATE TABLE IF NOT EXISTS `enum_type` (
                             `type_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '类型ID',
                             `type_name` varchar(50) NOT NULL COMMENT '类型名称（英文唯一标识）',
                             `description` varchar(255) DEFAULT NULL COMMENT '类型描述',
                             `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                             PRIMARY KEY (`type_id`),
                             UNIQUE KEY `type_name` (`type_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='枚举类型表';

CREATE TABLE IF NOT EXISTS `exercise_record` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `exercise_type_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运动类型',
                                   `exercise_date` date NOT NULL COMMENT '运动日期',
                                   `exercise_count` int(11) NOT NULL DEFAULT '0',
                                   `description` text COLLATE utf8mb4_unicode_ci COMMENT '运动描述',
                                   `create_user` bigint(20) NOT NULL COMMENT '创建用户',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `update_user` bigint(20) DEFAULT NULL COMMENT '更新用户',
                                   `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '是否删除（0：未删除，1：已删除）',
                                   `time_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '时间ID',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运动记录表';

CREATE TABLE IF NOT EXISTS `expense` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '账单ID',
                           `amt` decimal(10,2) NOT NULL COMMENT '花费金额',
                           `transaction_amt` decimal(10,2) DEFAULT NULL COMMENT '交易金额',
                           `exp_type_id` int(11) NOT NULL COMMENT '支出类型ID',
                           `pay_type_id` int(11) DEFAULT NULL COMMENT '支付类型ID',
                           `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                           `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
                           `exp_time` datetime NOT NULL COMMENT '支出时间',
                           `is_deleted` int(11) DEFAULT '0' COMMENT '是否删除',
                           `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `exp_desc` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                           `transaction_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                           `counterparty` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                           `counterparty_acct` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                           `merchant_order_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家订单号',
                           `transaction_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '交易状态',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='支出表';

CREATE TABLE IF NOT EXISTS `income` (
                          `income_id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `amt` decimal(10,2) NOT NULL COMMENT '收入',
                          `inc_date` date NOT NULL COMMENT '收入时间',
                          `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                          `user_id` bigint(20) NOT NULL COMMENT '用户id',
                          `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
                          `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
                          `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `is_deleted` int(11) DEFAULT '0' COMMENT '是否删除',
                          `inc_type_id` int(11) NOT NULL COMMENT '收入类型',
                          `tax` decimal(10,2) DEFAULT NULL,
                          PRIMARY KEY (`income_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收入表';

CREATE TABLE IF NOT EXISTS `login_log` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT,
                             `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
                             `username` varchar(50) NOT NULL COMMENT '用户名',
                             `password` varchar(255) DEFAULT NULL COMMENT '密码 密码错误时存储',
                             `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `ip_address` varchar(45) NOT NULL COMMENT '创建时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志';

CREATE TABLE IF NOT EXISTS `memo` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                        `title` varchar(100) DEFAULT NULL,
                        `content` text COMMENT '备忘录内容',
                        `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
                        `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                        `update_user` bigint(20) DEFAULT NULL COMMENT '更新人',
                        `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                        `is_deleted` int(11) DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备忘录表';

CREATE TABLE IF NOT EXISTS `milestone` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                             `title` varchar(255) NOT NULL COMMENT '标题',
                             `description` text COMMENT '详细描述',
                             `date` date NOT NULL COMMENT '开始日期',
                             `end_date` date DEFAULT NULL COMMENT '结束日期',
                             `type` varchar(50) NOT NULL DEFAULT 'other' COMMENT '类型: work, study, life, other',
                             `tags` json DEFAULT NULL COMMENT '标签数组',
                             `create_user` bigint(20) NOT NULL COMMENT '创建人',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_user` bigint(20) NOT NULL COMMENT '更新人',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '是否删除',
                             PRIMARY KEY (`id`),
                             KEY `idx_user_date` (`user_id`,`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='里程碑表';

CREATE TABLE IF NOT EXISTS `performance` (
                               `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一标识',
                               `performance_name` varchar(100) NOT NULL COMMENT '演出名称',
                               `performer` varchar(50) DEFAULT NULL,
                               `performance_type` varchar(50) NOT NULL COMMENT '演出类型(演唱会/话剧/音乐会等)',
                               `performance_date` datetime NOT NULL COMMENT '演出日期',
                               `city` varchar(50) NOT NULL COMMENT '演出城市',
                               `venue` varchar(100) NOT NULL COMMENT '演出地点',
                               `ticket_price` decimal(10,2) NOT NULL COMMENT '票价',
                               `seat_info` varchar(100) DEFAULT NULL COMMENT '座位信息',
                               `duration` int(11) DEFAULT NULL COMMENT '演出时长(分钟)',
                               `rating` tinyint(4) DEFAULT NULL COMMENT '演出评分(1-5)',
                               `review` text COMMENT '演出评价',
                               `image_url` varchar(255) DEFAULT NULL COMMENT '演出海报/票根图片链接',
                               `purchase_platform` varchar(50) DEFAULT NULL COMMENT '购票平台',
                               `order_number` varchar(50) DEFAULT NULL COMMENT '购票订单号',
                               `create_by` int(11) DEFAULT NULL COMMENT '创建人',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_by` int(11) DEFAULT NULL COMMENT '修改人',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='演出表';

CREATE TABLE IF NOT EXISTS `performance_record` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一标识',
                                      `performance_name` varchar(100) NOT NULL COMMENT '演出名称',
                                      `performance_type` varchar(50) NOT NULL COMMENT '演出类型(演唱会/话剧/音乐会等)',
                                      `performance_date` datetime NOT NULL COMMENT '演出日期',
                                      `city` varchar(50) NOT NULL COMMENT '演出城市',
                                      `venue` varchar(100) NOT NULL COMMENT '演出地点',
                                      `ticket_price` decimal(10,2) NOT NULL COMMENT '票价',
                                      `seat_info` varchar(100) DEFAULT NULL COMMENT '座位信息',
                                      `duration` int(11) DEFAULT NULL COMMENT '演出时长(分钟)',
                                      `rating` tinyint(4) DEFAULT NULL COMMENT '演出评分(1-5)',
                                      `review` text COMMENT '演出评价',
                                      `image_url` varchar(255) DEFAULT NULL COMMENT '演出海报/票根图片链接',
                                      `purchase_platform` varchar(50) DEFAULT NULL COMMENT '购票平台',
                                      `order_number` varchar(50) DEFAULT NULL COMMENT '购票订单号',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='演出记录表';

CREATE TABLE IF NOT EXISTS `sys_dict_data` (
                                 `dict_code` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
                                 `dict_id` bigint(20) NOT NULL,
                                 `dict_sort` int(4) DEFAULT '0' COMMENT '字典排序',
                                 `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
                                 `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
                                 `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
                                 `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
                                 `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
                                 `is_default` char(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
                                 `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
                                 `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                 PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

CREATE TABLE IF NOT EXISTS `sys_dict_type` (
                                 `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
                                 `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
                                 `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
                                 `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
                                 `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                 PRIMARY KEY (`dict_id`),
                                 UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS `task` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                        `content` varchar(100) DEFAULT NULL,
                        `detail` varchar(1000) DEFAULT NULL,
                        `column_id` int(11) DEFAULT NULL,
                        `due_date` datetime DEFAULT NULL COMMENT '最后时间',
                        `sort_order` int(11) DEFAULT NULL COMMENT '排序字段',
                        `is_deleted` int(11) NOT NULL DEFAULT '0',
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

CREATE TABLE IF NOT EXISTS `task_column` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '列ID',
                               `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                               `title` varchar(255) NOT NULL COMMENT '列标题',
                               `sort_order` int(11) NOT NULL COMMENT '排序',
                               `bg_color` varchar(10) DEFAULT NULL,
                               `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删除,1-已删除)',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务列表';

CREATE TABLE IF NOT EXISTS `task_detail` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `task_id` bigint(20) NOT NULL COMMENT '关联任务ID',
                               `content` varchar(255) NOT NULL COMMENT '明细任务内容',
                               `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成 0-未完成 1-已完成',
                               `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序权重',
                               `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
                               `create_user` bigint(20) NOT NULL COMMENT '创建人',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_user` bigint(20) NOT NULL COMMENT '更新人',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务明细表';

CREATE TABLE IF NOT EXISTS `thought` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `content` mediumtext NOT NULL COMMENT '没人',
                           `user_id` bigint(19) NOT NULL COMMENT '用户 ID',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `create_user` bigint(20) DEFAULT NULL,
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                           `update_user` bigint(20) DEFAULT NULL,
                           `is_deleted` int(11) DEFAULT '0',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='思考表';

CREATE TABLE IF NOT EXISTS `thought_rela_event` (
                                      `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                      `thought_id` bigint(20) NOT NULL,
                                      `content` text NOT NULL,
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `create_user` bigint(20) DEFAULT NULL,
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `update_user` bigint(20) DEFAULT NULL,
                                      `is_deleted` int(11) DEFAULT '0',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='思考关联事件表';

CREATE TABLE IF NOT EXISTS `time_record` (
                               `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '记录ID',
                               `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                               `create_user` bigint(20) NOT NULL COMMENT '用户ID',
                               `category_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类ID',
                               `date` date NOT NULL COMMENT '日期（YYYY-MM-DD格式）',
                               `start_time` smallint(5) unsigned NOT NULL COMMENT '开始时间（分钟，0-1440）',
                               `end_time` smallint(5) unsigned NOT NULL COMMENT '结束时间（分钟，0-1440）',
                               `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
                               `description` text COLLATE utf8mb4_unicode_ci COMMENT '详细描述',
                               `duration` smallint(5) unsigned NOT NULL COMMENT '时长（分钟）',
                               `is_manual` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否手动创建：0-系统，1-手动',
                               `is_deleted` tinyint(4) NOT NULL DEFAULT '0',
                               `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_time_record_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间段记录表';

CREATE TABLE IF NOT EXISTS `time_tracker_category` (
                                         `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                         `code` varchar(50) NOT NULL COMMENT '分类标识(如: rest, work)',
                                         `name` varchar(50) NOT NULL COMMENT '分类名称',
                                         `color` varchar(20) NOT NULL COMMENT '颜色值(Hex)',
                                         `description` varchar(255) DEFAULT NULL COMMENT '描述',
                                         `is_track_time` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否记录时间',
                                         `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序权重',
                                         `create_user` bigint(20) NOT NULL COMMENT '创建人',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_user` bigint(20) NOT NULL COMMENT '更新人',
                                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '是否删除',
                                         PRIMARY KEY (`id`),
                                         KEY `idx_user_code` (`user_id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间追踪-分类配置表';


CREATE TABLE IF NOT EXISTS `mail_log` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `send_to` varchar(100) NOT NULL COMMENT '接收者邮箱',
  `subject` varchar(255) DEFAULT NULL COMMENT '邮件标题',
  `content` text COMMENT '邮件内容',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型：register-注册, login-登录, reset_pwd-重置密码, system_notice-系统通知等',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '发送状态：1-成功，0-失败',
  `error_msg` varchar(255) DEFAULT NULL COMMENT '失败原因',
  `ip_address` varchar(45) DEFAULT NULL COMMENT '请求IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_send_to_biz_type` (`send_to`, `biz_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送记录表';

CREATE TABLE IF NOT EXISTS `api_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `api_key` varchar(128) NOT NULL COMMENT 'API Key',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `expired_at` datetime DEFAULT NULL COMMENT '过期时间',
  `create_user` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint(20) NOT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_key` (`api_key`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API Key 表';

CREATE TABLE IF NOT EXISTS `api_key_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_key_id` bigint(20) NOT NULL COMMENT 'API Key ID',
  `request_path` varchar(255) NOT NULL COMMENT '请求路径',
  `request_method` varchar(10) NOT NULL COMMENT '请求方法',
  `response_status` int(11) DEFAULT NULL COMMENT '响应状态码',
  `client_ip` varchar(45) DEFAULT NULL COMMENT '客户端IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_api_key_id` (`api_key_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API Key 调用日志表';

CREATE TABLE IF NOT EXISTS `message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送用户ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收用户ID',
  `title` varchar(255) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `type` int(11) NOT NULL DEFAULT '0' COMMENT '消息类型: 0-系统通知, 1-用户消息',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
  `create_user` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint(20) NOT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_is_read` (`receiver_id`, `is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';


-- ============================================================
-- Source: 2026-03-18_createtable.sql
-- ============================================================

CREATE TABLE `llm_key` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                           `model_name` varchar(100) NOT NULL COMMENT '模型名称',
                           `api_key` varchar(255) NOT NULL COMMENT 'API密钥（加密存储）',
                           `base_url` varchar(255) NOT NULL COMMENT '基础URL',
                           `is_default` int(1) DEFAULT '0' COMMENT '是否默认：0-否，1-是',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大模型密钥表';


CREATE TABLE IF NOT EXISTS `chat_message` (
                                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `role` varchar(50) NOT NULL COMMENT '角色: user-用户, assistant-助手',
    `content` text COMMENT '消息内容',
    `model_name` varchar(100) DEFAULT NULL COMMENT '模型名称',
    `create_user` bigint(20) NOT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user` bigint(20) NOT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息表';


CREATE TABLE IF NOT EXISTS `chat_session` (
                                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `title` varchar(255) DEFAULT NULL COMMENT '会话标题',
    `create_user` bigint(20) NOT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user` bigint(20) NOT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话会话表';

ALTER TABLE `chat_message` ADD COLUMN `conversation_id` bigint(20) DEFAULT NULL COMMENT '会话ID' AFTER `user_id`;
ALTER TABLE `chat_message` ADD INDEX `idx_conversation_id` (`conversation_id`);


-- ============================================================
-- Source: 2026-03-19_add_priority_to_task_detail.sql
-- ============================================================

ALTER TABLE `task_detail` ADD COLUMN `priority` int(11) DEFAULT '20' COMMENT '优先级: 1-高, 10-中, 20-低' AFTER `sort`;


-- ============================================================
-- Source: 2026-03-22_time_tracker_category.sql
-- ============================================================


-- 新增 is_enabled 字段，用于控制分类是否启用
ALTER TABLE time_tracker_category ADD COLUMN is_enabled tinyint NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用';

alter table time_tracker_category modify name varchar(50) null comment '分类名称';

ALTER TABLE time_tracker_category modify COLUMN color varchar(20) null comment '颜色值(Hex)';

-- 新增 template_id 字段
ALTER TABLE time_tracker_category ADD COLUMN template_id BIGINT NULL COMMENT '模板ID，指向被覆盖的公共分类ID';

update time_record t
set category_id  = (select distinct id from time_tracker_category c where c.code = t.category_id)
where exists(
    select id from time_tracker_category c where c.code = t.category_id
);
alter table time_tracker_category modify code varchar(50) null comment '分类标识(如: rest, work)';
-- 迁移完成后，也可以直接删除字段
-- ALTER TABLE time_tracker_category DROP COLUMN code;

-- 新增 icon 字段，用于存储分类图标
ALTER TABLE time_tracker_category ADD COLUMN icon VARCHAR(100) NULL COMMENT '图标名称(Iconify格式)';


-- device 表结构变更：添加规格字段、结束日期字段，移除订单号字段

-- 添加设备规格字段（位于 name 字段后）
ALTER TABLE `device` ADD COLUMN `spec` varchar(255) DEFAULT NULL COMMENT '设备规格' AFTER `name`;

-- 添加结束日期字段（位于 image 字段后）
ALTER TABLE `device` ADD COLUMN `end_date` varchar(255) DEFAULT NULL COMMENT '结束日期（用于计算日均费用）' AFTER `image`;

-- 移除订单号字段
ALTER TABLE `device` DROP COLUMN `order_number`;


-- ============================================================
-- Source: 2026-03-23_create_mbti_result.sql
-- ============================================================

-- MBTI测试结果表
-- 创建时间: 2026-03-23
-- 描述: 存储用户MBTI人格测试结果

CREATE TABLE IF NOT EXISTS `mbti_result` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `test_id` VARCHAR(255) DEFAULT NULL COMMENT '测试ID',
    `mbti_type` VARCHAR(10) DEFAULT NULL COMMENT 'MBTI类型',
    `raw_result` TEXT DEFAULT NULL COMMENT '原始结果数据(JSON)',
    `results_page` VARCHAR(500) DEFAULT NULL COMMENT '官方结果页面URL',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '是否删除(0-否,1-是)',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_test_id` (`test_id`),
    INDEX `idx_mbti_type` (`mbti_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBTI测试结果表';


-- ============================================================
-- Source: 2026-04-03_add_last_active_at_to_user.sql
-- ============================================================

ALTER TABLE `user`
    ADD COLUMN `last_active_at` timestamp NULL DEFAULT NULL COMMENT '最后活跃时间' AFTER `updated_at`;


-- ============================================================
-- Source: 2026-04-05_goal.sql
-- ============================================================

-- 目标管理表
-- 支持年度目标、月度目标、日目标的多层级目标管理

CREATE TABLE `goal` (
  `id` BIGINT NOT NULL COMMENT '目标ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `type` TINYINT DEFAULT NULL COMMENT '目标类型：1=年度目标，2=月度目标，3=日目标',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '目标标题',
  `description` TEXT COMMENT '目标描述',
  `content` TEXT COMMENT '目标详细内容/行动计划',
  `status` TINYINT DEFAULT NULL COMMENT '目标状态：0=待开始，1=进行中，2=已完成，3=已放弃',
  `progress` INT DEFAULT 0 COMMENT '目标进度（0-100）',
  `target_value` INT DEFAULT NULL COMMENT '目标值',
  `current_value` INT DEFAULT 0 COMMENT '当前值',
  `year` INT DEFAULT NULL COMMENT '年份（用于年度目标筛选）',
  `month` INT DEFAULT NULL COMMENT '月份（用于月度目标筛选）',
  `day` INT DEFAULT NULL COMMENT '日期（用于日目标筛选）',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父目标ID',
  `start_date` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_date` DATETIME DEFAULT NULL COMMENT '结束时间',
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  `tags` VARCHAR(1000) DEFAULT NULL COMMENT '目标标签（JSON格式存储）',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0=未删除，1=已删除',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_user` BIGINT DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='目标管理表';


-- ============================================================
-- Source: 2026-04-06_add_is_starred_to_task_detail.sql
-- ============================================================

-- 添加关注字段到 task_detail 表
ALTER TABLE task_detail
ADD COLUMN is_starred INT(1) NOT NULL DEFAULT 0 COMMENT '是否关注: 0-未关注, 1-已关注';

-- 添加开始时间和结束时间字段
ALTER TABLE task_detail
ADD COLUMN start_time datetime DEFAULT NULL COMMENT '开始时间';

ALTER TABLE task_detail
ADD COLUMN end_time datetime DEFAULT NULL COMMENT '结束时间';


-- ============================================================
-- Source: 2026-04-08_add_time_type_to_time_tracker_category.sql
-- ============================================================

-- 新增 time_type 字段，用于标记分类的时间类型
ALTER TABLE time_tracker_category
ADD COLUMN time_type TINYINT NOT NULL DEFAULT 1
COMMENT '时间类型: 1-必须, 2-积极, 3-休闲';


-- ============================================================
-- Source: 2026-04-11_create_honor_table.sql
-- ============================================================

-- 荣誉中心表结构
-- 支持用户自主记录个人荣誉（优秀员工、国家奖学金等）

-- 荣誉分类表
CREATE TABLE `honor_category` (
  `id` BIGINT NOT NULL COMMENT '分类ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（NULL表示系统预设分类）',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '分类颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='荣誉分类表';

-- 系统预设分类
INSERT INTO `honor_category` (`id`, `user_id`, `name`, `icon`, `color`, `sort_order`) VALUES
(1, NULL, '学业成就', '🎓', '#4CAF50', 1),
(2, NULL, '奖学金', '💰', '#FFC107', 2),
(3, NULL, '工作荣誉', '💼', '#2196F3', 3),
(4, NULL, '竞赛获奖', '🏆', '#FF5722', 4),
(5, NULL, '社会实践', '🤝', '#9C27B0', 5),
(6, NULL, '荣誉称号', '⭐', '#FFD700', 6),
(7, NULL, '其他荣誉', '📜', '#607D8B', 7);

-- 荣誉记录表
CREATE TABLE `honor_record` (
  `id` BIGINT NOT NULL COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '荣誉标题',
  `description` TEXT COMMENT '荣誉描述',
  `honor_date` DATE NOT NULL COMMENT '获得日期',
  `issuer` VARCHAR(200) DEFAULT NULL COMMENT '颁发机构/组织',
  `level` VARCHAR(20) DEFAULT NULL COMMENT '荣誉级别：1-校级，2-市级，3-省级，4-国家级，5-国际级',
  `category_id` BIGINT DEFAULT NULL COMMENT '所属分类ID（可为空）',
  `custom_category` VARCHAR(50) DEFAULT NULL COMMENT '自定义分类名称（当不选择预设分类时使用）',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签（JSON格式存储）',
  `attachments` VARCHAR(2000) DEFAULT NULL COMMENT '附件URL列表（JSON格式）',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `is_public` TINYINT DEFAULT 1 COMMENT '是否公开：0-私密，1-公开',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_user` BIGINT DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_honor_date` (`honor_date`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='荣誉记录表';


-- ============================================================
-- Source: 2026-04-18_create_anniversary_table.sql
-- ============================================================

-- 纪念日表结构
CREATE TABLE `anniversary_record` (
  `id` BIGINT NOT NULL COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `target_date` DATE NOT NULL COMMENT '目标日期',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：anniversary-纪念日(正数), countdown-倒数日(倒数)',
  `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `color` VARCHAR(50) DEFAULT NULL COMMENT '渐变色class',
  `icon` VARCHAR(20) DEFAULT NULL COMMENT 'Emoji图标',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_user` BIGINT DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_date` (`target_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纪念日记录表';


-- ============================================================
-- Source: 2026-04-18_create_cbti_tables.sql
-- ============================================================

-- CBTI 人格测试相关表
-- 创建时间: 2026-04-18
-- 作者: Ethan
-- 描述: 存储 CBTI 人格类型数据与用户测试历史
-- 更新功能简介:
-- 1) 新增 cbti_personality 表，用于维护人格基础信息（向量、描述、图片对象路径、是否隐藏等）
-- 2) 新增 cbti_result 表，用于记录用户测试结果、维度得分和答案快照
-- 3) 为查询场景补充必要索引（user_id/personality_code/create_time）

CREATE TABLE IF NOT EXISTS `cbti_personality` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `code` VARCHAR(20) NOT NULL COMMENT '人格代码（唯一）',
    `name` VARCHAR(64) NOT NULL COMMENT '人格名称',
    `motto` VARCHAR(255) DEFAULT NULL COMMENT '座右铭',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '主题色（HEX）',
    `vector` JSON DEFAULT NULL COMMENT '人格向量（长度15，数值为-1/0/1/2）',
    `description` TEXT DEFAULT NULL COMMENT '人格描述',
    `strengths` JSON DEFAULT NULL COMMENT '优势（字符串数组）',
    `weaknesses` JSON DEFAULT NULL COMMENT '弱点/注意（字符串数组）',
    `tech_stack` VARCHAR(255) DEFAULT NULL COMMENT '技术栈',
    `spirit` TEXT DEFAULT NULL COMMENT '灵魂格言',
    `image_object` VARCHAR(255) DEFAULT NULL COMMENT 'MinIO对象路径（如 images/cbti/characters/SUDO.png）',
    `is_special` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否隐藏人格（0-否，1-是）',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '是否删除(0-否,1-是)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cbti_personality_code` (`code`),
    INDEX `idx_cbti_personality_special` (`is_special`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CBTI 人格类型表';

CREATE TABLE IF NOT EXISTS `cbti_result` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `personality_code` VARCHAR(20) NOT NULL COMMENT '人格代码',
    `similarity` INT NOT NULL DEFAULT 0 COMMENT '匹配度（0-100）',
    `dimensions` JSON DEFAULT NULL COMMENT '15维度结果(JSON)',
    `answers` JSON DEFAULT NULL COMMENT '答题结果(JSON，题号->选项值)',
    `hidden_answers` JSON DEFAULT NULL COMMENT '彩蛋答题结果(JSON)',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '是否删除(0-否,1-是)',
    PRIMARY KEY (`id`),
    INDEX `idx_cbti_result_user_id` (`user_id`),
    INDEX `idx_cbti_result_personality_code` (`personality_code`),
    INDEX `idx_cbti_result_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CBTI 测试历史表';


INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543797787045890, 'SUDO', '万能管理员', '遇事不决，sudo 一下。', '#16A34A', '[2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1, 1, 2]', '恭喜您，您测出了编程界最罕见的人格——SUDO。您是人形的 root 权限，行走的超级管理员。代码质量高、Bug 处理快、团队协作强、技术热情爆棚，而且从不把锅甩给别人。当所有人都在 Stack Overflow 上搜索答案时，您就是那个写答案的人。当系统崩了、数据库锁死、产品经理又改需求时，全世界都在喊一个名字——SUDO。因为只有 SUDO 能解决一切问题。有人说 SUDO 不存在，这种人格完美到不像真人。确实，选出这个结果的人，要么是在吹牛，要么真的是 AI。', '["全栈能力拉满", "团队核心骨干", "技术影响力强"]', '["可能不存在", "容易让同事产生绝望感"]', '全栈 + DevOps + 架构设计 + AI（大概）', 'Permission granted. 你不是在写代码，你是在用代码重编译宇宙。', 'images/cbti/characters/SUDO.png', 0, 0, '2026-04-24 13:10:58', 0, '2026-04-24 21:41:39', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543800064552961, 'README', '文档侠', '别问我，看文档。', '#2563EB', '[2, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 2]', '恭喜您！您测出了编程界最被低估的人格——README。在这个连注释都懒得写的年代，您居然会写文档？会写架构图？会画时序图？您简直是数字时代的活化石，代码世界的非物质文化遗产传承人。README 人格的核心技能不是写代码，而是让别人看得懂你的代码——这在整个编程史上的难度系数，大概排在「让产品经理不改需求」的后面，「让设计师出完整标注」的前面。您的 PR 描述比很多人的毕业论文都详细，您的 README.md 比很多公司的产品文档都专业。可惜的是，没人看。', '["文档能力SSS级", "Code Review详尽专业", "知识传承型人才"]', '["写文档的时间比写代码长", "容易被当成\\"管太多\\""]', 'Markdown + Notion + Confluence + 世界上最好的注释', '代码是写给机器跑的，但首先是写给人读的。可惜没人读。', 'images/cbti/characters/README.png', 0, 0, '2026-04-24 13:10:58', 0, '2026-04-24 21:41:40', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543800920190977, 'GIT-F', '强推人', 'git push --force，信仰之推。', '#DC2626', '[0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0]', '恭喜您！您测出了全网最令同事闻风丧胆的人格——GIT-F。GIT-F 的全称是 Git Force，信仰之推。在 GIT-F 的世界观里，冲突不是用来解决的，是用来覆盖的。代码不是用来维护的，是用来替换的。文档不是用来写的，是用来删的。GIT-F 的行为模式可以用一句话概括：先推了再说。出了问题？再推一次。还有问题？删库重来。同事们对 GIT-F 的态度分两种：没被 force push 过的，觉得这只是个梗；被 force push 过的，已经在默默备份代码了。', '["执行力极强（物理）", "不纠结不犹豫", "绝不内耗"]', '["可能导致团队集体崩溃", "Git历史都在颤抖"]', 'git push --force --no-verify', '别人在解决冲突的时候，我已经把冲突本身消灭了。', 'images/cbti/characters/GIT-F.png', 0, 0, '2026-04-24 13:10:58', 0, '2026-04-24 21:41:40', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543801784217602, 'CRUD', '增删改查侠', '需求再复杂，本质都是CRUD。', '#64748B', '[1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 1]', '恭喜您，您测出了全中国程序员占比最高的人格——CRUD。别急着哭，CRUD 不是一种贬低，而是一种觉悟。当别人在争论微服务还是单体、Rust 还是 Go、Vim 还是 VS Code 时，CRUD 人格早已看透了这个行业最朴素的真理：代码的本质，就是增删改查。万物皆可 CRUD。用户管理？CRUD。订单系统？CRUD。社交网络？带关系的 CRUD。人工智能？在向量数据库里 CRUD。CRUD 人格的最大智慧在于：我知道自己在做什么，我也知道大部分人其实也在做同样的事，只是用更花哨的名字包装起来而已。', '["务实", "基本功扎实", "不被概念忽悠"]', '["可能被35岁危机提前找上门", "技术天花板清晰可见"]', 'Spring Boot / MyBatis / jQuery（没错，还在用）', '你说你在做分布式微服务架构？让我看看你的核心代码——哦，CRUD啊。', 'images/cbti/characters/CRUD.png', 0, 0, '2026-04-24 13:10:59', 0, '2026-04-24 21:41:40', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543802644049922, 'BUG-0', '零Bug战士', 'Bug？在我的代码里不存在。别的代码我不管。', '#7C3AED', '[2, 2, 2, 2, 2, 2, 1, 1, 1, 2, 1, 1, 1, 1, 2]', '恭喜您！您测出了全网最令测试同学心碎的人格——BUG-0。不是因为您Bug多，恰恰相反——测试在您的代码面前完全找不到Bug，失去了工作的意义。BUG-0 的代码经过三层防御：编译前自检、运行时自愈、上线后自守。您写的不是代码，是一座坚不可摧的数字堡垒。唯一的问题是，BUG-0 只保证自己的代码没Bug。同事的代码出了问题，BUG-0 的反应是："这不是我的模块。" 说完继续优化自己那段已经没有任何Bug的代码。', '["代码质量极高", "Debug能力MAX", "极度可靠"]', '["可能对同事的代码过于冷漠", "完美主义偶尔影响进度"]', 'TypeScript(strict) + Jest + SonarQube + 强迫症', '我的代码没有Bug，但我不保证你的有。', 'images/cbti/characters/BUG-0.png', 0, 0, '2026-04-24 13:10:59', 0, '2026-04-24 21:41:40', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543803562602497, '404', '隐身人', '我？不在工位。不在线。不存在。', '#9CA3AF', '[1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1]', '恭喜您，您测出了全网最难被找到的人格——404。正如 HTTP 状态码 404 Not Found，这种人格的核心特征就是：找不到。站会时找不到人，群消息永远已读不回，需要帮忙时永远不在工位。同事以为您去上厕所了？不，你在楼下咖啡店远程装忙。领导以为您在开会？不，你在会议室看小说。但神奇的是，每次 deadline 来临，404 总能准时交出一份刚好及格的代码。所以有一种说法：404 Not Found 不代表不存在，只代表你没有权限看到他在做什么。', '["时间管理（摸鱼层面）大师", "准时交付（虽然刚刚好）", "心态超好"]', '["存在感为零", "职业发展基本靠缘分"]', '任何能远程的工具 + VPN + 多设备在线挂机', '最好的代码是交付的代码。最好的工作状态是看不见。', 'images/cbti/characters/404.png', 0, 0, '2026-04-24 13:10:59', 0, '2026-04-24 21:41:41', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543804435017729, 'VIBE', '氛围程序员', '我不写代码，我只是告诉AI我的感觉。', '#D946EF', '[0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 2, 0, 0]', '恭喜您，您测出了2026年最热门的人格——VIBE。Vibe Coding，氛围编程。在VIBE的世界观里，写代码是上个世纪的手艺活，就像手搓汇编一样原始。现代程序员的核心能力不是coding，是prompting。VIBE 不理解代码，不需要理解代码，甚至可能看不懂代码——但这不重要。"Cursor，帮我写一个电商系统。" "Claude，这个Bug帮我修一下。" "ChatGPT，帮我写个面试八股文。" VIBE 的 GitHub 贡献图很绿，但严格来说，那些代码都是AI写的。VIBE自己写过的最长的代码是什么？大概是 npm install。', '["效率极高（如果prompt写得好的话）", "快速原型验证", "不怕新技术"]', '["离开AI立刻变回石器时代", "不懂原理导致生产事故"]', 'Cursor + Claude + 祈祷', '我不是程序员，我是AI的项目经理。', 'images/cbti/characters/VIBE.png', 0, 0, '2026-04-24 13:10:59', 0, '2026-04-24 21:41:41', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543805303238658, 'LGTM', '老好人', 'LGTM 👍（Lord Give This Man a brain）', '#F59E0B', '[1, 1, 1, 1, 1, 2, 0, 1, 1, 0, 0, 0, 1, 0, 1]', '恭喜您，您测出了团队里最受欢迎也最容易被压榨的人格——LGTM。在LGTM的字典里，没有"拒绝"这两个字。Code Review？LGTM。帮忙改Bug？好的。帮写文档？行吧。帮调接口？来了来了。周末加班？......嗯好吧。LGTM 人格的口头禅是"没事没事"和"我来看看"。半夜三点被叫起来修同事的Bug也不抱怨，被产品经理改了八版需求也只是笑笑。所有人都喜欢LGTM，因为LGTM永远不说不。但LGTM最大的问题也在于此——你什么时候才能学会说"不"？', '["团队氛围制造者", "责任感强", "人缘好"]', '["被过度消耗", "自己的成长时间全给了别人"]', '什么都会一点，什么都不精', 'LGTM 不是 Looks Good To Me，是 Lord Give This Man 一些边界感。', 'images/cbti/characters/LGTM.png', 0, 0, '2026-04-24 13:10:59', 0, '2026-04-24 21:41:41', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543806154682369, 'NPM-i', '轮子收藏家', 'npm install 一切。自己写？不存在的。', '#EF4444', '[0, 0, 1, 1, 0, 0, 0, 0, 0, 2, 0, 2, 2, 0, 0]', '恭喜您，您测出了编程界最懂"站在巨人肩膀上"的人格——NPM-i。在NPM-i的世界观里，所有问题都有一个对应的npm包。左边距需要调整？npm install left-pad。判断一个数是不是奇数？npm install is-odd。生成一个随机数？那得装三个包。NPM-i 的 node_modules 文件夹已经比银河系还重了，package.json 的 dependencies 列表需要翻三页才能看完。同事问："这段逻辑为什么要引一个外部库？自己写不就两行代码的事？" NPM-i："自己写容易有Bug，人家那个库经过了 battle-tested。" 说完，那个库的最后一次更新是 2019 年，下载量 17。', '["开发速度快", "知道很多开源库", "不重复造轮子（物理）"]', '["node_modules 可以压垮硬盘", "依赖安全隐患", "脱离轮子寸步难行"]', 'npm install universe', '人生苦短，我用 npm install。', 'images/cbti/characters/NPM-i.png', 0, 0, '2026-04-24 13:11:00', 0, '2026-04-24 21:41:41', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543807001931778, 'DEL-F', '删库跑路人', 'rm -rf / 或者跑路，总要选一个。', '#B91C1C', '[0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0]', '恭喜您，您测出了全网最高危的人格——DEL-F。DELETE FORCE，删库跑路人。请注意，DEL-F 不一定真的会删库跑路，但 DEL-F 的内心深处始终住着一个念头："大不了删库跑路。" 这个念头在以下场景会被激活：产品经理第七次改需求时、线上出了第五个P0事故时、年终考核被打了3.25时、凌晨三点还在加班时。DEL-F 的桌面上可能贴着一张便利贴，写着 rm -rf /，这不是威胁，这是一种精神图腾，一种对这个荒谬世界最后的、最温柔的反抗。DEL-F 的存在提醒着所有人：善待你的程序员，否则......', '["直球表达不满", "不内耗", "极度真实"]', '["容易冲动", "简历上最好别提这个测试结果"]', 'rm -rf / && echo \'goodbye\'', '我不是在删库，我是在做减法。', 'images/cbti/characters/DEL-F.png', 0, 0, '2026-04-24 13:11:00', 0, '2026-04-24 21:41:41', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543808016953345, 'FIXME', '永远在修的人', 'TODO: fix this later（后来再也没后来了）', '#EA580C', '[1, 0, 1, 2, 1, 2, 1, 1, 0, 1, 0, 0, 1, 1, 1]', '恭喜您，您测出了编程界最勤劳也最悲催的人格——FIXME。FIXME 人格的一天是这样的：早上来修昨天的Bug → 修完之后发现引入了新Bug → 修新Bug的时候发现之前有个老Bug一直被隐藏 → 修老Bug的时候产品说需求变了 → 改完需求发现又多了三个Bug → 下班。第二天重复以上步骤。FIXME 不是不努力，FIXME 可能是全公司最努力的人。只是 FIXME 的努力，永远在修补，从来不在创造。FIXME 的git log里最常见的关键词是：fix、hotfix、bugfix、quickfix、please-work-this-time-fix。FIXME 最大的梦想是写一段不需要再改的代码，但这个梦想的实现难度大概等于让产品经理不改需求。', '["抗压能力极强", "修Bug速度快", "责任心强"]', '["永远在救火模式", "缺乏系统性思考"]', 'Debug 工具 + 热修复 + 祈祷', '我这辈子写的代码，80% 是在修剩下的 20%。', 'images/cbti/characters/FIXME.png', 0, 0, '2026-04-24 13:11:00', 0, '2026-04-24 21:41:41', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543808939700226, 'HACK', '野生黑客', '又不是不能用.jpg', '#059669', '[0, 0, 0, 2, 1, 1, 0, 0, 0, 2, 2, 2, 2, 0, 0]', '恭喜您，您测出了编程界最自由的灵魂——HACK。HACK 的代码风格可以用四个字形容：百无禁忌。变量名用 emoji？可以。一个函数写 500 行？能跑就行。CSS 全用 !important？效率高。HACK 的 GitHub 上全是半成品项目，README 写着 WIP，但每个项目都用了最前沿的技术。你问 HACK 为什么不把项目做完？HACK 说：做完就不好玩了。HACK 是 Hackathon 的常客，24小时内能从零做出一个能跑的 Demo，虽然代码的可读性约等于密文。HACK 的人生哲学是：完美是优秀的敌人，能跑就是好代码。', '["创造力MAX", "技术嗅觉灵敏", "解决问题速度极快"]', '["代码维护性为零", "同事接手代码后原地爆炸"]', '每周换一个新框架，永远在 alpha 版', 'Don\'t ask me how it works. I don\'t know either. But it works.', 'images/cbti/characters/HACK.png', 0, 0, '2026-04-24 13:11:00', 0, '2026-04-24 21:41:41', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543809967304706, 'CTRL-C', '复制粘贴工程师', 'CV大法好。Stack Overflow是我爹。', '#0EA5E9', '[0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 2, 0, 0]', '恭喜您，您测出了编程界最诚实的人格——CTRL-C。也叫 Copy-Paste Engineer，复制粘贴工程师。在CTRL-C的世界观里，这个世界上没有不能通过 Ctrl+C Ctrl+V 解决的问题。如果有，那就多复制两遍。CTRL-C 的技术栈本质上只有一种：Stack Overflow + ChatGPT + 同事的代码。CTRL-C 写的每一行代码都来自别处。但你别说，CTRL-C 的开发效率还真不低——因为那些代码本来就是能跑的。CTRL-C 最大的恐惧不是Bug，是Stack Overflow宕机。到那一天，CTRL-C 就真的不会写代码了。', '["开发效率快", "善于利用现有资源", "极度务实"]', '["可能引入版权问题", "离开搜索引擎战斗力归零"]', 'Stack Overflow + GitHub Copilot + Ctrl+C Ctrl+V', 'Good artists copy, great artists paste. — CTRL-C', 'images/cbti/characters/CTRL-C.png', 0, 0, '2026-04-24 13:11:00', 0, '2026-04-24 21:41:42', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543810827137025, 'RUSH', '极限速通者', 'Deadline是第一生产力。', '#F97316', '[0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 2, 0, 1, 0, 0]', '恭喜您，您测出了编程界最能创造奇迹的人格——RUSH。RUSH 人格有一个独特的物理定律：当 Deadline 还有 7 天时，RUSH 的代码产出为 0；当 Deadline 还有 1 天时，RUSH 的代码产出为之前七天的总和乘以三。这不是夸张，这是 RUSH 的真实工作曲线。同事们常常看到 RUSH 前四天在摸鱼、聊天、喝咖啡、研究一个跟需求毫无关系的新技术——然后在最后一天，RUSH 进入了某种超自然状态，键盘发出噼里啪啦的声响，代码如瀑布般倾泻，Bug 自动退散。第二天早上，需求做完了。代码质量嘛......又不是不能用。', '["极限状态下爆发力惊人", "抗压能力意外地强", "效率奇高（限最后一天）"]', '["前六天基本是废物", "代码质量随deadline距离指数衰减"]', '什么快用什么 + energy drinks', 'Deadline 不是死线，是复活线。没有它我活不了。', 'images/cbti/characters/RUSH.png', 0, 0, '2026-04-24 13:11:01', 0, '2026-04-24 21:41:42', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543811686969345, 'RTFM', '原教旨主义者', 'Read The F***ing Manual。不看文档别来问我。', '#1D4ED8', '[2, 2, 2, 2, 2, 1, 2, 1, 1, 2, 1, 1, 0, 2, 2]', '恭喜您，您测出了编程界最硬核的人格——RTFM。Read The Fucking Manual，不看文档别来问我。RTFM 是技术原教旨主义者，一切以官方文档为准，一切以规范为纲。RTFM 不用 AI 写代码，因为 AI 会产生幻觉；不看博客教程，因为二手信息有偏差；不听同事的建议，因为他们没读过 RFC。RTFM 的浏览器收藏夹里全是官方文档链接，桌上可能还放着《深入理解计算机系统》和《TCP/IP详解》的纸质版。当实习生问 RTFM 一个问题时，RTFM 的回答永远是："文档上写了。" 哪怕那份文档有 3000 页。', '["技术功底极深", "代码质量高", "不被技术潮流带偏"]', '["沟通方式令人窒息", "可能错过新技术的红利"]', '官方文档 + RFC + 源码 + 纸质书', '这世界上 90% 的技术问题，答案都在文档里。剩下 10% 在源码里。', 'images/cbti/characters/RTFM.png', 0, 0, '2026-04-24 13:11:01', 0, '2026-04-24 21:41:42', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543812542607361, '//TODO', '永远下一版', '// TODO: 下次一定', '#A16207', '[1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0]', '恭喜您，您测出了编程界最言行不一致的人格——//TODO。在TODO的代码里散落着上百条 // TODO 注释，每一条都是一个未兑现的承诺，一个永远不会到来的"下次"。// TODO: 重构这段逻辑。写于2023年，至今仍在生产环境跑着。// TODO: 加上错误处理。后来这个Bug直接上了线上事故复盘。// TODO: 优化性能。后来用户投诉了，产品经理来追责了，TODO 终于变成了 FIXME。//TODO 的人生哲学是：先让它跑起来，优化是未来的事。问题是，未来永远在未来。//TODO 的 git blame 就是一部个人成长的黑历史。', '["快速交付", "有一定技术追求（虽然从不兑现）", "乐观"]', '["技术债堆成山", "TODO永远清不完"]', '任何能快速出活的 + 大量的 TODO', '世界上最遥远的距离不是生与死，是 TODO 和 DONE 之间。', 'images/cbti/characters/TODO.png', 0, 0, '2026-04-24 13:11:01', 0, '2026-04-24 21:41:42', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543813394051074, '996', '卷王之王', '你下班了？我再优化一下那个0.01ms。', '#BE123C', '[2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1]', '恭喜您，您测出了编程界令人又敬又怕的人格——996。不，这不是ICU的前奏，这是一种燃烧的生命状态。996 的一天是这样的：早上到公司先把昨晚想到的三个优化点实现了，然后开始做今天的需求，中午吃饭时看技术文章，下午 Code Review 完顺手重构了同事的代码，晚上留下来把周末想做的 Side Project 推进了三个 commit。你以为 996 是被逼的？不，996 是主动的。996 不是在加班，996 是在享受。因为对 996 来说，写代码不是工作，是呼吸。你见过有人抱怨呼吸太累吗？唯一的问题是：996 会无意识地给所有同事制造巨大的peer pressure。', '["产出极高", "技术成长极快", "项目推进力极强"]', '["令同事窒息", "可能忽略健康", "不是所有人都想卷"]', '什么前沿用什么 + 个人技术博客日更', '我不是在工作，我是在修炼。每一行代码都是渡劫。', 'images/cbti/characters/996.png', 0, 0, '2026-04-24 13:11:01', 0, '2026-04-24 21:41:42', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543814241300481, 'GOTO', '叛逆者', '规则是用来打破的，goto 也是用来跳的。', '#4F46E5', '[0, 0, 0, 1, 1, 1, 2, 0, 0, 2, 1, 2, 2, 0, 0]', '恭喜您，您测出了编程界最不走寻常路的人格——GOTO。众所周知，goto 语句被教科书列为"有害的"，是结构化编程的公敌。但 GOTO 人格就是喜欢跳。跳过流程、跳过规范、跳过所有人的预期。当所有人都在用 React 时，GOTO 在用 Svelte。当所有人都在学 TypeScript 时，GOTO 在写 Zig。当所有人都在遵守 RESTful 规范时，GOTO 设计了一套自创的 API 风格。GOTO 不是叛逆，GOTO 是自由。在 GOTO 眼里，所有的"最佳实践"都只是"别人的实践"。GOTO 有自己的实践，虽然同事们不一定看得懂。', '["创新能力强", "不被框架限制", "独立思考"]', '["代码对团队不友好", "可能为了不同而不同"]', '冷门语言 + 自创框架 + "你没听过但它很厉害"', 'Goto considered harmful？那是因为你不会用。', 'images/cbti/characters/GOTO.png', 0, 0, '2026-04-24 13:11:01', 0, '2026-04-24 21:41:42', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543815084355586, 'PING', '社交达人', '我 ping 你一下，你在吗？', '#EC4899', '[1, 0, 0, 0, 1, 1, 1, 2, 2, 0, 0, 0, 1, 0, 1]', '恭喜您，您测出了编程界最外向的人格——PING。在大部分程序员还在与代码进行一对一的深情对视时，PING 已经在群里 @ 了全公司的人。PING 的核心能力不是写代码，是建立连接。PING 是团建的组织者、段子手、茶水间的社交蝴蝶、所有群的活跃分子。同事们都喜欢 PING，虽然有时候不太确定 PING 到底写了多少代码。PING 的代码质量？嗯......能用就行。PING 的技术深度？呃......够用就好。但 PING 的人脉有多广？从前端到后端、从测试到运维、从实习生到CTO，PING 都能聊上两句。在职场里，写代码的能力排第三，前两名是"会说话"和"会做人"。', '["团队润滑剂", "跨团队沟通无障碍", "人脉广"]', '["技术深度有限", "可能被当成\\"只会聊天的\\""]', '飞书 + 企业微信 + 嘴', '最好的代码不如一次好的沟通。最好的沟通不如请大家喝一杯。', 'images/cbti/characters/PING.png', 0, 0, '2026-04-24 13:11:02', 0, '2026-04-24 21:41:43', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543815931604994, 'NULL', '空指针', 'NullPointerException: 人生意义 is null', '#78716C', '[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]', '恭喜您，您测出了编程界最稀有也最真实的人格——NULL。就像 Java 里的 NullPointerException，NULL 人格的核心特征就是：空。代码质量？随便。Bug归谁？无所谓。团队协作？已读不回。技术热情？早就没了。AI取代论？太好了快来替我上班。NULL 不是不会编程，NULL 是对这个行业彻底祛魅了。NULL 清醒地认识到：代码只是 0 和 1 的排列组合，工作只是时间换金钱的交易，所谓的「改变世界」不过是资本的叙事。NULL 是全公司最没有内耗的人，因为 NULL 根本就不在乎。NULL 的桌面上可能放着一本《被讨厌的勇气》——NULL 不怕被讨厌，NULL 怕的是被 on-call。', '["心态超稳", "绝不内耗", "上班下班泾渭分明"]', '["职业发展约等于 null", "随时可能被优化"]', '公司用什么就用什么，从不自己选', '生活的意义不在代码里，代码的意义也不在生活里。那意义在哪？在下班后。', 'images/cbti/characters/NULL.png', 0, 0, '2026-04-24 13:11:02', 0, '2026-04-24 21:41:43', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543816783048706, 'SENIOR', '面试造火箭', '手撕红黑树，入职增删改查。', '#7E22CE', '[2, 1, 2, 2, 1, 0, 1, 0, 2, 2, 1, 0, 0, 2, 2]', '恭喜您，您测出了互联网行业最具讽刺意味的人格——SENIOR。面试的时候手撕红黑树、现场推导 Raft 协议、白板画分布式系统架构、口述 JVM 内存模型——面试官频频点头：厉害，P7 水平。入职后做的第一件事是什么？给后台管理系统加了一个导出 Excel 的按钮。SENIOR 的简历写着「精通高并发高可用分布式系统」，日常工作是调 CSS 居中和对接第三方 SDK。SENIOR 的 LeetCode 有 500 道 Hard，工作中最复杂的算法是 Array.sort()。SENIOR 不是徒有虚名，SENIOR 的技术确实很强，只是公司的业务配不上 SENIOR 的能力。每当深夜独自加班改 Bug 时，SENIOR 会想起面试时那道动态规划题，那才是自己人生的巅峰时刻。', '["面试能力满分", "基础功底深厚", "算法思维强"]', '["日常工作与能力严重不匹配", "容易陷入「学了没用」的虚无"]', '面试用: 分布式+中间件+算法 / 实际用: CRUD+Excel', '世界上最遥远的距离，是面试和工作之间的距离。', 'images/cbti/characters/SENIOR.png', 0, 0, '2026-04-24 13:11:02', 0, '2026-04-24 21:41:43', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543817672241154, 'YAML', '配置工程师', '我不写代码，我写配置文件。', '#0891B2', '[2, 1, 2, 1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 1, 2]', '恭喜您，您测出了云原生时代最特殊的人格——YAML。在 YAML 的世界里，所有问题的答案都是一个配置文件。部署？写 YAML。服务发现？写 YAML。CI/CD？写 YAML。监控告警？还是 YAML。YAML 工程师已经很久没有写过一行「真正的代码」了，但这并不影响 YAML 成为团队里最不可或缺的人——因为没有人比 YAML 更懂基础设施。YAML 的屏幕上永远开着十几个终端窗口，里面跑着 kubectl、terraform、ansible。同事们写完代码丢过来一句「帮我部署一下」，YAML 微微一笑，打开了又一个 .yaml 文件。据说 YAML 做过的最噩梦是：缩进错了一个空格，整个集群挂了。', '["基础设施能力拉满", "稳定性意识强", "DevOps 全能"]', '["可能忘了怎么写业务代码", "YAML 缩进错误引发的 PTSD"]', 'Kubernetes + Docker + Terraform + YAML YAML YAML', 'Infrastructure as Code，我的人生也 as YAML。', 'images/cbti/characters/YAML.png', 0, 0, '2026-04-24 13:11:02', 0, '2026-04-24 21:41:43', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543818519490561, 'STACK', '八股文战神', 'HashMap 底层原理？问我就对了。', '#B45309', '[1, 1, 1, 1, 1, 0, 2, 0, 2, 2, 0, 0, 0, 2, 2]', '恭喜您，您测出了面试圈最令面试官满意的人格——STACK。STACK 的全名是 Stack Overflow of Knowledge，知识栈溢出者。STACK 能把 HashMap 的底层实现讲四十分钟不带喘气，能把 TCP 三次握手画出十九种变体图，能把 JVM 垃圾回收器的演进历史从 Serial 讲到 ZGC 不带打草稿。STACK 的牛客网刷题记录比很多人的朋友圈都活跃，STACK 的面经库可以出一本书。但问题来了：STACK 在实际工作中用过 HashMap 的红黑树优化吗？没有。用过 TCP 粘包拆包吗？也没有。那 STACK 的知识到底有什么用？答：涨薪。面试造航母，STACK 是航母总设计师。至于入职后拧螺丝，那是另一个故事了。', '["面试通过率极高", "知识面广到恐怖", "分享能力强"]', '["实战经验可能不匹配", "容易把简单问题复杂化"]', '所有你能想到的技术，至少背过一遍面试题', '我可能没用过这个技术，但我能把它讲得比用过的人还好。', 'images/cbti/characters/STACK.png', 0, 0, '2026-04-24 13:11:03', 0, '2026-04-24 21:41:43', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543819396100098, 'SLEEP', 'Deadline觉醒者', '我没死，我只是在等 deadline。', '#6B7280', '[1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0]', '恭喜您，您测出了全中国最节能的人格——SLEEP。群里 99+ 条消息可以完全无视，JIRA 上的 ticket 可以挂到发霉，周报可以复制上周的（反正也没人看）。SLEEP 的工作状态有且仅有两种：休眠态和觉醒态。休眠态占据了一个迭代的 95%——在这个阶段，SLEEP 看起来像一个已经离职但工牌还没交的幽灵员工。但当 deadline 倒计时开始、当领导开始 @ 全体成员、当那封「请尽快提交」的邮件到达的时候，SLEEP 的 CPU 突然满载了。从休眠态到觉醒态的切换只需要 0.3 秒。然后，在最后 48 小时内，SLEEP 会交出一份不算优秀但绝对够用的成果，然后再次进入下一轮休眠。不鸣则已，一鸣也就那样。', '["压力转化效率极高", "从不做无用功", "心理韧性强"]', '["平时产出约等于零", "领导可能随时暴走"]', '任何能快速出活的（取决于觉醒后还剩多少时间）', '什么都不做，就不会做错。直到不做不行。', 'images/cbti/characters/SLEEP.png', 0, 0, '2026-04-24 13:11:03', 0, '2026-04-24 21:41:43', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543820302069762, 'FORK', '开源圣体', '不要 star，要 PR。', '#15803D', '[2, 2, 2, 2, 1, 2, 1, 1, 2, 2, 2, 2, 0, 1, 2]', '恭喜您，您测出了编程界最理想主义的人格——FORK。FORK 相信代码应该属于全人类，知识不应该被封锁在公司的私有仓库里。FORK 的 GitHub 主页像一座花园：精心维护的开源项目排列整齐，每个项目都有漂亮的 README、完整的 CI/CD、规范的 Contributing Guide。FORK 会认真回复每一个 Issue，耐心 Review 每一个 PR，哪怕那个 PR 只是修了一个 typo。FORK 的夜晚属于开源社区。白天在公司写的是「谋生的代码」，晚上回家写的才是「梦想的代码」。FORK 坚信一件事：也许改变世界不需要创业融资上市，只需要一个 git push origin main。', '["技术理想主义者", "社区影响力大", "代码质量高"]', '["开源投入可能影响本职工作", "理想主义有时撞上现实"]', 'GitHub + Open Source Everything', 'Talk is cheap. Show me the pull request.', 'images/cbti/characters/FORK.png', 0, 0, '2026-04-24 13:11:03', 0, '2026-04-24 21:41:43', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543821153513473, 'AGILE', '敏捷话术大师', '让我们做一个 retro，sync一下 blocker。', '#0D9488', '[1, 1, 1, 1, 1, 1, 2, 2, 2, 0, 0, 0, 1, 1, 1]', '恭喜您，您测出了编程界最「专业」的人格——AGILE。注意，这里的「专业」指的不是技术专业，而是开会专业。AGILE 的一天是这样的：9:00 站会 → 9:30 需求评审 → 10:30 Sprint Planning → 11:30 和产品 sync → 午饭 → 13:30 跨团队对齐 → 14:30 Retro → 15:30 One-on-One → 16:30 Demo → 17:00 发现今天还没写一行代码。AGILE 的嘴里永远挂着各种英文术语：blocker、bandwidth、stakeholder、alignment、velocity、burndown chart。但你问 AGILE 这些词到底是什么意思？AGILE 会微笑着回答：Let me circle back on that。AGILE 不是不会写代码，AGILE 只是把所有时间都花在了「确保大家知道要写什么代码」上。至于代码什么时候写？Next sprint。', '["项目管理能力强", "流程意识好", "沟通能力拉满"]', '["可能一天下来没写一行代码", "术语多到同事想静音"]', 'JIRA + Confluence + 无穷无尽的会议', '让我们不要在这里讨论这个问题，我会创建一个 ticket 来跟进。', 'images/cbti/characters/AGILE.png', 0, 0, '2026-04-24 13:11:03', 0, '2026-04-24 21:41:44', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543822000762882, 'REGEX', '正则之神', '/^(?=.*我)(?=.*理解)(?=.*正则).+$/g — 匹配失败', '#4338CA', '[2, 2, 2, 2, 2, 0, 0, 0, 0, 2, 0, 1, 0, 1, 2]', '恭喜您，您测出了编程界最深不可测的人格——REGEX。据说世界上有两类程序员：一类不会写正则表达式，另一类写了正则表达式但下次看也看不懂了。REGEX 属于第三类：不仅看得懂，还特别享受。REGEX 解决问题的方式永远是先写一个正则。解析日志？正则。校验邮箱？正则。提取数据？正则。同事问怎么做字符串匹配？REGEX 沉思了 0.5 秒然后甩出一行 /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/g。同事看了三分钟表示完全看不懂。REGEX 说：这很简单啊。REGEX 的代码只有 REGEX 自己看得懂，但运行效率确实很高。REGEX 是孤独的，因为理解 REGEX 的人，全世界可能不超过 100 个。', '["文本处理能力无敌", "逻辑极度严谨", "技术深度恐怖"]', '["写出来的东西没人能维护", "社交属性约等于零"]', 'Vim + grep + sed + awk + 无穷的正则', '有些人用正则解决一个问题，然后他们就有了两个问题。我不一样，我有零个。', 'images/cbti/characters/REGEX.png', 0, 0, '2026-04-24 13:11:03', 0, '2026-04-24 21:41:44', 0);
INSERT INTO cbti_personality (id, code, name, motto, color, vector, description, strengths, weaknesses, tech_stack, spirit, image_object, is_special, create_user, create_time, update_user, update_time, is_deleted) VALUES (2047543822852206593, 'JAVA', '咖啡因驱动开发者', 'public static void main(Coffee[] args) { drink(); }', '#92400E', '[-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1]', '传说在硅谷的某个角落，有一种程序员，他们的血管里流淌的不是血液，是现磨美式。他们的心跳频率与咖啡机的滴漏声同步，他们的 Bug 修复速度与咖啡因浓度呈正相关。你就是这样的存在——JAVA 人格，咖啡因驱动开发者。是的，这里的 JAVA 不是那个编程语言（虽然 Java 的 Logo 也是一杯咖啡），这里的 JAVA 指的是：Just Another Victim of Americano。你的日程表不是按小时划分的，是按"第几杯咖啡"划分的。第一杯：系统启动。第二杯：进入工作流。第三杯：代码如神。第四杯：心跳加速但思路更清晰。第五杯：手在抖但代码还在写。同事们已经学会通过你桌上咖啡杯的数量来判断现在该不该找你汇报问题。一杯的时候：可以。三杯的时候：最好等等。五杯的时候：保持距离，这人已经不是人了。', '["续航能力强（咖啡因加持）", "咖啡品鉴能力SSS级", "深夜战斗力极强"]', '["离开咖啡战斗力归零", "牙齿可能不太白", "可能产生咖啡依赖"]', 'CoffeeScript（字面意义上的）', '给我一杯咖啡，我能撬动整个代码库。给我第二杯，我能把它重写。给我第三杯......医生说不能再喝了。', 'images/cbti/characters/JAVA.png', 1, 0, '2026-04-24 13:11:04', 0, '2026-04-24 21:41:44', 0);


-- ============================================================
-- Source: 2026-04-19_create_sys_menu.sql
-- ============================================================

-- 系统菜单表（后端菜单模式）
-- 创建时间: 2026-04-19
-- 作者: Ethan
-- 更新功能简介:
-- 1) 新增 sys_menu 表，支持树形菜单、角色可见性、启用状态、排序与逻辑删除
-- 2) 提供默认菜单种子数据，覆盖首页/业务模块/系统管理等基础导航
-- 3) 配合前端 backend 菜单模式，实现“后台配置菜单，前端动态渲染”

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID（0为根）',
    `name` VARCHAR(64) NOT NULL COMMENT '路由名称',
    `path` VARCHAR(255) NOT NULL COMMENT '路由路径（唯一）',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '组件标识（BasicLayout/IFrameView 或 views 相对路径，如 system/user/index）',
    `redirect` VARCHAR(255) DEFAULT NULL COMMENT '重定向',
    `meta` JSON DEFAULT NULL COMMENT '路由 meta（title/icon/order/keepAlive/hideInMenu/link等）',
    `roles` VARCHAR(255) DEFAULT NULL COMMENT '可访问角色（逗号分隔，空表示所有）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，0禁用）',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` INT DEFAULT 0 COMMENT '是否删除(0-否,1-是)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_menu_path` (`path`),
    INDEX `idx_sys_menu_parent_id` (`parent_id`),
    INDEX `idx_sys_menu_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

-- 初始化种子（覆盖式插入由应用层处理；这里仅提供默认菜单）
-- 说明：component 为 views 相对路径（不带 .vue），前端会映射到 /views/**.vue

INSERT IGNORE INTO `sys_menu` (`id`,`parent_id`,`name`,`path`,`component`,`redirect`,`meta`,`roles`,`sort`,`status`,`is_deleted`)
VALUES
-- 主页（置顶）
(1001, 0, 'Analytics', '/analytics', 'dashboard/home/index', NULL,
 JSON_OBJECT('order', -1, 'affixTab', true, 'icon', 'lucide:home', 'title', '主页', 'keepAlive', true, 'maxIdleTime', 60),
 NULL, -1, 1, 0),

-- 仪表盘分组
(1100, 0, 'Dashboard', '/dashboard', 'BasicLayout', NULL,
 JSON_OBJECT('icon','lucide:layout-dashboard','order',0,'title','仪表盘'),
 NULL, 0, 1, 0),
(1101, 1100, 'Workspace', '/workspace', 'dashboard/workspace/index', NULL,
 JSON_OBJECT('icon','carbon:workspace','title','工作台'),
 NULL, 0, 1, 0),

-- 任务中心
(1200, 0, 'TaskCenter', '/task-center', 'BasicLayout', NULL,
 JSON_OBJECT('icon','mdi:clipboard-text-clock-outline','title','任务中心','order',1),
 NULL, 1, 1, 0),
(1201, 1200, 'TaskCenterTodo', '/task-center/todo', 'task-center/todo/index', NULL,
 JSON_OBJECT('icon','mdi:format-list-checks','title','待办'),
 NULL, 0, 1, 0),

-- 时间
(1300, 0, 'TimeManagement', '/time-management', 'BasicLayout', NULL,
 JSON_OBJECT('icon','mdi:clock-outline','title','时间','order',2,'keepAlive',true),
 NULL, 2, 1, 0),
(1301, 1300, 'TimeTracker', '/time-management/time-tracker', 'time-management/time-tracker/index', NULL,
 JSON_OBJECT('icon','mdi:history','title','时迹','backTop',false,'keepAlive',true,'maxIdleTime',60),
 NULL, 0, 1, 0),
(1302, 1300, 'TimeTrackerDashboard', '/time-management/dashboard', 'time-management/dashboard/index', NULL,
 JSON_OBJECT('icon','mdi:view-dashboard-outline','title','看板','backTop',false,'keepAlive',true),
 NULL, 1, 1, 0),
(1303, 1300, 'CategoryConfig', '/time-management/my-categories', 'time-management/time-tracker/category-config/index', NULL,
 JSON_OBJECT('icon','mdi:tag-multiple-outline','title','我的分类','backTop',false),
 NULL, 2, 1, 0),
(1304, 1300, 'TimeTrackerCategoryAdmin', '/time-management/category-admin', 'time-management/time-tracker/admin/index', NULL,
 JSON_OBJECT('icon','mdi:shield-account-outline','title','分类管理（管理员）','backTop',false),
 'admin', 3, 1, 0),

-- 编程看板
(1400, 0, 'Coding', '/coding', 'BasicLayout', NULL,
 JSON_OBJECT('icon','lucide:code-2','title','编程看板','order',3,'keepAlive',true),
 NULL, 3, 1, 0),
(1401, 1400, 'GithubGraph', '/coding/github', 'coding/github/index', NULL,
 JSON_OBJECT('icon','mdi:github','title','Github','backTop',false),
 NULL, 0, 1, 0),
(1402, 1400, 'LeetCode', '/coding/leetcode', 'coding/leetcode/index', NULL,
 JSON_OBJECT('icon','simple-icons:leetcode','title','LeetCode'),
 NULL, 1, 1, 0),

-- 记录
(1500, 0, 'Demos', '/my-hub', 'BasicLayout', NULL,
 JSON_OBJECT('icon','ic:baseline-view-in-ar','title','记录','order',3,'keepAlive',true),
 NULL, 4, 1, 0),
(1501, 1500, 'exercise', '/my-hub/exercise', 'my-hub/exercise/index', NULL,
 JSON_OBJECT('icon','mdi:run-fast','title','运动','backTop',false),
 NULL, 0, 1, 0),
(1502, 1500, 'videoWatch', '/my-hub/videoWatch', 'my-hub/videoWatch/index', NULL,
 JSON_OBJECT('icon','mdi:video-vintage','title','视频观看','backTop',false),
 NULL, 1, 1, 0),
(1503, 1500, 'think', '/my-hub/think', 'my-hub/think/index', NULL,
 JSON_OBJECT('icon','mdi:lightbulb-on-outline','title','闪念','backTop',false),
 NULL, 2, 1, 0),
(1504, 1500, 'memo', '/my-hub/memo', 'my-hub/memo/index', NULL,
 JSON_OBJECT('icon','mdi:note-text-outline','title','笔记','backTop',false),
 NULL, 3, 1, 0),
(1505, 1500, 'performance', '/my-hub/performance', 'my-hub/performance/index', NULL,
 JSON_OBJECT('icon','mdi:chart-line-variant','title','活动','backTop',false),
 NULL, 4, 1, 0),
(1506, 1500, 'milestone', '/my-hub/milestone', 'my-hub/milestone/index', NULL,
 JSON_OBJECT('icon','mdi:flag-variant','title','里程碑','backTop',false),
 NULL, 5, 1, 0),
(1507, 1500, 'anniversary', '/my-hub/anniversary', 'my-hub/anniversary/index', NULL,
 JSON_OBJECT('icon','mdi:calendar-heart','title','纪念日','backTop',false),
 NULL, 6, 1, 0),

-- 财务中心
(1600, 0, 'FinanceManagement', '/finance-management', 'BasicLayout', NULL,
 JSON_OBJECT('icon','mdi:finance','title','财务中心','order',4,'keepAlive',true),
 NULL, 5, 1, 0),
(1601, 1600, 'financeDashboard', '/finance-management/dashboard', 'my-hub/finance-dashboard/index', NULL,
 JSON_OBJECT('icon','mdi:chart-areaspline','title','概览','backTop',false),
 NULL, 0, 1, 0),
(1602, 1600, 'incomeManagement', '/finance-management/income', 'my-hub/income/index', NULL,
 JSON_OBJECT('icon','mdi:cash-plus','title','收入','backTop',false),
 NULL, 1, 1, 0),
(1603, 1600, 'expenseManagement', '/finance-management/expense', 'my-hub/expense/index', NULL,
 JSON_OBJECT('icon','mdi:cash-minus','title','支出','backTop',false),
 NULL, 2, 1, 0),
(1604, 1600, 'alipayImport', '/finance-management/import', 'my-hub/expense/import', NULL,
 JSON_OBJECT('icon','mdi:import','title','账单导入','backTop',false),
 NULL, 3, 1, 0),

-- 物品中心
(1700, 0, 'Goods', '/goods', 'BasicLayout', NULL,
 JSON_OBJECT('icon','lucide:package','title','物品中心','order',5,'keepAlive',true),
 NULL, 6, 1, 0),
(1701, 1700, 'device', '/my-hub/device', 'my-hub/device/index', NULL,
 JSON_OBJECT('icon','mdi:monitor-dashboard','title','设备墙','backTop',false),
 NULL, 0, 1, 0),

-- 配置管理（admin）
(1800, 0, 'ConfigManagement', '/config-management', 'BasicLayout', NULL,
 JSON_OBJECT('icon','mdi:cog-outline','title','配置管理','order',9,'keepAlive',true),
 'admin', 9, 1, 0),
(1801, 1800, 'sysDictType', '/config-management/sysDictType', 'config-management/sysDictType/index', NULL,
 JSON_OBJECT('icon','mdi:book-settings-outline','title','字典类型','backTop',false),
 'admin', 0, 1, 0),
(1802, 1800, 'sysDictData', '/config-management/sysDictData', 'config-management/sysDictData/index', NULL,
 JSON_OBJECT('icon','mdi:database-search-outline','title','字典数据','backTop',false),
 'admin', 1, 1, 0),

-- 系统管理（admin）
(1900, 0, 'System', '/system', 'BasicLayout', NULL,
 JSON_OBJECT('icon','mdi:shield-account-outline','title','系统管理','order',10,'keepAlive',true),
 'admin', 10, 1, 0),
(1901, 1900, 'UserCenter', '/system/user', 'system/user/index', NULL,
 JSON_OBJECT('icon','mdi:account-group-outline','title','用户中心'),
 'admin', 0, 1, 0),
(1902, 1900, 'MenuManagement', '/system/menu', 'system/menu/index', NULL,
 JSON_OBJECT('icon','mdi:menu-open','title','权限菜单'),
 'admin', 1, 1, 0),

-- 消息中心
(2000, 0, 'Message', '/message', 'message/index', NULL,
 JSON_OBJECT('icon','ant-design:message-outlined','title','消息中心','order',10,'hideInMenu',false,'fullPathKey',false),
 NULL, 10, 1, 0),

-- 关于（admin）与个人中心（隐藏）
(2100, 0, 'VbenAbout', '/vben-admin/about', '_core/about/index', NULL,
 JSON_OBJECT('icon','lucide:copyright','title','关于','order',9999),
 'admin', 9999, 1, 0),
(2200, 0, 'Profile', '/profile', '_core/profile/index', NULL,
 JSON_OBJECT('icon','lucide:user','title','个人中心','hideInMenu',true),
 NULL, 9998, 1, 0);


-- ============================================================
-- Source: 2026-04-28_create_password_vault.sql
-- ============================================================

-- 密码库表
CREATE TABLE `password_vault` (
  `id` BIGINT NOT NULL COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题，如 GitHub',
  `website` VARCHAR(255) COMMENT '网站/应用名',
  `category` VARCHAR(50) DEFAULT '其他' COMMENT '分类：工作/生活/金融/社交/其他',
  `username` TEXT COMMENT '账号（SM4加密存储）',
  `password` TEXT COMMENT '密码（SM4加密存储）',
  `salt` VARCHAR(64) NOT NULL COMMENT 'PBKDF2盐值，每条记录唯一',
  `remark` TEXT COMMENT '备注（SM4加密存储）',
  `favorite` BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `create_user` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_user` BIGINT DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码库表';


-- ============================================================
-- Source: init_user.sql
-- ============================================================

-- 插入默认管理员账户，账号：aio-life 密码：aiolife123
INSERT INTO `aio-life`.user (id, username, password, nickname, created_at, updated_at, email, avatar, password_salt, role, introduction, is_deleted) VALUES (2028089890152828929, 'aio-life', '8ee41ff507c13ab9169ed485db69cfbb', 'aio-life', '2026-03-01 20:48:05', '2026-03-01 20:48:05', '', null, 'fca746ea79c84f69a897b313b374968e', 'admin', null, 0);


SET FOREIGN_KEY_CHECKS = 1;
