package com.cqf.office.controller;

import com.cqf.common.service.NoticeWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/office/websocket")
@RequiredArgsConstructor
public class WebSocketController {
    private final NoticeWebSocketService noticeWebSocketService;

    @PostMapping("/sendChunkComplete")
    public void sendChunkComplete(
        @RequestParam("kbId") Long kbId,
        @RequestParam("docId") Long docId,
        @RequestParam("docTitle") String docTitle,
        @RequestParam("chunkCount") Integer chunkCount,
        @RequestParam("username") String username
    ) {
        noticeWebSocketService.sendChunkComplete(kbId, docId, docTitle, chunkCount, username);
    }
}
