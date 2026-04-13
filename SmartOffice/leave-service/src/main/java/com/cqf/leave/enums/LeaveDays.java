package com.cqf.leave.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaveDays {
    //年假
    YEARLY_LEAVE(15),
    //病假
    SICK_LEAVE(30),
    //产假
    MATERNITY_LEAVE(180),
    //婚假
    MARRIAGE_LEAVE(3);
    private final Integer days;
}
