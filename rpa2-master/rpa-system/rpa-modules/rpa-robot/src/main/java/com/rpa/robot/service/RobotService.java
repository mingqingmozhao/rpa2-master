package com.rpa.robot.service;

import com.rpa.robot.dto.RobotDTO;
import com.rpa.robot.dto.RobotStatusDTO;
import com.rpa.robot.model.Robot;
import com.rpa.robot.repository.RobotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RobotService {
    
    private static final Logger log = LoggerFactory.getLogger(RobotService.class);
    
    @Autowired
    private RobotRepository robotRepository;
    
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
     * 根据 ID 查询机器人
     */
    @Transactional(readOnly = true)
    public RobotDTO getById(Long id) {
        log.info("根据 ID 查询机器人，ID: {}", id);
        return robotRepository.findById(id)
                .map(RobotDTO::fromEntity)
                .orElse(null);
    }
    
    /**
     * 根据编码查询机器人
     */
    @Transactional(readOnly = true)
    public RobotDTO getByCode(String robotCode) {
        log.info("根据编码查询机器人，编码：{}", robotCode);
        return robotRepository.findByRobotCode(robotCode)
                .map(RobotDTO::fromEntity)
                .orElse(null);
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
        
        // 检查编码是否已存在
        if (robotRepository.existsByRobotCode(dto.getRobotCode())) {
            throw new RuntimeException("机器人编码已存在：" + dto.getRobotCode());
        }
        
        Robot robot = dto.toEntity();
        robot.setStatus("OFFLINE"); // 默认离线
        robot.setIsDeleted(0);
        
        Robot saved = robotRepository.save(robot);
        log.info("机器人创建成功，ID: {}", saved.getId());
        
        return RobotDTO.fromEntity(saved);
    }
    
    /**
     * 更新机器人
     */
    public RobotDTO update(Long id, RobotDTO dto) {
        log.info("更新机器人，ID: {}", id);
        
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在，ID: " + id));
        
        // 检查编码是否被其他机器人使用
        if (!robot.getRobotCode().equals(dto.getRobotCode()) && 
            robotRepository.existsByRobotCode(dto.getRobotCode())) {
            throw new RuntimeException("机器人编码已存在：" + dto.getRobotCode());
        }
        
        robot.setRobotCode(dto.getRobotCode());
        robot.setRobotName(dto.getRobotName());
        robot.setType(dto.getType());
        robot.setStatus(dto.getStatus());
        robot.setIpAddress(dto.getIpAddress());
        robot.setDepartment(dto.getDepartment());
        robot.setOwnerId(dto.getOwnerId());
        robot.setOwnerName(dto.getOwnerName());
        robot.setDescription(dto.getDescription());
        robot.setRemark(dto.getRemark());
        
        Robot updated = robotRepository.save(robot);
        log.info("机器人更新成功，ID: {}", updated.getId());
        
        return RobotDTO.fromEntity(updated);
    }
    
    /**
     * 删除机器人（逻辑删除）
     */
    public void delete(Long id) {
        log.info("删除机器人，ID: {}", id);
        
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在，ID: " + id));
        
        robot.setIsDeleted(1);
        robotRepository.save(robot);
        
        log.info("机器人逻辑删除成功，ID: {}", id);
    }
    
    /**
     * 获取机器人状态统计
     */
    @Transactional(readOnly = true)
    public RobotStatusDTO getStatus() {
        log.info("获取机器人状态统计");
        
        Long online = robotRepository.countOnlineRobots();
        Long offline = robotRepository.countOfflineRobots();
        Long busy = robotRepository.countBusyRobots();
        Long fault = robotRepository.countFaultRobots();
        Long total = online + offline + busy + fault;
        
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
        log.info("更新机器人状态，ID: {}, 状态：{}", id, status);
        
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在，ID: " + id));
        
        robot.setStatus(status);
        robot.setLastHeartbeat(LocalDateTime.now());
        
        robotRepository.save(robot);
        log.info("机器人状态更新成功，ID: {}, 状态：{}", id, status);
    }
    
    /**
     * 更新心跳时间
     */
    public void updateHeartbeat(Long id) {
        log.debug("更新机器人心跳时间，ID: {}", id);
        
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机器人不存在，ID: " + id));
        
        robot.setLastHeartbeat(LocalDateTime.now());
        robotRepository.save(robot);
    }
}
