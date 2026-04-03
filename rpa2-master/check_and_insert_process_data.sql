-- 检查流程数据
USE financial_data_collect;

-- 显示当前流程数据
SELECT '当前流程数据数量:' AS info, COUNT(*) AS count FROM process_def WHERE is_deleted = 0;

-- 如果没有数据，插入测试数据
INSERT INTO process_def (process_code, process_name, description, category, version, status, 
    collection_config, parsing_config, processing_config, storage_config,
    create_time, update_time, is_deleted, create_by, update_by)
SELECT 'TEST001', '测试流程-数据收集', '用于测试的流程1', '数据收集', '1.0', 1,
    '{"type": "csv", "filePath": "/data/input.csv"}',
    '{"parser": "default", "encoding": "UTF-8"}',
    '{"transform": "uppercase", "filter": "not_null"}',
    '{"database": "financial_data_collect", "table": "data_result"}',
    NOW(), NOW(), 0, 'system', 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM process_def WHERE process_code = 'TEST001');

INSERT INTO process_def (process_code, process_name, description, category, version, status, 
    collection_config, parsing_config, processing_config, storage_config,
    create_time, update_time, is_deleted, create_by, update_by)
SELECT 'TEST002', '测试流程-数据分析', '用于测试的流程2', '数据分析', '1.0', 1,
    '{"type": "api", "url": "http://api.example.com/data"}',
    '{"parser": "json", "path": "$.data"}',
    '{"aggregate": "sum", "groupBy": "date"}',
    '{"database": "financial_data_collect", "table": "analysis_result"}',
    NOW(), NOW(), 0, 'system', 'system'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM process_def WHERE process_code = 'TEST002');

-- 显示插入后的数据
SELECT '插入后的流程数据:' AS info;
SELECT id, process_code, process_name, category, status, create_time 
FROM process_def WHERE is_deleted = 0;
