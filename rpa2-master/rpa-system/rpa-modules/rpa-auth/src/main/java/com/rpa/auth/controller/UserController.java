package com.rpa.auth.controller;

import com.rpa.auth.dto.AdminUpdateUserRequest;
import com.rpa.auth.dto.ApiResponse;
import com.rpa.auth.dto.CreateUserRequest;
import com.rpa.auth.dto.ResetPasswordRequest;
import com.rpa.auth.dto.UserListResponse;
import com.rpa.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<UserListResponse>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Integer status) {
        Page<UserListResponse> result = userService.findAll(
                username, realName, roleId, status, PageRequest.of(page - 1, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserListResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserListResponse>> create(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.create(request)));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserListResponse>> update(
            @PathVariable Long id, 
            @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.update(id, request)));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getUserId(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> resetPasswordByAdmin(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String password = body.get("password");
        userService.resetPassword(id, password);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 分配/变更用户角色（Body 方式，与前端一致）。
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long roleId = body.get("roleId");
        userService.assignRole(id, roleId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setStatus(status);
        userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
