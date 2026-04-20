package com.cqf.ai.controller;

import cn.hutool.core.bean.BeanUtil;
import com.cqf.ai.model.dto.ChatRequest;
import com.cqf.ai.model.po.ChatMessage;
import com.cqf.ai.model.po.ChatSession;
import com.cqf.ai.model.vo.ChatResponse;
import com.cqf.ai.model.vo.KbSimpleVO;
import com.cqf.ai.model.vo.MessageVO;
import com.cqf.ai.model.vo.SessionVO;
import com.cqf.ai.service.IChatMessageService;
import com.cqf.ai.service.IChatSessionService;
import com.cqf.api.client.AuthClient;
import com.cqf.api.client.KbDocumentClient;
import com.cqf.common.domain.vo.DocumentVo;
import com.cqf.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {
    private final IChatMessageService chatMessageService;
    private final IChatSessionService chatSessionService;
    private final AuthClient authClient;
    private final KbDocumentClient kbDocumentClient;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('ai:chat')")
    public Flux<String> chat(@RequestBody @Valid ChatRequest request) {
        return chatMessageService.chatStream(request);
    }
    /**
     * 获取会话历史列表
     */
    @GetMapping("/history")
    public Result<List<SessionVO>> history() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        List<SessionVO> voList=chatSessionService.history(userId);
        return Result.success(voList);
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/history/{sessionId}/messages")
    public Result<List<MessageVO>> messages(@PathVariable Long sessionId) {
        List<MessageVO> voList=chatMessageService.getMessage(sessionId);
        return Result.success(voList);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/history/{sessionId}")
    public Result<Void> deleteHistory(@PathVariable Long sessionId) {
        chatSessionService.deleteSession(sessionId);
        return Result.success();
    }

    /**
     * 创建新会话
     */
    @PostMapping("/session")
    public Result<SessionVO> createSession() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        SessionVO session = chatSessionService.createSession(userId);
        return Result.success(session);
    }
    /**
     * 获取知识库列表
     */
    @GetMapping("/kb/list/{isOwe}")
    public Result<List<KbSimpleVO>> listKbs(@PathVariable Integer isOwe) {
        // 从知识库服务获取
        List<DocumentVo> documentVos = kbDocumentClient.userDoc(isOwe);
        List<KbSimpleVO> list = documentVos.stream().map(documentVo -> {
            KbSimpleVO kbSimpleVO = new KbSimpleVO();
            kbSimpleVO.setDocId(documentVo.getId());
            kbSimpleVO.setDocTitle(documentVo.getTitle());
            kbSimpleVO.setKbId(documentVo.getKbId());
            kbSimpleVO.setKbName(documentVo.getKbName());
            kbSimpleVO.setFileType(documentVo.getFileType());
            return kbSimpleVO;
        }).toList();
        return Result.success(list);
    }

}
