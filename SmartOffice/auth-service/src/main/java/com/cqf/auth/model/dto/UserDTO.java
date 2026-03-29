package com.cqf.auth.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Long deptId;
    private List<Long> roleIds;
    private Integer status;
    private LocalDateTime createTime;
}
