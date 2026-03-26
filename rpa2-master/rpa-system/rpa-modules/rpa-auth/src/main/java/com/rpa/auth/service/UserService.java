package com.rpa.auth.service;

import com.rpa.auth.dto.CreateUserRequest;
import com.rpa.auth.dto.AdminUpdateUserRequest;
import com.rpa.auth.dto.UserListResponse;
import com.rpa.auth.exception.AuthException;
import com.rpa.auth.model.Role;
import com.rpa.auth.model.User;
import com.rpa.auth.model.UserRole;
import com.rpa.auth.repository.RoleRepository;
import com.rpa.auth.repository.UserRepository;
import com.rpa.auth.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询用户列表
     */
    public Page<UserListResponse> getUserList(int page, int size, String username, String realName, Long roleId, Integer status) {
        Pageable pageable = PageRequest.of(page, size);
        
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            
            if (realName != null && !realName.isEmpty()) {
                predicates.add(cb.like(root.get("nickname"), "%" + realName + "%"));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<UserListResponse> responseList = new ArrayList<>();
        
        for (User user : userPage.getContent()) {
            UserListResponse response = convertToResponse(user, roleId);
            responseList.add(response);
        }
        
        // 如果指定了 roleId，过滤用户
        if (roleId != null) {
            responseList.removeIf(r -> r.getRoleName() == null);
        }
        
        return new PageImpl<>(responseList, pageable, userPage.getTotalElements());
    }

    /**
     * 创建用户
     */
    @Transactional
    public void createUser(CreateUserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("邮箱已被使用");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1); // 默认启用
        userRepository.save(user);
        
        // 分配角色
        Optional<Role> roleOptional = roleRepository.findById(request.getRoleId());
        if (!roleOptional.isPresent()) {
            throw new AuthException("角色不存在");
        }
        
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(request.getRoleId());
        userRoleRepository.save(userRole);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(Long userId, AdminUpdateUserRequest request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new AuthException("用户不存在");
        }
        
        User user = userOptional.get();
        user.setNickname(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        userRepository.save(user);
        
        // 更新角色
        if (request.getRoleId() != null) {
            userRoleRepository.deleteByUserId(userId);
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(request.getRoleId());
            userRoleRepository.save(userRole);
        }
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AuthException("用户不存在");
        }
        
        // 删除用户角色关联
        userRoleRepository.deleteByUserId(userId);
        
        // 删除用户
        userRepository.deleteById(userId);
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new AuthException("用户不存在");
        }
        
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new AuthException("用户不存在");
        }
        
        User user = userOptional.get();
        user.setStatus(status);
        userRepository.save(user);
    }

    /**
     * 分配用户角色
     */
    @Transactional
    public void assignUserRole(Long userId, Long roleId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new AuthException("用户不存在");
        }
        
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (!roleOptional.isPresent()) {
            throw new AuthException("角色不存在");
        }
        
        // 删除原有角色
        userRoleRepository.deleteByUserId(userId);
        
        // 分配新角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);
    }

    /**
     * 将 User 转换为 UserListResponse
     */
    private UserListResponse convertToResponse(User user, Long filterRoleId) {
        UserListResponse response = new UserListResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getNickname());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreatedAt().format(FORMATTER));
        response.setRemark("");
        
        // 获取用户角色
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getUserId());
        if (!userRoles.isEmpty()) {
            Optional<Role> role = roleRepository.findById(userRoles.get(0).getRoleId());
            role.ifPresent(r -> response.setRoleName(r.getRoleName()));
            
            // 如果指定了 filterRoleId，检查是否匹配
            if (filterRoleId != null && !userRoles.get(0).getRoleId().equals(filterRoleId)) {
                response.setRoleName(null);
            }
        }
        
        return response;
    }
}
