package com.cqf.leave.model.dto;

import com.cqf.common.domain.QueryParam;
import lombok.Data;

@Data
public class LeaveQueryParam extends QueryParam {
    private Integer status;
    private String leaveType;
}
