-- 修复 task_info 表结构，添加缺失的列

-- 添加 category 列
ALTER TABLE task_info ADD COLUMN IF NOT EXISTS category VARCHAR(20) COMMENT '分类';

-- 添加 priority 列（如果不存在）
ALTER TABLE task_info ADD COLUMN IF NOT EXISTS priority INT DEFAULT 5 COMMENT '优先级';

-- 添加 remark 列（如果不存在）
ALTER TABLE task_info ADD COLUMN IF NOT EXISTS remark VARCHAR(255) COMMENT '备注';

-- 添加 enterprise_name 列（如果不存在）
ALTER TABLE task_info ADD COLUMN IF NOT EXISTS enterprise_name VARCHAR(100) COMMENT '企业名称';

-- 添加 tax_no 列（如果不存在）
ALTER TABLE task_info ADD COLUMN IF NOT EXISTS tax_no VARCHAR(20) COMMENT '税号';

SELECT '表结构修复完成！' AS result;
