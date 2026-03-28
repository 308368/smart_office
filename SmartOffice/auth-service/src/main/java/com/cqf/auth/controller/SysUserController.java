package com.cqf.auth.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.auth.model.dto.UserQueryParam;
import com.cqf.auth.model.vo.UserInfo;
import com.cqf.auth.model.vo.UserVo;
import com.cqf.common.domain.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqf.common.result.Result;


import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.service.ISysUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>
 * 用户表 前端控制器
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class SysUserController {
    private final ISysUserService service;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<UserVo>> list(UserQueryParam userQueryParam) {
        Page<SysUser> page = new Page<>(userQueryParam.getCurrent(), userQueryParam.getSize());
        LambdaQueryWrapper<SysUser> eq = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.isNotBlank(userQueryParam.getUsername()), SysUser::getUsername, userQueryParam.getUsername())
                .like(StringUtils.isNotBlank(userQueryParam.getPhone()), SysUser::getPhone, userQueryParam.getPhone())
                .eq(userQueryParam.getDeptId() != null, SysUser::getDeptId, userQueryParam.getDeptId())
                .eq(userQueryParam.getStatus() != null, SysUser::getStatus, userQueryParam.getStatus());
        Page<SysUser> sysUserPage = service.page(page, eq);
        PageResult<UserVo> userVoPageResult = new PageResult<>();
        userVoPageResult.setCurrent((int) sysUserPage.getCurrent());
        userVoPageResult.setSize((int) sysUserPage.getSize());
        userVoPageResult.setTotal((int) sysUserPage.getTotal());
        userVoPageResult.setPages((int) sysUserPage.getPages());
        List<UserVo> records = sysUserPage.getRecords().stream().map(sysUser -> {
            UserVo userVo = BeanUtil.copyProperties(sysUser, UserVo.class);
            return userVo;
        }).collect(Collectors.toList());
        userVoPageResult.setRecords(records);
        return Result.success(userVoPageResult);
    }
    
}
