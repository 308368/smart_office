package com.cqf.office.model.dto;

import com.cqf.common.domain.QueryParam;
import com.cqf.office.enums.ExpenseStatusEnum;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseQueryParam extends QueryParam {
    private ExpenseStatusEnum status;
    private String expenseType;
    private LocalDate startDate;
    private LocalDate endDate;
}
