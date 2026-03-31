package com.rpa.auth.controller;

import com.rpa.auth.dto.ApiResponse;
import com.rpa.auth.dto.CreatePermissionRequest;
import com.rpa.auth.dto.PermissionListResponse;
import com.rpa.auth.dto.PermissionTreeResponse;
import com.rpa.auth.dto.UpdatePermissionRequest;
import com.rpa.auth.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理接口（管理员操作）。
 */
@RestController
@RequestMapping("/admin/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 权限列表（表格展示，扁平）。
     * 包含关键字搜索（名称/标识）。
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PermissionListResponse>>> list(
            @RequestParam(required = false) String permName,
            @RequestParam(required = false) String permKey,
            @RequestParam(required = false) Integer permType) {
        List<PermissionListResponse> list;
        if (permName != null || permKey != null || permType != null) {
            list = permissionService.search(permName, permKey, permType);
        } else {
            list = permissionService.findAll();
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    /**
     * 权限树（角色分配时勾选，嵌套结构）。
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<PermissionTreeResponse>>> tree() {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.getTree()));
    }

    /**
     * 权限详情。
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionListResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                new PermissionListResponse(permissionService.findById(id))));
    }

    /**
     * 创建权限（菜单或按钮）。
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionListResponse>> create(
            @Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.create(request)));
    }

    /**
     * 更新权限。
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionListResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.update(id, request)));
    }

    /**
     * 删除权限（物理/逻辑删除）。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
