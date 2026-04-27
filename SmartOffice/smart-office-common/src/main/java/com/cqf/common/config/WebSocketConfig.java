package com.cqf.common.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.crypto.SecretKey;
import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
@ConditionalOnClass({EnableWebSocketMessageBroker.class, WebSocketMessageBrokerConfigurer.class})
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private String jwtSecret = "SmartOffice2026SecretKeyForJWT256Bit";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 从 header 中获取 token
                    String token = accessor.getFirstNativeHeader("token");
                    System.out.println("=== WebSocket CONNECT，token: " + token);
                    if (token != null && !token.isEmpty()) {
                        try {
                            // 将密钥字符串转换为 SecretKey
                            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
                            // 解析 token 获取用户名
                            Claims claims = Jwts.parser()
                                    .verifyWith(key)
                                    .build()
                                    .parseSignedClaims(token)
                                    .getPayload();
                            String username = claims.getSubject();
                            System.out.println("=== WebSocket 解析用户: " + username);

                            // 创建认证信息并绑定到 session
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    username, null, Collections.emptyList());
                            accessor.setUser(auth);
                        } catch (Exception e) {
                            System.out.println("=== WebSocket token 解析失败: " + e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}
