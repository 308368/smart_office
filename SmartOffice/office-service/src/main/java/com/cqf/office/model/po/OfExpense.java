package com.cqf.office.model.po;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.cqf.office.enums.ExpenseStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 报销申请表
 * </p>
 *
 * @author author
 * @since 2026-04-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("of_expense")
public class OfExpense implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报销编号
     */
    private String expenseNo;

    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 申请人姓名
     */
    private String userName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 报销类型: 差旅/餐饮/办公/交通/其他
     */
    private String expenseType;

    /**
     * 报销总额
     */
    private BigDecimal totalAmount;

    /**
     * 费用说明
     */
    private String description;

    /**
     * 状态 1待审批 2审批中 3已通过 4已拒绝 5已取消
     */
    @EnumValue
    private ExpenseStatusEnum status;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    private String approveComment;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0否 1是
     */
    private Integer deleted;


}
