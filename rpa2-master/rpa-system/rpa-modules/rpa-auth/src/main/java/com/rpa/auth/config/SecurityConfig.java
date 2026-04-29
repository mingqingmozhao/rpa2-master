package com.rpa.auth.config;

import com.rpa.auth.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    // 放行所有 API 接口的 CSRF 检查（使用 JWT 认证，不需要 CSRF）
                    new AntPathRequestMatcher("/api/**"),
                    new AntPathRequestMatcher("/api/ai/**"),
                    new AntPathRequestMatcher("/auth/**"),
                    new AntPathRequestMatcher("/task/**"),
                    new AntPathRequestMatcher("/robot/**"),
                    new AntPathRequestMatcher("/process/**"),
                    new AntPathRequestMatcher("/execution/**"),
                    new AntPathRequestMatcher("/data/**"),
                    // 放行 AI 接口
                    new AntPathRequestMatcher("/ai/**"),
                    // 放行批量执行和定时计划接口
                    new AntPathRequestMatcher("/batch/**"),
                    new AntPathRequestMatcher("/schedule/**"),
                    // 放行 WebSocket 路径
                    new AntPathRequestMatcher("/ws-task/**"),
                    new AntPathRequestMatcher("/ws-task-stomp/**"),
                    new AntPathRequestMatcher("/ws/**"),
                    // 放行静态资源
                    new AntPathRequestMatcher("/favicon.ico"),
                    new AntPathRequestMatcher("/*.html"),
                    new AntPathRequestMatcher("/*.css"),
                    new AntPathRequestMatcher("/*.js"),
                    new AntPathRequestMatcher("/*.png"),
                    new AntPathRequestMatcher("/*.jpg"),
                    new AntPathRequestMatcher("/*.jpeg"),
                    new AntPathRequestMatcher("/*.gif"),
                    new AntPathRequestMatcher("/*.svg"),
                    new AntPathRequestMatcher("/*.ico")
                )
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 放行 AI 接口（优先匹配）
                .requestMatchers("/ai/**", "/api/ai/**").permitAll()
                // 放行数据接口
                .requestMatchers("/data/**").permitAll()
                // 放行 Dashboard API 接口
                .requestMatchers("/api/task/stats", "/api/task/status-stats", "/api/task/trend", "/api/task/recent-tasks").permitAll()
                .requestMatchers("/api/robot/stats", "/api/process/stats", "/api/data/stats").permitAll()
                // 放行静态资源（使用具体扩展名，避免使用 ** 通配符）
                .requestMatchers("/*.html", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.svg", "/*.ico").permitAll()
                .requestMatchers("/", "/index.html", "/favicon.ico", "/vite.svg").permitAll()
                .requestMatchers("/assets/**", "/css/**", "/js/**", "/img/**", "/uploads/**").permitAll()
                .requestMatchers("/test-robot-websocket.html").permitAll()
                // 放行认证接口
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .requestMatchers("/error").permitAll()
                // 放行 WebSocket 端点
                .requestMatchers("/ws-task/**", "/ws-task-stomp/**", "/ws/**").permitAll()
                // 放行监控和测试接口
                .requestMatchers("/api/websocket/**", "/api/task-queue/**").permitAll()
                // 放行批量执行和定时计划接口
                .requestMatchers("/batch/**", "/schedule/**").permitAll()
                // 放行机器人客户端上报接口（无需JWT认证）
                .requestMatchers("/task/execution/robot/**").permitAll()
                .requestMatchers("/task/execution/save-invoices").permitAll()
                // 放行所有前端路由（用于 SPA）
                .requestMatchers("/{path:^(?!api|auth|ws-task|ws-task-stomp|ws).*$}/**").permitAll()
                // 明确放行 favicon.ico
                .requestMatchers("/favicon.ico").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
