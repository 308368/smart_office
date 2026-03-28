package com.cqf.auth.service.impl;

import com.cqf.auth.model.po.SysRole;
import com.cqf.auth.mapper.SysRoleMapper;
import com.cqf.auth.service.ISysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-26
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    private final SysRoleMapper roleMapper;

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return roleMapper.getRolesByUserId(userId);
    }
}
