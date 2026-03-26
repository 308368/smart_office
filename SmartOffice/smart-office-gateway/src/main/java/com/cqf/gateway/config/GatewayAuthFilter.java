package com.cqf.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 网关认证过滤器
 * 用于验证JWT令牌的合法性
 */
@Component
@Slf4j
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    /**
     * 白名单路径列表
     */
    private static List<String> whitelist = null;

    static {
        //加载白名单
        try (
                InputStream resourceAsStream = GatewayAuthFilter.class.getResourceAsStream("/security-whitelist.properties");
        ) {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            Set<String> strings = properties.stringPropertyNames();
            whitelist= new ArrayList<>(strings);

        } catch (Exception e) {
            log.error("加载/security-whitelist.properties出错:{}",e.getMessage());
            e.printStackTrace();
        }


    }

    @Autowired
    private JwtDecoder jwtDecoder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUrl = exchange.getRequest().getPath().value();
        log.info("网关接收到请求: {}", requestUrl);
        AntPathMatcher pathMatcher = new AntPathMatcher();
        //白名单放行
        for (String url : whitelist) {
            if (pathMatcher.match(url, requestUrl)) {
                return chain.filter(exchange);
            }
        }

        //检查token是否存在
        String token = getToken(exchange);
        if (!StringUtils.hasText(token)) {
            return buildReturnMono("没有认证", exchange);
        }

        //使用新的JwtDecoder验证令牌
        try {
            Jwt jwt = jwtDecoder.decode(token);
            log.info("JWT解析成功, subject: {}", jwt.getSubject());
            return chain.filter(exchange);
        } catch (Exception e) {
            log.info("认证令牌无效: {}, 错误: {}", token, e.getMessage());
            return buildReturnMono("认证令牌无效", exchange);
        }
    }

    /**
     * 获取token
     */
    private String getToken(ServerWebExchange exchange) {
        String tokenStr = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(tokenStr)) {
            return null;
        }
        String token = tokenStr.split(" ")[1];
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return token;
    }

    private Mono<Void> buildReturnMono(String error, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        String jsonString = "{\"error\":\"" + error + "\"}";
        byte[] bits = jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
