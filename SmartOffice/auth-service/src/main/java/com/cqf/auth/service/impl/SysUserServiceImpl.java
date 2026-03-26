package com.cqf.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqf.auth.mapper.SysUserMapper;
import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    // 登录逻辑已由 Spring Security 处理
}
