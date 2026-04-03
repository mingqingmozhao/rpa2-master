-- 流程定义表结构升级
-- 添加 category、version、remark 字段（兼容 MySQL 5.x）

USE financial_data_collect;

-- 添加 category 字段
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = 'financial_data_collect'
               AND TABLE_NAME = 'process_def'
               AND COLUMN_NAME = 'category');
SET @sql := IF(@exist = 0, 'ALTER TABLE process_def ADD COLUMN category VARCHAR(50) DEFAULT \'\' COMMENT \'分类\' AFTER process_name', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 version 字段
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = 'financial_data_collect'
               AND TABLE_NAME = 'process_def'
               AND COLUMN_NAME = 'version');
SET @sql := IF(@exist = 0, 'ALTER TABLE process_def ADD COLUMN version VARCHAR(20) DEFAULT \'1.0.0\' COMMENT \'版本号\' AFTER category', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 remark 字段
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = 'financial_data_collect'
               AND TABLE_NAME = 'process_def'
               AND COLUMN_NAME = 'remark');
SET @sql := IF(@exist = 0, 'ALTER TABLE process_def ADD COLUMN remark VARCHAR(255) DEFAULT \'\' COMMENT \'备注\' AFTER description', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
