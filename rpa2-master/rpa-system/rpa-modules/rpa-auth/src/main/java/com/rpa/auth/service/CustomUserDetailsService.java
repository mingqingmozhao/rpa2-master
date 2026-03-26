package com.rpa.auth.service;

import com.rpa.auth.model.Permission;
import com.rpa.auth.model.Role;
import com.rpa.auth.model.User;
import com.rpa.auth.repository.PermissionRepository;
import com.rpa.auth.repository.RoleRepository;
import com.rpa.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        User user = userOptional.get();

        // 从数据库获取用户角色
        List<String> roles = getUserRolesFromDb(user.getUserId());
        // 从数据库获取用户权限
        List<String> permissions = getUserPermissionsFromDb(user.getUserId());

        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // 添加具体权限
        authorities.addAll(permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));

        // 返回UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1, // 账号是否启用
                true, // 账号是否未过期
                true, // 凭证是否未过期
                true, // 账号是否未锁定
                authorities
        );
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
