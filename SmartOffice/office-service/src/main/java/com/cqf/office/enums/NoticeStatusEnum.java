package com.cqf.office.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeStatusEnum {
    //发布状态 0草稿 1已发布
    DRAFT(0),
    RELEASED(1);
    private final int status;
}
