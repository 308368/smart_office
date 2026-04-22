package com.cqf.office.model.vo;

import lombok.Data;

@Data
public class DashboardVo {
    private Integer docCount;
    private Integer pendingTicket;
    private Integer pendingLeave;
    private Integer chatCount;
}
