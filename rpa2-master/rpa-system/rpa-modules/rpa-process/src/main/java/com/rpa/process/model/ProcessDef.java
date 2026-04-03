package com.rpa.process.model;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDateTime;

@Entity
@Table(name = "process_def")
@DynamicUpdate
public class ProcessDef {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "process_code", nullable = false, unique = true, length = 50)
    private String processCode;
    
    @Column(name = "process_name", nullable = false, length = 50)
    private String processName;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "version", length = 20)
    private String version;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "steps")
    private Integer steps = 0;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "remark", length = 255)
    private String remark;
    
    @Column(name = "collect_script", columnDefinition = "TEXT")
    private String collectScript;
    
    @Column(name = "parse_script", columnDefinition = "TEXT")
    private String parseScript;
    
    @Column(name = "process_script", columnDefinition = "TEXT")
    private String processScript;
    
    @Column(name = "save_script", columnDefinition = "TEXT")
    private String saveScript;
    
    @Column(name = "create_user")
    private Long createUser;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
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
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProcessCode() {
        return processCode;
    }
    
    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }
    
    public String getProcessName() {
        return processName;
    }
    
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getSteps() {
        return steps;
    }
    
    public void setSteps(Integer steps) {
        this.steps = steps;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getCollectScript() {
        return collectScript;
    }
    
    public void setCollectScript(String collectScript) {
        this.collectScript = collectScript;
    }
    
    public String getParseScript() {
        return parseScript;
    }
    
    public void setParseScript(String parseScript) {
        this.parseScript = parseScript;
    }
    
    public String getProcessScript() {
        return processScript;
    }
    
    public void setProcessScript(String processScript) {
        this.processScript = processScript;
    }
    
    public String getSaveScript() {
        return saveScript;
    }
    
    public void setSaveScript(String saveScript) {
        this.saveScript = saveScript;
    }
    
    public Long getCreateUser() {
        return createUser;
    }
    
    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Integer getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
