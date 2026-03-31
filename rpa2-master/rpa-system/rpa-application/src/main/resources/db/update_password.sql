-- 更新所有用户密码为 admin123 (BCrypt 哈希)
-- 如果表不存在请先执行 schema.sql
-- 如果数据不存在请先执行 init.sql

-- 更新现有用户的密码
UPDATE sys_user SET password = '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO' WHERE username IN ('admin', 'operator01', 'operator02', 'operator03', 'viewer01', 'viewer02', 'viewer03', 'viewer04');

-- 如果用户不存在，则插入新用户
INSERT IGNORE INTO sys_user (id, username, password, real_name, email, phone, status, create_time) VALUES
(1, 'admin',      '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '张建国', 'admin@example.com',      '13800000001', 1, '2026-01-05 09:00:00'),
(2, 'operator01', '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '李明',   'operator01@example.com', '13800000002', 1, '2026-01-10 10:30:00'),
(3, 'operator02', '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '王芳',   'operator02@example.com', '13800000003', 1, '2026-01-12 14:15:00'),
(4, 'operator03', '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '赵强',   'operator03@example.com', '13800000004', 1, '2026-02-01 08:45:00'),
(5, 'viewer01',   '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '周婷',   'viewer01@example.com',   '13800000005', 1, '2026-02-05 11:00:00'),
(6, 'viewer02',   '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '吴磊',   'viewer02@example.com',   '13800000006', 1, '2026-02-08 15:30:00'),
(7, 'viewer03',   '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '郑雪',   'viewer03@example.com',   '13800000007', 0, '2026-02-15 09:20:00'),
(8, 'viewer04',   '$2a$10$uh1FQ6rrT/ZPXItOu5m/juu974Rvcv9NuMqMDjTdaq3HFTBPjbJRO', '孙浩',   'viewer04@example.com',   '13800000008', 1, '2026-02-20 13:45:00');

-- 插入角色（如果不存在）
INSERT IGNORE INTO sys_role (id, role_name, remark, status) VALUES
(1, 'ADMIN', '系统管理员，拥有所有权限', 1),
(2, 'OPERATOR', '运维人员，负责流程和机器人管理', 1),
(3, 'VIEWER', '访客，仅可查看数据', 1);

-- 分配用户角色（如果不存在）
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin    -> ADMIN
(2, 2),  -- operator01 -> OPERATOR
(3, 2),  -- operator02 -> OPERATOR
(4, 2),  -- operator03 -> OPERATOR
(5, 3),  -- viewer01  -> VIEWER
(6, 3),  -- viewer02  -> VIEWER
(7, 3),  -- viewer03  -> VIEWER
(8, 3);  -- viewer04  -> VIEWER
