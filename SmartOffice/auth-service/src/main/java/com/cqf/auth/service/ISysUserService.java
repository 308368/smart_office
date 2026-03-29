package com.cqf.auth.service;

import com.cqf.auth.model.dto.UserDTO;
import com.cqf.common.result.LoginResult;
import com.cqf.auth.model.po.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author author
 * @since 2026-03-25
 */
public interface ISysUserService extends IService<SysUser> {
    void saveUser(UserDTO dto);

    void updateUser(UserDTO dto);

    void updateRolesByUserId(UserDTO dto, Long userId);

//    LoginResult login(String username, String password);

}
