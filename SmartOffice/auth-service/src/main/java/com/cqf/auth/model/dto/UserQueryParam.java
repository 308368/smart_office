package com.cqf.auth.model.dto;

import com.cqf.common.domain.QueryParam;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserQueryParam extends QueryParam {
    private String username;
    private String phone;
    private Long deptId;
    private Integer status;
}
