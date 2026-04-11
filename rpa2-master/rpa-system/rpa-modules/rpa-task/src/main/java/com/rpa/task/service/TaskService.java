package com.rpa.task.service;

import com.rpa.auth.advice.GlobalExceptionHandler.BusinessException;
import com.rpa.task.model.TaskExecutionQueue;
import com.rpa.task.model.TaskInfo;
import com.rpa.task.repository.TaskInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskInfoRepository taskInfoRepository;
    
    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @Transactional(readOnly = true)
    public Page<TaskInfo> findAll(String keyword, Long processId, String status, int page, int pageSize) {
        Integer statusInt = null;
        if (status != null && !status.isBlank()) {
            switch (status) {
                case "pending": statusInt = 1; break;
                case "running": statusInt = 2; break;
                case "completed": statusInt = 3; break;
                case "failed": statusInt = 4; break;
            }
        }
        return taskInfoRepository.findAll(keyword, processId, statusInt, PageRequest.of(page - 1, pageSize));
    }

    @Transactional(readOnly = true)
    public TaskInfo findById(Long id) {
        return taskInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));
    }

    @Transactional
    public TaskInfo create(TaskInfo task) {
        if (taskInfoRepository.existsByTaskCode(task.getTaskCode())) {
            throw new BusinessException("任务编码已存在");
        }
        return taskInfoRepository.save(task);
    }

    @Transactional
    public TaskInfo update(Long id, TaskInfo task) {
        TaskInfo existingTask = taskInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        existingTask.setTaskName(task.getTaskName());
        existingTask.setProcessId(task.getProcessId());
        existingTask.setRobotId(task.getRobotId());
        existingTask.setCompanyName(task.getCompanyName());
        existingTask.setEnterpriseName(task.getEnterpriseName());
        existingTask.setTaxNo(task.getTaxNo());
        existingTask.setCategory(task.getCategory());
        existingTask.setStatus(task.getStatus());
        existingTask.setPriority(task.getPriority());
        existingTask.setRemark(task.getRemark());

        return taskInfoRepository.save(existingTask);
    }

    @Transactional
    public void delete(Long id) {
        if (!taskInfoRepository.existsById(id)) {
            throw new BusinessException("任务不存在");
        }
        taskInfoRepository.deleteById(id);
    }

    @Transactional
    public void executeTask(Long id) {
        TaskInfo task = taskInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        
        // 检查机器人是否已分配
        if (task.getRobotId() == null) {
            throw new BusinessException("任务未分配机器人，无法执行");
        }
        
        // 检查流程是否已配置
        if (task.getProcessId() == null) {
            throw new BusinessException("任务未关联流程，无法执行");
        }
        
        // 将任务添加到执行队列
        TaskExecutionQueue queue = taskSchedulerService.addToQueue(task);
        
        log.info("任务已添加到执行队列，taskId: {}, queueId: {}", id, queue.getId());
    }
}
