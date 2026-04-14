package com.cqf.office.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeVo {
    private Long id;
    private String title;
    private Integer noticeType;
    private Integer priority;
    private Integer publishStatus;
    private LocalDateTime publishTime;
    private String publishName;
    private Integer viewCount;
    private Boolean isRead;
}
