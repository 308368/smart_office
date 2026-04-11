package com.cqf.ticket.model.dto;

import lombok.Data;

@Data
public class TicketTransfer {
    private Long targetUserId;
    private String reason;
}
