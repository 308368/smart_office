package com.cqf.auth.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.auth.model.po.SysDept;
import com.cqf.auth.model.vo.UserInfo;
import com.cqf.auth.service.ISysDeptService;
import com.cqf.common.result.LoginResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqf.common.result.Result;
import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.service.ISysUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import java.util.Collection;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 */
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final ISysUserService service;
    private final RedisTemplate redisTemplate;
    private final ISysDeptService deptService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/ucenter/info")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public Result<UserInfo> info(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        SysUser user = service.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            return Result.error("用户不存在");
        }
        LoginResult loginResult=null;
        try {
            Object o =redisTemplate.opsForValue().get("smart:user:" + user.getId());
            JSON sysUserJson = JSONUtil.parse(o);
            loginResult = JSONUtil.toBean((JSONObject) sysUserJson, LoginResult.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("类型转换异常");
        }
        if (loginResult==null)return Result.error("用户登录过期");
        Long deptId = user.getDeptId();
        SysDept sysDept = deptService.lambdaQuery().eq(SysDept::getId, deptId).one();
        UserInfo userInfo = UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .deptId(deptId)
                .deptName(sysDept.getName())
                .permissions(authorities.stream().map(GrantedAuthority::getAuthority).toArray(String[]::new))
                .roles(loginResult.getRoles())
                .build();
        return Result.success(userInfo);
    }
}
