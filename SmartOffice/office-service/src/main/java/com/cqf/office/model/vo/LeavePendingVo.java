package com.cqf.office.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeavePendingVo {
    private Long id;
    private String leaveNo;
    private Long userId;
    private String userName;
    private String deptName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal duration;
    private String reason;
    private Integer status;
    private LocalDateTime createTime;
}
