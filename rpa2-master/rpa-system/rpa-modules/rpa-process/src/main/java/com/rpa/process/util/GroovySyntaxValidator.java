package com.rpa.process.util;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Groovy 语法校验器
 * 用于验证 Groovy 脚本的语法正确性
 */
public class GroovySyntaxValidator {
    
    /**
     * 校验 Groovy 脚本语法
     * @param script Groovy 脚本内容
     * @return 校验结果
     */
    public static ValidationResult validate(String script) {
        if (script == null || script.trim().isEmpty()) {
            return new ValidationResult(false, "脚本内容为空", new ArrayList<>());
        }
        
        List<String> errors = new ArrayList<>();
        
        try {
            // 创建 GroovyShell 进行语法检查
            GroovyShell shell = new GroovyShell();
            
            // 尝试解析脚本（不执行）
            shell.parse(script);
            
            // 如果没有异常，说明语法正确
            return new ValidationResult(true, "语法检查通过", errors);
            
        } catch (CompilationFailedException e) {
            // 获取编译错误信息
            errors.add(e.getMessage());
            return new ValidationResult(false, "语法检查失败", errors);
            
        } catch (Exception e) {
            // 其他异常
            errors.add(e.getMessage());
            return new ValidationResult(false, "语法检查失败：未知错误", errors);
        }
    }
    
    /**
     * 校验结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private List<String> errors;
        
        public ValidationResult(boolean valid, String message, List<String> errors) {
            this.valid = valid;
            this.message = message;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
}
