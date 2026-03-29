package com.cqf.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqf.auth.mapper.SysUserMapper;
import com.cqf.auth.mapper.SysUserRoleMapper;
import com.cqf.auth.model.dto.UserDTO;
import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.model.po.SysUserRole;
import com.cqf.auth.service.ISysUserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final PasswordEncoder passwordEncoder;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper userRoleMapper;
    @Lazy
    @Resource
    private ISysUserService currentService;
    @Override
    @Transactional
    public void saveUser(UserDTO dto) {

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        sysUserMapper.insert(user);
        currentService.updateRolesByUserId(dto, user.getId());
    }

    @Override
    @Transactional
    public void updateUser(UserDTO dto) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        currentService.updateRolesByUserId(dto, user.getId());
        sysUserMapper.updateById(user);
    }
    @Override
    public void updateRolesByUserId(UserDTO dto, Long userId) {
        List<Long> roleIds = dto.getRoleIds();
        ArrayList<SysUserRole> sysUserRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            sysUserRoles.add(sysUserRole);
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        userRoleMapper.insertBatch(sysUserRoles);
    }
    // 登录逻辑已由 Spring Security 处理
}
