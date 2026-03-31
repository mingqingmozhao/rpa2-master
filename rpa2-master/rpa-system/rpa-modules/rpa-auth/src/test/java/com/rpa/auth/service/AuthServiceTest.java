package com.rpa.auth.service;

import com.rpa.auth.advice.GlobalExceptionHandler.BusinessException;
import com.rpa.auth.model.Role;
import com.rpa.auth.model.User;
import com.rpa.auth.model.UserRole;
import com.rpa.auth.repository.RoleRepository;
import com.rpa.auth.repository.UserRepository;
import com.rpa.auth.repository.UserRoleRepository;
import com.rpa.auth.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.lenient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试。
 * <p>
 * 测试策略：
 * 1. 正常登录 — 用户存在、密码正确、有角色 → 返回 token
 * 2. 用户不存在
 * 3. 密码错误
 * 4. 账号被禁用
 * 5. 用户名为空或密码为空
 * 6. 登录成功但无角色
 * 7. 登录成功且有角色
 * 8. getCurrentUser 正常返回
 * 9. getCurrentUser 用户不存在
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    /** 用于生成测试用户库中的 BCrypt 密文；matches 委托给 {@link #passwordEncoder} mock */
    private final BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder(4);

    private User dbUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        dbUser = new User();
        dbUser.setId(1L);
        dbUser.setUsername("dbadmin");
        dbUser.setPassword(realEncoder.encode("admin123"));
        dbUser.setRealName("数据库管理员");
        dbUser.setStatus(1);
        dbUser.setIsDeleted(0);

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("ADMIN");
        adminRole.setStatus(1);
        adminRole.setIsDeleted(0);

        lenient().when(passwordEncoder.matches(any(CharSequence.class), anyString()))
                .thenAnswer(inv -> realEncoder.matches(inv.getArgument(0).toString(), inv.getArgument(1)));
    }

    // ==================== login() 测试 ====================

    @Nested
    @DisplayName("login() 方法")
    class LoginTests {

        @Test
        @DisplayName("【正常】用户名和密码正确，返回 token 和用户基本信息")
        void login_Success_ReturnsTokenAndUserInfo() {
            UserRole userRole = new UserRole();
            userRole.setUserId(1L);
            userRole.setRoleId(1L);

            when(userRepository.findByUsername("dbadmin")).thenReturn(Optional.of(dbUser));
            when(userRoleRepository.findByUserId(1L)).thenReturn(List.of(userRole));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
            when(jwtUtils.generateToken(eq("dbadmin"), anyMap())).thenReturn("mock-jwt-token");

            Map<String, Object> result = authService.login("dbadmin", "admin123");

            assertNotNull(result);
            assertEquals("mock-jwt-token", result.get("token"));
            assertEquals(1L, result.get("userId"));
            assertEquals("dbadmin", result.get("username"));
            assertEquals("数据库管理员", result.get("realName"));

            // 验证 JWT claim 中包含角色信息
            @SuppressWarnings({"unchecked", "rawtypes"})
            ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(jwtUtils).generateToken(eq("dbadmin"), claimsCaptor.capture());
            assertEquals("ADMIN", claimsCaptor.getValue().get("role"));
        }

        @Test
        @DisplayName("【异常】用户不存在，抛出 BusinessException")
        void login_UserNotFound_ThrowsBusinessException() {
            when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.login("nobody", "anypass"));

            assertEquals("用户名或密码错误", ex.getMessage());
        }

        @Test
        @DisplayName("【异常】密码错误，抛出 BusinessException")
        void login_WrongPassword_ThrowsBusinessException() {
            when(userRepository.findByUsername("dbadmin")).thenReturn(Optional.of(dbUser));
            when(passwordEncoder.matches(eq("wrongpassword"), eq(dbUser.getPassword()))).thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.login("dbadmin", "wrongpassword"));

            assertEquals("用户名或密码错误", ex.getMessage());
        }

        @Test
        @DisplayName("【异常】账号被禁用（status=0），抛出 BusinessException")
        void login_AccountDisabled_ThrowsBusinessException() {
            dbUser.setStatus(0);
            when(userRepository.findByUsername("dbadmin")).thenReturn(Optional.of(dbUser));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.login("dbadmin", "admin123"));

            assertEquals("账号已被禁用", ex.getMessage());
        }

        @Test
        @DisplayName("【异常】用户名为空，抛出 BusinessException")
        void login_BlankUsername_ThrowsBusinessException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.login("", "admin123"));

            assertEquals("用户名或密码不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("【异常】密码为空，抛出 BusinessException")
        void login_BlankPassword_ThrowsBusinessException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.login("admin", "   "));

            assertEquals("用户名或密码不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("【正常】用户存在但没有任何角色，使用默认角色 USER")
        void login_NoRoleAssigned_DefaultsToUSER() {
            when(userRepository.findByUsername("dbadmin")).thenReturn(Optional.of(dbUser));
            when(userRoleRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
            when(jwtUtils.generateToken(eq("dbadmin"), anyMap())).thenReturn("token-with-default-role");

            authService.login("dbadmin", "admin123");

            @SuppressWarnings({"unchecked", "rawtypes"})
            ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(jwtUtils).generateToken(eq("dbadmin"), claimsCaptor.capture());
            assertEquals("USER", claimsCaptor.getValue().get("role"));
        }

        @Test
        @DisplayName("【正常】用户存在、有角色（OPERATOR），JWT claim 中写入对应角色")
        void login_WithOperatorRole_JwtContainsOPERATOR() {
            Role operatorRole = new Role();
            operatorRole.setId(2L);
            operatorRole.setRoleName("OPERATOR");

            UserRole userRole = new UserRole();
            userRole.setUserId(1L);
            userRole.setRoleId(2L);

            when(userRepository.findByUsername("dbadmin")).thenReturn(Optional.of(dbUser));
            when(userRoleRepository.findByUserId(1L)).thenReturn(List.of(userRole));
            when(roleRepository.findById(2L)).thenReturn(Optional.of(operatorRole));
            when(jwtUtils.generateToken(eq("dbadmin"), anyMap())).thenReturn("token-op");

            authService.login("dbadmin", "admin123");

            @SuppressWarnings({"unchecked", "rawtypes"})
            ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(jwtUtils).generateToken(eq("dbadmin"), claimsCaptor.capture());
            assertEquals("OPERATOR", claimsCaptor.getValue().get("role"));
        }

        @Test
        @DisplayName("【正常】用户名前后有空格，自动 trim 后查询")
        void login_UsernameWithSpaces_Trimmed() {
            when(userRepository.findByUsername("dbadmin")).thenReturn(Optional.of(dbUser));
            when(userRoleRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
            when(jwtUtils.generateToken(eq("dbadmin"), anyMap())).thenReturn("token");

            authService.login("  dbadmin  ", "admin123");

            verify(userRepository).findByUsername("dbadmin");
        }
    }

    // ==================== getCurrentUser() 测试 ====================

    @Nested
    @DisplayName("getCurrentUser() 方法")
    class GetCurrentUserTests {

        @Test
        @DisplayName("【正常】用户存在，返回完整 User 对象")
        void getCurrentUser_Exists_ReturnsUser() {
            when(userRepository.findByUsername("dbadmin")).thenReturn(Optional.of(dbUser));

            User result = authService.getCurrentUser("dbadmin");

            assertNotNull(result);
            assertEquals("dbadmin", result.getUsername());
            assertEquals("数据库管理员", result.getRealName());
        }

        @Test
        @DisplayName("【异常】用户不存在，抛出 BusinessException")
        void getCurrentUser_NotExists_ThrowsBusinessException() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.getCurrentUser("ghost"));

            assertEquals("用户不存在", ex.getMessage());
        }

        @Test
        @DisplayName("【异常】用户名为空，抛出 BusinessException")
        void getCurrentUser_BlankUsername_ThrowsBusinessException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.getCurrentUser("   "));

            assertEquals("用户名不能为空", ex.getMessage());
        }
    }
}
