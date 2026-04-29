package com.rpa.task.controller;

import com.rpa.auth.dto.ApiResponse;
import com.rpa.task.dto.ExecutionRecordDTO;
import com.rpa.task.model.TaskExecutionQueue;
import com.rpa.task.model.TaskInfo;
import com.rpa.task.service.TaskService;
import com.rpa.task.service.TaskSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long processId,
            @RequestParam(required = false) Long robotId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String timeType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<TaskInfo> result;
        
        // 根据时间类型选择不同的查询方法
        if ("execution".equalsIgnoreCase(timeType)) {
            result = taskService.findAllByExecutionTime(keyword, processId, robotId, status, startDate, endDate, page, pageSize);
        } else {
            // 默认按创建时间筛选
            result = taskService.findAll(keyword, processId, robotId, status, startDate, endDate, page, pageSize);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", result.getContent());
        response.put("total", result.getTotalElements());
        
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskInfo> getById(@PathVariable Long id) {
        return ApiResponse.ok(taskService.findById(id));
    }

    @PostMapping
    public ApiResponse<TaskInfo> create(@RequestBody TaskInfo task) {
        return ApiResponse.ok(taskService.create(task));
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskInfo> update(@PathVariable Long id, @RequestBody TaskInfo task) {
        return ApiResponse.ok(taskService.update(id, task));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ApiResponse.ok(null);
    }
    
    /**
     * 批量删除任务
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> ids) {
        taskService.batchDelete(ids);
        return ApiResponse.ok(null);
    }
    
    /**
     * 获取任务的执行记录列表（包含流程和机器人信息）
     */
    @GetMapping("/execution/list")
    public ApiResponse<List<ExecutionRecordDTO>> executionList(
            @RequestParam Long taskId) {
        List<ExecutionRecordDTO> result = taskService.findExecutionRecordsWithDetails(taskId);
        return ApiResponse.ok(result);
    }
    
    /**
     * 立即触发处理任务队列
     */
    @PostMapping("/queue/trigger")
    public ApiResponse<Void> triggerQueue() {
        taskSchedulerService.triggerProcessQueue();
        return ApiResponse.ok(null);
    }
    
    /**
     * 获取待执行任务（供机器人客户端调用）
     */
    @GetMapping("/pending")
    public ApiResponse<TaskInfo> getPendingTask(
            @RequestParam(required = false) Long robotId) {
        try {
            TaskInfo pendingTask = taskService.findPendingTask(robotId);
            if (pendingTask != null) {
                return ApiResponse.ok(pendingTask);
            } else {
                return ApiResponse.ok(null);
            }
        } catch (Exception e) {
            log.error("获取待执行任务失败", e);
            return ApiResponse.error(500, "获取待执行任务失败：" + e.getMessage());
        }
    }
}
