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
    private LocalDateTime lastHeartbeat;
    private String department;
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
        dto.setStatus(robot.getStatus());
        dto.setIpAddress(robot.getIpAddress());
        dto.setLastHeartbeat(robot.getLastHeartbeat());
        dto.setDepartment(robot.getDepartment());
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
        robot.setStatus(this.status);
        robot.setIpAddress(this.ipAddress);
        robot.setLastHeartbeat(this.lastHeartbeat);
        robot.setDepartment(this.department);
        robot.setOwnerId(this.ownerId);
        robot.setOwnerName(this.ownerName);
        robot.setDescription(this.description);
        robot.setRemark(this.remark);
        return robot;
    }
}
