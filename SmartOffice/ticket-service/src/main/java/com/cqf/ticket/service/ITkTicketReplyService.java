package com.cqf.ticket.service;

import com.cqf.ticket.model.po.TkTicketReply;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 工单处理记录表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
public interface ITkTicketReplyService extends IService<TkTicketReply> {

    void reply(Long ticketId,String content);
}
