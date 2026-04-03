# 机器人管理模块使用说明

## 数据库迁移

请在 MySQL 客户端（如 Navicat）中执行以下 SQL 脚本添加新字段：

```sql
-- 文件位置：c:\Users\曹文武\Desktop\rpa2-master\rpa2-master\run_robot_migration.sql
```

或者直接执行以下 SQL 语句：

```sql
USE financial_data_collect;

-- 添加机器人类型字段
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
```

## API 测试

后端服务已启动：http://localhost:8080

### 1. 获取机器人列表（需要登录）
```bash
GET http://localhost:8080/robot?page=0&size=10
```

### 2. 获取机器人状态统计
```bash
GET http://localhost:8080/robot/status
```

### 3. 获取可用机器人
```bash
GET http://localhost:8080/robot/available
```

### 4. 创建机器人
```bash
POST http://localhost:8080/robot
Content-Type: application/json

{
  "robotCode": "ROBOT-001",
  "robotName": "测试机器人 1 号",
  "type": "UNATTENDED",
  "status": "OFFLINE",
  "ipAddress": "192.168.1.100",
  "description": "第一个测试机器人",
  "remark": "用于测试"
}
```

### 5. 更新机器人
```bash
PUT http://localhost:8080/robot/1
Content-Type: application/json

{
  "robotName": "测试机器人 1 号（已更新）",
  "status": "ONLINE"
}
```

### 6. 删除机器人
```bash
DELETE http://localhost:8080/robot/1
```

### 7. 更新机器人状态
```bash
PUT http://localhost:8080/robot/1/status?status=ONLINE
```

### 8. 更新心跳时间
```bash
POST http://localhost:8080/robot/1/heartbeat
```

## 前端页面

访问前端管理页面，导航到"机器人管理"菜单即可看到机器人列表页面。

功能包括：
- 机器人列表展示（分页）
- 状态统计卡片（在线/离线/忙碌/故障）
- 新建机器人
- 编辑机器人
- 删除机器人
- 搜索功能

## 注意事项

1. 数据库表 `robot_info` 必须存在
2. 后端服务必须在 8080 端口运行
3. 前端需要登录才能访问机器人管理页面
4. 需要权限：OPERATOR 或 ADMIN 角色
