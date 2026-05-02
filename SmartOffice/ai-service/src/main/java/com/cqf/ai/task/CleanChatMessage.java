package com.cqf.ai.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.ai.mapper.ChatMessageMapper;
import com.cqf.ai.mapper.ChatSessionMapper;
import com.cqf.ai.model.po.ChatMessage;
import com.cqf.ai.model.po.ChatSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 清理过期聊天消息定时任务
 * 每天21点执行，删除30天前的聊天记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanChatMessage {
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;

    /**
     * 每天21点执行，清理10天前的聊天消息
     */
    @Scheduled(cron = "0 0 21 * * ?")
    @Transactional
    public void cleanOldMessages() {
        log.info("开始执行聊天消息清理任务");

        LocalDateTime expireTime = LocalDateTime.now().minusDays(10);
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(ChatMessage::getCreateTime, expireTime);

        List<ChatMessage> chatMessages = chatMessageMapper.selectList(queryWrapper);
        int count = chatMessages.size();
        if (count > 0) {
            chatMessageMapper.delete(queryWrapper);
            log.info("清理完成，删除 {} 条过期聊天消息", count);
        } else {
            log.info("清理完成，无过期聊天消息");
        }
        Set<Long> sessionId = chatMessages.stream().map(ChatMessage::getSessionId).collect(Collectors.toSet());
        LambdaQueryWrapper<ChatSession> sessionLambdaQueryWrapper = new LambdaQueryWrapper<ChatSession>()
                .in(ChatSession::getId, sessionId);
        chatSessionMapper.delete(sessionLambdaQueryWrapper);
    }
}
