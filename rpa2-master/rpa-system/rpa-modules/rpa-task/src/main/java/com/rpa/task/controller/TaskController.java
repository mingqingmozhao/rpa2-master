package com.rpa.task.controller;

import com.rpa.auth.dto.ApiResponse;
import com.rpa.task.model.TaskInfo;
import com.rpa.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long processId,
            @RequestParam(required = false) String status) {
        Page<TaskInfo> result = taskService.findAll(keyword, processId, status, page, pageSize);
        
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

    @PostMapping("/{id}/execute")
    public ApiResponse<Void> executeTask(@PathVariable Long id) {
        taskService.executeTask(id);
        return ApiResponse.ok(null);
    }
}
