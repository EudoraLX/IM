-- 智能营销管理平台数据库表结构

-- 创建数据库
CREATE DATABASE IF NOT EXISTS imdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE imdb;

-- 线索表
CREATE TABLE IF NOT EXISTS `lead` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `lead_no` VARCHAR(50) NOT NULL COMMENT '线索编号',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `id_card_hash` VARCHAR(64) COMMENT '身份证号哈希值',
    `source_channel` VARCHAR(50) COMMENT '来源渠道',
    `status` VARCHAR(20) DEFAULT '新线索' COMMENT '状态：新线索、跟进中、已转化、已失效',
    `is_high_potential` TINYINT(1) DEFAULT 0 COMMENT '是否已转换为高潜线索：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lead_no` (`lead_no`),
    KEY `idx_contact_phone` (`contact_phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索表';

-- 监控指标配置表
CREATE TABLE IF NOT EXISTS `monitor_indicator` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `time_range_days` INT(11) DEFAULT 7 COMMENT '监控时间范围（天数）',
    `active_organizations` TEXT COMMENT '活跃机构（JSON格式存储多个机构）',
    `activity_threshold` INT(11) DEFAULT 3 COMMENT '活跃次数阈值',
    `status` TINYINT(1) DEFAULT 1 COMMENT '配置状态：0-未启用，1-已启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控指标配置表';

-- 推送频率配置表
CREATE TABLE IF NOT EXISTS `push_frequency` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `monitor_indicator_id` BIGINT(20) COMMENT '监控指标ID',
    `frequency_type` VARCHAR(20) DEFAULT 'daily' COMMENT '频率类型：hourly-每小时，daily-每日，weekly-每周，monthly-每月，custom-自定义',
    `push_interval` INT(11) DEFAULT 15 COMMENT '推送间隔（分钟）',
    `frequency_strategy` TEXT COMMENT '频率策略配置（JSON格式，存储每周分布）',
    `weekend_rule` TINYINT(1) DEFAULT 1 COMMENT '周末规则：0-不启用，1-启用',
    `min_push_threshold` INT(11) DEFAULT 30 COMMENT '最小推送阈值（秒）',
    `status` TINYINT(1) DEFAULT 1 COMMENT '配置状态：0-未启用，1-已启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送频率配置表';

-- 高潜线索表
CREATE TABLE IF NOT EXISTS `high_potential_lead` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `lead_id` BIGINT(20) NOT NULL COMMENT '线索ID',
    `user_id` VARCHAR(50) COMMENT '用户ID',
    `matching_rule` VARCHAR(100) COMMENT '命中规则',
    `lead_source_system` VARCHAR(100) COMMENT '线索来源系统',
    `matching_date` DATETIME COMMENT '命中日期',
    `status` VARCHAR(20) DEFAULT '待营销处理' COMMENT '状态：待营销处理、营销进行中、营销已转化、已联系失效',
    `high_potential_reason` VARCHAR(500) COMMENT '高潜原因',
    `lead_level` VARCHAR(10) DEFAULT 'B' COMMENT '线索分级：A-高潜，B-中潜，C-低潜',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_lead_id` (`lead_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_matching_date` (`matching_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高潜线索表';

-- 插入默认监控指标配置（冷启动）
INSERT INTO `monitor_indicator` (`time_range_days`, `active_organizations`, `activity_threshold`, `status`) 
VALUES (7, '["华东中心机构", "上海研发分部"]', 3, 1);

-- 活跃次数记录表
CREATE TABLE IF NOT EXISTS `activity_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `lead_no` VARCHAR(50) NOT NULL COMMENT '线索编号',
    `lead_id` BIGINT(20) COMMENT '线索ID（冗余字段，用于快速查询）',
    `activity_type` VARCHAR(50) DEFAULT '测试' COMMENT '活跃类型：测试、人脸验证、OCR验证、登录等',
    `organization` VARCHAR(100) COMMENT '活跃机构',
    `activity_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '活跃时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_lead_no` (`lead_no`),
    KEY `idx_lead_id` (`lead_id`),
    KEY `idx_activity_time` (`activity_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活跃次数记录表';

-- 插入默认推送频率配置（冷启动）
INSERT INTO `push_frequency` (`frequency_type`, `push_interval`, `weekend_rule`, `min_push_threshold`, `status`) 
VALUES ('daily', 15, 1, 30, 1);

