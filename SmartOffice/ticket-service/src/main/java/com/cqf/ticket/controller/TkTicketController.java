package com.cqf.ticket.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.po.SysUser;
import com.cqf.common.enums.TicketEnum;
import com.cqf.common.result.Result;
import com.cqf.ticket.model.dto.TicketDTO;
import com.cqf.ticket.model.dto.TicketHandleDTO;
import com.cqf.ticket.model.dto.TicketQueryParam;
import com.cqf.ticket.model.dto.TicketTransfer;
import com.cqf.ticket.model.po.TkTicket;
import com.cqf.ticket.model.vo.TicketTypeVo;
import com.cqf.ticket.model.vo.TkTicketDetailVo;
import com.cqf.ticket.model.vo.TkTicketVo;
import com.cqf.ticket.service.ITkTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 工单表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@RestController
@RequestMapping("/office/ticket")
@RequiredArgsConstructor
public class TkTicketController {
    private final ITkTicketService tkTicketService;
    private final AuthClient authClient;
    private final String[] ticketIcon={"Monitor","User","Money","More"};

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ticket:list')")
    public Result<PageResult<TkTicketVo>> list(TicketQueryParam queryParam) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        IPage<TkTicket> page = tkTicketService.lambdaQuery()
                .eq(queryParam.getStatus() != null, TkTicket::getStatus, queryParam.getStatus())
                .eq(queryParam.getCategory() != null, TkTicket::getCategory, queryParam.getCategory())
                .like(queryParam.getTitle() != null, TkTicket::getTitle, queryParam.getTitle())
                //TODO 只显示当前用户提交的工单
//                .eq(queryParam.getMyOnly().equals(true), TkTicket::getSubmitterName, username)
                .page(new Page<>(queryParam.getCurrent(), queryParam.getSize()));
        PageResult<TkTicketVo> tkTicketVoPageResult = new PageResult<>();
        tkTicketVoPageResult.setCurrent((int) page.getCurrent());
        tkTicketVoPageResult.setSize((int) page.getSize());
        tkTicketVoPageResult.setTotal((int) page.getTotal());
        tkTicketVoPageResult.setPages((int) page.getPages());
        tkTicketVoPageResult.setRecords(
                page.getRecords()
                        .stream()
                        .map(tkTicket -> BeanUtil.copyProperties(tkTicket, TkTicketVo.class))
                        .toList()
        );
        return Result.success(tkTicketVoPageResult);
    }
    @GetMapping("/type/list")
    public Result<List<TicketTypeVo>> typeList() {
        //获取全部工单类型（去重后的分类）
        List<String> categories = tkTicketService.lambdaQuery()
                .select(TkTicket::getCategory)
                .isNotNull(TkTicket::getCategory)
                .groupBy(TkTicket::getCategory)
                .list()
                .stream()
                .map(TkTicket::getCategory)
                .distinct()
                .toList();

        // 定义固定图标
        String[] icons = {"Monitor", "User", "Money", "More"};

        // 构建返回列表，添加id
        List<TicketTypeVo> ticketTypeVos = new java.util.ArrayList<>();
        long id = 1;
        for (String category : categories) {
            TicketTypeVo vo = new TicketTypeVo();
            vo.setId(id++);
            vo.setName(category);
            vo.setIcon(icons[(int) ((id - 1) % 4)]);
            ticketTypeVos.add(vo);
        }

        return Result.success(ticketTypeVos);
    }
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ticket:create')")
    public Result<TkTicketVo> create(@RequestBody TicketDTO ticketDTO) {
        TkTicket tkTicket = BeanUtil.copyProperties(ticketDTO, TkTicket.class);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = authClient.getUser(username);
        tkTicket.setSubmitterId(user.getId());
        tkTicket.setSubmitterName(user.getNickname());
        tkTicket.setTicketNo("TK" + System.currentTimeMillis()+user.getId());
        tkTicket.setStatus(1);
        tkTicketService.save(tkTicket);
        TkTicketVo tkTicketVo = BeanUtil.copyProperties(tkTicket, TkTicketVo.class);
        return Result.success(tkTicketVo);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ticket:detail')")
    public Result<TkTicketDetailVo> detail(@PathVariable Long id) {
        TkTicketDetailVo tkTicketVo =tkTicketService.getTicketDetail(id);
        return Result.success(tkTicketVo);
    }
    @PutMapping("/{id}/handle")
    @PreAuthorize("hasAuthority('ticket:handle')")
    public Result handle(@PathVariable("id") Long ticketId, @RequestBody TicketHandleDTO ticketHandleDTO) {
        tkTicketService.handleTicket(ticketId,ticketHandleDTO);
        return Result.success();
    }
    @PutMapping("/{id}/transfer")
    @PreAuthorize("hasAuthority('ticket:transfer')")
    public Result transfer(@PathVariable("id") Long ticketId,@RequestBody TicketTransfer ticketTransfer) {
        Long targetUserId = ticketTransfer.getTargetUserId();
        String reason = ticketTransfer.getReason();
        tkTicketService.transferTicket(ticketId,targetUserId, reason);
        return Result.success();
    }
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('ticket:handle')")
    public Result resolve(@PathVariable("id") Long ticketId, @RequestBody Map<String,String> body) {
        String remark = body.get("remark");
        tkTicketService.resolve(ticketId,remark);
        return Result.success();
    }
    @PutMapping("/{id}/close")
    public Result close(@PathVariable("id") Long ticketId) {
        tkTicketService.lambdaUpdate()
                .eq(TkTicket::getId, ticketId)
                .set(TkTicket::getStatus, TicketEnum.CLOSED.getStatus())
                .update();
        return Result.success();
    }

}
