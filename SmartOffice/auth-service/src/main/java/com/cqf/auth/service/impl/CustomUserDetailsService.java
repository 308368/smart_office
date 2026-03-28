package com.cqf.auth.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.auth.mapper.*;
import com.cqf.auth.model.dto.LoginDTO;
import com.cqf.auth.model.po.SysRole;
import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.service.AuthService;
import com.cqf.common.result.LoginResult;
import com.cqf.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 * 从数据库加载用户信息和权限
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final ApplicationContext applicationContext;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final RedisTemplate redisTemplate;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        LoginDTO loginDto = null;
        try {
            loginDto = JSONUtil.toBean(s, LoginDTO.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }
        String beanName = loginDto.getLoginType() + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        SysUser sysUser = authService.execute(loginDto);

        //封装用户信息
        return getUserPrincipal(sysUser);

    }

    public UserDetails getUserPrincipal(SysUser sysUser) {
        /*// 查询用户角色（这里简化处理，实际应该从sys_role、sys_user_role查询）
        // 默认给所有用户 USER 角色
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 如果是 admin 给管理员角色
        if ("admin".equals(sysUser.getUsername())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }*/
        List<String> menus = menuMapper.getPermissionByUserId(sysUser.getId());
        // 过滤掉 null 和空字符串，并确保每个权限标识都有效
        String[] authorities = menus.stream()
                .filter(permission -> permission != null && !permission.trim().isEmpty())
                .toArray(String[]::new);
        String[] roles = roleMapper.getRolesByUserId(sysUser.getId()).stream().map(SysRole::getCode).toArray(String[]::new);
        // 将权限列表传入 token
        LoginResult loginResult = LoginResult.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .nickname(sysUser.getNickname())
                .token(JwtUtil.createToken(sysUser.getId(), sysUser.getUsername(), menus))
                .roles(roles)
                .build();
        //将用户信息存入reids
        String userKey = "smart:user:" + sysUser.getId();
//        loginResult.setToken(null);
        redisTemplate.opsForValue().set(userKey, loginResult, 24, TimeUnit.HOURS);
        // 构建 UserDetails
        return User.builder()
                .username(JSONUtil.toJsonStr(loginResult))
                .password(sysUser.getPassword())
                .authorities( authorities)
                .accountLocked(sysUser.getStatus() == 0) // 0禁用
                .disabled(false)
                .build();
    }
}
