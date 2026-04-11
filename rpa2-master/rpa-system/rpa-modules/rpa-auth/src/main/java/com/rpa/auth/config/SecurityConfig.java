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
        // 定义 WebSocket 路径
        List<String> websocketPaths = Arrays.asList(
            "/ws-task/**",
            "/ws-task-stomp/**",
            "/ws/**"
        );
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    // 放行所有 API 接口的 CSRF 检查（使用 JWT 认证，不需要 CSRF）
                    new AntPathRequestMatcher("/api/**"),
                    new AntPathRequestMatcher("/auth/**"),
                    new AntPathRequestMatcher("/task/**"),
                    new AntPathRequestMatcher("/robot/**"),
                    new AntPathRequestMatcher("/process/**"),
                    new AntPathRequestMatcher("/execution/**"),
                    new AntPathRequestMatcher("/data/**"),
                    // 放行 WebSocket 路径
                    new AntPathRequestMatcher("/ws-task/**"),
                    new AntPathRequestMatcher("/ws-task-stomp/**"),
                    new AntPathRequestMatcher("/ws/**")
                )
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 放行静态资源
                .requestMatchers("/", "/index.html", "/favicon.ico", "/vite.svg").permitAll()
                .requestMatchers("/assets/**", "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                // 放行认证接口
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .requestMatchers("/error").permitAll()
                // 放行 WebSocket 端点
                .requestMatchers("/ws-task/**", "/ws-task-stomp/**", "/ws/**").permitAll()
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
