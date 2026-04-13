package com.cqf.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaveEnum {
//    状态 1待审批 2审批中 3已通过 4已拒绝 5已取消
    PENDING(1),
    APPROVING(2),
    APPROVED(3),
    REJECTED(4),
    CANCELLED(5);

    private final Integer code;
}
