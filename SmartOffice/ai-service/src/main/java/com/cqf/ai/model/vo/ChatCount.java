package com.cqf.ai.model.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ChatCount {
    private LocalDate date;
    private Integer count;
}
