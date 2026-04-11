package com.rpa.robot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * RPA 机器人实体
 * 用于管理执行 RPA 任务的机器人
 */
@Entity
@Table(name = "robot_info")
public class Robot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 机器人编码（唯一标识）
     */
    @Column(name = "robot_code", nullable = false, unique = true, length = 50)
    private String robotCode;
    
    /**
     * 机器人名称
     */
    @Column(name = "robot_name", nullable = false, length = 100)
    private String robotName;
    
    /**
     * 机器人类型：ATTENDED(有人值守) / UNATTENDED(无人值守)
     */
    @Column(name = "robot_type", nullable = false, length = 20)
    private String type = "UNATTENDED";
    
    /**
     * 状态：1-在线 2-工作中 3-离线 4-故障
     */
    @Column(name = "status", nullable = false)
    private Integer status = 3; // 默认离线
    
    /**
     * 机器人 IP 地址
     */
    @Column(name = "ip", length = 50)
    private String ipAddress;
    
    /**
     * 端口
     */
    @Column(name = "port")
    private Integer port = 8080;
    
    /**
     * 最后心跳时间
     */
    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;
    
    /**
     * 所属部门 ID
     */
    @Column(name = "department_id")
    private Long departmentId;
    
    /**
     * 所属部门名称
     */
    @Transient
    private String department;
    
    /**
     * 负责人 ID
     */
    @Column(name = "owner_id")
    private Long ownerId;
    
    /**
     * 负责人姓名
     */
    @Column(name = "owner_name", length = 50)
    private String ownerName;
    
    /**
     * 描述
     */
    @Column(name = "description", length = 255)
    private String description;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 255)
    private String remark;
    
    /**
     * 创建人
     */
    @Column(name = "create_user")
    private Long createUser;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted = 0;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRobotCode() { return robotCode; }
    public void setRobotCode(String robotCode) { this.robotCode = robotCode; }
    
    public String getRobotName() { return robotName; }
    public void setRobotName(String robotName) { this.robotName = robotName; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public Long getCreateUser() { return createUser; }
    public void setCreateUser(Long createUser) { this.createUser = createUser; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
