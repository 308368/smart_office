package com.cqf.office.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveVo {
    private Long id;
    private String leaveNo;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal days;
    private Integer status;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
}
