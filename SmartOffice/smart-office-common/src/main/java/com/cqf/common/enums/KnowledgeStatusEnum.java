package com.cqf.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KnowledgeStatusEnum {
    PRIVATE(0),
    PUBLIC(1);
    private final Integer status;
}
