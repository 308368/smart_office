package com.cqf.knowledge.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentVo {
    private Long id;
    private String title;
    private Long kbId;
    private String content;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Integer chunkCount;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
