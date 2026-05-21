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

CREATE TABLE IF NOT EXISTS `llm_key` (
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

CREATE OR REPLACE VIEW `conversation` AS
select
  `chat_session`.`id` AS `id`,
  `chat_session`.`user_id` AS `user_id`,
  `chat_session`.`title` AS `title`,
  `chat_session`.`create_user` AS `create_user`,
  `chat_session`.`create_time` AS `create_time`,
  `chat_session`.`update_user` AS `update_user`,
  `chat_session`.`update_time` AS `update_time`,
  `chat_session`.`is_deleted` AS `is_deleted`
from `chat_session`;


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

CREATE TABLE IF NOT EXISTS `goal` (
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
CREATE TABLE IF NOT EXISTS `honor_category` (
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

-- 荣誉记录表
CREATE TABLE IF NOT EXISTS `honor_record` (
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
CREATE TABLE IF NOT EXISTS `anniversary_record` (
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



-- ============================================================
-- Source: 2026-04-28_create_password_vault.sql
-- ============================================================

-- 密码库表
CREATE TABLE IF NOT EXISTS `password_vault` (
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
