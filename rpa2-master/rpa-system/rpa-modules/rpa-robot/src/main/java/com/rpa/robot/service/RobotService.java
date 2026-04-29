package com.rpa.robot.service;

import com.rpa.robot.dto.RobotDTO;
import com.rpa.robot.dto.RobotStatusDTO;
import com.rpa.robot.dto.RobotMonitorDTO;
import com.rpa.robot.model.Robot;
import com.rpa.robot.repository.RobotRepository;
import com.rpa.robot.util.IpValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RobotService {
    
    private static final Logger log = LoggerFactory.getLogger(RobotService.class);
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private RobotConnectionManager connectionManager;
    
    /**
     * 分页查询机器人列表
     */
    @Transactional(readOnly = true)
    public Page<RobotDTO> findAll(Pageable pageable) {
        log.info("分页查询机器人列表");
        return robotRepository.findAll(pageable)
                .map(RobotDTO::fromEntity);
    }
    
    /**
     * 按状态分页查询机器人列表
     */
    @Transactional(readOnly = true)
    public Page<RobotDTO> findAllByStatus(String status, Pageable pageable) {
        log.info("按状态分页查询机器人列表，status: {}", status);
        
        if (status == null || status.isEmpty()) {
            // 查询所有未删除的机器人
            return robotRepository.findAllRobots(pageable)
                    .map(RobotDTO::fromEntity);
        }
        
        // 将字符串状态转换为数字状态
        Integer statusInt = convertStatusToInteger(status);
        return robotRepository.findByStatus(statusInt, pageable)
                .map(RobotDTO::fromEntity);
    }
    
    /**
     * 根据 ID 查询机器人
     */
    @Transactional(readOnly = true)
    public RobotDTO getById(Long id) {
        log.info("根据 ID 查询机器人，ID: {}", id);
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在"));
        return RobotDTO.fromEntity(robot);
    }
    
    /**
     * 根据编码查询机器人
     */
    @Transactional(readOnly = true)
    public RobotDTO getByCode(String robotCode) {
        log.info("根据编码查询机器人，编码：{}", robotCode);
        Robot robot = robotRepository.findByRobotCodeAndIsDeletedFalse(robotCode).orElse(null);
        return robot != null ? RobotDTO.fromEntity(robot) : null;
    }
    
    /**
     * 根据编码查询机器人（不处理空值）
     */
    @Transactional(readOnly = true)
    public RobotDTO findByCode(String robotCode) {
        log.info("根据编码查询机器人，编码：{}", robotCode);
        Robot robot = robotRepository.findByRobotCodeAndIsDeletedFalse(robotCode).orElse(null);
        return robot != null ? RobotDTO.fromEntity(robot) : null;
    }
    
    /**
     * 搜索机器人
     */
    @Transactional(readOnly = true)
    public List<RobotDTO> search(String keyword) {
        log.info("搜索机器人，关键字：{}", keyword);
        return robotRepository.searchRobots(keyword).stream()
                .map(RobotDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建机器人
     */
    public RobotDTO create(RobotDTO dto) {
        log.info("创建机器人，编码：{}, 名称：{}", dto.getRobotCode(), dto.getRobotName());
        
        // 校验编码是否为空
        if (dto.getRobotCode() == null || dto.getRobotCode().trim().isEmpty()) {
            throw new RuntimeException("机器人编码不能为空");
        }
        
        // 校验名称是否为空
        if (dto.getRobotName() == null || dto.getRobotName().trim().isEmpty()) {
            throw new RuntimeException("机器人名称不能为空");
        }
        
        // 检查编码是否已存在
        if (robotRepository.existsByRobotCode(dto.getRobotCode())) {
            throw new RuntimeException("机器人编码已存在：" + dto.getRobotCode());
        }
        
        // 校验 IP 地址格式
        String ipError = IpValidator.validate(dto.getIpAddress());
        if (ipError != null) {
            throw new RuntimeException(ipError);
        }
        
        Robot robot = dto.toEntity();
        robot.setStatus(0); // 默认离线（0 = OFFLINE）
        robot.setIsDeleted(0);
        
        Robot saved = robotRepository.save(robot);
        log.info("机器人创建成功，ID: {}", saved.getId());
        
        return RobotDTO.fromEntity(saved);
    }
    
    /**
     * 更新机器人
     */
    @Transactional
    public RobotDTO update(Long id, RobotDTO dto) {
        log.info("更新机器人信息，ID: {}", id);
        
        Robot existing = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在"));
        
        if (dto.getRobotName() != null) {
            existing.setRobotName(dto.getRobotName());
        }
        if (dto.getIpAddress() != null) {
            existing.setIpAddress(dto.getIpAddress());
        }
        if (dto.getDepartmentName() != null) {
            existing.setDepartmentName(dto.getDepartmentName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        
        return RobotDTO.fromEntity(robotRepository.save(existing));
    }
    
    /**
     * 删除机器人（逻辑删除）
     */
    public void delete(Long id) {
        log.info("删除机器人，ID: {}", id);
        
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在，ID: " + id));
        
        // 检查是否已被删除
        if (robot.getIsDeleted() != null && robot.getIsDeleted() == 1) {
            log.warn("机器人已被删除，ID: {}", id);
            throw new RuntimeException("机器人已被删除");
        }
        
        // 检查机器人是否正在执行任务（状态为 2 = BUSY）
        if (robot.getStatus() != null && robot.getStatus() == 2) {
            log.warn("机器人正在执行任务，无法删除，ID: {}", id);
            throw new RuntimeException("机器人正在执行任务，无法删除");
        }
        
        robot.setIsDeleted(1);
        robot.setStatus(0); // 删除时设置为离线（0 = OFFLINE）
        robotRepository.save(robot);
        
        log.info("机器人逻辑删除成功，ID: {}", id);
    }
    
    /**
     * 获取机器人状态统计
     */
    @Transactional(readOnly = true)
    public RobotStatusDTO getStatus() {
        log.info("获取机器人状态统计");
        
        // 打印所有机器人的状态，用于调试
        log.info("========== 所有机器人状态详情 ==========");
        robotRepository.findAll().forEach(robot -> {
            log.info("机器人: {}, 状态: {}, isDeleted: {}", 
                    robot.getRobotCode(), robot.getStatus(), robot.getIsDeleted());
        });
        log.info("========== 机器人状态详情结束 ==========");
        
        Long online = robotRepository.countOnlineRobots();
        Long offline = robotRepository.countOfflineRobots();
        Long busy = robotRepository.countBusyRobots();
        Long fault = robotRepository.countFaultRobots();
        Long total = online + offline + busy + fault;
        
        log.info("机器人状态统计 - 在线: {}, 离线: {}, 忙碌: {}, 故障: {}, 总计: {}", 
                 online, offline, busy, fault, total);
        
        return new RobotStatusDTO(online, offline, busy, fault, total);
    }
    
    /**
     * 获取可用机器人列表（在线或忙碌）
     */
    @Transactional(readOnly = true)
    public List<RobotDTO> getAvailableRobots() {
        log.info("获取可用机器人列表");
        return robotRepository.findAvailableRobots().stream()
                .map(RobotDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新机器人状态
     */
    public void updateStatus(Long id, String status) {
        updateStatus(id, status, false);
    }

    /**
     * 更新机器人状态
     * @param id 机器人ID
     * @param status 新状态
     * @param force 是否强制更新（任务完成时需要强制切换状态）
     */
    public void updateStatus(Long id, String status, boolean force) {
        log.info("更新机器人状态，ID: {}, 状态：{}, force: {}", id, status, force);
        
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在，ID: " + id));
        
        // 检查是否已被删除
        if (robot.getIsDeleted() != null && robot.getIsDeleted() == 1) {
            log.warn("机器人已被删除，无法更新状态，ID: {}", id);
            throw new RuntimeException("机器人已被删除");
        }
        
        // 校验状态合法性
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("状态不能为空");
        }
        
        // 将字符串状态转换为数字状态
        Integer statusInt = convertStatusToInteger(status);
        if (statusInt == null) {
            throw new RuntimeException("非法的状态值：" + status + "，只能是 ONLINE/OFFLINE/BUSY/FAULT 之一");
        }
        
        // 检查状态切换的合法性（除非是强制更新）
        Integer oldStatus = robot.getStatus();
        if (!force && oldStatus != null && oldStatus == 2 && statusInt != 2) { // 2 = BUSY
            log.warn("机器人正在执行任务，无法切换状态，ID: {}, 当前状态：{}", id, oldStatus);
            throw new RuntimeException("机器人正在执行任务，无法切换状态");
        }
        
        // 更新状态
        robot.setStatus(statusInt);
        robot.setUpdateTime(LocalDateTime.now());
        robotRepository.save(robot);
        
        log.info("机器人状态更新成功，ID: {}, 原状态：{}, 新状态：{}", id, oldStatus != null ? oldStatus : "null", status);
    }
    
    /**
     * 根据机器人编码更新状态
     */
    @Transactional
    public void updateStatusByCode(String robotCode, String status) {
        log.info("根据编码更新机器人状态，编码：{}, 状态：{}", robotCode, status);
        
        Robot robot = robotRepository.findByRobotCodeAndIsDeletedFalse(robotCode).orElse(null);
        
        // 如果机器人不存在，记录警告但不抛异常（机器人可能还未在系统中注册）
        if (robot == null) {
            log.warn("机器人不存在，跳过状态更新，编码：{}", robotCode);
            return;
        }
        
        // 检查是否已被删除
        if (robot.getIsDeleted() != null && robot.getIsDeleted() == 1) {
            log.warn("机器人已被删除，无法更新状态，编码：{}", robotCode);
            throw new RuntimeException("机器人已被删除");
        }
        
        // 校验状态合法性
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("状态不能为空");
        }
        
        // 将字符串状态转换为数字状态
        Integer statusInt = convertStatusToInteger(status);
        if (statusInt == null) {
            throw new RuntimeException("非法的状态值：" + status + "，只能是 ONLINE/OFFLINE/BUSY/FAULT 之一");
        }
        
        // 检查状态切换的合法性
        Integer oldStatus = robot.getStatus();
        if (oldStatus == 2 && statusInt != 2) { // 2 = BUSY
            log.warn("机器人正在执行任务，无法切换状态，编码：{}, 当前状态：{}", robotCode, oldStatus);
            // 不抛出异常，直接返回
            return;
        }
        
        // 更新状态
        robot.setStatus(statusInt);
        robot.setUpdateTime(LocalDateTime.now());
        robotRepository.save(robot);
        
        log.info("机器人状态更新成功，编码：{}, 原状态：{}, 新状态：{}", robotCode, oldStatus, status);
    }

    /**
     * 根据编码更新机器人状态和端口
     */
    @Transactional
    public void updateStatusAndPortByCode(String robotCode, String status, Integer port) {
        log.info("根据编码更新机器人状态和端口，编码：{}, 状态：{}, 端口：{}", robotCode, status, port);

        Robot robot = robotRepository.findByRobotCodeAndIsDeletedFalse(robotCode).orElse(null);
        if (robot == null) {
            log.warn("机器人不存在，跳过状态更新，编码：{}", robotCode);
            return;
        }
        if (robot.getIsDeleted() != null && robot.getIsDeleted() == 1) {
            log.warn("机器人已被删除，无法更新状态，编码：{}", robotCode);
            return;
        }
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("状态不能为空");
        }

        Integer statusInt = convertStatusToInteger(status);
        if (statusInt == null) {
            throw new RuntimeException("非法的状态值：" + status + "，只能是 ONLINE/OFFLINE/BUSY/FAULT 之一");
        }

        Integer oldStatus = robot.getStatus();
        if (oldStatus == 2 && statusInt != 2) {
            log.warn("机器人正在执行任务，无法切换状态，编码：{}, 当前状态：{}", robotCode, oldStatus);
            return;
        }

        robot.setStatus(statusInt);
        if (port != null && port > 0) {
            robot.setPort(port);
        }
        robot.setUpdateTime(LocalDateTime.now());
        robotRepository.save(robot);

        log.info("机器人状态和端口更新成功，编码：{}, 原状态：{}, 新状态：{}, 端口：{}", robotCode, oldStatus, status, port);
    }
    
    /**
     * 将字符串状态转换为数字
     */
    private Integer convertStatusToInteger(String status) {
        if (status == null) return null;
        switch (status) {
            case "ONLINE": return 1;
            case "BUSY": return 2;
            case "OFFLINE": return 3;
            case "FAULT": return 4;
            default: return null;
        }
    }
    
    /**
     * 更新心跳
     */
    @Transactional
    public void updateHeartbeat(Long id, String status, Long currentTaskId) {
        log.debug("更新机器人心跳，ID: {}, status: {}, currentTaskId: {}", id, status, currentTaskId);
        
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在"));
        
        if (status != null) {
            robot.setStatus(convertStatusToInteger(status));
        }
        robot.setLastHeartbeat(LocalDateTime.now());
        
        if (currentTaskId != null) {
            log.debug("机器人正在执行任务，ID: {}", currentTaskId);
        }
        
        robotRepository.save(robot);
    }
    
    /**
     * 获取机器人监控信息
     */
    @Transactional(readOnly = true)
    public RobotMonitorDTO getMonitorInfo(Long id) {
        log.info("获取机器人监控信息，ID: {}", id);
        
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在，ID: " + id));
        
        RobotMonitorDTO monitor = new RobotMonitorDTO();
        // 将数字状态转换为字符串状态
        monitor.setStatus(convertStatusToString(robot.getStatus()));
        monitor.setId(robot.getId());
        monitor.setRobotCode(robot.getRobotCode());
        monitor.setRobotName(robot.getRobotName());
        monitor.setLastHeartbeat(robot.getLastHeartbeat());
        
        // 模拟监控数据（实际应该从机器人客户端采集）
        // 这里根据状态生成模拟数据
        if (robot.getStatus() != null && (robot.getStatus() == 1 || robot.getStatus() == 2)) { // 1=ONLINE, 2=BUSY
            monitor.setIsOnline(true);
            monitor.setCpuUsage(robot.getStatus() == 2 ? 65.5 : 15.2);
            monitor.setMemoryUsage(45.8);
            monitor.setDiskUsage(62.3);
            monitor.setNetworkUpload(robot.getStatus() == 2 ? 128.5 : 0.0);
            monitor.setNetworkDownload(robot.getStatus() == 2 ? 256.8 : 0.0);
            monitor.setThreadCount(robot.getStatus() == 2 ? 8 : 2);
            
            // 计算运行时长（从最后心跳时间开始）
            if (robot.getLastHeartbeat() != null) {
                Duration duration = Duration.between(robot.getLastHeartbeat(), LocalDateTime.now());
                monitor.setRunningDuration(duration.toMinutes());
            } else {
                monitor.setRunningDuration(0L);
            }
            
            // 判断健康状态
            monitor.setHealthStatus("HEALTHY");
            monitor.setAlarmMessage(null);
        } else if (robot.getStatus() != null && robot.getStatus() == 4) { // 4=FAULT
            monitor.setIsOnline(false);
            monitor.setCpuUsage(0.0);
            monitor.setMemoryUsage(0.0);
            monitor.setDiskUsage(0.0);
            monitor.setNetworkUpload(0.0);
            monitor.setNetworkDownload(0.0);
            monitor.setThreadCount(0);
            monitor.setRunningDuration(0L);
            monitor.setHealthStatus("CRITICAL");
            monitor.setAlarmMessage("机器人发生故障，需要人工干预");
        } else {
            // OFFLINE (3)
            monitor.setIsOnline(false);
            monitor.setCpuUsage(0.0);
            monitor.setMemoryUsage(0.0);
            monitor.setDiskUsage(0.0);
            monitor.setNetworkUpload(0.0);
            monitor.setNetworkDownload(0.0);
            monitor.setThreadCount(0);
            monitor.setRunningDuration(0L);
            monitor.setHealthStatus("WARNING");
            monitor.setAlarmMessage("机器人离线，无法执行任务");
        }
        
        // 模拟当前任务信息
        if (robot.getStatus() != null && robot.getStatus() == 2) { // 2=BUSY
            monitor.setCurrentTaskId(1L);
            monitor.setCurrentTaskName("发票采集任务-" + robot.getRobotCode());
        }
        
        return monitor;
    }
    
    /**
     * 将数字状态转换为字符串
     */
    private String convertStatusToString(Integer status) {
        if (status == null) return "OFFLINE";
        switch (status) {
            case 1: return "ONLINE";
            case 2: return "BUSY";
            case 3: return "OFFLINE";
            case 4: return "FAULT";
            default: return "OFFLINE";
        }
    }
    
    /**
     * 获取所有机器人监控信息列表
     */
    @Transactional(readOnly = true)
    public List<RobotMonitorDTO> getAllMonitorInfo() {
        log.info("获取所有机器人监控信息");
        
        List<Robot> robots = robotRepository.findAll();
        return robots.stream()
                .map(robot -> {
                    RobotMonitorDTO monitor = new RobotMonitorDTO();
                    monitor.setId(robot.getId());
                    monitor.setRobotCode(robot.getRobotCode());
                    monitor.setRobotName(robot.getRobotName());
                    // 将数字状态转换为字符串状态
                    monitor.setStatus(convertStatusToString(robot.getStatus()));
                    monitor.setLastHeartbeat(robot.getLastHeartbeat());
                    
                    // 模拟监控数据
                    if (robot.getStatus() != null && (robot.getStatus() == 1 || robot.getStatus() == 2)) {
                        monitor.setIsOnline(true);
                        monitor.setCpuUsage(robot.getStatus() == 2 ? 65.5 : 15.2);
                        monitor.setMemoryUsage(45.8);
                        monitor.setDiskUsage(62.3);
                        monitor.setNetworkUpload(robot.getStatus() == 2 ? 128.5 : 0.0);
                        monitor.setNetworkDownload(robot.getStatus() == 2 ? 256.8 : 0.0);
                        monitor.setThreadCount(robot.getStatus() == 2 ? 8 : 2);
                        
                        if (robot.getLastHeartbeat() != null) {
                            Duration duration = Duration.between(robot.getLastHeartbeat(), LocalDateTime.now());
                            monitor.setRunningDuration(duration.toMinutes());
                        } else {
                            monitor.setRunningDuration(0L);
                        }
                        
                        monitor.setHealthStatus("HEALTHY");
                        monitor.setAlarmMessage(null);
                    } else if (robot.getStatus() != null && robot.getStatus() == 4) {
                        monitor.setIsOnline(false);
                        monitor.setHealthStatus("CRITICAL");
                        monitor.setAlarmMessage("机器人发生故障");
                    } else {
                        monitor.setIsOnline(false);
                        monitor.setHealthStatus("WARNING");
                        monitor.setAlarmMessage("机器人离线");
                    }
                    
                    if (robot.getStatus() != null && robot.getStatus() == 2) {
                        monitor.setCurrentTaskId(1L);
                        monitor.setCurrentTaskName("执行中的任务");
                    }
                    
                    return monitor;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
