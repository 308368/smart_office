package com.cqf.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatSessionEnum {
//    状态 0结束 1进行中
    END(0),
    PROCESSING(1);
    private final Integer status;
}
