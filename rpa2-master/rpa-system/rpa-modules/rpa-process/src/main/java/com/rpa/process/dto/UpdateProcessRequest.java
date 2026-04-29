package com.rpa.process.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProcessRequest {
    
    @NotBlank(message = "流程编码不能为空")
    @Size(max = 50, message = "流程编码长度不能超过 50")
    private String processCode;
    
    @NotBlank(message = "流程名称不能为空")
    @Size(max = 50, message = "流程名称长度不能超过 50")
    private String processName;
    
    @Size(max = 50, message = "分类长度不能超过 50")
    private String category;
    
    @Size(max = 20, message = "版本长度不能超过 20")
    private String version;
    
    @Size(max = 255, message = "流程描述长度不能超过 255")
    private String description;
    
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
    
    private Integer steps = 0;
    private Integer status = 1;
    private String collectScript;
    private String parseScript;
    private String processScript;
    private String saveScript;
    
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
