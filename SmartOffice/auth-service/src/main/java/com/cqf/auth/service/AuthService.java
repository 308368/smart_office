package com.cqf.auth.service;

import com.cqf.auth.model.dto.LoginDTO;
import com.cqf.auth.model.po.SysUser;

public interface AuthService {
    /**
     * @description 认证方法
     * @param authParamsDto 认证参数
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @author Mr.M
     * @date 2022/9/29 12:11
     */
    SysUser execute(LoginDTO authParamsDto);
}
