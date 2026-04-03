-- 查看 robot_info 表的字段
SELECT 
  COLUMN_NAME AS '字段名',
  DATA_TYPE AS '类型',
  IS_NULLABLE AS '可空',
  COLUMN_DEFAULT AS '默认值',
  COLUMN_COMMENT AS '注释'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'financial_data_collect'
  AND TABLE_NAME = 'robot_info'
ORDER BY ORDINAL_POSITION;
