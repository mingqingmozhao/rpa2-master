# RPA 机器人流程管理详解

## 📋 核心概念

### 三者关系
```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  流程定义   │─────>│   任务      │─────>│   机器人    │
│  (模板)     │ 1:N  │  (实例)     │ N:1  │  (执行者)   │
└─────────────┘      └─────────────┘      └─────────────┘
```

- **流程 (Process)**：定义业务逻辑的模板，包含 4 个环节的脚本
- **任务 (Task)**：流程的具体实例，关联了具体的企业和机器人
- **机器人 (Robot)**：执行任务的自动化程序

---

## 🎯 完整工作流程

### 步骤 1：创建流程定义
**操作位置**：流程管理页面

```
流程编码：PROCESS-INVOICE-001
流程名称：发票采集流程
流程描述：从税务局网站采集发票数据

环节配置：
1. 采集环节：登录税务局网站，查询发票列表
2. 解析环节：解析 HTML，提取发票字段
3. 加工环节：数据清洗、格式转换
4. 落库环节：保存到发票数据表
```

**数据库记录**：
```sql
INSERT INTO process_def (
  process_code, process_name, 
  collect_script, parse_script, 
  process_script, save_script
) VALUES (
  'PROCESS-INVOICE-001',
  '发票采集流程',
  '-- 采集脚本',
  '-- 解析脚本',
  '-- 加工脚本',
  '-- 落库脚本'
);
```

---

### 步骤 2：创建任务（分配流程给机器人）
**操作位置**：任务管理页面

```
任务编码：TASK-20260402-001
任务名称：A 公司发票采集
选择流程：发票采集流程（从流程列表选择）
选择机器人：机器人 001（从在线机器人选择）
企业名称：A 公司
纳税人识别号：91110000XXXXXXXXXX
```

**数据库记录**：
```sql
INSERT INTO task_info (
  task_code, task_name,
  process_id,      -- 关联流程 ID
  robot_id,        -- 分配机器人 ID
  enterprise_name,
  tax_no
) VALUES (
  'TASK-20260402-001',
  'A 公司发票采集',
  1,               -- 流程 ID
  1,               -- 机器人 ID
  'A 公司',
  '91110000XXXXXXXXXX'
);
```

---

### 步骤 3：执行任务
**操作位置**：任务管理页面 → 点击"执行"

**执行流程**：
```
1. 系统检查机器人状态（是否在线）
2. 加载流程定义的 4 个脚本
3. 将任务参数传递给机器人
4. 机器人执行脚本
5. 实时返回执行状态
```

**执行状态流转**：
```
待执行 → 运行中 → 已完成
            ↓
          执行失败
```

---

### 步骤 4：机器人执行脚本
**机器人端执行逻辑**：

```groovy
// 1. 采集环节
def collectResult = collect() {
    // 登录税务局网站
    def loginResult = login(username, password)
    
    // 查询发票
    def invoices = queryInvoices(taxNo, startDate, endDate)
    
    return invoices
}

// 2. 解析环节
def parseResult = parse(collectResult) {
    // 解析 HTML
    def invoices = parseHtml(collectResult)
    
    // 提取字段
    return invoices.map { invoice ->
        [
            invoiceCode: invoice.code,
            invoiceNo: invoice.number,
            amount: invoice.amount,
            date: invoice.date
        ]
    }
}

// 3. 加工环节
def processResult = process(parseResult) {
    // 数据清洗
    parseResult.each { invoice ->
        // 去除空格
        invoice.invoiceCode = invoice.invoiceCode.trim()
        
        // 格式转换
        invoice.amount = invoice.amount.toBigDecimal()
        
        // 数据校验
        if (!validateInvoice(invoice)) {
            throw new Exception("发票校验失败：" + invoice.invoiceNo)
        }
    }
    
    return parseResult
}

// 4. 落库环节
def saveResult = save(processResult) {
    // 批量插入数据库
    processResult.each { invoice ->
        def sql = """
            INSERT INTO data_invoice 
            (invoice_code, invoice_no, amount, invoice_date, create_time)
            VALUES (?, ?, ?, ?, NOW())
        """
        executeSql(sql, [
            invoice.invoiceCode,
            invoice.invoiceNo,
            invoice.amount,
            invoice.date
        ])
    }
    
    return true
}
```

---

## 🔄 机器人如何"管理"流程

### 1. 流程加载
```java
// 机器人从数据库加载流程定义
ProcessDef process = processDefService.getById(task.getProcessId());

// 获取 4 个环节的脚本
String collectScript = process.getCollectScript();
String parseScript = process.getParseScript();
String processScript = process.getProcessScript();
String saveScript = process.getSaveScript();
```

### 2. 脚本执行
```java
// 使用 Groovy 引擎执行脚本
GroovyShell shell = new GroovyShell();

// 执行采集环节
shell.evaluate(collectScript);

// 执行解析环节
shell.evaluate(parseScript);

// ... 其他环节
```

### 3. 状态管理
```java
// 更新机器人状态
robot.setStatus("BUSY");  // 忙碌
robotRepository.save(robot);

// 执行完成后
if (success) {
    robot.setStatus("ONLINE");  // 在线
} else {
    robot.setStatus("FAULT");   // 故障
}
robotRepository.save(robot);
```

### 4. 任务分配
```java
// 自动分配策略（可选）
public Robot assignRobot(Task task) {
    // 1. 查找所有在线机器人
    List<Robot> availableRobots = robotRepository.findByStatus("ONLINE");
    
    // 2. 选择负载最低的机器人
    return availableRobots.stream()
        .min(Comparator.comparing(Robot::getCurrentTaskCount))
        .orElseThrow(() -> new RuntimeException("无可用机器人"));
}
```

---

## 📊 实际应用场景

### 场景 1：定时执行
```
1. 创建定时任务：每天 9:00 执行
2. 关联流程：发票采集流程
3. 分配机器人：机器人 001
4. 到点自动执行
```

### 场景 2：多企业批量采集
```
流程：发票采集流程（通用模板）

任务 1：A 公司发票采集 → 机器人 001
任务 2：B 公司发票采集 → 机器人 002
任务 3：C 公司发票采集 → 机器人 003

并行执行，提高效率
```

### 场景 3：故障转移
```
任务执行中：
- 机器人 001 故障
- 系统自动检测
- 重新分配给机器人 002
- 继续执行任务
```

---

## 🛠️ 管理功能

### 1. 机器人状态监控
```sql
-- 查看所有机器人状态
SELECT 
  robot_code,
  robot_name,
  status,
  last_heartbeat,
  current_task_id
FROM robot_info
WHERE is_deleted = 0;
```

**状态说明**：
- `ONLINE`：在线，可接受任务
- `BUSY`：忙碌，正在执行任务
- `OFFLINE`：离线，无法执行任务
- `FAULT`：故障，需要人工干预

---

### 2. 任务执行监控
```sql
-- 查看任务执行记录
SELECT 
  t.task_code,
  t.task_name,
  r.robot_name,
  t.execute_status,
  t.start_time,
  t.end_time,
  t.error_msg
FROM task_execute t
LEFT JOIN robot_info r ON t.robot_id = r.id
ORDER BY t.create_time DESC;
```

---

### 3. 流程版本管理（扩展功能）
```sql
-- 同一流程的多个版本
process_code: PROCESS-INVOICE-001
version: v1.0, v1.1, v2.0

-- 任务可以指定使用哪个版本
task_info.process_version: 'v1.0'
```

---

## 🎯 关键设计要点

### 1. 流程与机器人解耦
- 流程定义不包含机器人信息
- 任务作为中间层关联流程和机器人
- 同一个流程可以被多个机器人执行

### 2. 灵活的任务分配
- 手动分配：创建任务时选择机器人
- 自动分配：系统根据负载自动分配
- 动态调度：执行中可以切换机器人

### 3. 状态可追溯
- 机器人状态变化有记录
- 任务执行过程有日志
- 流程版本变更有历史

---

## 📈 扩展功能建议

### 1. 机器人分组
```sql
-- 添加机器人分组
ALTER TABLE robot_info ADD COLUMN group_id BIGINT;

-- 任务可以指定机器人组
ALTER TABLE task_info ADD COLUMN robot_group_id BIGINT;
```

### 2. 负载均衡
```java
// 轮询分配
public Robot assignRobotRoundRobin(List<Task> tasks) {
    // 轮流分配给不同机器人
}

// 最少任务分配
public Robot assignRobotLeastTasks() {
    // 选择当前任务最少的机器人
}
```

### 3. 执行队列
```sql
-- 任务执行队列
CREATE TABLE task_execution_queue (
  id BIGINT PRIMARY KEY,
  task_id BIGINT,
  robot_id BIGINT,
  priority INT,
  status VARCHAR(20),
  create_time DATETIME
);
```

---

## ✅ 总结

**机器人管理流程的核心**：

1. **流程是模板**：定义业务逻辑
2. **任务是实例**：关联流程和机器人
3. **机器人是执行者**：加载流程脚本并执行
4. **状态可监控**：实时了解执行情况

**管理方式**：
- 通过任务管理页面分配流程给机器人
- 通过机器人管理页面监控状态
- 通过执行日志查看详细信息

---

**完成时间**: 2026-04-02  
**版本**: 1.0.0
