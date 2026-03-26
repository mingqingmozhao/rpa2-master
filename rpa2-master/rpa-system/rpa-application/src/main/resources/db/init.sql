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
  -- 四步 Groovy 脚本（采集/解析/加工/落库）
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

-- ===================== 6. 初始化数据 =====================

-- 6.1 初始化角色数据
INSERT INTO `sys_role` (`role_name`, `remark`, `status`) VALUES
('ADMIN', '系统管理员，拥有所有权限', 1),
('OPERATOR', '运维人员，负责流程和机器人管理', 1),
('BUSINESS', '业务人员，负责任务管理和执行', 1);

-- 6.2 初始化权限数据
INSERT INTO `sys_permission` (`perm_name`, `perm_key`, `perm_type`, `parent_id`) VALUES
-- 系统管理权限
('用户管理', 'sys:user:manage', 1, 0),
('用户查看', 'sys:user:view', 2, 1),
('用户新增', 'sys:user:add', 2, 1),
('用户编辑', 'sys:user:edit', 2, 1),
('用户删除', 'sys:user:delete', 2, 1),
('角色管理', 'sys:role:manage', 1, 0),
('角色查看', 'sys:role:view', 2, 6),
('角色新增', 'sys:role:add', 2, 6),
('角色编辑', 'sys:role:edit', 2, 6),
('角色删除', 'sys:role:delete', 2, 6),
('权限管理', 'sys:perm:manage', 1, 0),
-- 流程管理权限
('流程查看', 'process:view', 1, 0),
('流程新增', 'process:add', 2, 12),
('流程编辑', 'process:edit', 2, 12),
('流程删除', 'process:delete', 2, 12),
-- 机器人管理权限
('机器人查看', 'robot:view', 1, 0),
('机器人新增', 'robot:add', 2, 16),
('机器人编辑', 'robot:edit', 2, 16),
('机器人删除', 'robot:delete', 2, 16),
-- 任务管理权限
('任务查看', 'task:view', 1, 0),
('任务新增', 'task:add', 2, 20),
('任务编辑', 'task:edit', 2, 20),
('任务删除', 'task:delete', 2, 20),
('任务执行', 'task:execute', 2, 20),
-- 执行监控权限
('执行记录查看', 'execute:view', 1, 0),
('日志查看', 'log:view', 2, 25),
-- 业务数据权限
('业务数据查看', 'business:view', 1, 0),
('业务数据导出', 'business:export', 2, 27);

-- 6.3 初始化用户数据（密码使用 BCrypt 加密，这里是 admin123 的 BCrypt 哈希）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `phone`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', '13800138000', 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '运维人员', 'operator@example.com', '13800138001', 1),
('business', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '业务人员', 'business@example.com', '13800138002', 1),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'zhangsan@example.com', '13800138003', 1),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 'lisi@example.com', '13800138004', 1),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 'wangwu@example.com', '13800138005', 1),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵六', 'zhaoliu@example.com', '13800138006', 1),
('sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙七', 'sunqi@example.com', '13800138007', 1);

-- 6.4 分配用户角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> ADMIN
(2, 2),  -- operator -> OPERATOR
(3, 3),  -- business -> BUSINESS
(4, 2),  -- zhangsan -> OPERATOR
(5, 3),  -- lisi -> BUSINESS
(6, 2),  -- wangwu -> OPERATOR
(7, 3),  -- zhaoliu -> BUSINESS
(8, 3);  -- sunqi -> BUSINESS

-- 6.5 分配角色权限
-- ADMIN 拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`)
SELECT 1, id FROM `sys_permission`;

-- OPERATOR 拥有流程、机器人、执行监控相关权限
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`)
SELECT 2, id FROM `sys_permission` WHERE perm_key IN (
  'process:view', 'process:add', 'process:edit', 'process:delete',
  'robot:view', 'robot:add', 'robot:edit', 'robot:delete',
  'execute:view', 'log:view'
);

-- BUSINESS 拥有任务、执行监控、业务数据相关权限
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`)
SELECT 3, id FROM `sys_permission` WHERE perm_key IN (
  'task:view', 'task:add', 'task:edit', 'task:delete', 'task:execute',
  'execute:view', 'log:view',
  'business:view', 'business:export'
);

-- ===================== 7. 流程定义数据 =====================
INSERT INTO `process_def` (`process_code`, `process_name`, `status`, `collect_script`, `parse_script`, `process_script`, `save_script`, `create_user`) VALUES
('PROC_INVOICE_001', '发票采集流程', 1, 
 'def collect():\n    # 采集发票数据\n    url = "https://example.com/invoice"\n    response = requests.get(url)\n    return response.text',
 'def parse(html):\n    # 解析 HTML\n    from bs4 import BeautifulSoup\n    soup = BeautifulSoup(html, "html.parser")\n    return {"data": soup.text}',
 'def process(data):\n    # 数据加工\n    data["processed"] = True\n    return data',
 'def save(data):\n    # 保存到数据库\n    print("Saving data:", data)\n    return True',
 2),
('PROC_INVOICE_002', '发票查验流程', 1,
 'def collect():\n    # 采集查验数据\n    url = "https://inv-veri.chinatax.gov.cn"\n    return {"url": url}',
 'def parse(data):\n    # 解析查验结果\n    return {"result": "verified"}',
 'def process(data):\n    # 数据加工\n    data["verified"] = True\n    return data',
 'def save(data):\n    # 保存查验结果\n    return True',
 2),
('PROC_INVOICE_003', '发票认证流程', 1,
 'def collect():\n    # 采集认证数据\n    return {"type": "certification"}',
 'def parse(data):\n    # 解析认证信息\n    return {"certified": True}',
 'def process(data):\n    # 数据加工\n    data["status"] = "certified"\n    return data',
 'def save(data):\n    # 保存认证结果\n    return True',
 4),
('PROC_REPORT_001', '报表生成流程', 1,
 'def collect():\n    # 采集报表数据\n    return {"report_type": "monthly"}',
 'def parse(data):\n    # 解析报表模板\n    return {"template": "standard"}',
 'def process(data):\n    # 生成报表\n    data["generated"] = True\n    return data',
 'def save(data):\n    # 保存报表\n    return True',
 6),
('PROC_TAX_001', '税务申报流程', 1,
 'def collect():\n    # 采集税务数据\n    return {"tax_type": "vat"}',
 'def parse(data):\n    # 解析税务信息\n    return {"tax_amount": 0}',
 'def process(data):\n    # 计算税额\n    data["calculated"] = True\n    return data',
 'def save(data):\n    # 保存申报数据\n    return True',
 2);

-- ===================== 8. 机器人信息数据 =====================
INSERT INTO `robot_info` (`robot_code`, `robot_name`, `robot_type`, `ip`, `port`, `status`, `last_heartbeat`, `cpu_usage`, `mem_usage`) VALUES
('ROBOT_001', '机器人 1 号', 'Windows', '192.168.1.101', 8081, 1, NOW(), 25.5, 45.2),
('ROBOT_002', '机器人 2 号', 'Windows', '192.168.1.102', 8081, 1, NOW(), 30.2, 50.1),
('ROBOT_003', '机器人 3 号', 'Windows', '192.168.1.103', 8081, 2, NOW(), 65.8, 72.3),
('ROBOT_004', '机器人 4 号', 'Linux', '192.168.1.104', 8081, 1, NOW(), 15.3, 35.6),
('ROBOT_005', '机器人 5 号', 'Windows', '192.168.1.105', 8081, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR), 0, 0),
('ROBOT_006', '机器人 6 号', 'Windows', '192.168.1.106', 8081, 1, NOW(), 42.1, 58.9),
('ROBOT_007', '机器人 7 号', 'Linux', '192.168.1.107', 8081, 2, NOW(), 78.5, 81.2),
('ROBOT_008', '机器人 8 号', 'Windows', '192.168.1.108', 8081, 1, NOW(), 22.7, 40.3);

-- ===================== 9. 任务信息数据 =====================
INSERT INTO `task_info` (`task_code`, `task_name`, `process_id`, `robot_id`, `company_name`, `tax_no`, `status`, `create_user`) VALUES
('TASK_001', 'A 公司发票采集任务', 1, 1, '北京科技有限公司', '91110000000000001A', 1, 3),
('TASK_002', 'B 公司发票查验任务', 2, 2, '上海贸易公司', '91310000000000002B', 1, 5),
('TASK_003', 'C 公司发票认证任务', 3, 3, '广州制造企业', '91440000000000003C', 1, 3),
('TASK_004', 'D 公司报表生成任务', 4, 4, '深圳科技公司', '91440300000000004D', 1, 7),
('TASK_005', 'E 公司税务申报任务', 5, 1, '杭州电商公司', '91330000000000005E', 1, 5),
('TASK_006', 'F 公司发票采集任务', 1, 2, '成都零售公司', '91510000000000006F', 1, 3),
('TASK_007', 'G 公司发票查验任务', 2, 3, '武汉物流公司', '91420000000000007G', 1, 8),
('TASK_008', 'H 公司发票认证任务', 3, 4, '南京服务公司', '91320000000000008H', 1, 5),
('TASK_009', 'I 公司报表生成任务', 4, 6, '重庆集团公司', '91500000000000009I', 1, 7),
('TASK_010', 'J 公司税务申报任务', 5, 7, '天津开发公司', '91120000000000010J', 1, 3),
('TASK_011', 'K 公司发票采集任务', 1, 8, '苏州制造企业', '91320500000000011K', 1, 8),
('TASK_012', 'L 公司发票查验任务', 2, 1, '青岛贸易公司', '91370200000000012L', 1, 5),
('TASK_013', 'M 公司发票认证任务', 3, 2, '大连科技公司', '91210200000000013M', 1, 3),
('TASK_014', 'N 公司报表生成任务', 4, 3, '厦门电商公司', '91350200000000014N', 1, 7),
('TASK_015', 'O 公司税务申报任务', 5, 4, '长沙服务企业', '91430100000000015O', 1, 5);

-- ===================== 10. 任务执行记录数据 =====================
INSERT INTO `task_execute` (`task_id`, `process_id`, `robot_id`, `execute_status`, `cost_time`, `error_msg`, `start_time`, `end_time`) VALUES
(1, 1, 1, 3, 15230, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 2, 2, 3, 12450, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 3, 3, 3, 18920, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(4, 4, 4, 3, 22100, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(5, 5, 1, 2, 0, NULL, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL),
(6, 1, 2, 3, 14560, NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(7, 2, 3, 4, 8500, '网络超时，无法连接服务器', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(8, 3, 4, 3, 16780, NULL, DATE_SUB(NOW(), INTERVAL 7 HOUR), DATE_SUB(NOW(), INTERVAL 7 HOUR)),
(9, 4, 6, 3, 20340, NULL, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(10, 5, 7, 3, 19250, NULL, DATE_SUB(NOW(), INTERVAL 9 HOUR), DATE_SUB(NOW(), INTERVAL 9 HOUR)),
(11, 1, 8, 2, 0, NULL, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NULL),
(12, 2, 1, 3, 13670, NULL, DATE_SUB(NOW(), INTERVAL 10 HOUR), DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(13, 3, 2, 4, 5200, '解析失败：数据格式错误', DATE_SUB(NOW(), INTERVAL 45 MINUTE), DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(14, 4, 3, 3, 21890, NULL, DATE_SUB(NOW(), INTERVAL 11 HOUR), DATE_SUB(NOW(), INTERVAL 11 HOUR)),
(15, 5, 4, 3, 18450, NULL, DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- ===================== 11. 任务执行日志数据 =====================
INSERT INTO `task_execute_log` (`execute_id`, `log_level`, `log_message`) VALUES
(1, 'INFO', '任务开始执行'),
(1, 'INFO', '正在采集发票数据'),
(1, 'INFO', '采集成功，获取到 15 条记录'),
(1, 'INFO', '正在解析数据'),
(1, 'INFO', '解析完成'),
(1, 'INFO', '正在加工数据'),
(1, 'INFO', '加工完成'),
(1, 'INFO', '正在保存数据'),
(1, 'INFO', '保存成功'),
(1, 'INFO', '任务执行完成，耗时 15230ms'),
(2, 'INFO', '任务开始执行'),
(2, 'INFO', '正在查验发票'),
(2, 'INFO', '查验成功'),
(2, 'INFO', '任务执行完成，耗时 12450ms'),
(7, 'INFO', '任务开始执行'),
(7, 'ERROR', '网络连接超时'),
(7, 'ERROR', '重试 3 次后仍然失败'),
(7, 'ERROR', '任务执行失败：网络超时，无法连接服务器'),
(11, 'INFO', '任务开始执行'),
(11, 'INFO', '正在采集发票数据'),
(11, 'INFO', '采集中...'),
(13, 'INFO', '任务开始执行'),
(13, 'ERROR', '数据格式不符合预期'),
(13, 'ERROR', '无法解析字段：invoice_code'),
(13, 'ERROR', '任务执行失败：解析失败：数据格式错误');

-- ===================== 12. 发票数据示例 =====================
INSERT INTO `data_invoice` (`task_id`, `invoice_code`, `invoice_no`, `invoice_date`, `check_code`, `machine_no`, `buyer_name`, `buyer_tax_no`, `seller_name`, `seller_tax_no`, `amount`, `tax_amount`, `total_amount`, `invoice_type`) VALUES
(1, '011002100111', '12345678', '2024-01-15', '12345678901234567890', '123456789012', '北京科技有限公司', '91110000000000001A', '供应商 A', '91110000111111111A', 10000.00, 1300.00, 11300.00, '增值税专用发票'),
(1, '011002100111', '12345679', '2024-01-16', '12345678901234567891', '123456789012', '北京科技有限公司', '91110000000000001A', '供应商 B', '91110000222222222B', 20000.00, 2600.00, 22600.00, '增值税专用发票'),
(2, '011002100111', '12345680', '2024-01-17', '12345678901234567892', '123456789012', '上海贸易公司', '91310000000000002B', '供应商 C', '91310000333333333C', 15000.00, 1950.00, 16950.00, '增值税专用发票'),
(3, '011002100111', '12345681', '2024-01-18', '12345678901234567893', '123456789012', '广州制造企业', '91440000000000003C', '供应商 D', '91440000444444444D', 25000.00, 3250.00, 28250.00, '增值税专用发票'),
(4, '011002100111', '12345682', '2024-01-19', '12345678901234567894', '123456789012', '深圳科技公司', '91440300000000004D', '供应商 E', '91440300555555555E', 30000.00, 3900.00, 33900.00, '增值税专用发票'),
(5, '011002100111', '12345683', '2024-01-20', '12345678901234567895', '123456789012', '杭州电商公司', '91330000000000005E', '供应商 F', '91330000666666666F', 18000.00, 2340.00, 20340.00, '增值税专用发票');

-- ===================== 完成提示 =====================
SELECT '========================================' AS '';
SELECT '数据库初始化完成！' AS message;
SELECT '========================================' AS '';
SELECT '测试用户列表（密码均为：admin123）：' AS info;
SELECT '----------------------------------------' AS '';
SELECT '用户名' AS username, '角色' AS role, '姓名' AS real_name
UNION ALL
SELECT 'admin', 'ADMIN', '系统管理员'
UNION ALL
SELECT 'operator', 'OPERATOR', '运维人员'
UNION ALL
SELECT 'business', 'BUSINESS', '业务人员'
UNION ALL
SELECT 'zhangsan', 'OPERATOR', '张三'
UNION ALL
SELECT 'lisi', 'BUSINESS', '李四'
UNION ALL
SELECT 'wangwu', 'OPERATOR', '王五'
UNION ALL
SELECT 'zhaoliu', 'BUSINESS', '赵六'
UNION ALL
SELECT 'sunqi', 'BUSINESS', '孙七';
SELECT '========================================' AS '';
SELECT '流程数量：' || COUNT(*) FROM process_def;
SELECT '机器人数量：' || COUNT(*) FROM robot_info;
SELECT '任务数量：' || COUNT(*) FROM task_info;
SELECT '执行记录数量：' || COUNT(*) FROM task_execute;
SELECT '发票数据数量：' || COUNT(*) FROM data_invoice;
SELECT '========================================' AS '';
