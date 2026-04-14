package com.cqf.office.model.dto;

import lombok.Data;

@Data
public class AUNoticeDTO {
    private Long id;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer priority;
    private Integer publishStatus;
}
