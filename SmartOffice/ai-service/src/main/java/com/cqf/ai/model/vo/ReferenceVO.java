package com.cqf.ai.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReferenceVO {
    private Long docId;             // 文档ID
    private String docTitle;       // 文档标题
    private String chunkContent;   // 引用内容
    private Double score;          // 相似度分数
}
