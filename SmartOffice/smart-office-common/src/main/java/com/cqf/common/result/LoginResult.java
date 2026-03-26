package com.cqf.common.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResult {
    private Long userId;
    private String username;
    private String nickname;
    private String token;
    private String[] roles;
}
