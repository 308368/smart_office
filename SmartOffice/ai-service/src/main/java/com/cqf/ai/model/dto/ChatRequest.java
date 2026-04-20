package com.cqf.ai.model.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String question;        // 用户问题

//    private Integer[] kbIds;           // 知识库ID列表，逗号分隔
    private Integer[] docIds;           // 知识库ID列表，逗号分隔

    private String sessionId;       // 会话ID，为空则创建新会话

    private Long promptId;          // 提示词模板ID，为空使用默认模板
}
