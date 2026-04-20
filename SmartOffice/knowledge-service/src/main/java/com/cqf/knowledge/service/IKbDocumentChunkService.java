package com.cqf.knowledge.service;

import com.cqf.common.domain.dto.ChunkSaveRequest;
import com.cqf.knowledge.model.po.KbDocumentChunk;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 文档向量切片表 服务类
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
public interface IKbDocumentChunkService extends IService<KbDocumentChunk> {

    void uploadChunk(ChunkSaveRequest request);

}
