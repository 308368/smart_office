package com.cqf.knowledge.model.dto;

import com.cqf.common.domain.QueryParam;
import lombok.Data;

@Data
public class DocQueryParam extends QueryParam {
    private Long kbId;
    private String title;
}
