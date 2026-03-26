package com.rpa.auth.controller;

import com.rpa.auth.dto.LoginRequest;
import com.rpa.auth.dto.LoginResponse;
import com.rpa.auth.dto.Result;
import com.rpa.auth.dto.UserInfoResponse;
import com.rpa.auth.dto.UpdateUserRequest;
import com.rpa.auth.dto.UpdatePasswordRequest;
import com.rpa.auth.service.AuthService;
import com.rpa.auth.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        // 简单实现：前端丢弃Token，后端不做业务处理
        // 如需服务端Token黑名单，可在此处将Token加入Redis黑名单
        authService.logout(request);
        return Result.success();
    }

    @GetMapping("/user-info")
    public Result<UserInfoResponse> getCurrentUserInfo() {
        // 从 SecurityContext 中获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserInfoResponse userInfo = authService.getCurrentUserInfo(username);
        return Result.success(userInfo);
    }

    @PutMapping("/user-info")
    public Result<UserInfoResponse> updateUserInfo(@Valid @RequestBody UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserInfoResponse updatedInfo = authService.updateUserInfo(username, request);
        return Result.success(updatedInfo);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        authService.updatePassword(username, request);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpServletRequest request) {
        try {
            // 获取当前用户名
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 验证文件
            if (file.isEmpty()) {
                throw new AuthException("上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new AuthException("只能上传图片文件");
            }

            // 验证文件大小（限制 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new AuthException("图片大小不能超过 5MB");
            }

            // 获取项目根目录路径
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = username + "_" + UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir, fileName);

            // 保存文件
            Files.write(filePath, file.getBytes());

            // 生成访问 URL（相对路径）
            String avatarUrl = "/uploads/avatars/" + fileName;

            // 更新用户头像
            UserInfoResponse updatedInfo = authService.uploadAvatar(username, avatarUrl);

            // 返回头像 URL
            Map<String, String> result = new HashMap<>();
            result.put("avatar", avatarUrl);
            result.put("username", username);

            return Result.success(result);
        } catch (AuthException e) {
            throw e;
        } catch (IOException e) {
            throw new AuthException("上传失败：" + e.getMessage());
        }
    }
}
