package com.cqf.ticket.controller;


import com.cqf.common.result.Result;
import com.cqf.ticket.service.ITkTicketAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 工单附件表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@RestController
@RequestMapping("/office/ticket")
@RequiredArgsConstructor
public class TkTicketAttachmentController {
    private final ITkTicketAttachmentService tkTicketAttachmentService;
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("ticketId") Long ticketId, @RequestParam("file") MultipartFile file) {
        String url=tkTicketAttachmentService.upload(ticketId,file);
        return Result.success(url);
    }
}
