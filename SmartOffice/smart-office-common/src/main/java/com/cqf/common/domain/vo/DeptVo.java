package com.cqf.common.domain.vo;

import lombok.Data;

@Data
public class DeptVo {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private String leader;
    private String phone;
    private Integer status;
}
