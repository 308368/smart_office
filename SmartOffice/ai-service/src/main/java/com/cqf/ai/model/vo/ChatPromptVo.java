package com.cqf.ai.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatPromptVo {
    private Long id;
    private String name;
    private String description;
    private String prompt;
    private String category;
    private Integer isPublic;
    private Long createBy;
    private LocalDateTime createTime;
}
