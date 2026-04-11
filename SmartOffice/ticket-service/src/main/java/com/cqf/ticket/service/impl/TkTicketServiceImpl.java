package com.cqf.ticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.po.SysUser;
import com.cqf.common.domain.vo.DeptVo;
import com.cqf.common.domain.vo.UserVo;
import com.cqf.common.enums.TicketEnum;
import com.cqf.common.result.Result;
import com.cqf.ticket.mapper.TkTicketReplyMapper;
import com.cqf.ticket.model.dto.TicketHandleDTO;
import com.cqf.ticket.model.po.TkTicket;
import com.cqf.ticket.mapper.TkTicketMapper;
import com.cqf.ticket.model.po.TkTicketReply;
import com.cqf.ticket.model.vo.TkTicketDetailVo;
import com.cqf.ticket.service.ITkTicketService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 工单表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@Service
@RequiredArgsConstructor
public class TkTicketServiceImpl extends ServiceImpl<TkTicketMapper, TkTicket> implements ITkTicketService {
    private final TkTicketMapper tkTicketMapper;
    private final AuthClient authClient;
    private final TkTicketReplyMapper tkTicketReplyMapper;
    @Resource
    @Lazy
    private ITkTicketService currentProxy;

    @Override
    public TkTicketDetailVo getTicketDetail(Long id) {
        TkTicketDetailVo tkTicketDetailVo = new TkTicketDetailVo();
        TkTicket tkTicket = tkTicketMapper.selectById(id);
        if (tkTicket == null) return null;
        //复制初始工单属性
        BeanUtil.copyProperties(tkTicket, tkTicketDetailVo);
        //获取工单提交人部门名称
        String deptName = getDeptName(tkTicket.getSubmitterId());
        tkTicketDetailVo.setSubmitterDept(deptName);
        //获取工单回复
        List<TkTicketReply> tkTicketReplies = tkTicketReplyMapper.selectList(new LambdaQueryWrapper<TkTicketReply>()
                .eq(TkTicketReply::getTicketId, tkTicket.getId()));
        tkTicketDetailVo.setReplies(tkTicketReplies);
        return tkTicketDetailVo;
    }

    @Override
    @Transactional
    public void handleTicket(Long ticketId, TicketHandleDTO ticketHandleDTO) {
        TkTicket tkTicket = tkTicketMapper.selectById(ticketId);
        if (tkTicket == null)new RuntimeException("工单不存在");

        Long handlerId = ticketHandleDTO.getHandlerId();
        String handlerName = ticketHandleDTO.getHandlerName();
        tkTicket.setHandlerId(handlerId);
        tkTicket.setHandlerName(handlerName);
        tkTicket.setStatus(TicketEnum.HANDLING.getStatus());
        tkTicketMapper.updateById(tkTicket);
        //添加工单回复
//        String remark = ticketHandleDTO.getRemark();
//        currentProxy.addTicketReply(ticketId, remark, handlerId, handlerName);
    }

    /**
     * 添加工单回复
     * @param ticketId
     * @param content
     * @param handlerId
     * @param handlerName
     */
    @Override
    public void addTicketReply(Long ticketId, String content, Long handlerId, String handlerName) {
        TkTicketReply tkTicketReply = new TkTicketReply();
        tkTicketReply.setTicketId(ticketId);
        tkTicketReply.setContent(content);
        tkTicketReply.setReplyType(1);
        tkTicketReply.setUserId(handlerId);
        tkTicketReply.setUserName(handlerName);
        tkTicketReplyMapper.insert(tkTicketReply);
    }

    @Override
    public void resolve(Long ticketId, String remark) {
        TkTicket tkTicket = tkTicketMapper.selectById(ticketId);
        if (tkTicket == null)throw new RuntimeException("工单不存在");
        String loginUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(loginUsername);
        if (!tkTicket.getHandlerId().equals(userId))throw new RuntimeException("您没有权限处理此工单");
        tkTicket.setStatus(TicketEnum.SOLVED.getStatus());
        tkTicket.setResolveTime(LocalDateTime.now());
        tkTicketMapper.updateById(tkTicket);
        //添加工单回复
        currentProxy.addTicketReply(ticketId, remark, tkTicket.getHandlerId(), tkTicket.getHandlerName());
    }

    @Override
    @Transactional
    public void transferTicket(Long ticketId, Long targetUserId, String reason) {
        TkTicket tkTicket = tkTicketMapper.selectById(ticketId);
        if (tkTicket == null)throw new RuntimeException("工单不存在");
        tkTicket.setHandlerId(targetUserId);
        Result<UserVo> clientById = authClient.getById(targetUserId);
        UserVo data = clientById.getData();
        if (data.equals( null)){
            throw new RuntimeException("工单提交人部门不存在");
        }
        tkTicket.setHandlerName(data.getNickname());
        tkTicketMapper.updateById(tkTicket);
        //添加工单回复
        currentProxy.addTicketReply(ticketId, reason, targetUserId, data.getNickname());
    }

    private String getDeptName(Long userId) {
        Result<List<DeptVo>> list = authClient.list();
        Result<UserVo> userVoResult = authClient.getById(userId);
        UserVo user = userVoResult.getData();
        if (user == null) return null;
        List<DeptVo> deptVoList = list.getData();
        Long deptId = user.getDeptId();
        for (DeptVo deptVo : deptVoList) {
            if (deptId.equals(deptVo.getId())) {
                return deptVo.getName();
            }
        }
        return null;
    }
}
