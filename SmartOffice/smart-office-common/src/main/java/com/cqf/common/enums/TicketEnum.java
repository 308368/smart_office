package com.cqf.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketEnum {
//    状态 1待处理 2处理中 3已解决 4已关闭 5已驳回
    WAIT_HANDLE(1),
    HANDLING(2),
    SOLVED(3),
    CLOSED(4),
    REJECTED(5);
    private final Integer status;

}
