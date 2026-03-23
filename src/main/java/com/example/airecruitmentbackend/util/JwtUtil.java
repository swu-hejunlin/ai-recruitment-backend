package com.example.airecruitmentbackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 负责生成和解析JWT令牌
 */
@Component
public class JwtUtil {

    /**
     * JWT密钥（从配置文件读取）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT过期时间（从配置文件读取，单位：毫秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成密钥对象
     * 使用足够长度的密钥确保安全性
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT令牌
     *
     * @param userId 用户ID
     * @param role   用户角色
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从JWT令牌中获取Claims
     *
     * @param token JWT令牌
     * @return Claims对象，包含用户信息
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从JWT令牌中获取用户角色
     *
     * @param token JWT令牌
     * @return 用户角色
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Integer.valueOf(claims.get("role").toString());
    }

    /**
     * 验证JWT令牌是否过期
     *
     * @param token JWT令牌
     * @return true-未过期，false-已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从HttpServletRequest中获取用户ID（兼容带Bearer前缀和不带的情况）
     *
     * @param request HttpServletRequest
     * @return 用户ID
     */
    public Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return getUserIdFromToken(token);
        }
        return null;
    }
}
