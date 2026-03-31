package com.rpa.auth.service;

import com.rpa.auth.advice.GlobalExceptionHandler.BusinessException;
import com.rpa.auth.dto.CreatePermissionRequest;
import com.rpa.auth.dto.PermissionListResponse;
import com.rpa.auth.dto.PermissionTreeResponse;
import com.rpa.auth.dto.UpdatePermissionRequest;
import com.rpa.auth.model.Permission;
import com.rpa.auth.repository.PermissionRepository;
import com.rpa.auth.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理服务。
 * 支持菜单级权限（perm_type=1）和按钮级权限（perm_type=2）的 CRUD。
 */
@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    // ==================== 查询 ====================

    /**
     * 获取所有权限（扁平列表，用于权限管理表格展示）。
     */
    @Transactional(readOnly = true)
    public List<PermissionListResponse> findAll() {
        return permissionRepository.findAll().stream()
                .map(PermissionListResponse::new)
                .toList();
    }

    /**
     * 根据 ID 列表查询权限
     */
    @Transactional(readOnly = true)
    public List<Permission> findByIds(List<Long> ids) {
        return permissionRepository.findAllById(ids);
    }

    /**
     * 按条件搜索权限。
     * @param permName 按名称模糊搜索（可选）
     * @param permKey  按标识模糊搜索（可选）
     * @param permType 权限类型：1菜单 2按钮（可选）
     */
    @Transactional(readOnly = true)
    public List<PermissionListResponse> search(String permName, String permKey, Integer permType) {
        return permissionRepository.searchPermissions(permName, permKey, permType).stream()
                .map(PermissionListResponse::new)
                .toList();
    }

    /**
     * 获取权限树（用于角色分配时勾选权限）。
     * 菜单权限自带 children 嵌套；按钮权限挂靠在父菜单下。
     */
    @Transactional(readOnly = true)
    public List<PermissionTreeResponse> getTree() {
        List<Permission> all = permissionRepository.findAll();
        return PermissionTreeResponse.buildTree(all);
    }

    /**
     * 获取单个权限详情。
     */
    @Transactional(readOnly = true)
    public Permission findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));
    }

    // ==================== 创建 ====================

    @Transactional
    public PermissionListResponse create(CreatePermissionRequest request) {
        if (!StringUtils.hasText(request.getPermName())) {
            throw new BusinessException("权限名称不能为空");
        }
        if (!StringUtils.hasText(request.getPermKey())) {
            throw new BusinessException("权限标识不能为空");
        }
        if (request.getPermType() == null ||
                (request.getPermType() != 1 && request.getPermType() != 2)) {
            throw new BusinessException("权限类型必须是 1(菜单) 或 2(按钮)");
        }

        // permKey 全系统唯一
        if (permissionRepository.existsByPermKey(request.getPermKey())) {
            throw new BusinessException("权限标识已存在：" + request.getPermKey());
        }

        // 按钮必须指定父菜单
        if (request.getPermType() == 2 && request.getParentId() == null) {
            throw new BusinessException("按钮权限必须归属某个菜单");
        }
        // 父菜单存在性校验
        if (request.getParentId() != null) {
            permissionRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("父权限不存在"));
        }

        Permission p = new Permission();
        p.setPermName(request.getPermName().trim());
        p.setPermKey(request.getPermKey().trim());
        p.setPermType(request.getPermType());
        p.setParentId(request.getParentId());
        p.setIsDeleted(0);

        return new PermissionListResponse(permissionRepository.save(p));
    }

    // ==================== 更新 ====================

    @Transactional
    public PermissionListResponse update(Long id, UpdatePermissionRequest request) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));

        if (StringUtils.hasText(request.getPermName())) {
            p.setPermName(request.getPermName().trim());
        }
        if (request.getParentId() != null) {
            // 禁止将自己设为父节点
            if (request.getParentId().equals(id)) {
                throw new BusinessException("不能将自己设为父权限");
            }
            permissionRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("父权限不存在"));
            p.setParentId(request.getParentId());
        }
        // 按钮类型不能改为菜单类型（children 数据会丢失）
        // 此处允许 parentId 置空将按钮升级为独立菜单

        return new PermissionListResponse(permissionRepository.save(p));
    }

    // ==================== 删除 ====================

    @Transactional
    public void delete(Long id) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));

        // 递归删除所有子权限
        List<Permission> children = permissionRepository.findByParentId(id);
        for (Permission child : children) {
            // 递归删除子权限的所有关联
            rolePermissionRepository.deleteByPermId(child.getId());
            permissionRepository.delete(child);
        }

        // 删除与角色的关联关系
        rolePermissionRepository.deleteByPermId(id);

        // 物理删除
        permissionRepository.delete(p);
    }
}
