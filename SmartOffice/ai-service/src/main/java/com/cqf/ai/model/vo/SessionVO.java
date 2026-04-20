package com.cqf.ai.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionVO {
    private Long id;               // 会话ID
    private String sessionNo;      // 会话编号
    private String title;          // 会话标题
    private String model;          // 使用模型
    private Integer status;        // 状态
    private Integer messageCount;  // 消息数量
    private String lastMessage;    // 最后一条消息
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
}
