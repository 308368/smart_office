package com.cqf.office.service.impl;

import com.cqf.api.client.KbDocumentClient;
import com.cqf.api.client.OfficeClient;
import com.cqf.api.client.TicketClient;
import com.cqf.api.client.VectorFeignClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.vo.TicketPendingVo;
import com.cqf.common.result.Result;
import com.cqf.office.model.vo.DashboardVo;
import com.cqf.office.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TicketClient ticketClient;
    private final KbDocumentClient kbDocumentClient;
    private final OfficeClient officeClient;
    private final VectorFeignClient vectorFeignClient;
    @Override
    public DashboardVo dashboard() {
        Result<PageResult<TicketPendingVo>> pageResultResult = ticketClient.pending(1, 10000);
        // 待处理工单数
        Integer pendingTicket = pageResultResult.getData().getTotal();
        //待处理请假数
        Long pendingLeave = officeClient.pendingLeave();
        //知识库文档总数
        Long docCount = kbDocumentClient.total();
        //AI对话总数
        Long chatCount = vectorFeignClient.count();
        DashboardVo dashboardVo = new DashboardVo();
        dashboardVo.setPendingTicket(pendingTicket);
        dashboardVo.setPendingLeave(pendingLeave.intValue());
        dashboardVo.setDocCount(docCount.intValue());
        dashboardVo.setChatCount(chatCount.intValue());
        return dashboardVo;
    }
}
