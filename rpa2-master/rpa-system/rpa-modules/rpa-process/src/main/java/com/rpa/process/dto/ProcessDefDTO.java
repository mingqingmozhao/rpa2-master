package com.rpa.process.dto;

import java.time.LocalDateTime;

public class ProcessDefDTO {
    
    private Long id;
    private String processCode;
    private String processName;
    private String category;
    private String version;
    private String description;
    private Integer steps;
    private Integer status;
    private String remark;
    private String collectScript;
    private String parseScript;
    private String processScript;
    private String saveScript;
    private Long createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public ProcessDefDTO() {}
    
    public ProcessDefDTO(Long id, String processCode, String processName, String category, String version,
                        String description, Integer steps, Integer status, String remark,
                        String collectScript, String parseScript, String processScript, String saveScript, 
                        Long createUser, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.processCode = processCode;
        this.processName = processName;
        this.category = category;
        this.version = version;
        this.description = description;
        this.steps = steps;
        this.status = status;
        this.remark = remark;
        this.collectScript = collectScript;
        this.parseScript = parseScript;
        this.processScript = processScript;
        this.saveScript = saveScript;
        this.createUser = createUser;
        this.createTime = createTime;
        this.updateTime = updateTime;
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
