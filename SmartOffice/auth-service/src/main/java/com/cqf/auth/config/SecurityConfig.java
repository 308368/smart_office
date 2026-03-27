package com.cqf.auth.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.cqf.auth.service.impl.CustomUserDetailsService;
import com.cqf.common.enums.LoginResultEnum;
import com.cqf.common.result.LoginResult;
import com.cqf.common.result.Result;
import com.cqf.common.utils.JwtUtil;
import jakarta.servlet.ServletException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Spring Security 配置
 * 配置登录路径为 /ucenter/login
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 自定义认证 Provider
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(csrf -> csrf.disable())
                // 配置CORS
                .cors(cors -> {})
                // 不使用Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置认证 Provider
                .authenticationProvider(daoAuthenticationProvider())
                // 配置表单登录
                .formLogin(form -> form
                        // 配置登录路径
                        .loginProcessingUrl("/ucenter/login")
                        // 登录成功处理器（自定义）
                        .successHandler((HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) -> {
                            // 认证成功后的处理
                            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                            String userDetail = userDetails.getUsername();
                            LoginResult loginResult = JSONUtil.toBean(userDetail, LoginResult.class);
                            Result<LoginResult> success = Result.success(loginResult);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(success));
                        })
                        // 登录失败处理器（自定义）
                        .failureHandler((request, response, exception) -> {
                            // 认证失败后的处理
                            response.setContentType("application/json;charset=UTF-8");
                            Result<Object> failure = Result.error(LoginResultEnum.USERNAME_OR_PASSWORD_ERROR.getMessage());
                            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(failure));
                        })
                )
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 登录接口放行
                        .requestMatchers("/ucenter/login", "/ucenter/register").permitAll()
                        // OPTIONS 请求放行（CORS预检）
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                // 自定义未认证时的返回内容（不重定向）
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
                        })
                );

        return http.build();
    }

}
