package com.cqf.ai.model.dto;

import lombok.Data;

@Data
public class ChatQueryParam {
    private Integer current = 1;   // 当前页
    private Integer size = 10;     // 每页条数
}
