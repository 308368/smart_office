package com.cqf.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.po.SysUser;
import com.cqf.ticket.mapper.TkTicketMapper;
import com.cqf.ticket.model.po.TkTicket;
import com.cqf.ticket.model.po.TkTicketReply;
import com.cqf.ticket.mapper.TkTicketReplyMapper;
import com.cqf.ticket.service.ITkTicketReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 工单处理记录表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@Service
@RequiredArgsConstructor
public class TkTicketReplyServiceImpl extends ServiceImpl<TkTicketReplyMapper, TkTicketReply> implements ITkTicketReplyService {
    private final TkTicketReplyMapper tkTicketReplyMapper;
    private final TkTicketMapper tkTicketMapper;
    private final AuthClient authClient;

    @Override
    public void reply(Long ticketId,String content) {
        TkTicket tkTicket = tkTicketMapper.selectById(ticketId);
        if (tkTicket == null)throw new RuntimeException("工单不存在");
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = authClient.getUser(name);
        //添加工单回复
        TkTicketReply tkTicketReply = new TkTicketReply();
        tkTicketReply.setTicketId(ticketId);
        tkTicketReply.setContent(content);
        tkTicketReply.setUserId(user.getId());
        tkTicketReply.setUserName(user.getNickname());
        tkTicketReply.setReplyType(1);
        tkTicketReplyMapper.insert(tkTicketReply);
    }
}
