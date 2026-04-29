package com.rpa.task.service;

import com.rpa.auth.advice.GlobalExceptionHandler.BusinessException;
import com.rpa.process.model.ProcessDef;
import com.rpa.robot.dto.RobotDTO;
import com.rpa.task.dto.ExecutionRecordDTO;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskInfoRepository taskInfoRepository;
    
    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @Autowired(required = false)
    private com.rpa.process.service.ProcessDefService processDefService;

    @Autowired(required = false)
    private com.rpa.robot.service.RobotService robotService;

    @Transactional(readOnly = true)
    public Page<TaskInfo> findAll(String keyword, Long processId, Long robotId, String status, String startDate, String endDate, int page, int pageSize) {
        Integer statusInt = null;
        if (status != null && !status.isBlank()) {
            switch (status) {
                case "pending": statusInt = 1; break;
                case "running": statusInt = 2; break;
                case "completed": statusInt = 3; break;
                case "failed": statusInt = 4; break;
                case "enabled": statusInt = 1; break; // 启用包括所有非禁用状态
                case "disabled": statusInt = 0; break; // 禁用状态
            }
        }
        
        java.time.LocalDateTime startDateTime = null;
        java.time.LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.isBlank()) {
            startDateTime = java.time.LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isBlank()) {
            endDateTime = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
        }
        
        return taskInfoRepository.findAll(keyword, processId, robotId, statusInt, startDateTime, endDateTime, PageRequest.of(page - 1, pageSize));
    }

    @Transactional(readOnly = true)
    public Page<TaskInfo> findAllByExecutionTime(String keyword, Long processId, Long robotId, String status, String startDate, String endDate, int page, int pageSize) {
        Integer statusInt = null;
        if (status != null && !status.isBlank()) {
            switch (status) {
                case "pending": statusInt = 1; break;
                case "running": statusInt = 2; break;
                case "completed": statusInt = 3; break;
                case "failed": statusInt = 4; break;
                case "enabled": statusInt = 1; break;
                case "disabled": statusInt = 0; break;
            }
        }
        
        java.time.LocalDateTime startDateTime = null;
        java.time.LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.isBlank()) {
            startDateTime = java.time.LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isBlank()) {
            endDateTime = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
        }
        
        // 先查询符合执行时间条件的任务ID列表
        List<Long> taskIds = null;
        if (startDateTime != null && endDateTime != null) {
            taskIds = taskInfoRepository.findTaskIdsByExecutionTimeBetween(startDateTime, endDateTime);
            if (taskIds == null || taskIds.isEmpty()) {
                // 如果没有执行记录，返回空结果
                return new org.springframework.data.domain.PageImpl<>(
                    java.util.Collections.emptyList(), 
                    PageRequest.of(page - 1, pageSize), 
                    0
                );
            }
        }
        
        // 根据任务ID列表查询（分页）
        return taskInfoRepository.findByIdIn(taskIds, keyword, processId, robotId, statusInt, PageRequest.of(page - 1, pageSize));
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
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的任务");
        }
        // 批量验证是否存在
        List<Long> existingIds = taskInfoRepository.findAllById(ids).stream()
            .map(TaskInfo::getId)
            .toList();
        
        if (existingIds.size() != ids.size()) {
            throw new BusinessException("部分任务不存在，请刷新重试");
        }
        
        taskInfoRepository.deleteByIdIn(ids);
    }

    @Transactional
    public void executeTask(Long id) {
        TaskInfo task = taskInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        
        // 不再要求必须分配机器人，会在队列处理时动态分配
        // if (task.getRobotId() == null) {
        //     throw new BusinessException("任务未分配机器人，无法执行");
        // }
        
        // 检查流程是否已配置
        if (task.getProcessId() == null) {
            throw new BusinessException("任务未关联流程，无法执行");
        }
        
        // 将任务添加到执行队列
        TaskExecutionQueue queue = taskSchedulerService.addToQueue(task);
        
        log.info("任务已添加到执行队列，taskId: {}, queueId: {}", id, queue.getId());
    }
    
    @Transactional(readOnly = true)
    public List<TaskExecutionQueue> findExecutionListByTaskId(Long taskId) {
        return taskSchedulerService.findExecutionListByTaskId(taskId);
    }
    
    /**
     * 获取待执行任务
     */
    @Transactional(readOnly = true)
    public TaskInfo findPendingTask(Long robotId) {
        // 查询启用的、状态为待执行的任务
        return taskInfoRepository.findFirstByStatusAndEnabledTrueOrderByPriorityDescCreateTimeAsc(1);
    }
    
    /**
     * 禁用任务
     */
    @Transactional
    public void disableTask(Long id) {
        TaskInfo task = taskInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        
        task.setIsDisabled(1);
        taskInfoRepository.save(task);
        
        log.info("任务已禁用，taskId: {}", id);
    }
    
    /**
     * 启用任务
     */
    @Transactional
    public void enableTask(Long id) {
        TaskInfo task = taskInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        
        task.setIsDisabled(0);
        taskInfoRepository.save(task);
        
        log.info("任务已启用，taskId: {}", id);
    }
    
    /**
     * 停止任务
     */
    @Transactional
    public void stopTask(Long id) {
        TaskInfo task = taskInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        
        // 将任务状态改为失败
        task.setStatus(4); // failed
        taskInfoRepository.save(task);
        
        // 停止执行队列中的任务
        List<TaskExecutionQueue> runningQueues = taskSchedulerService.findExecutionListByTaskId(id);
        for (TaskExecutionQueue queue : runningQueues) {
            if ("RUNNING".equals(queue.getStatus()) || "QUEUED".equals(queue.getStatus())) {
                queue.setStatus("CANCELLED");
                queue.setEndTime(java.time.LocalDateTime.now());
                taskSchedulerService.updateQueue(queue);
            }
        }
        
        log.info("任务已停止，taskId: {}", id);
    }
    
    /**
     * 停止之前的执行
     */
    @Transactional
    public void stopPreviousExecution(Long taskId) {
        List<TaskExecutionQueue> runningQueues = taskSchedulerService.findExecutionListByTaskId(taskId);
        for (TaskExecutionQueue queue : runningQueues) {
            if ("RUNNING".equals(queue.getStatus()) || "QUEUED".equals(queue.getStatus())) {
                queue.setStatus("CANCELLED");
                queue.setEndTime(java.time.LocalDateTime.now());
                taskSchedulerService.updateQueue(queue);
                log.info("已停止之前的执行，queueId: {}", queue.getId());
            }
        }
    }
    
    /**
     * 获取任务的执行记录列表（包含流程和机器人信息）
     */
    @Transactional(readOnly = true)
    public List<ExecutionRecordDTO> findExecutionRecordsWithDetails(Long taskId) {
        List<TaskExecutionQueue> queues = taskSchedulerService.findExecutionListByTaskId(taskId);
        
        return queues.stream().map(queue -> {
            ExecutionRecordDTO dto = ExecutionRecordDTO.fromQueue(queue);
            
            // 获取流程信息
            if (queue.getProcessId() != null) {
                try {
                    var process = getProcessService().getById(queue.getProcessId());
                    if (process != null) {
                        dto.setProcessCode(process.getProcessCode());
                        dto.setProcessName(process.getProcessName());
                    }
                } catch (Exception e) {
                    log.warn("获取流程信息失败，processId: {}", queue.getProcessId(), e);
                }
            }
            
            // 获取机器人信息
            if (queue.getRobotId() != null) {
                try {
                    var robot = getRobotService().getById(queue.getRobotId());
                    if (robot != null) {
                        dto.setRobotCode(robot.getRobotCode());
                        dto.setRobotName(robot.getRobotName());
                    }
                } catch (Exception e) {
                    log.warn("获取机器人信息失败，robotId: {}", queue.getRobotId(), e);
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取流程服务（延迟加载避免循环依赖）
     */
    private com.rpa.process.service.ProcessDefService getProcessService() {
        return processDefService;
    }
    
    private com.rpa.robot.service.RobotService getRobotService() {
        return robotService;
    }
}
