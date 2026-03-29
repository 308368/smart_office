package com.cqf.auth.service;

import com.cqf.auth.model.po.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author author
 * @since 2026-03-26
 */
public interface ISysRoleService extends IService<SysRole> {
    List<SysRole> getRolesByUserId(Long userId);

    List<Long> getMenuIdsByRoleId(Long id);

    void assignMenus(Long id, List<Long> menuIds);
}
