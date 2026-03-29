package com.cqf.auth.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户 VO - 对应 /system/user/list 接口返回
 */
@Data
@Builder
public class UserVo {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private Long deptId;
    private String deptName;
    private Integer status;
    private String createTime;
    private String avatar;
    private List<Long> roleIds;
}
