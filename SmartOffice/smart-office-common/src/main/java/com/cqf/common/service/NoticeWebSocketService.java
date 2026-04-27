package com.cqf.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
public class NoticeWebSocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    public void sentNoticePublish(String title, String content){
        System.out.println("=== 发送公告通知 ===");
        System.out.println("title: " + title);
        System.out.println("simpMessagingTemplate: " + simpMessagingTemplate);
        HashMap<String, String> msg = new HashMap<>();
        msg.put("title", title);
        msg.put("content", content);
        System.out.println("发送消息到 /topic/notice");
        // 推送到 /topic/notice，前端订阅这个地址
        simpMessagingTemplate.convertAndSend("/topic/notice", msg);
        System.out.println("公告发送完成");
    }
    /**
     * 文档分块完成通知
     */
    public void sendChunkComplete(Long kbId, Long docId, String docTitle, int chunkCount,String username) {
        System.out.println("=== 发送文档分块通知 ===");
        System.out.println("username: " + username);
        System.out.println("kbId: " + kbId + ", docId: " + docId + ", docTitle: " + docTitle);
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "chunk_complete");
        msg.put("kbId", kbId);
        msg.put("docId", docId);
        msg.put("docTitle", docTitle);
        msg.put("chunkCount", chunkCount);
        msg.put("timestamp", System.currentTimeMillis());
        System.out.println("消息内容: " + msg);
        simpMessagingTemplate.convertAndSendToUser(
                username,
            "/queue/document",
            msg
        );
        System.out.println("发送完成");
    }
}
