package com.cqf.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;

/**
 * JWT令牌配置类
 * 用于配置JWT解码器，验证令牌有效性
 */
@Configuration
public class TokenConfig {

    /**
     * JWT签名密钥
     * 用于解密和验证JWT令牌（需与auth-service保持一致，至少32字节）
     */
    String SIGNING_KEY = "SmartOffice2026SecretKeyForJWT256Bit";

    /**
     * 创建JWT解码器Bean
     * 使用NimbusJwtDecoder进行JWT令牌的解析和验证
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(
            new SecretKeySpec(SIGNING_KEY.getBytes(), "HmacSHA256")
        ).build();
    }
}
