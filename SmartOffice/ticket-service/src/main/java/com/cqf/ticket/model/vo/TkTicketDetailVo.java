package com.cqf.ticket.model.vo;

import com.cqf.ticket.model.po.TkTicket;
import com.cqf.ticket.model.po.TkTicketReply;
import lombok.Data;

import java.util.List;
@Data
public class TkTicketDetailVo extends TkTicket {
    private String submitterDept;
    private List<TkTicketReply> replies;
}
