package com.rpa.process.controller;

import com.rpa.auth.dto.ApiResponse;
import com.rpa.process.dto.CreateProcessRequest;
import com.rpa.process.dto.ProcessDefDTO;
import com.rpa.process.dto.UpdateProcessRequest;
import com.rpa.process.model.ProcessDef;
import com.rpa.process.service.ProcessDefService;
import com.rpa.process.util.GroovySyntaxValidator;
import com.rpa.process.util.GroovySyntaxValidator.ValidationResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class ProcessController {
    
    private static final Logger log = LoggerFactory.getLogger(ProcessController.class);
    
    @Autowired
    private ProcessDefService processDefService;
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProcesses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("=== 查询流程列表 ===");
        log.info("关键字：{}, 状态：{}, 页码：{}, 每页条数：{}", keyword, status, page, pageSize);
        
        try {
            Page<ProcessDef> processPage = processDefService.getProcesses(keyword, status, page, pageSize);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", processPage.getContent().stream()
                    .map(this::convertToDTO)
                    .toList());
            result.put("total", processPage.getTotalElements());
            
            log.info("查询成功，共 {} 条记录", processPage.getTotalElements());
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            log.error("查询流程列表失败", e);
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessDefDTO>> getById(@PathVariable Long id) {
        log.info("=== 获取流程详情，ID: {}", id);
        try {
            ProcessDef process = processDefService.getById(id);
            log.info("获取流程详情成功：{}", process.getProcessName());
            return ResponseEntity.ok(ApiResponse.ok(convertToDTO(process)));
        } catch (Exception e) {
            log.error("获取流程详情失败，ID: {}", id, e);
            throw e;
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ProcessDefDTO>> create(@Valid @RequestBody CreateProcessRequest request) {
        log.info("=== 创建流程 ===");
        log.info("流程编码：{}, 流程名称：{}", request.getProcessCode(), request.getProcessName());
        try {
            ProcessDef process = new ProcessDef();
            process.setProcessCode(request.getProcessCode());
            process.setProcessName(request.getProcessName());
            process.setCategory(request.getCategory());
            process.setDescription(request.getDescription());
            process.setSteps(request.getSteps());
            process.setStatus(request.getStatus());
            process.setCollectScript(request.getCollectScript());
            process.setParseScript(request.getParseScript());
            process.setProcessScript(request.getProcessScript());
            process.setSaveScript(request.getSaveScript());
            
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                try {
                    Long userId = Long.parseLong(username);
                    process.setCreateUser(userId);
                } catch (NumberFormatException e) {
                    log.warn("用户 ID 转换失败，使用默认值：1", e);
                    process.setCreateUser(1L);
                }
            } else {
                process.setCreateUser(1L);
            }
            
            ProcessDef created = processDefService.create(process);
            log.info("流程创建成功，ID: {}", created.getId());
            return ResponseEntity.ok(ApiResponse.ok(convertToDTO(created)));
        } catch (Exception e) {
            log.error("创建流程失败", e);
            throw e;
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessDefDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProcessRequest request) {
        
        log.info("=== 更新流程，ID: {}", id);
        log.info("接收到的完整 request: {}", request);
        log.info("流程编码：{}", request.getProcessCode());
        log.info("流程名称：{}", request.getProcessName());
        log.info("分类：{}", request.getCategory());
        log.info("版本：{}, 状态：{}", request.getVersion(), request.getStatus());
        try {
            ProcessDef process = new ProcessDef();
            process.setProcessCode(request.getProcessCode());
            process.setProcessName(request.getProcessName());
            process.setCategory(request.getCategory());
            process.setVersion(request.getVersion());
            process.setDescription(request.getDescription());
            process.setSteps(request.getSteps());
            process.setStatus(request.getStatus());
            process.setCollectScript(request.getCollectScript());
            process.setParseScript(request.getParseScript());
            process.setProcessScript(request.getProcessScript());
            process.setSaveScript(request.getSaveScript());
            
            log.info("准备更新流程，分类字段值：{}, 版本：{}", process.getCategory(), process.getVersion());
            
            ProcessDef updated = processDefService.update(id, process);
            log.info("流程更新成功，ID: {}, 新分类：{}", updated.getId(), updated.getCategory());
            return ResponseEntity.ok(ApiResponse.ok(convertToDTO(updated)));
        } catch (Exception e) {
            log.error("更新流程失败，ID: {}", id, e);
            throw e;
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("=== 删除流程，ID: {}", id);
        try {
            processDefService.delete(id);
            log.info("流程删除成功，ID: {}", id);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (Exception e) {
            log.error("流程删除失败，ID: {}", id, e);
            throw e;
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProcessDefDTO>> toggleStatus(@PathVariable Long id) {
        log.info("=== 切换流程状态，ID: {}", id);
        try {
            processDefService.toggleStatus(id);
            ProcessDef process = processDefService.getById(id);
            log.info("流程状态切换成功，ID: {}, 新状态：{}", id, process.getStatus());
            return ResponseEntity.ok(ApiResponse.ok(convertToDTO(process)));
        } catch (Exception e) {
            log.error("流程状态切换失败，ID: {}", id, e);
            throw e;
        }
    }
    
    @GetMapping("/{id}/scripts")
    public ResponseEntity<ApiResponse<Map<String, String>>> getScripts(@PathVariable Long id) {
        log.info("=== 获取流程脚本，ID: {}", id);
        try {
            ProcessDef process = processDefService.getById(id);
            Map<String, String> scripts = new HashMap<>();
            scripts.put("collectScript", process.getCollectScript() != null ? process.getCollectScript() : "");
            scripts.put("parseScript", process.getParseScript() != null ? process.getParseScript() : "");
            scripts.put("processScript", process.getProcessScript() != null ? process.getProcessScript() : "");
            scripts.put("saveScript", process.getSaveScript() != null ? process.getSaveScript() : "");
            log.info("获取流程脚本成功，ID: {}", id);
            return ResponseEntity.ok(ApiResponse.ok(scripts));
        } catch (Exception e) {
            log.error("获取流程脚本失败，ID: {}", id, e);
            throw e;
        }
    }
    
    @PutMapping("/{id}/scripts")
    public ResponseEntity<ApiResponse<ProcessDefDTO>> updateScripts(
            @PathVariable Long id,
            @RequestBody Map<String, String> scripts) {
        
        log.info("=== 更新流程脚本，ID: {}", id);
        try {
            ProcessDef process = processDefService.getById(id);
            process.setCollectScript(scripts.get("collectScript"));
            process.setParseScript(scripts.get("parseScript"));
            process.setProcessScript(scripts.get("processScript"));
            process.setSaveScript(scripts.get("saveScript"));
            
            ProcessDef updated = processDefService.update(id, process);
            log.info("流程脚本更新成功，ID: {}", updated.getId());
            return ResponseEntity.ok(ApiResponse.ok(convertToDTO(updated)));
        } catch (Exception e) {
            log.error("更新流程脚本失败，ID: {}", id, e);
            throw e;
        }
    }
    
    /**
     * 校验 Groovy 脚本语法
     */
    @PostMapping("/validate/groovy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateGroovy(@RequestBody Map<String, String> request) {
        log.info("=== 校验 Groovy 脚本语法 ===");
        
        String script = request.get("script");
        String stepName = request.getOrDefault("stepName", "未知环节");
        
        if (script == null || script.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "脚本内容为空");
            result.put("errors", List.of("脚本内容为空"));
            result.put("stepName", stepName);
            
            return ResponseEntity.ok(ApiResponse.ok(result));
        }
        
        try {
            // 使用 Groovy 编译器进行语法校验
            ValidationResult validationResult = GroovySyntaxValidator.validate(script);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", validationResult.isValid());
            result.put("message", validationResult.getMessage());
            result.put("errors", validationResult.getErrors());
            result.put("stepName", stepName);
            
            if (validationResult.isValid()) {
                log.info("Groovy 脚本语法校验通过，环节：{}", stepName);
                return ResponseEntity.ok(ApiResponse.ok(result));
            } else {
                log.warn("Groovy 脚本语法校验失败，环节：{}, 错误：{}", stepName, validationResult.getErrors());
                // 返回错误信息，但使用 ok 包装
                return ResponseEntity.ok(ApiResponse.ok(result));
            }
        } catch (Exception e) {
            log.error("Groovy 脚本语法校验异常，环节：{}", stepName, e);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "校验异常：" + e.getMessage());
            result.put("errors", List.of(e.getMessage()));
            result.put("stepName", stepName);
            
            return ResponseEntity.ok(ApiResponse.ok(result));
        }
    }
    
    /**
     * 校验流程所有环节脚本
     */
    @PostMapping("/{id}/validate/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateAllSteps(@PathVariable Long id) {
        log.info("=== 校验流程所有环节脚本，流程 ID: {}", id);
        
        try {
            ProcessDef process = processDefService.getById(id);
            
            Map<String, Object> overallResult = new HashMap<>();
            Map<String, Object> stepResults = new HashMap<>();
            boolean allValid = true;
            StringBuilder errorMessage = new StringBuilder();
            
            // 校验采集环节
            if (process.getCollectScript() != null && !process.getCollectScript().trim().isEmpty()) {
                ValidationResult result = GroovySyntaxValidator.validate(process.getCollectScript());
                stepResults.put("collect", createStepResult("采集环节", result));
                if (!result.isValid()) {
                    allValid = false;
                    errorMessage.append("采集环节：").append(result.getErrors().get(0)).append("; ");
                }
            }
            
            // 校验解析环节
            if (process.getParseScript() != null && !process.getParseScript().trim().isEmpty()) {
                ValidationResult result = GroovySyntaxValidator.validate(process.getParseScript());
                stepResults.put("parse", createStepResult("解析环节", result));
                if (!result.isValid()) {
                    allValid = false;
                    errorMessage.append("解析环节：").append(result.getErrors().get(0)).append("; ");
                }
            }
            
            // 校验加工环节
            if (process.getProcessScript() != null && !process.getProcessScript().trim().isEmpty()) {
                ValidationResult result = GroovySyntaxValidator.validate(process.getProcessScript());
                stepResults.put("process", createStepResult("加工环节", result));
                if (!result.isValid()) {
                    allValid = false;
                    errorMessage.append("加工环节：").append(result.getErrors().get(0)).append("; ");
                }
            }
            
            // 校验落库环节
            if (process.getSaveScript() != null && !process.getSaveScript().trim().isEmpty()) {
                ValidationResult result = GroovySyntaxValidator.validate(process.getSaveScript());
                stepResults.put("save", createStepResult("落库环节", result));
                if (!result.isValid()) {
                    allValid = false;
                    errorMessage.append("落库环节：").append(result.getErrors().get(0)).append("; ");
                }
            }
            
            overallResult.put("allValid", allValid);
            overallResult.put("stepResults", stepResults);
            overallResult.put("message", allValid ? "所有环节语法校验通过" : errorMessage.toString());
            
            if (allValid) {
                log.info("流程所有环节语法校验通过，ID: {}", id);
                return ResponseEntity.ok(ApiResponse.ok(overallResult));
            } else {
                log.warn("流程语法校验失败，ID: {}, 错误：{}", id, errorMessage.toString());
                // 返回错误信息，但使用 ok 包装
                return ResponseEntity.ok(ApiResponse.ok(overallResult));
            }
        } catch (Exception e) {
            log.error("流程语法校验异常，ID: {}", id, e);
            Map<String, Object> result = new HashMap<>();
            result.put("allValid", false);
            result.put("message", "校验异常：" + e.getMessage());
            
            return ResponseEntity.ok(ApiResponse.ok(result));
        }
    }
    
    /**
     * 创建环节校验结果
     */
    private Map<String, Object> createStepResult(String stepName, ValidationResult result) {
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepName", stepName);
        stepResult.put("valid", result.isValid());
        stepResult.put("message", result.getMessage());
        stepResult.put("errors", result.getErrors());
        return stepResult;
    }
    
    private ProcessDefDTO convertToDTO(ProcessDef process) {
        return new ProcessDefDTO(
                process.getId(),
                process.getProcessCode(),
                process.getProcessName(),
                process.getCategory(),
                process.getVersion() != null ? process.getVersion() : "1.0.0",
                process.getDescription(),
                process.getSteps(),
                process.getStatus(),
                process.getRemark(),
                process.getCollectScript(),
                process.getParseScript(),
                process.getProcessScript(),
                process.getSaveScript(),
                process.getCreateUser(),
                process.getCreateTime(),
                process.getUpdateTime()
        );
    }
}
