package com.rpa.auth.service;

import com.rpa.auth.advice.GlobalExceptionHandler.BusinessException;
import com.rpa.auth.dto.CreateRoleRequest;
import com.rpa.auth.dto.RolePageResponse;
import com.rpa.auth.dto.UpdateRoleRequest;
import com.rpa.auth.model.Permission;
import com.rpa.auth.model.Role;
import com.rpa.auth.model.RolePermission;
import com.rpa.auth.model.UserRole;
import com.rpa.auth.repository.PermissionRepository;
import com.rpa.auth.repository.RolePermissionRepository;
import com.rpa.auth.repository.RoleRepository;
import com.rpa.auth.repository.UserRoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色管理服务。
 */
@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    // ==================== 查询 ====================

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        if (MOCK_MODE || roleRepository == null) {
            return mockRoles();
        }
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RolePageResponse findAllPage(int page, int pageSize, String keyword, String roleCode, Integer status) {
        if (MOCK_MODE) {
            List<Role> all = mockRoles();
            List<Role> filtered = all.stream()
                    .filter(r -> keyword == null || r.getRoleName().contains(keyword) || (r.getRemark() != null && r.getRemark().contains(keyword)))
                    .filter(r -> roleCode == null || roleCode.isBlank() || keywordMatches(r.getRoleCode(), roleCode))
                    .filter(r -> status == null || r.getStatus().equals(status))
                    .toList();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, filtered.size());
            List<Role> records = start < filtered.size() ? filtered.subList(start, end) : List.of();
            return new RolePageResponse(records, filtered.size(), page, pageSize);
        }
        Page<Role> p = roleRepository.findByIsDeleted(PageRequest.of(page - 1, pageSize));
        // 在内存中进行关键词过滤（因为需要兼容多个字段的模糊查询）
        List<Role> filtered = p.getContent().stream()
                .filter(r -> keyword == null || keyword.isBlank() || r.getRoleName().contains(keyword) || (r.getRemark() != null && r.getRemark().contains(keyword)))
                // 角色编码匹配：由于数据库中没有 role_code 字段，使用 role_name 进行匹配
                .filter(r -> roleCode == null || roleCode.isBlank() || (r.getRoleName() != null && r.getRoleName().toUpperCase().contains(roleCode.toUpperCase())))
                .filter(r -> status == null || r.getStatus().equals(status))
                .toList();
        return new RolePageResponse(filtered, filtered.size(), page, pageSize);
    }

    private boolean keywordMatches(String actual, String keyword) {
        return actual != null && actual.contains(keyword);
    }

    @Transactional(readOnly = true)
    public Role findById(Long id) {
        if (MOCK_MODE || roleRepository == null) {
            return mockRoles().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
        return roleRepository.findByIdAndIsDeleted(id);
    }

    // ==================== 创建 ====================

    @Transactional
    public Role create(CreateRoleRequest request) {
        if (!StringUtils.hasText(request.getRoleName())) {
            throw new BusinessException("角色名称不能为空");
        }
        if (!MOCK_MODE) {
            if (roleRepository.findByRoleName(request.getRoleName().trim()) != null) {
                throw new BusinessException("角色名称已存在");
            }
            Role role = new Role();
            role.setRoleName(request.getRoleName().trim());
            if (StringUtils.hasText(request.getRoleCode())) {
                role.setRoleCode(request.getRoleCode().trim());
            }
            role.setRemark(request.getRemark());
            role.setStatus(1);
            role.setIsDeleted(0);
            return roleRepository.save(role);
        }
        // Mock 模式：返回模拟角色
        Role mockRole = new Role();
        mockRole.setId(99L);
        mockRole.setRoleName(request.getRoleName().trim());
        mockRole.setRoleCode(request.getRoleCode() != null ? request.getRoleCode() : request.getRoleName().toUpperCase());
        mockRole.setRemark(request.getRemark());
        mockRole.setStatus(1);
        mockRole.setIsDeleted(0);
        return mockRole;
    }

    // ==================== 更新 ====================

    @Transactional
    public Role update(Long id, UpdateRoleRequest request) {
        if (!MOCK_MODE) {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("角色不存在"));
            if (StringUtils.hasText(request.getRoleName())) {
                // 检查新名称是否与其他角色重名
                Role existing = roleRepository.findByRoleName(request.getRoleName().trim());
                if (existing != null && !existing.getId().equals(id)) {
                    throw new BusinessException("角色名称已存在");
                }
                role.setRoleName(request.getRoleName().trim());
            }
            if (request.getRemark() != null) {
                role.setRemark(request.getRemark());
            }
            if (request.getStatus() != null) {
                role.setStatus(request.getStatus());
            }
            return roleRepository.save(role);
        }
        // Mock 模式
        Role mock = mockRoles().stream()
                .filter(r -> r.getId().equals(id)).findFirst()
                .orElseThrow(() -> new BusinessException("角色不存在"));
        if (request.getRoleName() != null) mock.setRoleName(request.getRoleName());
        if (request.getRemark() != null) mock.setRemark(request.getRemark());
        if (request.getStatus() != null) mock.setStatus(request.getStatus());
        return mock;
    }

    // ==================== 删除 ====================

    @Transactional
    public void delete(Long id) {
        if (!MOCK_MODE) {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("角色不存在"));
            
            // 检查角色是否已被删除
            if (role.getIsDeleted() == 1) {
                throw new BusinessException("角色已被删除");
            }
            
            System.out.println("[RoleService.delete] 开始删除角色 ID: " + id);
            
            // 直接使用原生 SQL 查询是否有用户使用该角色
            String checkSql = "SELECT COUNT(*) FROM sys_user_role WHERE role_id = ?";
            var query = entityManager.createNativeQuery(checkSql);
            query.setParameter(1, id);
            Object result = query.getSingleResult();
            long count = ((Number) result).longValue();
            System.out.println("[RoleService.delete] 原生 SQL 查询角色 ID " + id + " 的用户数量：" + count);
            
            if (count > 0) {
                throw new BusinessException("该角色已分配给 " + count + " 个用户，无法删除");
            }
            
            // 删除角色 - 权限关联
            rolePermissionRepository.deleteByRoleId(id);
            // 逻辑删除角色
            System.out.println("[RoleService.delete] 设置 is_deleted=1 之前：" + role.getIsDeleted());
            role.setIsDeleted(1);
            System.out.println("[RoleService.delete] 设置 is_deleted=1 之后：" + role.getIsDeleted());
            System.out.println("[RoleService.delete] 准备保存角色...");
            Role savedRole = roleRepository.save(role);
            System.out.println("[RoleService.delete] 角色保存之后，is_deleted=" + savedRole.getIsDeleted());
            System.out.println("[RoleService.delete] 角色 ID: " + id + " 删除成功");
            return;
        }
        // Mock 模式：静默成功
    }

    // ==================== 角色-权限关联 ====================

    /**
     * 查询某角色已有的权限 ID 列表。
     */
    @Transactional(readOnly = true)
    public List<Long> getPermissionIds(Long roleId) {
        if (!MOCK_MODE) {
            return rolePermissionRepository.findByRoleId(roleId).stream()
                    .map(RolePermission::getPermId)
                    .toList();
        }
        // Mock：返回该角色所有权限
        return permissionRepository.findAll().stream()
                .map(Permission::getId)
                .toList();
    }

    /**
     * 为角色批量分配权限（覆盖式：先删后加）。
     */
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permIds) {
        if (!MOCK_MODE) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new BusinessException("角色不存在"));
            // 校验所有权限存在
            for (Long pid : permIds) {
                if (permissionRepository.findById(pid).isEmpty()) {
                    throw new BusinessException("权限 ID 不存在：" + pid);
                }
            }
            // 删除旧关联
            rolePermissionRepository.deleteByRoleId(roleId);
            // 批量插入新关联
            for (Long pid : new HashSet<>(permIds)) { // 去重
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermId(pid);
                rolePermissionRepository.save(rp);
            }
            return;
        }
        // Mock 模式：静默成功
    }

    // ==================== Mock 数据 ====================

    private static final boolean MOCK_MODE = false;

    private List<Role> mockRoles() {
        Role admin = new Role();
        admin.setId(1L);
        admin.setRoleName("ADMIN");
        admin.setRemark("超级管理员，拥有所有权限");
        admin.setStatus(1);
        admin.setIsDeleted(0);

        Role operator = new Role();
        operator.setId(2L);
        operator.setRoleName("OPERATOR");
        operator.setRemark("操作员，负责日常 RPA 流程操作");
        operator.setStatus(1);
        operator.setIsDeleted(0);

        Role viewer = new Role();
        viewer.setId(3L);
        viewer.setRoleName("VIEWER");
        viewer.setRemark("访客，仅可查看数据");
        viewer.setStatus(1);
        viewer.setIsDeleted(0);

        return List.of(admin, operator, viewer);
    }
}
