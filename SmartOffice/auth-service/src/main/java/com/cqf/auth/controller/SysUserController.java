package com.cqf.auth.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.auth.mapper.SysRoleMenuMapper;
import com.cqf.auth.mapper.SysUserRoleMapper;
import com.cqf.auth.model.dto.UserDTO;
import com.cqf.auth.model.dto.UserQueryParam;
import com.cqf.auth.model.po.SysDept;
import com.cqf.auth.model.po.SysUserRole;
import com.cqf.auth.model.vo.UserInfo;
import com.cqf.auth.model.vo.UserVo;
import com.cqf.auth.service.ISysDeptService;
import com.cqf.common.domain.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.cqf.common.result.Result;


import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.service.ISysUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final ISysUserService sysUserService;
    private final ISysDeptService deptService;
    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper sysRoleMenuMapper;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<UserVo>> list(UserQueryParam userQueryParam) {
        Page<SysUser> page = new Page<>(userQueryParam.getCurrent(), userQueryParam.getSize());
        LambdaQueryWrapper<SysUser> eq = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.isNotBlank(userQueryParam.getUsername()), SysUser::getUsername, userQueryParam.getUsername())
                .like(StringUtils.isNotBlank(userQueryParam.getPhone()), SysUser::getPhone, userQueryParam.getPhone())
                .eq(userQueryParam.getDeptId() != null, SysUser::getDeptId, userQueryParam.getDeptId())
                .eq(userQueryParam.getStatus() != null, SysUser::getStatus, userQueryParam.getStatus());
        Page<SysUser> sysUserPage = sysUserService.page(page, eq);
        PageResult<UserVo> userVoPageResult = new PageResult<>();
        userVoPageResult.setCurrent((int) sysUserPage.getCurrent());
        userVoPageResult.setSize((int) sysUserPage.getSize());
        userVoPageResult.setTotal((int) sysUserPage.getTotal());
        userVoPageResult.setPages((int) sysUserPage.getPages());
        List<UserVo> records = sysUserPage.getRecords().stream().map(sysUser -> {
            UserVo userVo = BeanUtil.copyProperties(sysUser, UserVo.class);
            List<SysUserRole> sysUserRoles = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getId()));
            userVo.setRoleIds(sysUserRoles.stream().map(SysUserRole::getRoleId).toList());
            return userVo;
        }).collect(Collectors.toList());
        userVoPageResult.setRecords(records);
        return Result.success(userVoPageResult);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<UserVo> getById(@PathVariable Long id) {
        SysUser sysUser = sysUserService.getById(id);
        if (sysUser == null)Result.error("用户不存在");
        UserVo userVo = BeanUtil.copyProperties(sysUser, UserVo.class);
        SysDept sysDept = deptService.getById(sysUser.getDeptId());
        if (sysDept != null)userVo.setDeptName(sysDept.getName());
        List<SysUserRole> sysUserRoles = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getId()));
        userVo.setRoleIds(sysUserRoles.stream().map(SysUserRole::getRoleId).toList());
        return Result.success(userVo);
    }
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Void> add(@RequestBody UserDTO dto) {
        sysUserService.saveUser(dto);
        return Result.success();
    }



    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> update(@RequestBody UserDTO dto) {
        sysUserService.updateUser(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:remove')")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }

    @PutMapping("/resetPwd/{id}")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    public Result<Void> resetPwd(@PathVariable Long id) {
        sysUserService.lambdaUpdate()
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, passwordEncoder.encode("123456"))
                .update();
        return Result.success();
    }
    
}
