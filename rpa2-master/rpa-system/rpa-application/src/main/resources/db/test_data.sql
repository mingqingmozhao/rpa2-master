-- ========================================
-- 额外测试数据 SQL
-- 用于手动测试用户管理、任务管理等功能
-- ========================================

USE financial_data_collect;

-- ===================== 1. 额外用户数据 =====================
-- 密码都是：admin123 (BCrypt 加密后的哈希值)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `phone`, `status`) VALUES
('test001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户 1', 'test001@example.com', '13900139001', 1),
('test002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户 2', 'test002@example.com', '13900139002', 1),
('test003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户 3', 'test003@example.com', '13900139003', 1),
('test004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户 4', 'test004@example.com', '13900139004', 0),
('test005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户 5', 'test005@example.com', '13900139005', 1),
('chenjiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈九', 'chenjiu@example.com', '13900139006', 1),
('zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周八', 'zhouba@example.com', '13900139007', 1),
('wuqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '吴七', 'wuqi@example.com', '13900139008', 1);

-- 分配角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(9, 3),   -- test001 -> BUSINESS
(10, 3),  -- test002 -> BUSINESS
(11, 2),  -- test003 -> OPERATOR
(12, 3),  -- test004 -> BUSINESS (已禁用)
(13, 3),  -- test005 -> BUSINESS
(14, 2),  -- chenjiu -> OPERATOR
(15, 3),  -- zhouba -> BUSINESS
(16, 2);  -- wuqi -> OPERATOR

-- ===================== 2. 额外流程数据 =====================
INSERT INTO `process_def` (`process_code`, `process_name`, `status`, `collect_script`, `parse_script`, `process_script`, `save_script`, `create_user`) VALUES
('PROC_EXPORT_001', '出口退税流程', 1, 
 'def collect():\n    return {"type": "export_tax"}',
 'def parse(data):\n    return {"tax_rate": 0.13}',
 'def process(data):\n    return data',
 'def save(data):\n    return True',
 2),
('PROC_IMPORT_001', '进口报关流程', 1,
 'def collect():\n    return {"type": "import"}',
 'def parse(data):\n    return {"customs": "shanghai"}',
 'def process(data):\n    return data',
 'def save(data):\n    return True',
 4),
('PROC_ACCOUNT_001', '会计凭证流程', 1,
 'def collect():\n    return {"type": "accounting"}',
 'def parse(data):\n    return {"voucher_type": "general"}',
 'def process(data):\n    return data',
 'def save(data):\n    return True',
 6),
('PROC_AUDIT_001', '审计核查流程', 1,
 'def collect():\n    return {"type": "audit"}',
 'def parse(data):\n    return {"audit_level": "standard"}',
 'def process(data):\n    return data',
 'def save(data):\n    return True',
 2),
('PROC_FINANCE_001', '财务报表流程', 1,
 'def collect():\n    return {"type": "finance_report"}',
 'def parse(data):\n    return {"report_period": "monthly"}',
 'def process(data):\n    return data',
 'def save(data):\n    return True',
 11);

-- ===================== 3. 额外机器人数据 =====================
INSERT INTO `robot_info` (`robot_code`, `robot_name`, `robot_type`, `ip`, `port`, `status`, `last_heartbeat`, `cpu_usage`, `mem_usage`) VALUES
('ROBOT_009', '机器人 9 号', 'Windows', '192.168.1.109', 8081, 1, NOW(), 35.2, 48.7),
('ROBOT_010', '机器人 10 号', 'Linux', '192.168.1.110', 8081, 1, NOW(), 28.9, 42.3),
('ROBOT_011', '机器人 11 号', 'Windows', '192.168.1.111', 8081, 2, NOW(), 72.1, 79.5),
('ROBOT_012', '机器人 12 号', 'Windows', '192.168.1.112', 8081, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), 0, 0),
('ROBOT_013', '机器人 13 号', 'Linux', '192.168.1.113', 8081, 1, NOW(), 19.8, 38.4),
('ROBOT_014', '机器人 14 号', 'Windows', '192.168.1.114', 8081, 1, NOW(), 45.6, 55.2),
('ROBOT_015', '机器人 15 号', 'Windows', '192.168.1.115', 8081, 2, NOW(), 81.3, 85.9);

-- ===================== 4. 额外任务数据 =====================
INSERT INTO `task_info` (`task_code`, `task_name`, `process_id`, `robot_id`, `company_name`, `tax_no`, `status`, `create_user`) VALUES
('TASK_016', 'P 公司发票采集任务', 1, 5, '珠海科技公司', '91440400000000016P', 1, 9),
('TASK_017', 'Q 公司发票查验任务', 2, 6, '汕头贸易公司', '91440500000000017Q', 1, 10),
('TASK_018', 'R 公司发票认证任务', 3, 7, '佛山制造企业', '91440600000000018R', 1, 12),
('TASK_019', 'S 公司报表生成任务', 4, 8, '东莞科技公司', '91441900000000019S', 1, 13),
('TASK_020', 'T 公司税务申报任务', 5, 9, '中山电商公司', '91442000000000020T', 1, 14),
('TASK_021', 'U 公司出口退税任务', 6, 10, '江门贸易公司', '91440700000000021U', 1, 9),
('TASK_022', 'V 公司进口报关任务', 7, 11, '惠州制造企业', '91441300000000022V', 1, 10),
('TASK_023', 'W 公司会计凭证任务', 8, 12, '顺德科技公司', '91440606000000023W', 1, 12),
('TASK_024', 'X 公司审计核查任务', 9, 13, '南海电商公司', '91440605000000024X', 1, 13),
('TASK_025', 'Y 公司财务报表任务', 10, 14, '番禺服务企业', '91440113000000025Y', 1, 14),
('TASK_026', 'Z 公司发票采集任务', 1, 15, '增城集团公司', '91440118000000026Z', 1, 9),
('TASK_027', 'AA 公司发票查验任务', 2, 1, '从化贸易公司', '91440117000000027A', 1, 10),
('TASK_028', 'AB 公司发票认证任务', 3, 2, '花都制造企业', '91440114000000028B', 1, 12),
('TASK_029', 'AC 公司报表生成任务', 4, 3, '白云科技公司', '91440111000000029C', 1, 13),
('TASK_030', 'AD 公司税务申报任务', 5, 4, '天河电商公司', '91440106000000030D', 1, 14);

-- ===================== 5. 额外执行记录数据 =====================
INSERT INTO `task_execute` (`task_id`, `process_id`, `robot_id`, `execute_status`, `cost_time`, `error_msg`, `start_time`, `end_time`) VALUES
(16, 1, 5, 3, 16780, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(17, 2, 6, 3, 14230, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(18, 3, 7, 2, 0, NULL, DATE_SUB(NOW(), INTERVAL 20 MINUTE), NULL),
(19, 4, 8, 3, 19560, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(20, 5, 9, 4, 7800, '系统资源不足', DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(21, 6, 10, 3, 21340, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(22, 7, 11, 3, 18920, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(23, 8, 12, 4, 6500, '数据库连接失败', DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
(24, 9, 13, 3, 22150, NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(25, 10, 14, 3, 20780, NULL, DATE_SUB(NOW(), INTERVAL 7 HOUR), DATE_SUB(NOW(), INTERVAL 7 HOUR)),
(26, 1, 15, 2, 0, NULL, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL),
(27, 2, 1, 3, 15670, NULL, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(28, 3, 2, 3, 17890, NULL, DATE_SUB(NOW(), INTERVAL 9 HOUR), DATE_SUB(NOW(), INTERVAL 9 HOUR)),
(29, 4, 3, 3, 23450, NULL, DATE_SUB(NOW(), INTERVAL 10 HOUR), DATE_SUB(NOW(), INTERVAL 10 HOUR)),
(30, 5, 4, 3, 19230, NULL, DATE_SUB(NOW(), INTERVAL 11 HOUR), DATE_SUB(NOW(), INTERVAL 11 HOUR));

-- ===================== 6. 额外执行日志数据 =====================
INSERT INTO `task_execute_log` (`execute_id`, `log_level`, `log_message`) VALUES
(16, 'INFO', '任务开始执行'),
(16, 'INFO', '正在采集发票数据'),
(16, 'INFO', '采集成功'),
(16, 'INFO', '任务执行完成'),
(18, 'INFO', '任务开始执行'),
(18, 'INFO', '正在认证发票'),
(18, 'INFO', '认证中...'),
(20, 'INFO', '任务开始执行'),
(20, 'ERROR', '系统资源不足'),
(20, 'ERROR', '无法分配执行线程'),
(20, 'ERROR', '任务执行失败'),
(23, 'INFO', '任务开始执行'),
(23, 'ERROR', '数据库连接超时'),
(23, 'ERROR', '重试失败'),
(23, 'ERROR', '任务执行失败'),
(26, 'INFO', '任务开始执行'),
(26, 'INFO', '正在采集数据'),
(26, 'INFO', '采集中...');

-- ===================== 7. 额外发票数据 =====================
INSERT INTO `data_invoice` (`task_id`, `invoice_code`, `invoice_no`, `invoice_date`, `check_code`, `machine_no`, `buyer_name`, `buyer_tax_no`, `seller_name`, `seller_tax_no`, `amount`, `tax_amount`, `total_amount`, `invoice_type`) VALUES
(16, '011002100111', '22345678', '2024-02-01', '22345678901234567890', '223456789012', '珠海科技公司', '91440400000000016P', '供应商 G', '91440400777777777G', 12000.00, 1560.00, 13560.00, '增值税专用发票'),
(16, '011002100111', '22345679', '2024-02-02', '22345678901234567891', '223456789012', '珠海科技公司', '91440400000000016P', '供应商 H', '91440400888888888H', 18000.00, 2340.00, 20340.00, '增值税专用发票'),
(17, '011002100111', '22345680', '2024-02-03', '22345678901234567892', '223456789012', '汕头贸易公司', '91440500000000017Q', '供应商 I', '91440500999999999I', 22000.00, 2860.00, 24860.00, '增值税专用发票'),
(18, '011002100111', '22345681', '2024-02-04', '22345678901234567893', '223456789012', '佛山制造企业', '91440600000000018R', '供应商 J', '91440600101010101J', 28000.00, 3640.00, 31640.00, '增值税专用发票'),
(19, '011002100111', '22345682', '2024-02-05', '22345678901234567894', '223456789012', '东莞科技公司', '91441900000000019S', '供应商 K', '91441900111111112K', 35000.00, 4550.00, 39550.00, '增值税专用发票'),
(20, '011002100111', '22345683', '2024-02-06', '22345678901234567895', '223456789012', '中山电商公司', '91442000000000020T', '供应商 L', '91442000121212123L', 16000.00, 2080.00, 18080.00, '增值税专用发票');

-- ===================== 完成提示 =====================
SELECT '========================================' AS '';
SELECT '额外测试数据插入完成！' AS message;
SELECT '========================================' AS '';
SELECT '数据统计：' AS info;
SELECT '----------------------------------------' AS '';
SELECT '总用户数：' || COUNT(*) FROM sys_user;
SELECT '总流程数：' || COUNT(*) FROM process_def;
SELECT '总机器人数：' || COUNT(*) FROM robot_info;
SELECT '总任务数：' || COUNT(*) FROM task_info;
SELECT '总执行记录数：' || COUNT(*) FROM task_execute;
SELECT '总发票数据数：' || COUNT(*) FROM data_invoice;
SELECT '========================================' AS '';
