package com.cqf.office.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class NoticeWebSocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    public void sentNoticePublish(String title, String content){
        HashMap<String, String> msg = new HashMap<>();
        msg.put("title", title);
        msg.put("content", content);
        // 推送到 /topic/notice，前端订阅这个地址
        simpMessagingTemplate.convertAndSend("/topic/notice", msg);

    }
}
