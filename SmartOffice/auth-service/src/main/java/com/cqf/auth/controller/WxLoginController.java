package com.cqf.auth.controller;

import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.service.WxAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class WxLoginController {
    private final WxAuthService wxAuthService;
    @Value("${frontaddr:localhost:3000}")
    private String frontAddr;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.info("code:{},state:{}", code, state);
        //请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库
        SysUser sysUser = wxAuthService.wxAuth(code);
//http://localhost/api/ucenter/login?username=%7B%22username%22:%22admin%22,%22password%22:%22123456%22%7D&password=123456
        String username = sysUser.getUsername();

//        return "redirect:http://localhost:80/login?username=" + username + "&authType=wx";
        return "redirect:http://"+frontAddr+"/login?username=" + username + "&authType=wx";
    }
}

