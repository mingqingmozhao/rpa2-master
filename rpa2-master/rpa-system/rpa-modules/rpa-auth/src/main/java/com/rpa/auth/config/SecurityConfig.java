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
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 放行根路径和静态资源
                .requestMatchers("/", "/index.html", "/favicon.ico", "/vite.svg").permitAll()
                .requestMatchers("/assets/**", "/css/**", "/js/**", "/img/**").permitAll()
                // 放行上传文件（头像等）
                .requestMatchers("/uploads/**").permitAll()
                // 放行登录认证接口
                .requestMatchers("/auth/login", "/auth/logout").permitAll()
                // 放行所有前端 SPA 路由（由 Vue Router 处理，Spring 只负责返回 index.html）
                .requestMatchers(
                    "/login", "/dashboard", "/user-info",
                    "/user-management", "/role-management", "/resource-management",
                    "/task", "/execution", "/robot", "/process",
                    "/data-collection", "/data-parsing", "/data-processing", "/data-query"
                ).permitAll()
                // 放行 Swagger 文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 放行 Chrome DevTools
                .requestMatchers("/.well-known/**").permitAll()
                // 管理员接口需要 ADMIN 角色
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 其他接口需要认证
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
                "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
