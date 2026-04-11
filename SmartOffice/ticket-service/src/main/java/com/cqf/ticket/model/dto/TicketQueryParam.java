package com.cqf.ticket.model.dto;

import com.cqf.common.domain.QueryParam;
import lombok.Data;

@Data
public class TicketQueryParam extends QueryParam {
    private Integer status;
    private String category;
    private String title;
    private Boolean myOnly=false;
}
