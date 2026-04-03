-- ============================================
-- 机器人管理模块 - 完整数据库脚本
-- 数据库：financial_data_collect
-- 创建时间：2026-04-02
-- ============================================

USE financial_data_collect;

-- 如果表不存在，创建完整的 robot_info 表
CREATE TABLE IF NOT EXISTS robot_info (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '机器人主键 ID',
  robot_code VARCHAR(50) NOT NULL COMMENT '机器人编码 (唯一)',
  robot_name VARCHAR(50) NOT NULL COMMENT '机器人名称',
  robot_type VARCHAR(20) DEFAULT 'UNATTENDED' COMMENT '机器人类型：ATTENDED-有人值守 UNATTENDED-无人值守',
  robot_type_new VARCHAR(20) DEFAULT 'UNATTENDED' COMMENT '机器人类型（新）：ATTENDED-有人值守 UNATTENDED-无人值守',
  department_id BIGINT DEFAULT NULL COMMENT '所属部门 ID',
  owner_id BIGINT DEFAULT NULL COMMENT '负责人 ID',
  owner_name VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名',
  description VARCHAR(255) DEFAULT NULL COMMENT '机器人描述',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
  ip VARCHAR(20) NOT NULL COMMENT 'IP 地址',
  port INT NOT NULL COMMENT '端口',
  execute_thread VARCHAR(50) COMMENT '执行线程',
  status INT DEFAULT 0 COMMENT '状态：1-在线 2-工作中 0-离线 -1-故障',
  current_task_id BIGINT COMMENT '当前执行任务 ID',
  last_heartbeat DATETIME COMMENT '最后心跳时间',
  cpu_usage DECIMAL(5,2) DEFAULT 0 COMMENT 'CPU 使用率',
  mem_usage DECIMAL(5,2) DEFAULT 0 COMMENT '内存使用率',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  is_deleted TINYINT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_robot_code (robot_code),
  KEY idx_status (status),
  KEY idx_department_id (department_id),
  KEY idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器人信息表';

-- ============================================
-- 测试数据（可选）
-- ============================================

-- 插入测试机器人数据
INSERT INTO robot_info (
  robot_code, robot_name, robot_type, robot_type_new, 
  ip, port, status, description, remark
) VALUES 
(
  'ROBOT-001', 
  '测试机器人 1 号', 
  'UNATTENDED', 
  'UNATTENDED',
  '192.168.1.100', 
  8080, 
  1, 
  '第一个测试机器人（无人值守）', 
  '用于功能测试'
),
(
  'ROBOT-002', 
  '测试机器人 2 号', 
  'ATTENDED', 
  'ATTENDED',
  '192.168.1.101', 
  8080, 
  0, 
  '第二个测试机器人（有人值守）', 
  '用于集成测试'
),
(
  'ROBOT-003', 
  '生产机器人-A', 
  'UNATTENDED', 
  'UNATTENDED',
  '192.168.1.102', 
  8080, 
  2, 
  '生产环境机器人', 
  '24 小时运行'
) ON DUPLICATE KEY UPDATE robot_name = VALUES(robot_name);

-- 查询测试数据
SELECT 
  id,
  robot_code AS '机器人编码',
  robot_name AS '机器人名称',
  robot_type_new AS '机器人类型',
  ip AS 'IP 地址',
  port AS '端口',
  CASE status 
    WHEN 1 THEN '在线'
    WHEN 2 THEN '工作中'
    WHEN 0 THEN '离线'
    WHEN -1 THEN '故障'
    ELSE '未知'
  END AS '状态',
  description AS '描述',
  create_time AS '创建时间'
FROM robot_info
WHERE is_deleted = 0
ORDER BY id;

-- ============================================
-- 常用查询 SQL
-- ============================================

-- 1. 查询所有在线机器人
SELECT * FROM robot_info 
WHERE status = 1 AND is_deleted = 0;

-- 2. 查询所有工作中机器人
SELECT * FROM robot_info 
WHERE status = 2 AND is_deleted = 0;

-- 3. 查询可用机器人（在线或工作中）
SELECT * FROM robot_info 
WHERE status IN (1, 2) AND is_deleted = 0;

-- 4. 统计机器人状态
SELECT 
  CASE status 
    WHEN 1 THEN '在线'
    WHEN 2 THEN '工作中'
    WHEN 0 THEN '离线'
    WHEN -1 THEN '故障'
    ELSE '未知'
  END AS '状态',
  COUNT(*) AS '数量'
FROM robot_info
WHERE is_deleted = 0
GROUP BY status;

-- 5. 查询某个负责人的机器人
-- SELECT * FROM robot_info 
-- WHERE owner_id = 1 AND is_deleted = 0;

-- 6. 查询某个部门的机器人
-- SELECT * FROM robot_info 
-- WHERE department_id = 1 AND is_deleted = 0;

SELECT '数据库脚本执行完成！' AS message;
