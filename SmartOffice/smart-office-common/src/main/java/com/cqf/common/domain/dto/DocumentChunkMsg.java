package com.cqf.common.domain.dto;

import lombok.Data;

@Data
public class DocumentChunkMsg {
    private Long kbId;

    private Long documentId;

    /**
     * 文件访问URL（MinIO或其他对象存储地址）
     */
    private String fileUrl;

    private String fileName;
}
