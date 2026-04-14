package com.cqf.office.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeDTO {
    private Long  id;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer priority;
    private Integer publishStatus;
    private LocalDateTime publishTime;
    private Long publisherId;
    private String publisherName;
    private Integer viewCount;
}
