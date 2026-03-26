package com.cqf.common.enums;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "成功"),
    FAILED(500, "失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限");
    private final Integer code;
    private final String msg;
    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
