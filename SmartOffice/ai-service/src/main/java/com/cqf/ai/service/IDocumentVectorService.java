package com.cqf.ai.service;

import com.cqf.common.domain.dto.DocumentChunkMsg;

public interface IDocumentVectorService {

    /**
     * 处理文档：分块、向量化、存入ES和MySQL
     */
    void processDocument(DocumentChunkMsg msg);

    /**
     * 根据文档ID删除向量库中的向量
     */
    void deleteByDocId(Long documentId);
}
