-- ===================== 1. 初始化角色数据 =====================
INSERT INTO sys_role (role_name, remark, status) VALUES
('ADMIN', '系统管理员，拥有所有权限', 1),
('OPERATOR', '运维人员，负责流程和机器人管理', 1),
('VIEWER', '访客，仅可查看数据', 1);

-- ===================== 2. 初始化权限数据 =====================
INSERT INTO sys_permission (perm_name, perm_key, perm_type, parent_id) VALUES
-- 系统管理（一级菜单 id=1）
('系统管理', 'sys:manage', 1, 0),
('用户管理', 'sys:user:manage', 1, 0),
('用户查看', 'sys:user:view', 2, 2),
('用户新增', 'sys:user:add', 2, 2),
('用户编辑', 'sys:user:edit', 2, 2),
('用户删除', 'sys:user:delete', 2, 2),
('角色管理', 'sys:role:manage', 1, 0),
('角色查看', 'sys:role:view', 2, 7),
('角色新增', 'sys:role:add', 2, 7),
('角色编辑', 'sys:role:edit', 2, 7),
('角色删除', 'sys:role:delete', 2, 7),
('权限管理', 'sys:perm:manage', 1, 0),
-- 任务管理（一级菜单 id=12）
('任务管理', 'task:manage', 1, 0),
('任务查看', 'task:view', 2, 12),
('任务新增', 'task:add', 2, 12),
('任务编辑', 'task:edit', 2, 12),
('任务删除', 'task:delete', 2, 12),
('任务执行', 'task:execute', 2, 12),
-- 流程管理（一级菜单 id=18）
('流程管理', 'process:manage', 1, 0),
('流程查看', 'process:view', 2, 18),
('流程新增', 'process:add', 2, 18),
('流程编辑', 'process:edit', 2, 18),
('流程删除', 'process:delete', 2, 18),
-- 机器人管理（一级菜单 id=23）
('机器人管理', 'robot:manage', 1, 0),
('机器人查看', 'robot:view', 2, 23),
('机器人新增', 'robot:add', 2, 23),
('机器人编辑', 'robot:edit', 2, 23),
('机器人删除', 'robot:delete', 2, 23),
-- 执行监控（一级菜单 id=28）
('执行监控', 'execute:manage', 1, 0),
('执行记录查看', 'execute:view', 2, 28),
-- 数据管理（一级菜单 id=30）
('数据管理', 'data:manage', 1, 0),
('数据查询', 'data:query', 2, 30),
('数据导出', 'data:export', 2, 30),
('数据删除', 'data:delete', 2, 30);

-- ===================== 3. 初始化用户数据（8 人：1 管理员 + 3 运维 + 4 访客）=====================
-- BCrypt 密码统一为：admin123
INSERT INTO sys_user (username, password, real_name, email, phone, status, create_time) VALUES
-- 管理员 1 人
('admin',      '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '张建国', 'admin@example.com',      '13800000001', 1, '2026-01-05 09:00:00'),
-- 运维人员 3 人
('operator01', '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '李明',   'operator01@example.com', '13800000002', 1, '2026-01-10 10:30:00'),
('operator02', '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '王芳',   'operator02@example.com', '13800000003', 1, '2026-01-12 14:15:00'),
('operator03', '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '赵强',   'operator03@example.com', '13800000004', 1, '2026-02-01 08:45:00'),
-- 访客 4 人
('viewer01',   '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '周婷',   'viewer01@example.com',   '13800000005', 1, '2026-02-05 11:00:00'),
('viewer02',   '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '吴磊',   'viewer02@example.com',   '13800000006', 1, '2026-02-08 15:30:00'),
('viewer03',   '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '郑雪',   'viewer03@example.com',   '13800000007', 0, '2026-02-15 09:20:00'),
('viewer04',   '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '孙浩',   'viewer04@example.com',   '13800000008', 1, '2026-02-20 13:45:00');

-- ===================== 4. 分配用户角色 =====================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin    -> ADMIN
(2, 2),  -- operator01 -> OPERATOR
(3, 2),  -- operator02 -> OPERATOR
(4, 2),  -- operator03 -> OPERATOR
(5, 3),  -- viewer01  -> VIEWER
(6, 3),  -- viewer02  -> VIEWER
(7, 3),  -- viewer03  -> VIEWER (已禁用)
(8, 3);  -- viewer04  -> VIEWER

-- ===================== 5. 分配角色权限 =====================
-- ADMIN：拥有所有权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 1, id FROM sys_permission;

-- OPERATOR：流程管理、机器人管理、执行监控、任务执行（无用户/角色/权限管理，无数据删除）
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 2, id FROM sys_permission WHERE perm_key IN (
  'process:manage', 'process:view', 'process:add', 'process:edit', 'process:delete',
  'robot:manage', 'robot:view', 'robot:add', 'robot:edit', 'robot:delete',
  'execute:manage', 'execute:view',
  'task:view', 'task:execute'
);

-- VIEWER：任务查看、数据查询导出（无新增/编辑/删除/执行，无流程/机器人管理，无系统管理）
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 3, id FROM sys_permission WHERE perm_key IN (
  'task:view', 'task:manage',
  'execute:view',
  'data:manage', 'data:query', 'data:export'
);

-- ===================== 6. 初始化流程数据 =====================
INSERT INTO process_def (process_code, process_name, description, steps, status, collect_script, parse_script, process_script, save_script, create_user) VALUES
('PROC_INVOICE_001', '发票采集流程',     '自动登录税局系统，采集企业进项/销项发票明细数据',  4, 1,
 'def collect():\n    url = "https://etax.example.com/invoice"\n    return {"url": url, "type": "invoice"}',
 'def parse(data):\n    return {"invoices": data.get("items", [])}',
 'def process(data):\n    data["processed"] = True\n    return data',
 'def save(data):\n    return True', 2),
('PROC_INVOICE_002', '发票查验流程',     '批量核验发票真伪，导出查验结果报告',               4, 1,
 'def collect():\n    return {"type": "verify"}',
 'def parse(data):\n    return {"verified": True}',
 'def process(data):\n    data["status"] = "verified"\n    return data',
 'def save(data):\n    return True', 3),
('PROC_REPORT_001',  '财务报表生成流程',  '按月自动生成资产负债表、利润表、现金流量表',      4, 1,
 'def collect():\n    return {"type": "report", "period": "monthly"}',
 'def parse(data):\n    return {"template": "standard_v2"}',
 'def process(data):\n    data["generated"] = True\n    return data',
 'def save(data):\n    return True', 2),
('PROC_TAX_001',     '税务申报流程',     '完成增值税、所得税等税种的自动申报',                 4, 1,
 'def collect():\n    return {"type": "tax", "scope": "all"}',
 'def parse(data):\n    return {"forms": data.get("forms", [])}',
 'def process(data):\n    data["filed"] = True\n    return data',
 'def save(data):\n    return True', 2),
('PROC_RECON_001',   '银行对账流程',     '自动获取银行回单与企业日记账进行比对，生成差异表',  4, 1,
 'def collect():\n    return {"type": "bank_recon"}',
 'def parse(data):\n    return {"matches": [], "disputes": []}',
 'def process(data):\n    data["reconciled"] = True\n    return data',
 'def save(data):\n    return True', 3),
('PROC_INVOICE_003', '发票 OCR 识别流程', '扫描/拍照发票图片，自动 OCR 识别关键字段',         4, 0,
 'def collect():\n    return {"type": "ocr"}',
 'def parse(data):\n    return {"recognized": True}',
 'def process(data):\n    data["verified"] = True\n    return data',
 'def save(data):\n    return True', 3);

-- ===================== 7. 初始化机器人数据 =====================
INSERT INTO robot_info (robot_code, robot_name, robot_type, ip, port, execute_thread, status, cpu_usage, mem_usage, last_heartbeat) VALUES
('ROBOT_001', 'Windows 机器人 1 号', 'Windows', '192.168.1.101', 8081, 'thread-proc-01', 1, 25.50, 45.20, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
('ROBOT_002', 'Windows 机器人 2 号', 'Windows', '192.168.1.102', 8081, 'thread-proc-02', 1, 30.20, 50.10, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
('ROBOT_003', 'Linux 机器人 1 号',   'Linux',   '192.168.1.103', 8081, 'thread-proc-03', 2, 65.80, 72.30, DATE_SUB(NOW(), INTERVAL 1 MINUTE)),
('ROBOT_004', 'Windows 机器人 3 号', 'Windows', '192.168.1.104', 8081, 'thread-proc-04', 0,  0.00,  0.00,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
('ROBOT_005', 'Linux 机器人 2 号',   'Linux',   '192.168.1.105', 8081, 'thread-proc-05', 1, 40.00, 55.00, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
('ROBOT_006', 'Windows 机器人 4 号', 'Windows', '192.168.1.106', 8081, 'thread-proc-06', 1, 18.00, 38.00, DATE_SUB(NOW(), INTERVAL 1 MINUTE));

-- ===================== 8. 初始化任务数据 =====================
INSERT INTO task_info (task_code, task_name, process_id, robot_id, enterprise_name, tax_no, status, create_user, create_time) VALUES
-- 发票采集类
('TASK_001', '北京科技有限公司-发票采集',       1, 1, '北京科技有限公司',       '91110000000000001A', 1, 3, '2026-02-01 09:00:00'),
('TASK_002', '上海贸易公司-发票采集',           1, 2, '上海贸易公司',           '91310000000000002B', 1, 3, '2026-02-03 10:15:00'),
('TASK_003', '广州制造企业-发票采集',           1, 1, '广州制造企业',           '91440000000000003C', 1, 4, '2026-02-05 11:30:00'),
('TASK_004', '深圳科技公司-发票采集',           1, 5, '深圳科技公司',           '91440300000000004D', 1, 3, '2026-02-08 14:00:00'),
-- 发票查验类
('TASK_005', '北京科技有限公司-发票查验',       2, 2, '北京科技有限公司',       '91110000000000001A', 1, 3, '2026-02-02 09:30:00'),
('TASK_006', '成都商贸公司-发票查验',           2, 6, '成都商贸公司',           '91510000000000005E', 1, 4, '2026-02-09 15:00:00'),
-- 财务报表类
('TASK_007', '北京科技有限公司-月报生成',       3, 1, '北京科技有限公司',       '91110000000000001A', 1, 3, '2026-02-10 08:00:00'),
('TASK_008', '杭州互联网公司-月报生成',        3, 2, '杭州互联网公司',         '93330000000000006F', 1, 3, '2026-02-11 08:30:00'),
('TASK_009', '南京制造集团-月报生成',          3, 5, '南京制造集团',           '93320000000000007G', 1, 4, '2026-02-14 09:00:00'),
-- 税务申报类
('TASK_010', '北京科技有限公司-税务申报',       4, 1, '北京科技有限公司',       '91110000000000001A', 1, 3, '2026-02-15 08:00:00'),
('TASK_011', '深圳科技公司-税务申报',          4, 2, '深圳科技公司',           '91440300000000004D', 1, 3, '2026-02-15 09:00:00'),
-- 银行对账类
('TASK_012', '北京科技有限公司-银行对账',       5, 6, '北京科技有限公司',       '91110000000000001A', 1, 4, '2026-02-18 10:00:00'),
-- 停用/草稿任务
('TASK_013', '武汉物流公司-发票采集（停用）',  1, 4, '武汉物流公司',           '91420000000000008H', 0, 3, '2026-01-20 14:00:00'),
('TASK_014', '西安建筑公司-月报生成（草稿）',  3, 0, '西安建筑公司',           '91610000000000009I', 0, 3, '2026-02-20 16:00:00');

-- ===================== 9. 初始化执行记录数据 =====================
INSERT INTO task_execute (task_id, process_id, robot_id, execute_status, cost_time, error_msg, start_time, end_time) VALUES
-- TASK_001: 3条执行记录（成功、成功、失败）
(1, 1, 1, 3, 15230, NULL,                                     DATE_SUB(NOW(), INTERVAL 6 HOUR),   DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(1, 1, 1, 3, 14500, NULL,                                     DATE_SUB(NOW(), INTERVAL 2 DAY),   DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 1, 2, 4, 22000, '网络连接超时，请检查网络配置',             DATE_SUB(NOW(), INTERVAL 3 DAY),   DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- TASK_002: 2条（成功、运行中）
(2, 1, 2, 3, 12450, NULL,                                     DATE_SUB(NOW(), INTERVAL 1 DAY),   DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 1, 2, 2,     0, NULL,                                     DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL),
-- TASK_003: 1条成功
(3, 1, 1, 3, 18920, NULL,                                     DATE_SUB(NOW(), INTERVAL 5 HOUR),   DATE_SUB(NOW(), INTERVAL 4 HOUR)),
-- TASK_005: 1条成功
(5, 2, 2, 3,  8500, NULL,                                     DATE_SUB(NOW(), INTERVAL 4 HOUR),   DATE_SUB(NOW(), INTERVAL 4 HOUR)),
-- TASK_007: 1条成功
(7, 3, 1, 3, 32100, NULL,                                     DATE_SUB(NOW(), INTERVAL 1 DAY),   DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- TASK_008: 1条失败（解析脚本异常）
(8, 3, 2, 4,  5600, '解析脚本执行异常: JSON 格式错误',         DATE_SUB(NOW(), INTERVAL 18 HOUR),  DATE_SUB(NOW(), INTERVAL 18 HOUR)),
-- TASK_010: 1条运行中
(10, 4, 1, 2,     0, NULL,                                     DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL),
-- TASK_012: 1条待执行
(12, 5, 6, 1,     0, NULL,                                     DATE_SUB(NOW(), INTERVAL 2 HOUR),   NULL);

-- ===================== 10. 初始化发票数据 =====================
INSERT INTO data_collection (task_id, source_url, raw_data, collect_status, create_time) VALUES
(1, 'https://etax.example.com/invoice/list?taxNo=91110000000000001A',
 '{"invoiceCode":"1110016001","invoiceNo":"12345678","totalAmount":10000.00}', 1,
 DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(1, 'https://etax.example.com/invoice/list?taxNo=91110000000000001A',
 '{"invoiceCode":"1110016002","invoiceNo":"12345679","totalAmount":5000.00}', 1,
 DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(2, 'https://etax.example.com/invoice/list?taxNo=91310000000000002B',
 '{"invoiceCode":"2110016001","invoiceNo":"87654321","totalAmount":30000.00}', 1,
 DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'https://etax.example.com/verify',
 '{"invoiceCode":"1110016001","invoiceNo":"12345678","checkCode":"ABC123"}', 1,
 DATE_SUB(NOW(), INTERVAL 4 HOUR));

INSERT INTO data_invoice (task_id, invoice_code, invoice_no, invoice_date, check_code,
                          buyer_name, buyer_tax_no, seller_name, seller_tax_no,
                          amount, tax_amount, total_amount, invoice_type, create_time) VALUES
(1, '1110016001', '12345678', '2026-01-15', 'ABC123',
 '北京科技有限公司', '91110000000000001A', '北京物资供应公司', '91110000000000011X',
 8849.56, 1150.44, 10000.00, 'VAT_SPECIAL', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(1, '1110016002', '12345679', '2026-01-20', 'DEF456',
 '北京科技有限公司', '91110000000000001A', '天津制造公司',   '91120000000000021Y',
 4426.55,  575.45,  5002.00, 'VAT_SPECIAL', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(2, '2110016001', '87654321', '2026-02-01', 'GHI789',
 '上海贸易公司',   '91310000000000002B', '上海物流公司', '91310000000000031Z',
 26548.67, 3451.33, 30000.00, 'VAT_SPECIAL', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, '2110016002', '87654322', '2026-02-05', 'JKL012',
 '上海贸易公司',   '91310000000000002B', '杭州电商公司', '93330000000000032W',
 8849.56, 1150.44, 10000.00, 'VAT_NORMAL',  DATE_SUB(NOW(), INTERVAL 1 DAY));
