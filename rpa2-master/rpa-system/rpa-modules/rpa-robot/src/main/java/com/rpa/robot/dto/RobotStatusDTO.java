package com.rpa.robot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机器人状态统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotStatusDTO {
    
    /**
     * 在线机器人数量
     */
    private Long online;
    
    /**
     * 离线机器人数量
     */
    private Long offline;
    
    /**
     * 忙碌机器人数量
     */
    private Long busy;
    
    /**
     * 故障机器人数量
     */
    private Long fault;
    
    /**
     * 总机器人数量
     */
    private Long total;
}
