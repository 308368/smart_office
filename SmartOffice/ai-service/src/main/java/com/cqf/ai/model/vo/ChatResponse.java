package com.cqf.ai.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String answer;          // AI回答
    private String sessionId;      // 会话ID
    private String sessionNo;      // 会话编号
    private List<ReferenceVO> references;  // 引用的知识
    private Integer tokens;         // 消耗token
    private Long duration;         // 响应耗时(ms)
}
