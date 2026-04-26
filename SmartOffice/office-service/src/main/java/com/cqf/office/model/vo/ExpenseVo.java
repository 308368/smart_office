package com.cqf.office.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExpenseVo {
    private Long id;
    private String expenseNo;
    private String userName;
    private String deptName;
    private String expenseType;
    private BigDecimal totalAmount;
    private String description;
    private Integer status;
    private String statusText;
    private String approverName;
    private LocalDateTime approveTime;
    private String approveComment;
    private LocalDateTime createTime;
    private List<ExpenseDetailVo.ExpenseItemVo> items;
}
