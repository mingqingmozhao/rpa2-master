package com.rpa.robot.controller;

import com.rpa.auth.dto.ApiResponse;
import com.rpa.robot.dto.RobotDTO;
import com.rpa.robot.dto.RobotStatusDTO;
import com.rpa.robot.dto.RobotMonitorDTO;
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
    
    public RobotController() {
        log.info("========== RobotController 实例化成功 ==========");
    }
    
    @Autowired
    private RobotService robotService;
    
    /**
     * 分页查询机器人列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRobotList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        log.info("========== 进入 getRobotList 方法 ==========");
        log.info("查询机器人列表，page: {}, pageSize: {}, keyword: {}, status: {}", page, pageSize, keyword, status);
        
        try {
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
            
            log.info("开始查询机器人列表，page: {}, pageSize: {}, keyword: {}, status: {}", page, pageSize, keyword, status);
            
            Page<RobotDTO> robotPage;
            if (keyword != null && !keyword.isEmpty()) {
                // 搜索
                log.info("执行关键字搜索，keyword: {}", keyword);
                List<RobotDTO> robots = robotService.search(keyword);
                // 状态筛选
                if (status != null && !status.isEmpty()) {
                    log.info("执行状态筛选，status: {}", status);
                    robots = robots.stream()
                            .filter(robot -> status.equals(robot.getStatus()))
                            .collect(java.util.stream.Collectors.toList());
                }
                // 手动分页
                int start = (page - 1) * pageSize;
                int end = Math.min(start + pageSize, robots.size());
                List<RobotDTO> subList = robots.subList(start, end);
                robotPage = new org.springframework.data.domain.PageImpl<>(subList, pageable, robots.size());
            } else {
                // 带状态的分页查询
                log.info("执行分页查询，status: {}", status);
                robotPage = robotService.findAllByStatus(status, pageable);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", robotPage.getContent());
            result.put("total", robotPage.getTotalElements());
            result.put("page", page);
            result.put("pageSize", pageSize);
            
            log.info("查询成功，返回 {} 条记录", robotPage.getContent().size());
            
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            log.error("查询机器人列表失败，page: {}, pageSize: {}, keyword: {}, status: {}", page, pageSize, keyword, status, e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "查询失败：" + e.getMessage()));
        }
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
    
    /**
     * 获取机器人监控信息
     */
    @GetMapping("/{id}/monitor")
    public ResponseEntity<ApiResponse<RobotMonitorDTO>> getMonitorInfo(@PathVariable Long id) {
        log.info("获取机器人监控信息，ID: {}", id);
        
        try {
            RobotMonitorDTO monitor = robotService.getMonitorInfo(id);
            return ResponseEntity.ok(ApiResponse.ok(monitor));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 获取所有机器人监控信息列表
     */
    @GetMapping("/monitor")
    public ResponseEntity<ApiResponse<List<RobotMonitorDTO>>> getAllMonitorInfo() {
        log.info("获取所有机器人监控信息");
        
        try {
            List<RobotMonitorDTO> monitors = robotService.getAllMonitorInfo();
            return ResponseEntity.ok(ApiResponse.ok(monitors));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 启动机器人（发送启动指令）
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<Void>> startRobot(@PathVariable Long id) {
        log.info("启动机器人，ID: {}", id);
        
        try {
            // TODO: 实现通过 WebSocket 发送启动指令给机器人客户端
            // 目前只是更新状态为在线
            robotService.updateStatus(id, "ONLINE");
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    /**
     * 停止机器人（发送停止指令）
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<Void>> stopRobot(@PathVariable Long id) {
        log.info("停止机器人，ID: {}", id);
        
        try {
            // TODO: 实现通过 WebSocket 发送停止指令给机器人客户端
            // 目前只是更新状态为离线
            robotService.updateStatus(id, "OFFLINE");
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        }
    }
}
