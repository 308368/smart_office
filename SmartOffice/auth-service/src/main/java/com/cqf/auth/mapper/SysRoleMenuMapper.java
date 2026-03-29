package com.cqf.auth.mapper;

import com.cqf.auth.model.po.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 角色菜单关联表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-03-26
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    void insertBatch(List<SysRoleMenu> sysRoleMenus);
}
