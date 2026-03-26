package com.rpa.auth.controller;

import com.rpa.auth.dto.CreateUserRequest;
import com.rpa.auth.dto.AdminUpdateUserRequest;
import com.rpa.auth.dto.ResetPasswordRequest;
import com.rpa.auth.dto.Result;
import com.rpa.auth.dto.UserListResponse;
import com.rpa.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Integer status) {
        
        Page<UserListResponse> userPage = userService.getUserList(
            page - 1, pageSize, username, realName, roleId, status);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", userPage.getContent());
        result.put("total", userPage.getTotalElements());
        
        return Result.success(result);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return Result.success();
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        userService.updateUser(userId, request);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success();
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUsers(@RequestParam String userIds) {
        String[] ids = userIds.split(",");
        for (String id : ids) {
            userService.deleteUser(Long.parseLong(id.trim()));
        }
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{userId}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(userId, request.getPassword());
        return Result.success();
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success();
    }

    /**
     * 分配用户角色
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignUserRole(
            @PathVariable Long userId,
            @RequestParam Long roleId) {
        userService.assignUserRole(userId, roleId);
        return Result.success();
    }
}
