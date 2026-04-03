package com.rpa.process.service;

import com.rpa.process.model.ProcessDef;
import com.rpa.process.repository.ProcessDefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProcessDefService {
    
    private static final Logger log = LoggerFactory.getLogger(ProcessDefService.class);
    
    @Autowired
    private ProcessDefRepository processDefRepository;
    
    public Page<ProcessDef> getProcesses(String keyword, Integer status, int page, int pageSize) {
        log.debug("查询流程：keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<ProcessDef> result = processDefRepository.searchProcesses(keyword, status, pageable);
        log.debug("查询结果：共 {} 条记录", result.getTotalElements());
        return result;
    }
    
    public ProcessDef getById(Long id) {
        log.debug("获取流程详情，ID: {}", id);
        ProcessDef process = processDefRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("流程不存在"));
        if (process.getIsDeleted() != null && process.getIsDeleted() == 1) {
            log.warn("流程已被删除，ID: {}", id);
            throw new RuntimeException("流程已被删除");
        }
        log.debug("获取流程成功：{}", process.getProcessName());
        return process;
    }
    
    public ProcessDef getByCode(String code) {
        return processDefRepository.findByProcessCode(code)
                .orElseThrow(() -> new RuntimeException("流程不存在"));
    }
    
    public boolean existsByCode(String code) {
        return processDefRepository.existsByProcessCode(code);
    }
    
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return processDefRepository.existsByProcessCodeAndIdNot(code, id);
    }
    
    @Transactional
    public ProcessDef create(ProcessDef process) {
        log.info("创建流程：{}", process.getProcessCode());
        if (existsByCode(process.getProcessCode())) {
            log.error("流程编码已存在：{}", process.getProcessCode());
            throw new RuntimeException("流程编码已存在");
        }
        process.setIsDeleted(0);
        ProcessDef created = processDefRepository.save(process);
        log.info("流程创建成功，ID: {}", created.getId());
        return created;
    }
    
    @Transactional
    public ProcessDef update(Long id, ProcessDef process) {
        log.info("更新流程，ID: {}", id);
        ProcessDef existing = getById(id);
        
        log.info("更新前的数据：processCode={}, processName={}, status={}", 
            existing.getProcessCode(), existing.getProcessName(), existing.getStatus());
        
        if (existsByCodeAndIdNot(process.getProcessCode(), id)) {
            log.error("流程编码已存在：{}", process.getProcessCode());
            throw new RuntimeException("流程编码已存在");
        }
        
        existing.setProcessCode(process.getProcessCode());
        existing.setProcessName(process.getProcessName());
        existing.setDescription(process.getDescription());
        existing.setSteps(process.getSteps());
        existing.setStatus(process.getStatus());
        existing.setCollectScript(process.getCollectScript());
        existing.setParseScript(process.getParseScript());
        existing.setProcessScript(process.getProcessScript());
        existing.setSaveScript(process.getSaveScript());
        
        log.info("准备保存更新后的数据...");
        ProcessDef updated = processDefRepository.save(existing);
        log.info("流程更新成功，ID: {}, 新 status: {}", updated.getId(), updated.getStatus());
        return updated;
    }
    
    @Transactional
    public void delete(Long id) {
        log.info("删除流程，ID: {}", id);
        ProcessDef process = getById(id);
        
        // 检查是否有其他表引用此流程（如果有外键关联，需要先处理）
        // TODO: 添加关联检查逻辑
        
        // 物理删除
        processDefRepository.delete(process);
        log.info("流程物理删除成功，ID: {}", id);
    }
    
    @Transactional
    public void toggleStatus(Long id) {
        log.info("切换流程状态，ID: {}", id);
        ProcessDef process = getById(id);
        Integer oldStatus = process.getStatus();
        process.setStatus(process.getStatus() == 1 ? 0 : 1);
        processDefRepository.save(process);
        log.info("流程状态切换成功，ID: {}, 旧状态：{}, 新状态：{}", id, oldStatus, process.getStatus());
    }
}
