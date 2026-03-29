package com.cqf.auth.mapper;

import com.cqf.auth.model.po.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-03-26
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    @Select("SELECT DISTINCT m.permission FROM sys_menu m INNER JOIN sys_role_menu rm ON m.id = rm.menu_id INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> getPermissionByUserId(Long userId);

    List<SysMenu> getMenuByRoleIds(List<Long> roleIds);
}
