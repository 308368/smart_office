package com.cqf.auth.model.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 登录方式
     */
    private String loginType="password";
}
