package com.cqf.office.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExpenseDetailVo {

    private Long id;

    private String expenseNo;

    private String userName;

    private String deptName;

    private String expenseType;

    private BigDecimal totalAmount;

    private String description;

    private Integer status;

    private String statusText;

    private Long approverId;

    private String approverName;

    private LocalDateTime approveTime;

    private String approveComment;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private List<ExpenseItemVo> items;

    @Data
    public static class ExpenseItemVo {

        private Long id;

        private String itemType;

        private BigDecimal amount;

        private String description;

        private LocalDate expenseDate;

        private String receiptUrl;
    }
}
