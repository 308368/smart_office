package com.cqf.office.config;

import com.cqf.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * 从 token 中解析用户信息并设置到 Spring Security
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 获取请求头中的 token
        String authHeader = request.getHeader("Authorization");
        log.info("JWT Filter 执行, authHeader: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // 从 token 中解析用户信息
                String username = JwtUtil.getUsername(token);
                Long userId = JwtUtil.getUserId(token);
                List<String> permissions = JwtUtil.getPermissions(token);

                if (username != null && permissions != null) {
                    log.info("token: {}, username: {}, userId: {}, permissions: {}", token, username, userId, permissions);
                    // 转换为 Spring Security 权限对象，过滤空字符串
                    List<SimpleGrantedAuthority> authorities = permissions.stream()
                            .filter(p -> p != null && !p.isEmpty())
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // 设置到 SecurityContext
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("JWT 认证设置成功, username: {}, authorities: {}", username, authorities);
                }

            } catch (Exception e) {
                // token 无效，继续过滤链
            }
        }

        filterChain.doFilter(request, response);
    }
}