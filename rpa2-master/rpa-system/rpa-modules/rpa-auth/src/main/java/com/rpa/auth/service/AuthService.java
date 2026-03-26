package com.rpa.auth.service;

import com.rpa.auth.dto.LoginRequest;
import com.rpa.auth.dto.LoginResponse;
import com.rpa.auth.dto.UserInfoResponse;
import com.rpa.auth.dto.UpdateUserRequest;
import com.rpa.auth.dto.UpdatePasswordRequest;
import com.rpa.auth.exception.AuthException;
import com.rpa.auth.model.Permission;
import com.rpa.auth.model.Role;
import com.rpa.auth.model.User;
import com.rpa.auth.repository.PermissionRepository;
import com.rpa.auth.repository.RoleRepository;
import com.rpa.auth.repository.UserRepository;
import com.rpa.auth.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 从配置文件读取过期时间（单位：毫秒）
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户信息
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (!userOptional.isPresent()) {
            throw new AuthException("用户名或密码错误");
        }

        User user = userOptional.get();

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }

        // 3. 检查账号状态
        if (user.getStatus() != 1) {
            throw new AuthException("账号已禁用");
        }

        // 4. 加载用户详情
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_USER")
            );

        // 5. 生成Token
        Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("userId", user.getUserId());
        String token = jwtUtils.generateToken(extraClaims, userDetails);

        // 6. 计算剩余有效期（使用配置的过期时间）
        long expiresIn = jwtExpirationMs / 1000; // 转换为秒

        // 7. 从数据库获取用户角色和权限
        List<String> roles = getUserRolesFromDb(user.getUserId());
        List<String> permissions = getUserPermissionsFromDb(user.getUserId());

        // 8. 组装返回结果
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(null); // 头像字段，根据实际业务填充
        userInfo.setStatus(user.getStatus());

        response.setUserInfo(userInfo);
        response.setRoles(roles);
        response.setPermissions(permissions);

        return response;
    }

    /**
     * 获取当前登录用户信息
     *
     * @param username 用户名
     * @return 用户信息响应
     */
    public UserInfoResponse getCurrentUserInfo(String username) {
        // 查询用户信息
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            throw new AuthException("用户不存在");
        }

        User user = userOptional.get();

        // 从数据库获取用户角色和权限
        List<String> roles = getUserRolesFromDb(user.getUserId());
        List<String> permissions = getUserPermissionsFromDb(user.getUserId());

        // 组装返回结果
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar()); // 头像字段，从用户对象中获取
        response.setStatus(user.getStatus());
        response.setRoles(roles);
        response.setPermissions(permissions);

        return response;
    }

    /**
     * 退出登录
     * 简单实现：前端丢弃Token，后端不做业务处理
     * 如需服务端Token黑名单，可在此处将Token加入Redis黑名单
     *
     * @param request HTTP请求
     */
    public void logout(HttpServletRequest request) {
        // 从请求头中获取 Token
        String token = extractTokenFromRequest(request);

        if (token != null) {
            // 简单实现：不做任何处理，前端丢弃 Token 即可
            // TODO: 如需 Token 黑名单，可在此处将 Token 加入 Redis
            // long remainingTime = jwtUtils.getRemainingTime(token);
            // redisTemplate.opsForValue().set("blacklist:token:" + token, "1", remainingTime, TimeUnit.SECONDS);
        }
    }

    /**
     * 更新用户信息
     *
     * @param username 用户名
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    public UserInfoResponse updateUserInfo(String username, UpdateUserRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            throw new AuthException("用户不存在");
        }

        User user = userOptional.get();

        // 验证邮箱是否重复
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AuthException("邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }

        // 验证手机号是否重复
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            // 检查手机号是否被其他用户使用
            Optional<User> existingUser = userRepository.findByPhone(request.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(user.getUserId())) {
                throw new AuthException("手机号已被使用");
            }
            user.setPhone(request.getPhone());
        }

        // 更新昵称
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }

        // 更新头像
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userRepository.save(user);

        // 返回更新后的用户信息
        return getCurrentUserInfo(username);
    }

    /**
     * 修改密码
     *
     * @param username 用户名
     * @param request 密码修改请求
     */
    public void updatePassword(String username, UpdatePasswordRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            throw new AuthException("用户不存在");
        }

        User user = userOptional.get();

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AuthException("原密码错误");
        }

        // 验证新密码长度
        if (request.getNewPassword().length() < 6) {
            throw new AuthException("密码长度不能少于 6 位");
        }

        // 加密新密码并保存
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 上传头像
     *
     * @param username 用户名
     * @param avatarUrl 头像 URL
     * @return 更新后的用户信息
     */
    public UserInfoResponse uploadAvatar(String username, String avatarUrl) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            throw new AuthException("用户不存在");
        }

        User user = userOptional.get();
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return getCurrentUserInfo(username);
    }

    /**
     * 从请求头中提取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 从数据库获取用户角色列表
     */
    private List<String> getUserRolesFromDb(Long userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    /**
     * 从数据库获取用户权限列表
     */
    private List<String> getUserPermissionsFromDb(Long userId) {
        List<Permission> permissions = permissionRepository.findPermissionsByUserId(userId);
        return permissions.stream()
                .map(Permission::getPermKey)
                .collect(Collectors.toList());
    }
}
