package com.cqf.ai.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;               // 消息ID
    private String role;           // 角色
    private String content;        // 内容
    private String model;          // 模型
    private Integer tokens;        // token
    private Long costTime;         // 耗时
    private LocalDateTime createTime;  // 创建时间
}
