# 机器人管理模块集成说明

## 📋 概述

机器人管理模块已完成开发，现在需要将机器人与任务管理、流程管理进行集成。

---

## ✅ 已完成的工作

### 1. 机器人管理模块
- ✅ 机器人实体类（Robot）
- ✅ 机器人 Repository、Service、Controller
- ✅ 机器人 CRUD API
- ✅ 机器人状态管理 API
- ✅ 前端机器人管理页面
- ✅ 机器人状态统计

### 2. 任务管理集成
- ✅ TaskInfo 表已有 `robot_id` 字段
- ✅ 更新任务 API 支持机器人分配
- ✅ 前端任务页面添加机器人选择器
- ✅ 动态加载可用机器人列表
- ✅ 机器人状态显示（在线/忙碌/离线）

### 3. 流程管理
- ✅ ProcessDef 表结构完整
- ✅ 流程脚本校验功能
- ✅ 流程设计页面

---

## 🔧 需要添加的功能

### 1. 任务管理页面 - 机器人选择器 ✅

**文件**: `frontend/rpa-spider-management-web/src/views/task/TaskList.vue`

**已更新内容**:
```vue
<!-- 流程选择器 -->
<el-form-item label="流程" prop="processId">
  <el-select v-model="formData.processId" placeholder="请选择流程" :disabled="isEdit">
    <el-option
      v-for="item in processList"
      :key="item.id"
      :label="item.processName"
      :value="item.id"
    >
      <span>{{ item.processName }}</span>
      <span style="float: right; color: #8492a6; font-size: 13px">{{ item.processCode }}</span>
    </el-option>
  </el-select>
</el-form-item>

<!-- 机器人选择器 -->
<el-form-item label="机器人" prop="robotId">
  <el-select v-model="formData.robotId" placeholder="请选择机器人" clearable>
    <el-option
      v-for="item in robotList"
      :key="item.id"
      :label="item.robotName"
      :value="item.id"
    >
      <span>{{ item.robotName }}</span>
      <el-tag size="small" :type="getStatusType(item.status)">
        {{ getStatusLabel(item.status) }}
      </el-tag>
    </el-option>
  </el-select>
</el-form-item>
```

**JavaScript 代码**:
```javascript
import { getProcessList, getAvailableRobots } from '@/api/task'

// 加载流程列表
const loadProcessList = async () => {
  const res = await getProcessList({ page: 1, pageSize: 100, status: 1 })
  processList.value = res.data.records || []
}

// 加载可用机器人列表
const loadRobotList = async () => {
  const res = await getAvailableRobots()
  robotList.value = res.data || []
}
```

---

## 📊 数据库关系

### 表结构

```sql
-- 流程定义表
CREATE TABLE process_def (
  id BIGINT PRIMARY KEY,
  process_code VARCHAR(50),
  process_name VARCHAR(50),
  -- ... 其他字段
);

-- 机器人信息表
CREATE TABLE robot_info (
  id BIGINT PRIMARY KEY,
  robot_code VARCHAR(50),
  robot_name VARCHAR(50),
  type VARCHAR(20),  -- ATTENDED/UNATTENDED
  status VARCHAR(20), -- ONLINE/OFFLINE/BUSY/FAULT
  -- ... 其他字段
);

-- 任务信息表
CREATE TABLE task_info (
  id BIGINT PRIMARY KEY,
  task_code VARCHAR(50),
  task_name VARCHAR(50),
  process_id BIGINT,  -- 关联 process_def.id
  robot_id BIGINT,    -- 关联 robot_info.id
  -- ... 其他字段
);
```

### 关系图

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│ process_def │─────<│ task_info   │>────│ robot_info  │
│  (流程表)   │ 1:N  │  (任务表)   │  N:1 │  (机器人表) │
└─────────────┘      └─────────────┘      └─────────────┘
```

---

## 🚀 API 接口

### 机器人相关 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/robot` | GET | 分页查询机器人列表 |
| `/robot/{id}` | GET | 查询机器人详情 |
| `/robot` | POST | 创建机器人 |
| `/robot/{id}` | PUT | 更新机器人 |
| `/robot/{id}` | DELETE | 删除机器人 |
| `/robot/status` | GET | 获取状态统计 |
| `/robot/available` | GET | **获取可用机器人** |
| `/robot/{id}/status` | PUT | 更新机器人状态 |
| `/robot/{id}/heartbeat` | POST | 更新心跳时间 |

### 任务相关 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/task/list` | GET | 分页查询任务列表 |
| `/task` | POST | 创建任务（包含 robot_id） |
| `/task/{id}` | PUT | 更新任务（可更新 robot_id） |
| `/task/{id}` | DELETE | 删除任务 |
| `/task/{id}` | GET | 查询任务详情 |
| `/task/{id}/execute` | POST | 执行任务 |

### 流程相关 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/process/list` | GET | 分页查询流程列表 |
| `/process` | POST | 创建流程 |
| `/process/{id}` | PUT | 更新流程 |
| `/process/{id}` | DELETE | 删除流程 |
| `/process/{id}` | GET | 查询流程详情 |
| `/process/{id}/validate/all` | POST | 校验所有环节脚本 |

---

## 💡 使用流程

### 1. 创建机器人
```bash
POST /robot
{
  "robotCode": "ROBOT-001",
  "robotName": "测试机器人 1 号",
  "type": "UNATTENDED",
  "status": "ONLINE",
  "ipAddress": "192.168.1.100",
  "description": "第一个测试机器人"
}
```

### 2. 创建流程
在流程管理页面创建并配置流程脚本。

### 3. 创建任务（分配机器人）
```bash
POST /task
{
  "taskCode": "TASK-001",
  "taskName": "发票采集任务",
  "processId": 1,        // 选择流程
  "robotId": 1,          // 分配机器人
  "taxNo": "91110000XXXXXXXXXX",
  "enterpriseName": "测试企业"
}
```

### 4. 执行任务
```bash
POST /task/{id}/execute
```

系统会根据任务的 `robot_id` 字段，将任务分配给指定的机器人执行。

---

## 🎯 机器人分配策略

### 当前实现
- 手动分配：创建任务时手动选择机器人
- 机器人可清空：允许不指定机器人（待分配）

### 可扩展功能（可选）
1. **自动分配**: 根据机器人负载自动分配
2. **轮询分配**: 轮流分配给可用机器人
3. **优先级分配**: 根据任务优先级分配
4. **分组分配**: 按部门/组分配机器人

---

## 📝 测试步骤

### 1. 准备数据
```sql
-- 1. 创建机器人
INSERT INTO robot_info (robot_code, robot_name, type, status, ip, port) 
VALUES ('ROBOT-001', '测试机器人 1 号', 'UNATTENDED', 'ONLINE', '192.168.1.100', 8080);

-- 2. 创建流程（已有）
-- 3. 创建任务
INSERT INTO task_info (task_code, task_name, process_id, robot_id, tax_no) 
VALUES ('TASK-001', '测试任务', 1, 1, '91110000XXXXXXXXXX');
```

### 2. 前端测试
1. 访问任务管理页面
2. 点击"新建任务"
3. 选择流程（下拉列表显示所有启用的流程）
4. 选择机器人（下拉列表显示在线/忙碌的机器人）
5. 保存任务
6. 查看任务列表，确认机器人信息显示正确

### 3. API 测试
```bash
# 获取可用机器人
curl http://localhost:8080/robot/available

# 创建任务
curl -X POST http://localhost:8080/task \
  -H "Content-Type: application/json" \
  -d '{
    "taskCode": "TASK-002",
    "taskName": "测试任务 2",
    "processId": 1,
    "robotId": 1
  }'
```

---

## 🔍 常见问题

### Q1: 机器人选择器显示为空？
**A**: 检查以下几点：
1. 数据库中是否有机器人数据
2. 机器人状态是否为 ONLINE 或 BUSY
3. 后端服务是否正常运行
4. 浏览器控制台是否有错误信息

### Q2: 任务执行时找不到机器人？
**A**: 
1. 检查任务的 robot_id 是否为空
2. 检查机器人是否在线（status = 'ONLINE'）
3. 检查机器人心跳时间是否正常更新

### Q3: 如何修改已创建任务的机器人？
**A**: 
1. 在任务列表页面点击"编辑"
2. 重新选择机器人
3. 保存即可

---

## 📈 后续优化建议

### 1. 机器人状态监控
- 实时显示机器人状态
- 心跳检测（超过 5 分钟无心跳标记为离线）
- 故障告警

### 2. 任务调度优化
- 自动分配机器人
- 任务队列管理
- 负载均衡

### 3. 执行历史记录
- 记录每次任务执行的机器人
- 统计机器人执行效率
- 生成执行报告

### 4. 机器人分组
- 按部门/业务类型分组
- 组内机器人共享
- 跨组调度

---

## ✅ 完成清单

- [x] 机器人管理模块开发
- [x] 任务管理添加机器人选择器
- [x] 流程管理页面完善
- [x] 动态加载机器人列表
- [x] 动态加载流程列表
- [x] 机器人状态显示
- [x] API 接口测试
- [x] 前端页面测试

---

**完成时间**: 2026-04-02  
**版本**: 1.0.0  
**状态**: ✅ 已完成并测试
