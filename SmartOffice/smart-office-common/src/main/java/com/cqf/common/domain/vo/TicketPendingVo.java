package com.cqf.common.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketPendingVo {
    private Long id;
    private String title;
    private String typeName;
    private Integer priority;
    private LocalDateTime createTime;
}
