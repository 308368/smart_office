package com.cqf.auth.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Long deptId;
    private String deptName;
    private String[] roles;
    private String[] permissions;
}
