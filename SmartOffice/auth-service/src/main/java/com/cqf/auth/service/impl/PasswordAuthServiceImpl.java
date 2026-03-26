package com.cqf.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.auth.mapper.SysUserMapper;
import com.cqf.auth.model.dto.LoginDTO;
import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.service.AuthService;
import com.cqf.common.enums.LoginResultEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("password_authservice")
@RequiredArgsConstructor
public class PasswordAuthServiceImpl implements AuthService {
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser execute(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        // 查询用户
        SysUser sysUser = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (sysUser == null) {
            throw new UsernameNotFoundException(LoginResultEnum.USERNAME_ERROR.getMessage());
        }
        if (!passwordEncoder.matches(password, sysUser.getPassword())) {
            throw new RuntimeException(LoginResultEnum.PASSWORD_ERROR.getMessage());
        }
        return sysUser;
    }
}
