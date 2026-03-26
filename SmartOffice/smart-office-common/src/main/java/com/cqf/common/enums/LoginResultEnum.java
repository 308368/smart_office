package com.cqf.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录结果枚举
 */
@Getter
@AllArgsConstructor
public enum LoginResultEnum {

    SUCCESS("登录成功"),
    USERNAME_ERROR("用户名错误"),
    PASSWORD_ERROR("密码错误"),
    USER_DISABLED("用户已被禁用"),
    UNKNOWN_ERROR("未知错误"),
    USERNAME_OR_PASSWORD_ERROR("用户名或密码错误");

    private final String message;
}