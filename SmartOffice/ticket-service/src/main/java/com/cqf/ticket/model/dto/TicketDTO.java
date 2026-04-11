package com.cqf.ticket.model.dto;

import lombok.Data;

@Data
public class TicketDTO {
    private String title;
    private String category;
    private String content;
    private Integer priority;
    private String[] attachments;
}
