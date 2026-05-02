package com.cqf.auth.service;

import com.cqf.auth.model.po.SysUser;

public interface WxAuthService {


    SysUser wxAuth(String code);
}
