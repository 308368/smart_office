package com.cqf.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {
    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }

//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        // 调用 CustomUserDetailsService.loadUserByUsername 获取用户
//        UserDetails user = this.getUserDetailsService().loadUserByUsername(authentication.getName());
//
//        // 直接认证成功，不校验密码
//        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
//                user, "", user.getAuthorities());
//        return result;
//    }

}
