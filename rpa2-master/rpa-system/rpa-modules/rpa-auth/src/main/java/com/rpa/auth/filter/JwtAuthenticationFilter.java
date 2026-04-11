package com.rpa.auth.filter;

import com.rpa.auth.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        String method = request.getMethod();
        String path = request.getRequestURI();
        logger.info("JwtAuthenticationFilter - Processing request: " + method + " " + path);
        logger.info("JwtAuthenticationFilter - ContextPath: " + request.getContextPath());
        logger.info("JwtAuthenticationFilter - ServletPath: " + request.getServletPath());
        
        // 跳过 WebSocket 请求的认证
        if (path.contains("/ws-task") || path.contains("/ws-task-stomp") || path.contains("/ws")) {
            logger.info("JwtAuthenticationFilter - >>>>> Skipping WebSocket request: " + path);
            chain.doFilter(request, response);
            return;
        }
        
        String header = request.getHeader("Authorization");
        String token = null;
        String username = null;
        
        logger.debug("JwtAuthenticationFilter - Authorization header: " + (header != null ? "present" : "null"));
        
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            logger.debug("JwtAuthenticationFilter - Token extracted from Authorization header");
        } else {
            // 尝试从 Cookie 中读取 token
            logger.debug("JwtAuthenticationFilter - Trying to extract token from Cookie");
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("rpa_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        logger.debug("JwtAuthenticationFilter - Token extracted from Cookie");
                        break;
                    }
                }
            }
        }
        
        if (token == null) {
            logger.debug("JwtAuthenticationFilter - No token found, continuing without authentication");
            chain.doFilter(request, response);
            return;
        }
        
        try {
            if (token != null && !token.isBlank()) {
                username = jwtUtils.getUsernameFromToken(token);
                logger.debug("JwtAuthenticationFilter - Username extracted from token: " + username);
            }
        } catch (Exception e) {
            // token 无效或解析失败，继续执行（不认证）
            logger.warn("JwtAuthenticationFilter - Failed to parse token: " + e.getMessage(), e);
            username = null;
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                logger.debug("JwtAuthenticationFilter - Checking token expiration for user: " + username);
                if (!jwtUtils.isTokenExpired(token)) {
                    String role = jwtUtils.getRoleFromToken(token);
                    logger.debug("JwtAuthenticationFilter - Role from token: " + role);
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (role != null && !role.isBlank()) {
                        String r = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        authorities.add(new SimpleGrantedAuthority(r));
                    }
                    UserDetails userDetails = new User(username, "", authorities);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("JwtAuthenticationFilter - Authentication successful for user: " + username);
                } else {
                    logger.warn("JwtAuthenticationFilter - Token expired for user: " + username);
                }
            } catch (Exception e) {
                // 认证失败，继续执行
                logger.error("JwtAuthenticationFilter - Authentication failed: " + e.getMessage(), e);
            }
        }
        
        chain.doFilter(request, response);
    }
}
