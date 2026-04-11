package com.cqf.ticket.controller;


import com.cqf.common.result.Result;
import com.cqf.ticket.service.ITkTicketReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 工单处理记录表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@RestController
@RequestMapping("/office/ticket")
@RequiredArgsConstructor
public class TkTicketReplyController {
    private final ITkTicketReplyService tkTicketReplyService;
    @PostMapping("/{id}/reply")
    public Result reply(@PathVariable("id") Long ticketId,@RequestBody Map<String, String> body){
        String content = body.get("content");
        tkTicketReplyService.reply(ticketId,content);
        return Result.success();
    }

}
