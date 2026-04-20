package com.cqf.knowledge.service;

import com.cqf.common.domain.vo.DocumentVo;
import com.cqf.knowledge.model.po.KbDocument;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

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

    InputStream downloadFileFromMinIO(String videofiles, String mergeFilePath);

    void removeDoc(Long kbId, Long docId);
}
