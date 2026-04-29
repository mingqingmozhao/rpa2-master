package com.rpa.robot.dto;

import com.rpa.robot.model.Robot;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机器人 DTO
 */
@Data
public class RobotDTO {
    
    private Long id;
    private String robotCode;
    private String robotName;
    private String type;
    private String status;
    private String ipAddress;
    private Integer port;
    private LocalDateTime lastHeartbeat;
    private String departmentName;
    private Long ownerId;
    private String ownerName;
    private String description;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 从实体转换为 DTO
     */
    public static RobotDTO fromEntity(Robot robot) {
        if (robot == null) {
            return null;
        }
        
        RobotDTO dto = new RobotDTO();
        dto.setId(robot.getId());
        dto.setRobotCode(robot.getRobotCode());
        dto.setRobotName(robot.getRobotName());
        dto.setType(robot.getType());
        // 将数字状态转换为字符串状态
        dto.setStatus(convertStatusToString(robot.getStatus()));
        dto.setIpAddress(robot.getIpAddress());
        dto.setPort(robot.getPort());
        dto.setLastHeartbeat(robot.getLastHeartbeat());
        dto.setDepartmentName(robot.getDepartmentName());
        dto.setOwnerId(robot.getOwnerId());
        dto.setOwnerName(robot.getOwnerName());
        dto.setDescription(robot.getDescription());
        dto.setRemark(robot.getRemark());
        dto.setCreateTime(robot.getCreateTime());
        dto.setUpdateTime(robot.getUpdateTime());
        
        return dto;
    }
    
    /**
     * 从 DTO 转换为实体
     */
    public Robot toEntity() {
        Robot robot = new Robot();
        robot.setId(this.id);
        robot.setRobotCode(this.robotCode);
        robot.setRobotName(this.robotName);
        robot.setType(this.type);
        // 将字符串状态转换为数字状态
        robot.setStatus(convertStatusToInteger(this.status));
        robot.setIpAddress(this.ipAddress);
        robot.setPort(this.port);
        robot.setLastHeartbeat(this.lastHeartbeat);
        robot.setDepartmentName(this.departmentName);
        robot.setOwnerId(this.ownerId);
        robot.setOwnerName(this.ownerName);
        robot.setDescription(this.description);
        robot.setRemark(this.remark);
        return robot;
    }
    
    /**
     * 将数字状态转换为字符串
     */
    private static String convertStatusToString(Integer status) {
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
     * 将字符串状态转换为数字
     */
    private static Integer convertStatusToInteger(String status) {
        if (status == null) return 3;
        switch (status) {
            case "ONLINE": return 1;
            case "BUSY": return 2;
            case "OFFLINE": return 3;
            case "FAULT": return 4;
            default: return 3;
        }
    }
}
