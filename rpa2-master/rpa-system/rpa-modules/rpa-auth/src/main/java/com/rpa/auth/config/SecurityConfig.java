package com.rpa.auth.config;

import com.rpa.auth.filter.JwtAuthenticationFilter;
import com.rpa.auth.handler.CustomAccessDeniedHandler;
import com.rpa.auth.handler.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 禁用CSRF
            .csrf(csrf -> csrf.disable())

            // 配置会话管理为无状态
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置URL授权规则
            .authorizeHttpRequests(auth -> auth
                // 放行静态资源
                .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                .requestMatchers("/**/*.js", "/**/*.css", "/**/*.png", "/**/*.jpg", "/**/*.svg", "/**/*.woff", "/**/*.woff2", "/**/*.ttf", "/**/*.eot").permitAll()
                .requestMatchers("/assets/**").permitAll()
                // 放行登录接口
                .requestMatchers("/auth/login").permitAll()
                // 放行退出接口（无需认证）
                .requestMatchers("/auth/logout").permitAll()
                // 放行用户信息接口
                .requestMatchers("/auth/user-info").authenticated()
                // 放行头像上传接口
                .requestMatchers("/auth/avatar").authenticated()
                // 放行H2 控制台
                .requestMatchers("/h2-console/**").permitAll()
                // 管理员接口 - 需要 ADMIN 角色
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 流程管理接口 - 需要 OPERATOR 或 ADMIN 角色
                .requestMatchers("/process/**").hasAnyRole("OPERATOR", "ADMIN")
                // 机器人管理接口 - 需要 OPERATOR 或 ADMIN 角色
                .requestMatchers("/robot/**").hasAnyRole("OPERATOR", "ADMIN")
                // 任务管理接口 - 需要 BUSINESS、OPERATOR 或 ADMIN 角色
                .requestMatchers("/task/**").hasAnyRole("BUSINESS", "OPERATOR", "ADMIN")
                // 执行监控接口 - 所有登录用户可访问
                .requestMatchers("/execute/**").authenticated()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )

            // 配置异常处理
            .exceptionHandling(exception -> exception
                // 认证入口点（未认证）
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                // 访问拒绝处理器（无权限）
                .accessDeniedHandler(customAccessDeniedHandler)
            )

            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置
     * 允许前端跨域访问
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源（前端地址）
        // 生产环境应配置具体域名，如：Arrays.asList("http://localhost:3000", "https://yourdomain.com")
        configuration.setAllowedOrigins(Arrays.asList("*"));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));

        // 是否允许携带凭证（cookies）
        // 注意：如果设置为true，allowedOrigins不能为"*"，需要指定具体域名
        configuration.setAllowCredentials(false);

        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
