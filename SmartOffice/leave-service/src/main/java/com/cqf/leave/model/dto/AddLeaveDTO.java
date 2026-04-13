package com.cqf.leave.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddLeaveDTO {
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
