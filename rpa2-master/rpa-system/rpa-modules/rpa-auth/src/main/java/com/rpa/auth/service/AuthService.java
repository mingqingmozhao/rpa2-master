package com.rpa.auth.service;

import com.rpa.auth.advice.GlobalExceptionHandler.BusinessException;
import com.rpa.auth.model.Role;
import com.rpa.auth.model.User;
import com.rpa.auth.model.UserRole;
import com.rpa.auth.repository.RoleRepository;
import com.rpa.auth.repository.UserRepository;
import com.rpa.auth.repository.UserRoleRepository;
import com.rpa.auth.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 启动时确保 admin 用户存在。
     * 如果数据库里没有 admin，自动创建一个。
     */
    @PostConstruct
    @Transactional
    public void initAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            User saved = userRepository.save(admin);

            Role adminRole = roleRepository.findByRoleName("ADMIN");
            if (adminRole != null) {
                UserRole ur = new UserRole();
                ur.setUserId(saved.getId());
                ur.setRoleId(adminRole.getId());
                userRoleRepository.save(ur);
            }
        }
    }

    /**
     * 用户登录。
     *
     * @param rawUsername 登录账号（不允许空）
     * @param rawPassword 明文密码（不允许空）
     * @return 包含 JWT token 和用户信息的 Map
     * @throws BusinessException 凭证错误或账号被禁用时抛出
     */
    public Map<String, Object> login(String rawUsername, String rawPassword) {
        if (!StringUtils.hasText(rawUsername) || !StringUtils.hasText(rawPassword)) {
            throw new BusinessException("用户名或密码不能为空");
        }

        // =================================================================
        // 【开发/演示用】硬编码账号：输入任意用户名 + 密码 admin123 即可登录
        // 上线前务必删除此段，改回正常的数据库校验
        // =================================================================
        if ("admin".equals(rawUsername.trim()) && "admin123".equals(rawPassword)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ADMIN");
            claims.put("userId", 1L);
            String token = jwtUtils.generateToken("admin", claims);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", 1L);
            result.put("username", "admin");
            result.put("realName", "系统管理员");
            return result;
        }
        // =================================================================

        User user = userRepository.findByUsername(rawUsername.trim())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 按 sys_user_role 表中的 roleId 查找角色名称
        String roleName = "USER";
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        if (!userRoles.isEmpty()) {
            Long roleId = userRoles.get(0).getRoleId();
            Role role = roleRepository.findById(roleId).orElse(null);
            if (role != null && StringUtils.hasText(role.getRoleName())) {
                roleName = role.getRoleName();
            }
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roleName);
        claims.put("userId", user.getId());

        String token = jwtUtils.generateToken(user.getUsername(), claims);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        return result;
    }

    public User getCurrentUser(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("用户名不能为空");
        }
        return userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
