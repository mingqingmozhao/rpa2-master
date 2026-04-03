-- ============================================
-- 机器人管理模块 - 数据库字段修复脚本
-- 数据库：financial_data_collect
-- 执行时间：2026-04-02
-- ============================================

USE financial_data_collect;

-- 检查 robot_info 表是否存在
SELECT '开始修复 robot_info 表...' AS message;

-- 添加缺失的字段
-- 1. 添加 type 字段（机器人类型）
ALTER TABLE robot_info 
ADD COLUMN IF NOT EXISTS type VARCHAR(20) DEFAULT 'UNATTENDED' COMMENT '机器人类型：ATTENDED-有人值守 UNATTENDED-无人值守' 
AFTER robot_name;

-- 2. 添加 department 字段（所属部门）
ALTER TABLE robot_info 
ADD COLUMN IF NOT EXISTS department VARCHAR(100) DEFAULT NULL COMMENT '所属部门' 
AFTER last_heartbeat;

-- 3. 添加 owner_id 字段（负责人 ID）
ALTER TABLE robot_info 
ADD COLUMN IF NOT EXISTS owner_id BIGINT DEFAULT NULL COMMENT '负责人 ID' 
AFTER department;

-- 4. 添加 owner_name 字段（负责人姓名）
ALTER TABLE robot_info 
ADD COLUMN IF NOT EXISTS owner_name VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名' 
AFTER owner_id;

-- 5. 添加 description 字段（描述）
ALTER TABLE robot_info 
ADD COLUMN IF NOT EXISTS description VARCHAR(255) DEFAULT NULL COMMENT '机器人描述' 
AFTER owner_name;

-- 6. 添加 remark 字段（备注）
ALTER TABLE robot_info 
ADD COLUMN IF NOT EXISTS remark VARCHAR(500) DEFAULT NULL COMMENT '备注信息' 
AFTER description;

-- 7. 添加 update_time 字段（更新时间）
ALTER TABLE robot_info 
ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' 
AFTER create_time;

-- 8. 修改 status 字段类型和注释
ALTER TABLE robot_info 
MODIFY COLUMN status VARCHAR(20) DEFAULT 'OFFLINE' COMMENT '状态：ONLINE-在线 OFFLINE-离线 BUSY-忙碌 FAULT-故障';

-- 9. 删除旧的 robot_type 字段（如果有）
-- ALTER TABLE robot_info DROP COLUMN IF EXISTS robot_type;

-- 10. 删除 robot_type_new 字段（不需要了）
ALTER TABLE robot_info DROP COLUMN IF EXISTS robot_type_new;

-- 显示表结构
SELECT '表结构修复完成！' AS message;

-- 查看当前表结构
DESC robot_info;

-- 插入测试数据
INSERT INTO robot_info (
  robot_code, 
  robot_name, 
  type, 
  status, 
  ip, 
  port, 
  description,
  remark,
  create_time
) VALUES (
  'ROBOT-001',
  '测试机器人 1 号',
  'UNATTENDED',
  'OFFLINE',
  '192.168.1.100',
  8080,
  '第一个测试机器人',
  '用于功能测试',
  NOW()
) ON DUPLICATE KEY UPDATE robot_name = VALUES(robot_name);

SELECT '测试数据插入完成！' AS message;

-- 查看测试数据
SELECT 
  id,
  robot_code AS '机器人编码',
  robot_name AS '机器人名称',
  type AS '类型',
  status AS '状态',
  ip AS 'IP 地址',
  port AS '端口',
  description AS '描述',
  create_time AS '创建时间'
FROM robot_info
WHERE is_deleted = 0
ORDER BY id DESC
LIMIT 5;
