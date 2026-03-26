package com.cqf.auth.controller;

import com.cqf.auth.model.vo.UserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqf.common.result.Result;
import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.service.ISysUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 */
@RestController
@RequiredArgsConstructor
public class SysUserController {
    private final ISysUserService service;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/ucenter/info")
    public Result<UserInfo> info(Authentication authentication) {
        String username = authentication.getName();
        SysUser user = service.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            return Result.error("用户不存在");
        }

        UserInfo userInfo = UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .deptId(user.getDeptId())
                .build();
        return Result.success(userInfo);
    }
}
