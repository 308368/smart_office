package com.cqf.office.service.impl;

import com.cqf.api.client.KbDocumentClient;
import com.cqf.api.client.OfficeClient;
import com.cqf.api.client.TicketClient;
import com.cqf.api.client.VectorFeignClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.vo.TicketPendingVo;
import com.cqf.common.result.Result;
import com.cqf.office.enums.ExpenseStatusEnum;
import com.cqf.office.model.po.OfExpense;
import com.cqf.office.model.vo.DashboardVo;
import com.cqf.office.service.DashboardService;
import com.cqf.office.service.IOfExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TicketClient ticketClient;
    private final KbDocumentClient kbDocumentClient;
    private final OfficeClient officeClient;
    private final VectorFeignClient vectorFeignClient;
    private final IOfExpenseService expenseService;
    @Override
    public DashboardVo dashboard() {
        Result<PageResult<TicketPendingVo>> pageResultResult = ticketClient.pending(1, 10000);
        // 待处理工单数
        Integer pendingTicket = pageResultResult.getData().getTotal();
        //待处理请假数
        Long pendingLeave = officeClient.pendingLeave();
        //待处理报销数
        Long pendingExpense = expenseService.lambdaQuery()
                .eq(OfExpense::getStatus, ExpenseStatusEnum.PENDING.getCode()).count();
        //知识库文档总数
        Long docCount = kbDocumentClient.total();
        //AI对话总数
        Long chatCount = vectorFeignClient.count();
        DashboardVo dashboardVo = new DashboardVo();
        dashboardVo.setPendingTicket(pendingTicket);
        dashboardVo.setPendingLeave((int) (pendingLeave+pendingExpense));
        dashboardVo.setDocCount(docCount.intValue());
        dashboardVo.setChatCount(chatCount.intValue());
        return dashboardVo;
    }
}
