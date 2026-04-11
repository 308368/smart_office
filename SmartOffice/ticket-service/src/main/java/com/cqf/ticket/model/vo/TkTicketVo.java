package com.cqf.ticket.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TkTicketVo {
    private Long id;
    private String ticketNo;
    private String title;
    private String category;
    private Integer priority;
    private Integer status;
    private String submitterName;
    private LocalDateTime createTime;

}
