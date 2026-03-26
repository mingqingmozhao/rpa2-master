package com.rpa.auth.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具类（适配 Java 17 + Spring Boot 3.x）
 * 替换了过时的 DatatypeConverter，使用 Java 内置 Base64 和新版 JJWT API
 */
@Component
public class JwtUtils {

    // 从配置文件读取密钥（建议至少32位，避免密钥过短报错）
    @Value("${jwt.secret}")
    private String secretKey;

    // 从配置文件读取过期时间（单位：毫秒）
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * 生成 Token（核心方法，无 DatatypeConverter 依赖）
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * 生成带自定义Claims的Token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // 生成安全密钥（新版 JJWT 推荐使用 Keys.hmacShaKeyFor，自动处理 Base64）
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256) // 明确指定算法
                .compact();
    }

    /**
     * 从 Token 中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 通用方法：提取 Token 中的 Claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 验证 Token 是否有效（匹配用户名 + 未过期）
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * 检查 Token 是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 提取过期时间
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 解析 Token 并获取所有 Claims（处理签名验证）
     */
    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Http 请求头中提取 Token（默认 Bearer 前缀）
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 去掉 "Bearer " 前缀
        }
        return null;
    }
}
