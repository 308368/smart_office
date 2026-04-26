package com.cqf.office.model.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 报销明细表
 * </p>
 *
 * @author author
 * @since 2026-04-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("of_expense_item")
public class OfExpenseItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报销ID
     */
    private Long expenseId;

    /**
     * 费用类型
     */
    private String itemType;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 费用说明
     */
    private String description;

    /**
     * 费用日期
     */
    private LocalDate expenseDate;

    /**
     * 发票/收据URL
     */
    private String receiptUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
