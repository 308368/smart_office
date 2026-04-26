package com.cqf.office.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AddExpenseDTO {

    private String expenseType;

    private BigDecimal totalAmount;

    private String description;

    private List<ExpenseItemDTO> items;

    @Data
    public static class ExpenseItemDTO {
        private String itemType;
        private BigDecimal amount;
        private LocalDate expenseDate;
        private String description;
        private String receiptUrl;
        private Integer fileIndex;  // 对应 files 数组的索引
    }
}
