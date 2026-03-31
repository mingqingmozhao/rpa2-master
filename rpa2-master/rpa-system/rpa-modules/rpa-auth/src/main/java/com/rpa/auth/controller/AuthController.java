package com.rpa.auth.controller;

import com.rpa.auth.advice.GlobalExceptionHandler.BusinessException;
import com.rpa.auth.dto.ApiResponse;
import com.rpa.auth.dto.UpdatePasswordRequest;
import com.rpa.auth.dto.UpdateUserInfoRequest;
import com.rpa.auth.model.User;
import com.rpa.auth.service.AuthService;
import com.rpa.auth.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 认证与个人中心接口。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @PostConstruct
    private void init() {
        // 确保上传目录存在（使用绝对路径）
        File dir = new File(System.getProperty("user.dir"), uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File avatarDir = new File(dir, "avatars");
        if (!avatarDir.exists()) {
            avatarDir.mkdirs();
        }
        // 更新 uploadDir 为绝对路径
        uploadDir = dir.getAbsolutePath();
    }

    // ==================== 登录 / 退出 ====================

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException("用户名或密码不能为空");
        }

        Map<String, Object> result = authService.login(username.trim(), password);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 退出登录。
     * 清除本地 SecurityContext，JWT 的有效性由前端丢弃 token 实现。
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        request.getSession(false);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== 当前用户信息 ====================

    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未登录"));
        }

        String username = auth.getName();
        User user;
        try {
            user = authService.getCurrentUser(username);
        } catch (BusinessException ex) {
            // 数据库无此用户时（演示账号等情况），返回模拟用户，
            // 但角色优先从 SecurityContext authorities（JWT）获取
            user = new User();
            user.setId(1L);
            user.setUsername(username);
            user.setRealName("系统管理员");
            user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
            user.setStatus(1);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("status", user.getStatus());

        // 从 SecurityContext authorities（JWT 中解析）提取角色
        if (auth.getAuthorities() != null) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                String authority = ga.getAuthority();
                String role = authority.startsWith("ROLE_")
                        ? authority.substring(5)
                        : authority;
                userInfo.put("role", role);
                userInfo.put("roles", List.of(role));
                break;
            }
        } else {
            userInfo.put("roles", List.of());
        }

        return ResponseEntity.ok(ApiResponse.ok(userInfo));
    }

    // ==================== 个人资料修改 ====================

    /**
     * 修改个人资料（姓名、邮箱、手机号）。
     * PUT /auth/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @Valid @RequestBody UpdateUserInfoRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未登录"));
        }
        String username = auth.getName();

        User user = userService.updateProfile(username, request);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ==================== 头像上传 ====================

    private static final Pattern IMAGE_PATTERN =
            Pattern.compile("\\.(jpg|jpeg|png|gif|webp|bmp)$", Pattern.CASE_INSENSITIVE);
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    /**
     * 上传头像。
     * POST /auth/avatar  (Content-Type: multipart/form-data)
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未登录"));
        }
        String username = auth.getName();

        // 校验文件
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("图片大小不能超过 2MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !IMAGE_PATTERN.matcher(originalFilename).find()) {
            throw new BusinessException("仅支持 JPG/PNG/GIF/WEBP/BMP 格式的图片");
        }

        // 生成唯一文件名
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String newFileName = username + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        // 确保上传目录存在
        File dir = new File(uploadDir, "avatars");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Path targetPath = dir.toPath().resolve(newFileName);

        try {
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new BusinessException("头像上传失败：" + e.getMessage());
        }

        // 更新数据库中的头像路径
        String avatarUrl = "/uploads/avatars/" + newFileName;
        userService.updateAvatar(username, avatarUrl);

        Map<String, String> data = new HashMap<>();
        data.put("avatar", avatarUrl);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ==================== 密码修改 ====================

    /**
     * 修改个人密码。
     * POST /auth/password
     */
    @PostMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody UpdatePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未登录"));
        }
        String username = auth.getName();

        if (request.getNewPassword().length() < 6 || request.getNewPassword().length() > 20) {
            throw new BusinessException("新密码长度为6-20位");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        userService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
