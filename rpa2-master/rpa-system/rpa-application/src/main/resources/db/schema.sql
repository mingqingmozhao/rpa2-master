-- 创建数据库
CREATE DATABASE IF NOT EXISTS financial_data_collect DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE financial_data_collect;

-- ===================== 1. 系统管理模块：用户 + 角色 + 权限 =====================

-- 1.1 用户表（对应用户登录、个人信息管理、账号禁用）
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键 ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号 (唯一)',
  `password` VARCHAR(100) NOT NULL COMMENT '加密密码 (BCrypt/MD5)',
  `real_name` VARCHAR(30) DEFAULT '' COMMENT '真实姓名',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '头像地址',
  `phone` VARCHAR(11) DEFAULT '' COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT '' COMMENT '邮箱',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) COMMENT '账号唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 1.2 角色表（角色管理）
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色主键 ID',
  `role_name` VARCHAR(30) NOT NULL COMMENT '角色名称 (管理员/运维/业务)',
  `remark` VARCHAR(100) DEFAULT '' COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 1.3 权限表（菜单/按钮级权限管理）
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限主键 ID',
  `perm_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `perm_key` VARCHAR(100) NOT NULL COMMENT '权限标识 (如:task:add)',
  `perm_type` TINYINT NOT NULL COMMENT '权限类型：1-菜单 2-按钮',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限 ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_key` (`perm_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 1.4 用户 - 角色关联表（多对多）
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `role_id` BIGINT NOT NULL COMMENT '角色 ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 1.5 角色 - 权限关联表（多对多）
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色 ID',
  `perm_id` BIGINT NOT NULL COMMENT '权限 ID',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_perm_id` (`perm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ===================== 2. 核心配置模块：流程 + 机器人 =====================

-- 2.1 流程定义表（流程定义、设计、维护）
CREATE TABLE IF NOT EXISTS `process_def` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流程主键 ID',
  `process_code` VARCHAR(50) NOT NULL COMMENT '流程编码 (唯一)',
  `process_name` VARCHAR(50) NOT NULL COMMENT '流程名称',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `collect_script` TEXT COMMENT '采集脚本',
  `parse_script` TEXT COMMENT '解析脚本',
  `process_script` TEXT COMMENT '加工脚本',
  `save_script` TEXT COMMENT '落库脚本',
  `create_user` BIGINT COMMENT '创建人 (运维)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_code` (`process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- 2.2 机器人信息表（机器人新增、维护、监控）
CREATE TABLE IF NOT EXISTS `robot_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '机器人主键 ID',
  `robot_code` VARCHAR(50) NOT NULL COMMENT '机器人编码 (唯一)',
  `robot_name` VARCHAR(50) NOT NULL COMMENT '机器人名称',
  `robot_type` VARCHAR(20) DEFAULT '' COMMENT '机器人类型',
  `ip` VARCHAR(20) NOT NULL COMMENT 'IP 地址',
  `port` INT NOT NULL COMMENT '端口',
  `execute_thread` VARCHAR(50) COMMENT '系统分配的专属执行线程',
  `status` TINYINT DEFAULT 0 COMMENT '状态：1-在线 2-工作中 0-离线',
  `last_heartbeat` DATETIME COMMENT '最后心跳时间',
  `cpu_usage` DECIMAL(5,2) DEFAULT 0 COMMENT 'CPU 使用率',
  `mem_usage` DECIMAL(5,2) DEFAULT 0 COMMENT '内存使用率',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_robot_code` (`robot_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器人信息表';

-- ===================== 3. 任务管理模块 =====================

-- 3.1 任务表（任务新增、维护、查询、执行）
CREATE TABLE IF NOT EXISTS `task_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务主键 ID',
  `task_code` VARCHAR(50) NOT NULL COMMENT '任务编码 (唯一)',
  `task_name` VARCHAR(50) NOT NULL COMMENT '任务名称',
  `process_id` BIGINT NOT NULL COMMENT '关联流程 ID',
  `robot_id` BIGINT DEFAULT 0 COMMENT '关联机器人 ID',
  `company_name` VARCHAR(100) DEFAULT '' COMMENT '企业名称',
  `tax_no` VARCHAR(20) DEFAULT '' COMMENT '纳税人识别号 (税号)',
  `status` TINYINT DEFAULT 1 COMMENT '任务状态：1-正常 0-停用',
  `create_user` BIGINT COMMENT '创建人 (业务)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_code` (`task_code`),
  KEY `idx_process_id` (`process_id`),
  KEY `idx_robot_id` (`robot_id`),
  KEY `idx_tax_no` (`tax_no`),
  KEY `idx_company_name` (`company_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务信息表';

-- ===================== 4. 执行监控模块 =====================

-- 4.1 任务执行主记录表（执行记录查询、状态监控）
CREATE TABLE IF NOT EXISTS `task_execute` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '执行记录 ID',
  `task_id` BIGINT NOT NULL COMMENT '任务 ID',
  `process_id` BIGINT NOT NULL COMMENT '流程 ID',
  `robot_id` BIGINT NOT NULL COMMENT '执行机器人 ID',
  `execute_status` TINYINT NOT NULL COMMENT '执行状态：1-待执行 2-运行中 3-完成 4-失败',
  `cost_time` BIGINT DEFAULT 0 COMMENT '执行耗时 (毫秒)',
  `error_msg` TEXT COMMENT '错误信息',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_execute_status` (`execute_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行记录表';

-- 4.2 任务执行日志表（详细日志记录）
CREATE TABLE IF NOT EXISTS `task_execute_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `execute_id` BIGINT NOT NULL COMMENT '执行记录 ID',
  `log_level` VARCHAR(10) DEFAULT 'INFO' COMMENT '日志级别',
  `log_message` TEXT COMMENT '日志内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_execute_id` (`execute_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

-- ===================== 5. 数据管理模块 =====================

-- 5.1 数据采集表
CREATE TABLE IF NOT EXISTS `data_collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '采集 ID',
  `task_id` BIGINT NOT NULL COMMENT '任务 ID',
  `source_url` VARCHAR(255) COMMENT '数据来源 URL',
  `raw_data` LONGTEXT COMMENT '原始数据',
  `collect_status` TINYINT DEFAULT 0 COMMENT '采集状态：0-待采集 1-采集中 2-完成 3-失败',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据采集表';

-- 5.2 数据解析表
CREATE TABLE IF NOT EXISTS `data_parsed` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '解析 ID',
  `collection_id` BIGINT NOT NULL COMMENT '采集 ID',
  `parsed_data` LONGTEXT COMMENT '解析后数据',
  `parse_status` TINYINT DEFAULT 0 COMMENT '解析状态：0-待解析 1-解析中 2-完成 3-失败',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_collection_id` (`collection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据解析表';

-- 5.3 数据加工表
CREATE TABLE IF NOT EXISTS `data_processed` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '加工 ID',
  `parsed_id` BIGINT NOT NULL COMMENT '解析 ID',
  `processed_data` LONGTEXT COMMENT '加工后数据',
  `process_status` TINYINT DEFAULT 0 COMMENT '加工状态：0-待加工 1-加工中 2-完成 3-失败',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parsed_id` (`parsed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据加工表';

-- 5.4 数据落库表（发票数据）
CREATE TABLE IF NOT EXISTS `data_invoice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '发票 ID',
  `task_id` BIGINT NOT NULL COMMENT '任务 ID',
  `invoice_code` VARCHAR(20) COMMENT '发票代码',
  `invoice_no` VARCHAR(20) COMMENT '发票号码',
  `invoice_date` DATE COMMENT '开票日期',
  `check_code` VARCHAR(50) COMMENT '校验码',
  `machine_no` VARCHAR(50) COMMENT '机器编号',
  `buyer_name` VARCHAR(100) COMMENT '购买方名称',
  `buyer_tax_no` VARCHAR(20) COMMENT '购买方税号',
  `seller_name` VARCHAR(100) COMMENT '销售方名称',
  `seller_tax_no` VARCHAR(20) COMMENT '销售方税号',
  `amount` DECIMAL(18,2) DEFAULT 0 COMMENT '金额',
  `tax_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '税额',
  `total_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '价税合计',
  `invoice_type` VARCHAR(20) COMMENT '发票类型',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_invoice_code_no` (`invoice_code`, `invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票数据表';

-- ===================== 完成提示 =====================
SELECT '========================================' AS '';
SELECT '数据库表结构创建完成！' AS message;
SELECT '========================================' AS '';
