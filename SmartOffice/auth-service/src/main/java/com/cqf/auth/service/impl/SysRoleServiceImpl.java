package com.cqf.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.auth.mapper.SysMenuMapper;
import com.cqf.auth.mapper.SysRoleMenuMapper;
import com.cqf.auth.model.po.SysMenu;
import com.cqf.auth.model.po.SysRole;
import com.cqf.auth.mapper.SysRoleMapper;
import com.cqf.auth.model.po.SysRoleMenu;
import com.cqf.auth.model.vo.SysMenuVo;
import com.cqf.auth.service.ISysMenuService;
import com.cqf.auth.service.ISysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper  roleMenuMapper;

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return roleMapper.getRolesByUserId(userId);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long id) {
        List<SysMenu> menus = menuMapper.getMenuByRoleIds(Collections.singletonList(id));
        return menus.stream().map(SysMenu::getId).toList();
    }

    @Override
    @Transactional
    public void assignMenus(Long id, List<Long> menuIds) {
        List<SysRoleMenu> sysRoleMenus = menuIds.stream().map(menuId -> {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(id);
            sysRoleMenu.setMenuId(menuId);
            return sysRoleMenu;
        }).toList();
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        roleMenuMapper.insertBatch(sysRoleMenus);
    }
}
