package com.rpa.robot.controller;

import com.rpa.auth.dto.ApiResponse;
import com.rpa.robot.dto.RobotDTO;
import com.rpa.robot.dto.RobotStatusDTO;
import com.rpa.robot.service.RobotService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器人管理控制器
 */
@RestController
@RequestMapping("/robot")
public class RobotController {
    
    private static final Logger log = LoggerFactory.getLogger(RobotController.class);
    
    @Autowired
    private RobotService robotService;
    
    /**
     * 分页查询机器人列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRobotList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        
        log.info("查询机器人列表，page: {}, pageSize: {}, keyword: {}", page, pageSize, keyword);
        
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<RobotDTO> robotPage;
        if (keyword != null && !keyword.isEmpty()) {
            // 搜索
            List<RobotDTO> robots = robotService.search(keyword);
            // 手动分页
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, robots.size());
            List<RobotDTO> subList = robots.subList(start, end);
            robotPage = new org.springframework.data.domain.PageImpl<>(subList, pageable, robots.size());
        } else {
            robotPage = robotService.findAll(pageable);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", robotPage.getContent());
        result.put("total", robotPage.getTotalElements());
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    
    /**
     * 根据 ID 查询机器人详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RobotDTO>> getRobotById(@PathVariable Long id) {
        log.info("查询机器人详情，ID: {}", id);
        
        RobotDTO robot = robotService.getById(id);
        if (robot == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "机器人不存在"));
        }
        
        return ResponseEntity.ok(ApiResponse.ok(robot));
    }
    
    /**
     * 创建机器人
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RobotDTO>> createRobot(@Valid @RequestBody RobotDTO dto) {
        log.info("创建机器人，编码：{}, 名称：{}", dto.getRobotCode(), dto.getRobotName());
        
        try {
            RobotDTO created = robotService.create(dto);
            return ResponseEntity.ok(ApiResponse.ok(created));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 更新机器人
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RobotDTO>> updateRobot(
            @PathVariable Long id,
            @Valid @RequestBody RobotDTO dto) {
        log.info("更新机器人，ID: {}", id);
        
        try {
            RobotDTO updated = robotService.update(id, dto);
            return ResponseEntity.ok(ApiResponse.ok(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 删除机器人
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRobot(@PathVariable Long id) {
        log.info("删除机器人，ID: {}", id);
        
        try {
            robotService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 获取机器人状态统计
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<RobotStatusDTO>> getRobotStatus() {
        log.info("获取机器人状态统计");
        
        RobotStatusDTO status = robotService.getStatus();
        return ResponseEntity.ok(ApiResponse.ok(status));
    }
    
    /**
     * 获取可用机器人列表
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RobotDTO>>> getAvailableRobots() {
        log.info("获取可用机器人列表");
        
        List<RobotDTO> robots = robotService.getAvailableRobots();
        return ResponseEntity.ok(ApiResponse.ok(robots));
    }
    
    /**
     * 更新机器人状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateRobotStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        log.info("更新机器人状态，ID: {}, 状态：{}", id, params.get("status"));
        
        try {
            String status = params.get("status");
            robotService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 更新心跳时间
     */
    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<ApiResponse<Void>> updateHeartbeat(@PathVariable Long id) {
        log.debug("更新机器人心跳时间，ID: {}", id);
        
        try {
            robotService.updateHeartbeat(id);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
}
