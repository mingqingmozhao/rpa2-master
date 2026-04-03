-- ============================================
-- 机器人管理模块 - 数据库字段快速修复
-- 数据库：financial_data_collect
-- ============================================

USE financial_data_collect;

-- 1. 添加 type 字段
ALTER TABLE robot_info 
ADD COLUMN type VARCHAR(20) DEFAULT 'UNATTENDED' 
COMMENT '机器人类型：ATTENDED-有人值守 UNATTENDED-无人值守' 
AFTER robot_name;

-- 2. 添加 department 字段
ALTER TABLE robot_info 
ADD COLUMN department VARCHAR(100) DEFAULT NULL 
COMMENT '所属部门' 
AFTER last_heartbeat;

-- 3. 添加 owner_id 字段
ALTER TABLE robot_info 
ADD COLUMN owner_id BIGINT DEFAULT NULL 
COMMENT '负责人 ID' 
AFTER department;

-- 4. 添加 owner_name 字段
ALTER TABLE robot_info 
ADD COLUMN owner_name VARCHAR(50) DEFAULT NULL 
COMMENT '负责人姓名' 
AFTER owner_id;

-- 5. 添加 description 字段
ALTER TABLE robot_info 
ADD COLUMN description VARCHAR(255) DEFAULT NULL 
COMMENT '机器人描述' 
AFTER owner_name;

-- 6. 添加 remark 字段
ALTER TABLE robot_info 
ADD COLUMN remark VARCHAR(500) DEFAULT NULL 
COMMENT '备注信息' 
AFTER description;

-- 7. 添加 update_time 字段
ALTER TABLE robot_info 
ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP 
COMMENT '更新时间' 
AFTER create_time;

-- 8. 修改 status 字段类型
ALTER TABLE robot_info 
MODIFY COLUMN status VARCHAR(20) DEFAULT 'OFFLINE' 
COMMENT '状态：ONLINE-在线 OFFLINE-离线 BUSY-忙碌 FAULT-故障';

-- 查看表结构
SELECT '表结构修复完成！' AS message;
DESC robot_info;
