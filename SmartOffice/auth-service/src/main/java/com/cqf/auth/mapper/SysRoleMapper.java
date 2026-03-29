package com.cqf.auth.mapper;

import com.cqf.auth.model.po.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqf.auth.model.po.SysRoleMenu;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-03-26
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("SELECT r.* FROM sys_role r left join sys_user_role ur on r.id=ur.role_id WHERE user_id = #{userId}")
    List<SysRole> getRolesByUserId(Long userId);

}
