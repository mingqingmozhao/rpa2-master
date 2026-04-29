-- 创建数据库（仅首次使用）
CREATE DATABASE IF NOT EXISTS financial_data_collect DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE financial_data_collect;

-- ===================== 1. 系统管理模块：用户 + 角色 + 权限 =====================

-- 1.1 用户表
CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键 ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号 (唯一)',
  password VARCHAR(100) NOT NULL COMMENT '加密密码 (BCrypt)',
  real_name VARCHAR(30) DEFAULT '' COMMENT '真实姓名',
  avatar VARCHAR(255) DEFAULT '' COMMENT '头像地址',
  phone VARCHAR(11) DEFAULT '' COMMENT '手机号',
  email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
  status INT NOT NULL DEFAULT 1 COMMENT '账号状态：1-启用 0-禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 1.2 角色表
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色主键 ID',
  role_code VARCHAR(50) COMMENT '角色编码（唯一）',
  role_name VARCHAR(30) NOT NULL COMMENT '角色名称',
  remark VARCHAR(100) DEFAULT '' COMMENT '角色描述',
  status INT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_name (role_name),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 1.3 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限主键 ID',
  perm_name VARCHAR(50) NOT NULL COMMENT '权限名称',
  perm_key VARCHAR(100) NOT NULL COMMENT '权限标识',
  perm_type INT NOT NULL COMMENT '权限类型：1-菜单 2-按钮',
  parent_id BIGINT DEFAULT 0 COMMENT '父权限 ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_key (perm_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 1.4 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 1.5 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  perm_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_role_id (role_id),
  KEY idx_perm_id (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ===================== 2. 核心配置模块：流程 + 机器人 =====================

-- 2.1 流程定义表
CREATE TABLE IF NOT EXISTS process_def (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '流程主键 ID',
  process_code VARCHAR(50) NOT NULL COMMENT '流程编码 (唯一)',
  process_name VARCHAR(50) NOT NULL COMMENT '流程名称',
  description VARCHAR(255) DEFAULT '' COMMENT '流程描述',
  steps INT DEFAULT 0 COMMENT '步骤数',
  status INT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  collect_script TEXT COMMENT '采集脚本',
  parse_script TEXT COMMENT '解析脚本',
  process_script TEXT COMMENT '加工脚本',
  save_script TEXT COMMENT '落库脚本',
  create_user BIGINT COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_process_code (process_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- 2.2 机器人信息表
CREATE TABLE IF NOT EXISTS robot_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '机器人主键 ID',
  robot_code VARCHAR(50) NOT NULL COMMENT '机器人编码 (唯一)',
  robot_name VARCHAR(50) NOT NULL COMMENT '机器人名称',
  robot_type VARCHAR(20) DEFAULT '' COMMENT '机器人类型',
  ip VARCHAR(20) NOT NULL COMMENT 'IP 地址',
  port INT NOT NULL COMMENT '端口',
  execute_thread VARCHAR(50) COMMENT '执行线程',
  status INT DEFAULT 0 COMMENT '状态：1-在线 2-工作中 0-离线',
  current_task_id BIGINT COMMENT '当前执行任务 ID',
  last_heartbeat DATETIME COMMENT '最后心跳时间',
  cpu_usage DECIMAL(5,2) DEFAULT 0 COMMENT 'CPU 使用率',
  mem_usage DECIMAL(5,2) DEFAULT 0 COMMENT '内存使用率',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_robot_code (robot_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器人信息表';

-- ===================== 3. 任务管理模块 =====================

-- 3.1 任务表
CREATE TABLE IF NOT EXISTS task_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务主键 ID',
  task_code VARCHAR(50) NOT NULL COMMENT '任务编码 (唯一)',
  task_name VARCHAR(50) NOT NULL COMMENT '任务名称',
  process_id BIGINT NOT NULL COMMENT '关联流程 ID',
  robot_id BIGINT DEFAULT 0 COMMENT '关联机器人 ID',
  enterprise_name VARCHAR(100) DEFAULT '' COMMENT '企业名称',
  tax_no VARCHAR(20) DEFAULT '' COMMENT '纳税人识别号',
  status INT DEFAULT 1 COMMENT '任务状态：1-正常 0-停用',
  create_user BIGINT COMMENT '创建人',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_code (task_code),
  KEY idx_process_id (process_id),
  KEY idx_robot_id (robot_id),
  KEY idx_tax_no (tax_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务信息表';

-- ===================== 4. 执行监控模块 =====================

-- 4.1 任务执行主记录表
CREATE TABLE IF NOT EXISTS task_execute (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '执行记录 ID',
  task_id BIGINT NOT NULL COMMENT '任务 ID',
  process_id BIGINT NOT NULL COMMENT '流程 ID',
  robot_id BIGINT NOT NULL COMMENT '执行机器人 ID',
  execute_status INT NOT NULL COMMENT '执行状态：1-待执行 2-运行中 3-完成 4-失败',
  cost_time BIGINT DEFAULT 0 COMMENT '执行耗时 (毫秒)',
  error_msg TEXT COMMENT '错误信息',
  start_time DATETIME DEFAULT NULL,
  end_time DATETIME DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_task_id (task_id),
  KEY idx_execute_status (execute_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行记录表';

-- 4.2 任务执行日志表
CREATE TABLE IF NOT EXISTS task_execute_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  execute_id BIGINT NOT NULL,
  log_level VARCHAR(10) DEFAULT 'INFO',
  log_message TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_execute_id (execute_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

-- ===================== 5. 数据管理模块 =====================

-- 5.1 数据采集表
CREATE TABLE IF NOT EXISTS data_collection (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  source_url VARCHAR(255),
  raw_data LONGTEXT,
  collect_status INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.2 数据解析表
CREATE TABLE IF NOT EXISTS data_parsed (
  id BIGINT NOT NULL AUTO_INCREMENT,
  collection_id BIGINT NOT NULL,
  parsed_data LONGTEXT,
  parse_status INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_collection_id (collection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.3 数据加工表
CREATE TABLE IF NOT EXISTS data_processed (
  id BIGINT NOT NULL AUTO_INCREMENT,
  parsed_id BIGINT NOT NULL,
  processed_data LONGTEXT,
  process_status INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_parsed_id (parsed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.4 发票数据表
CREATE TABLE IF NOT EXISTS data_invoice (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  process_id BIGINT,
  processed_id BIGINT,
  invoice_code VARCHAR(20),
  invoice_no VARCHAR(20),
  invoice_date DATE,
  check_code VARCHAR(50),
  machine_no VARCHAR(50),
  buyer_name VARCHAR(100),
  buyer_tax_no VARCHAR(20),
  seller_name VARCHAR(100),
  seller_tax_no VARCHAR(20),
  amount DECIMAL(18,2) DEFAULT 0,
  tax_amount DECIMAL(18,2) DEFAULT 0,
  total_amount DECIMAL(18,2) DEFAULT 0,
  invoice_type VARCHAR(20),
  tax_no VARCHAR(20),
  enterprise_name VARCHAR(100),
  category VARCHAR(20),
  status VARCHAR(20) DEFAULT 'NORMAL',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_task_id (task_id),
  KEY idx_invoice_code_no (invoice_code, invoice_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
