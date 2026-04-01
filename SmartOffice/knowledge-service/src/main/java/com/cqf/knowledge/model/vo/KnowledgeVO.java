package com.cqf.knowledge.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeVO {
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private Integer docCount;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
