package com.cqf.office.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExpenseStatusEnum {
    //状态 1待审批 2审批中 3已通过 4已拒绝 5已取消
    PENDING(1, "待审批"),
    APPROVING(2, "审批中"),
    APPROVED(3, "已通过"),
    REJECTED(4, "已拒绝"),
    CANCELED(5, "已取消");
    @EnumValue
    @JsonValue
    private final int code;
    private final String label;
}
