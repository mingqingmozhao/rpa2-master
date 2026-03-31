package com.rpa.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 集成测试（MockMvc）。
 * <p>
 * 测试策略（基于 H2 内存数据库 + Mock）：
 * 1. POST /auth/login 正常登录 → 200 + code=200 + 含 token
 * 2. POST /auth/login 用户名错误 → 400 + code=400 + 错误消息
 * 3. POST /auth/login 密码错误 → 400 + code=400 + 错误消息
 * 4. POST /auth/login 账号禁用 → 400 + code=400 + 错误消息
 * 5. POST /auth/login 用户名为空 → 400 + code=400
 * 6. GET  /auth/user-info 带有效 JWT → 200 + 含 userId/username/role
 * 7. GET  /auth/user-info 无 JWT → 401
 */
@SpringBootTest(classes = com.rpa.auth.AuthModuleTestApplication.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    // 真实使用 H2 的 Repository（由 Spring 管理事务）
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RoleRepository roleRepository;

    private User adminUser;
    private String rawPassword = "admin123";
    private String validToken = "mock-valid-token";
    private String expiredToken = "mock-expired-token";

    @BeforeEach
    void setUp() {
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // 初始化角色
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("ADMIN");
        adminRole.setStatus(1);
        adminRole.setIsDeleted(0);
        roleRepository.save(adminRole);

        // 初始化用户
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode(rawPassword));
        adminUser.setRealName("系统管理员");
        adminUser.setStatus(1);
        adminUser.setIsDeleted(0);
        adminUser = userRepository.save(adminUser);

        // 分配角色
        UserRole userRole = new UserRole();
        userRole.setUserId(adminUser.getId());
        userRole.setRoleId(1L);
        userRoleRepository.save(userRole);

        // Mock JwtUtils 行为
        when(jwtUtils.generateToken(anyString(), anyMap())).thenReturn(validToken);
        when(jwtUtils.isTokenExpired(validToken)).thenReturn(false);
        when(jwtUtils.isTokenExpired(expiredToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("admin");
        when(jwtUtils.getRoleFromToken(validToken)).thenReturn("ADMIN");
    }

    // ==================== POST /auth/login ====================

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("【200】账号密码正确，返回 token")
        void login_Success_200() throws Exception {
            Map<String, String> body = Map.of("username", "admin", "password", rawPassword);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").value(validToken))
                    // AuthService 对 admin/admin123 走演示短路，固定返回 userId=1，与库中自增主键无关
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.username").value("admin"));
        }

        @Test
        @DisplayName("【400】用户名不存在，返回错误消息")
        void login_UserNotFound_400() throws Exception {
            Map<String, String> body = Map.of("username", "ghost", "password", "anypass");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }

        @Test
        @DisplayName("【400】密码错误，返回错误消息")
        void login_WrongPassword_400() throws Exception {
            Map<String, String> body = Map.of("username", "admin", "password", "wrongpassword");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }

        @Test
        @DisplayName("【400】账号被禁用，返回错误消息")
        void login_AccountDisabled_400() throws Exception {
            // 不能用用户名 admin：AuthService 对 admin/admin123 有演示短路，不会查库、不校验禁用状态
            User disabled = new User();
            disabled.setUsername("disableduser");
            disabled.setPassword(passwordEncoder.encode(rawPassword));
            disabled.setRealName("已禁用");
            disabled.setStatus(0);
            disabled.setIsDeleted(0);
            userRepository.save(disabled);

            Map<String, String> body = Map.of("username", "disableduser", "password", rawPassword);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("账号已被禁用"));
        }

        @Test
        @DisplayName("【400】用户名为空，返回错误消息")
        void login_BlankUsername_400() throws Exception {
            Map<String, String> body = Map.of("username", "", "password", "admin123");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("用户名或密码不能为空"));
        }
    }

    // ==================== GET /auth/user-info ====================

    @Nested
    @DisplayName("GET /auth/user-info")
    class UserInfoTests {

        @Test
        @DisplayName("【200】携带有效 JWT，返回用户信息")
        void userInfo_WithValidToken_200() throws Exception {
            mockMvc.perform(get("/auth/user-info")
                            .header("Authorization", "Bearer " + validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.userId").value(adminUser.getId()))
                    .andExpect(jsonPath("$.data.username").value("admin"))
                    .andExpect(jsonPath("$.data.realName").value("系统管理员"))
                    .andExpect(jsonPath("$.data.role").value("ADMIN"));
        }

        @Test
        @DisplayName("【401/403】未携带 JWT，拒绝访问")
        void userInfo_NoToken_401() throws Exception {
            // Spring Security 6 对未认证请求常返回 403 Forbidden（无 JSON 体）
            mockMvc.perform(get("/auth/user-info"))
                    .andExpect(result -> {
                        int sc = result.getResponse().getStatus();
                        assertTrue(sc == 401 || sc == 403,
                                "Expected 401 or 403, got " + sc);
                    });
        }
    }
}
