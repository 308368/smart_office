package com.cqf.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT令牌
 */
public class JwtUtil {

    /**
     * 签名密钥（至少32字节，256位）
     */
    public static final String SIGNING_KEY = "SmartOffice2026SecretKeyForJWT256Bit";

    /**
     * 生成JWT令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT令牌字符串
     */
    public static String createToken(Long userId, String username) {
        return createToken(userId, username, null);
    }

    /**
     * 生成JWT令牌（带权限）
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param permissions 权限列表
     * @return JWT令牌字符串
     */
    public static String createToken(Long userId, String username, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        if (permissions != null && !permissions.isEmpty()) {
            claims.put("permissions", permissions);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24小时
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return Claims 包含用户信息
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取权限列表
     */
    @SuppressWarnings("unchecked")
    public static List<String> getPermissions(String token) {
        Claims claims = parseToken(token);
        return claims.get("permissions", List.class);
    }

    /**
     * 验证token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取签名密钥
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
