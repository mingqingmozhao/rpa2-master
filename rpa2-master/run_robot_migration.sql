-- 迁移脚本：为 robot_info 表添加新字段（简化版）
-- 执行方式：在 Navicat 或其他 MySQL 客户端中直接执行

USE financial_data_collect;

-- 添加机器人类型字段（有人值守/无人值守）
ALTER TABLE robot_info 
ADD COLUMN robot_type_new VARCHAR(20) DEFAULT 'UNATTENDED' COMMENT '机器人类型：ATTENDED-有人值守 UNATTENDED-无人值守' 
AFTER robot_name;

-- 添加所属部门字段
ALTER TABLE robot_info 
ADD COLUMN department_id BIGINT DEFAULT NULL COMMENT '所属部门 ID' 
AFTER robot_type_new;

-- 添加负责人 ID 字段
ALTER TABLE robot_info 
ADD COLUMN owner_id BIGINT DEFAULT NULL COMMENT '负责人 ID' 
AFTER department_id;

-- 添加负责人姓名字段
ALTER TABLE robot_info 
ADD COLUMN owner_name VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名' 
AFTER owner_id;

-- 添加描述字段
ALTER TABLE robot_info 
ADD COLUMN description VARCHAR(255) DEFAULT NULL COMMENT '机器人描述' 
AFTER owner_name;

-- 添加备注字段
ALTER TABLE robot_info 
ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注信息' 
AFTER description;

-- 修改状态字段注释，明确状态值
ALTER TABLE robot_info 
MODIFY COLUMN status INT DEFAULT 0 COMMENT '状态：1-在线 2-工作中 0-离线 -1-故障';

SELECT '迁移成功完成！' AS message;
