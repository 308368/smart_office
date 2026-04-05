package com.cqf.knowledge.service;

import com.cqf.knowledge.model.po.KbDocument;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.knowledge.model.vo.DocumentVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 知识库文档表 服务类
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
public interface IKbDocumentService extends IService<KbDocument> {

    DocumentVo upload(Long kbId, MultipartFile file);

    DocumentVo addMediaFilesToDb(Long kbId,Long fileSize,String fileName,String extension,String objectName,String bucket);
}
