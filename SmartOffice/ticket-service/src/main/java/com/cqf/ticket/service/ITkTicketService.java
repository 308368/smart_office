package com.cqf.ticket.service;

import com.cqf.ticket.model.dto.TicketHandleDTO;
import com.cqf.ticket.model.po.TkTicket;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.ticket.model.vo.TkTicketDetailVo;

import java.util.Map;

/**
 * <p>
 * 工单表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
public interface ITkTicketService extends IService<TkTicket> {

    TkTicketDetailVo getTicketDetail(Long id);

    void handleTicket(Long ticketId, TicketHandleDTO ticketHandleDTO);

    void transferTicket(Long ticketId, Long targetUserId, String reason);

    void addTicketReply(Long ticketId, String content, Long handlerId, String handlerName);

    void resolve(Long ticketId, String remark);

    Map<String, Long> getstats();
}
