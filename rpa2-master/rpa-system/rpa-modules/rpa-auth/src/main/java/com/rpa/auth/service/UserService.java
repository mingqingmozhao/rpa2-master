package com.rpa.auth.service;

import com.rpa.auth.advice.GlobalExceptionHandler.BusinessException;
import com.rpa.auth.dto.AdminUpdateUserRequest;
import com.rpa.auth.dto.CreateUserRequest;
import com.rpa.auth.dto.UpdateUserInfoRequest;
import com.rpa.auth.dto.UserListResponse;
import com.rpa.auth.model.Role;
import com.rpa.auth.model.User;
import com.rpa.auth.model.UserRole;
import com.rpa.auth.repository.RoleRepository;
import com.rpa.auth.repository.UserRepository;
import com.rpa.auth.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理服务（演示用：数据库为空时自动降级为内存模拟数据）。
 * <p>
 * 演示开关 {@link #MOCK_MODE} 为 true 时，
 * findAll / findById / create / update / delete 所有操作均走内存 mock。
 * 上线时设为 false，恢复真实数据库操作。
 */
@Service
public class UserService {

    private static final boolean MOCK_MODE = false;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 查询 ====================

    @Transactional(readOnly = true)
    public Page<UserListResponse> findAll(String username, String realName,
                                          Long roleId, Integer status, Pageable pageable) {
        if (MOCK_MODE) {
            return mockUserPage(username, realName, roleId, status, pageable);
        }
        return userRepository.findAll(username, realName, roleId, status, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public UserListResponse findById(Long id) {
        if (MOCK_MODE) {
            UserListResponse mock = mockUsers().stream()
                    .filter(u -> u.getUserId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (mock == null) {
                throw new BusinessException("用户不存在");
            }
            return mock;
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToResponse(user);
    }

    // ==================== 创建 ====================

    @Transactional
    public UserListResponse create(CreateUserRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        if (!MOCK_MODE) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("用户名已存在");
            }
            User user = new User();
            user.setUsername(request.getUsername().trim());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setStatus(1);
            User saved = userRepository.save(user);
            if (request.getRoleId() != null) {
                UserRole ur = new UserRole();
                ur.setUserId(saved.getId());
                ur.setRoleId(request.getRoleId());
                userRoleRepository.save(ur);
            }
            return convertToResponse(saved);
        }

        // Mock 模式：直接返回成功（id 自动递增）
        long nextId = mockUsers().stream()
                .mapToLong(UserListResponse::getUserId).max().orElse(0) + 1;
        UserListResponse resp = new UserListResponse();
        resp.setUserId(nextId);
        resp.setUsername(request.getUsername().trim());
        resp.setRealName(request.getRealName());
        resp.setEmail(request.getEmail());
        resp.setPhone(request.getPhone());
        resp.setStatus(1);
        if (request.getRoleId() != null) {
            resp.setRoleId(request.getRoleId());
            Role r = new Role();
            r.setId(request.getRoleId());
            // 从 DB 或 mock 查找角色名
            String roleName = roleRepository != null
                    ? roleRepository.findById(request.getRoleId())
                            .map(Role::getRoleName).orElse("USER")
                    : mockRoleName(request.getRoleId());
            resp.setRoleName(roleName);
        }
        return resp;
    }

    // ==================== 更新 ====================

    @Transactional
    public UserListResponse update(Long id, AdminUpdateUserRequest request) {
        if (MOCK_MODE) {
            UserListResponse existing = mockUsers().stream()
                    .filter(u -> u.getUserId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            if (request.getRealName() != null) existing.setRealName(request.getRealName());
            if (request.getEmail() != null) existing.setEmail(request.getEmail());
            if (request.getPhone() != null) existing.setPhone(request.getPhone());
            if (request.getStatus() != null) existing.setStatus(request.getStatus());
            if (request.getRoleId() != null) {
                existing.setRoleId(request.getRoleId());
                existing.setRoleName(mockRoleName(request.getRoleId()));
            }
            return existing;
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        userRepository.save(user);

        if (request.getRoleId() != null) {
            userRoleRepository.deleteByUserId(id);
            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRoleId(request.getRoleId());
            userRoleRepository.save(ur);
        }
        return convertToResponse(user);
    }

    // ==================== 删除 ====================

    @Transactional
    public void delete(Long id) {
        if (!MOCK_MODE) {
            if (!userRepository.existsById(id)) {
                throw new BusinessException("用户不存在");
            }
            userRoleRepository.deleteByUserId(id);
            userRepository.deleteById(id);
        }
        // Mock 模式：静默成功（数据在内存，重启后恢复）
    }

    // ==================== 密码重置 ====================

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        if (!MOCK_MODE) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

    // ==================== 角色分配 ====================

    @Transactional
    public void assignRole(Long userId, Long roleId) {
        if (MOCK_MODE) {
            return;
        }
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        userRoleRepository.deleteByUserId(userId);
        if (roleId != null) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new BusinessException("角色不存在"));
            if (role.getStatus() == null || role.getStatus() != 1) {
                throw new BusinessException("角色不可用");
            }
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleRepository.save(ur);
        }
    }

    // ==================== 个人资料修改 ====================

    @Transactional
    public User updateProfile(String username, UpdateUserInfoRequest request) {
        if (!MOCK_MODE) {
            User user = userRepository.findByUsername(username.trim())
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            
            boolean hasChanges = false;
            
            if (request.getRealName() != null) {
                String newRealName = request.getRealName().trim();
                if (!newRealName.equals(user.getRealName())) {
                    user.setRealName(newRealName);
                    hasChanges = true;
                }
            }
            
            if (request.getEmail() != null) {
                String newEmail = request.getEmail().trim();
                if (!newEmail.equals(user.getEmail())) {
                    user.setEmail(newEmail);
                    hasChanges = true;
                }
            }
            
            if (request.getPhone() != null) {
                String newPhone = request.getPhone().trim();
                if (!newPhone.equals(user.getPhone())) {
                    user.setPhone(newPhone);
                    hasChanges = true;
                }
            }
            
            if (!hasChanges) {
                throw new BusinessException("输入和之前的一致，未修改");
            }
            
            return userRepository.save(user);
        }
        // Mock 模式：返回模拟用户
        User mock = new User();
        mock.setId(1L);
        mock.setUsername(username);
        mock.setRealName(request.getRealName() != null ? request.getRealName() : "系统管理员");
        mock.setEmail(request.getEmail());
        mock.setPhone(request.getPhone());
        mock.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
        mock.setStatus(1);
        return mock;
    }

    // ==================== 密码修改 ====================

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (!MOCK_MODE) {
            User user = userRepository.findByUsername(username.trim())
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new BusinessException("旧密码不正确");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return;
        }
        // Mock 模式：静默成功
    }

    // ==================== 头像更新 ====================

    @Transactional
    public void updateAvatar(String username, String avatarUrl) {
        if (!MOCK_MODE) {
            User user = userRepository.findByUsername(username.trim())
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            return;
        }
        // Mock 模式：静默成功
    }

    // ==================== 内部工具 ====================

    private UserListResponse convertToResponse(User user) {
        UserListResponse response = new UserListResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());

        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        if (!userRoles.isEmpty()) {
            Long roleId = userRoles.get(0).getRoleId();
            response.setRoleId(roleId);
            roleRepository.findById(roleId).ifPresent(
                    role -> response.setRoleName(role.getRoleName()));
        }
        return response;
    }

    // ==================== Mock 数据 ====================

    private Page<UserListResponse> mockUserPage(String username, String realName,
                                                 Long roleId, Integer status, Pageable pageable) {
        List<UserListResponse> all = mockUsers().stream()
                .filter(u -> !StringUtils.hasText(username) ||
                        u.getUsername().contains(username))
                .filter(u -> !StringUtils.hasText(realName) ||
                        (u.getRealName() != null && u.getRealName().contains(realName)))
                .filter(u -> roleId == null || (u.getRoleId() != null && u.getRoleId().equals(roleId)))
                .filter(u -> status == null || u.getStatus().equals(status))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<UserListResponse> page = start < all.size()
                ? all.subList(start, end)
                : List.of();

        return new PageImpl<>(page, pageable, all.size());
    }

    private List<UserListResponse> mockUsers() {
        UserListResponse u1 = new UserListResponse();
        u1.setUserId(1L);
        u1.setUsername("admin");
        u1.setRealName("系统管理员");
        u1.setEmail("admin@example.com");
        u1.setPhone("13800138000");
        u1.setStatus(1);
        u1.setRoleId(1L);
        u1.setRoleName("ADMIN");
        u1.setRemark("超级管理员");
        u1.setCreateTime(LocalDateTime.of(2026, 1, 1, 10, 0, 0));

        UserListResponse u2 = new UserListResponse();
        u2.setUserId(2L);
        u2.setUsername("operator01");
        u2.setRealName("操作员 张三");
        u2.setEmail("op01@example.com");
        u2.setPhone("13800138001");
        u2.setStatus(1);
        u2.setRoleId(2L);
        u2.setRoleName("OPERATOR");
        u2.setRemark("RPA 流程操作员");
        u2.setCreateTime(LocalDateTime.of(2026, 2, 1, 10, 0, 0));

        UserListResponse u3 = new UserListResponse();
        u3.setUserId(3L);
        u3.setUsername("operator02");
        u3.setRealName("操作员 李四");
        u3.setEmail("op02@example.com");
        u3.setPhone("13800138002");
        u3.setStatus(1);
        u3.setRoleId(2L);
        u3.setRoleName("OPERATOR");
        u3.setRemark("RPA 流程操作员");
        u3.setCreateTime(LocalDateTime.of(2026, 2, 15, 10, 0, 0));

        UserListResponse u4 = new UserListResponse();
        u4.setUserId(4L);
        u4.setUsername("viewer01");
        u4.setRealName("访客 王五");
        u4.setEmail("viewer01@example.com");
        u4.setPhone("13800138003");
        u4.setStatus(1);
        u4.setRoleId(3L);
        u4.setRoleName("VIEWER");
        u4.setRemark("仅可查看数据");
        u4.setCreateTime(LocalDateTime.of(2026, 3, 1, 10, 0, 0));

        UserListResponse u5 = new UserListResponse();
        u5.setUserId(5L);
        u5.setUsername("disabled01");
        u5.setRealName("已禁用账号");
        u5.setEmail("disabled@example.com");
        u5.setPhone("13800138004");
        u5.setStatus(0);  // 已禁用
        u5.setRoleId(3L);
        u5.setRoleName("VIEWER");
        u5.setCreateTime(LocalDateTime.of(2026, 3, 10, 10, 0, 0));

        return List.of(u1, u2, u3, u4, u5);
    }

    private String mockRoleName(Long roleId) {
        if (roleId == null) return "USER";
        return switch (roleId.intValue()) {
            case 1 -> "ADMIN";
            case 2 -> "OPERATOR";
            case 3 -> "VIEWER";
            default -> "USER";
        };
    }
}
