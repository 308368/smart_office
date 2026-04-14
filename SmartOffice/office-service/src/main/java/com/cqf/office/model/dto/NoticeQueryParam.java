package com.cqf.office.model.dto;

import com.cqf.common.domain.QueryParam;
import lombok.Data;

@Data
public class NoticeQueryParam extends QueryParam {
    private String title;
    private Integer publishStatus;
}
