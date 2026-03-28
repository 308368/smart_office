package com.cqf.common.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private Integer total;
    private Integer size;
    private Integer current;
    private Integer pages;
}
