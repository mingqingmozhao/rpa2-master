package com.rpa.auth.controller;

import com.rpa.auth.dto.ApiResponse;
import com.rpa.auth.dto.AssignPermissionRequest;
import com.rpa.auth.dto.CreateRoleRequest;
import com.rpa.auth.dto.PermissionInfo;
import com.rpa.auth.dto.RoleDetailResponse;
import com.rpa.auth.dto.RolePageResponse;
import com.rpa.auth.dto.UpdateRoleRequest;
import com.rpa.auth.model.Permission;
import com.rpa.auth.model.Role;
import com.rpa.auth.service.PermissionService;
import com.rpa.auth.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口（管理员操作）。
 */
@RestController
@RequestMapping("/admin/role")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;

    // ==================== 角色 CRUD ====================

    /**
     * 分页查询角色列表（兼容前端 el-pagination）。
     * GET /admin/role/list?page=1&pageSize=10&keyword=&roleCode=&status=
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<RolePageResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {
        RolePageResponse result = roleService.findAllPage(page, pageSize, keyword, roleCode, status);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDetailResponse>> getById(@PathVariable Long id) {
        Role role = roleService.findById(id);
        if (role == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, "角色不存在"));
        }
        // 获取角色的权限列表
        List<Long> permissionIds = roleService.getPermissionIds(id);
        // 获取完整的权限对象列表（用于显示权限名称）
        List<PermissionInfo> permissions = new java.util.ArrayList<>();
        if (!permissionIds.isEmpty()) {
            List<com.rpa.auth.model.Permission> permList = permissionService.findByIds(permissionIds);
            for (com.rpa.auth.model.Permission p : permList) {
                permissions.add(new PermissionInfo(p.getId(), p.getPermName(), p.getPermKey(), p.getPermType()));
            }
        }
        RoleDetailResponse response = new RoleDetailResponse();
        response.setId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setRemark(role.getRemark());
        response.setStatus(role.getStatus());
        response.setCreateTime(role.getCreateTime());
        response.setPermissions(permissions);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> create(
            @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== 角色-权限关联 ====================

    /**
     * 获取某角色已有的权限列表（嵌套树形）。
     * GET /admin/role/{id}/permissions
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<List<Long>>> getPermissions(@PathVariable Long id) {
        Role role = roleService.findById(id);
        if (role == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, "角色不存在"));
        }
        return ResponseEntity.ok(ApiResponse.ok(roleService.getPermissionIds(id)));
    }

    /**
     * 为角色分配/覆盖权限。
     * PUT /admin/role/{id}/permissions
     * Body: { permIds: [1, 2, 3] }
     */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(
            @PathVariable Long id,
            @RequestBody AssignPermissionRequest request) {
        Role role = roleService.findById(id);
        if (role == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, "角色不存在"));
        }
            roleService.assignPermissions(id, request.getPermissionIds());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
