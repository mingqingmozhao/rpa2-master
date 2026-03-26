package com.rpa.auth.config;

import com.rpa.auth.model.Permission;
import com.rpa.auth.model.Role;
import com.rpa.auth.model.RolePermission;
import com.rpa.auth.model.User;
import com.rpa.auth.model.UserRole;
import com.rpa.auth.repository.PermissionRepository;
import com.rpa.auth.repository.RolePermissionRepository;
import com.rpa.auth.repository.RoleRepository;
import com.rpa.auth.repository.UserRepository;
import com.rpa.auth.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 初始化权限
        initPermissions();

        // 初始化角色
        initRoles();

        // 初始化用户
        initUsers();
    }

    /**
     * 初始化权限数据
     */
    private void initPermissions() {
        if (permissionRepository.count() == 0) {
            // 系统管理权限
            createPermission("用户管理", "sys:user:manage", 2);
            createPermission("角色管理", "sys:role:manage", 2);
            createPermission("权限管理", "sys:perm:manage", 2);

            // 流程管理权限
            createPermission("流程查看", "process:view", 1);
            createPermission("流程新增", "process:add", 2);
            createPermission("流程编辑", "process:edit", 2);
            createPermission("流程删除", "process:delete", 2);

            // 机器人管理权限
            createPermission("机器人查看", "robot:view", 1);
            createPermission("机器人新增", "robot:add", 2);
            createPermission("机器人编辑", "robot:edit", 2);
            createPermission("机器人删除", "robot:delete", 2);

            // 任务管理权限
            createPermission("任务查看", "task:view", 1);
            createPermission("任务新增", "task:add", 2);
            createPermission("任务编辑", "task:edit", 2);
            createPermission("任务删除", "task:delete", 2);
            createPermission("任务执行", "task:execute", 2);

            // 执行监控权限
            createPermission("执行记录查看", "execute:view", 1);
            createPermission("日志查看", "log:view", 1);

            // 业务数据权限
            createPermission("业务数据查看", "business:view", 1);
            createPermission("业务数据导出", "business:export", 2);

            System.out.println("权限数据初始化完成");
        }
    }

    private void createPermission(String name, String key, Integer type) {
        Permission perm = new Permission();
        perm.setPermName(name);
        perm.setPermKey(key);
        perm.setPermType(type);
        perm.setParentId(0L);
        permissionRepository.save(perm);
    }

    /**
     * 初始化角色数据
     */
    private void initRoles() {
        if (roleRepository.count() == 0) {
            // 创建管理员角色
            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole.setRemark("系统管理员，拥有所有权限");
            adminRole.setStatus(1);
            roleRepository.save(adminRole);

            // 创建运维角色
            Role opsRole = new Role();
            opsRole.setRoleName("OPERATOR");
            opsRole.setRemark("运维人员，负责流程和机器人管理");
            opsRole.setStatus(1);
            roleRepository.save(opsRole);

            // 创建业务角色
            Role bizRole = new Role();
            bizRole.setRoleName("BUSINESS");
            bizRole.setRemark("业务人员，负责任务管理和执行");
            bizRole.setStatus(1);
            roleRepository.save(bizRole);

            System.out.println("角色数据初始化完成: ADMIN, OPERATOR, BUSINESS");

            // 为角色分配权限
            assignPermissionsToRoles();
        }
    }

    /**
     * 为角色分配权限
     */
    private void assignPermissionsToRoles() {
        List<Permission> allPermissions = permissionRepository.findAll();

        // 获取角色
        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equals("ADMIN"))
                .findFirst().orElse(null);
        Role opsRole = roleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equals("OPERATOR"))
                .findFirst().orElse(null);
        Role bizRole = roleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equals("BUSINESS"))
                .findFirst().orElse(null);

        // ADMIN拥有所有权限
        if (adminRole != null) {
            for (Permission perm : allPermissions) {
                assignPermissionToRole(adminRole.getRoleId(), perm.getPermId());
            }
            System.out.println("ADMIN角色权限分配完成");
        }

        // OPERATOR拥有流程、机器人、执行监控权限
        if (opsRole != null) {
            List<String> opsPerms = Arrays.asList(
                    "process:view", "process:add", "process:edit", "process:delete",
                    "robot:view", "robot:add", "robot:edit", "robot:delete",
                    "execute:view", "log:view"
            );
            for (Permission perm : allPermissions) {
                if (opsPerms.contains(perm.getPermKey())) {
                    assignPermissionToRole(opsRole.getRoleId(), perm.getPermId());
                }
            }
            System.out.println("OPERATOR角色权限分配完成");
        }

        // BUSINESS拥有任务、执行监控、业务数据权限
        if (bizRole != null) {
            List<String> bizPerms = Arrays.asList(
                    "task:view", "task:add", "task:edit", "task:delete", "task:execute",
                    "execute:view", "log:view",
                    "business:view", "business:export"
            );
            for (Permission perm : allPermissions) {
                if (bizPerms.contains(perm.getPermKey())) {
                    assignPermissionToRole(bizRole.getRoleId(), perm.getPermId());
                }
            }
            System.out.println("BUSINESS角色权限分配完成");
        }
    }

    private void assignPermissionToRole(Long roleId, Long permId) {
        RolePermission rp = new RolePermission();
        rp.setRoleId(roleId);
        rp.setPermId(permId);
        rolePermissionRepository.save(rp);
    }

    /**
     * 初始化用户数据
     */
    private void initUsers() {
        if (userRepository.count() == 0) {
            // 创建管理员用户
            User admin = createUser("admin", "admin123", "管理员", "admin@example.com", "13800138000");
            assignRoleToUser(admin.getUserId(), "ADMIN");
            System.out.println("管理员用户创建成功: admin/admin123 (角色: ADMIN)");

            // 创建运维用户
            User operator = createUser("operator", "operator123", "运维人员", "operator@example.com", "13800138001");
            assignRoleToUser(operator.getUserId(), "OPERATOR");
            System.out.println("运维用户创建成功: operator/operator123 (角色: OPERATOR)");

            // 创建业务用户
            User business = createUser("business", "business123", "业务人员", "business@example.com", "13800138002");
            assignRoleToUser(business.getUserId(), "BUSINESS");
            System.out.println("业务用户创建成功: business/business123 (角色: BUSINESS)");

            // 创建多角色用户（同时拥有运维和业务角色）
            User multiRole = createUser("multi", "multi123", "多角色用户", "multi@example.com", "13800138003");
            assignRoleToUser(multiRole.getUserId(), "OPERATOR");
            assignRoleToUser(multiRole.getUserId(), "BUSINESS");
            System.out.println("多角色用户创建成功: multi/multi123 (角色: OPERATOR, BUSINESS)");
        }
    }

    /**
     * 创建用户
     */
    private User createUser(String username, String password, String nickname, String email, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        return userRepository.save(user);
    }

    /**
     * 为用户分配角色
     */
    private void assignRoleToUser(Long userId, String roleName) {
        Role role = roleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equals(roleName))
                .findFirst()
                .orElse(null);

        if (role != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getRoleId());
            userRoleRepository.save(userRole);
        }
    }
}
