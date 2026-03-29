package com.cqf.auth.model.dto;

import lombok.Data;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
}
