package com.cqf.ticket.service;

import com.cqf.ticket.model.po.TkTicketAttachment;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 工单附件表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
public interface ITkTicketAttachmentService extends IService<TkTicketAttachment> {

    String upload(Long ticketId, MultipartFile file);
}
