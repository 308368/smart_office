package com.cqf.office.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeHomeVo {
    private Long id;
    private String title;
    private String publishName;
    private LocalDateTime publishTime;
    private Boolean isRead;
}
