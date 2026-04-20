package com.cqf.ai.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.ai.mapper.ChatMessageMapper;
import com.cqf.ai.model.po.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 清理过期聊天消息定时任务
 * 每天21点执行，删除30天前的聊天记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanChatMessage {
    private final ChatMessageMapper chatMessageMapper;

    /**
     * 每天21点执行，清理30天前的聊天消息
     */
    @Scheduled(cron = "0 0 21 * * ?")
    public void cleanOldMessages() {
        log.info("开始执行聊天消息清理任务");

        LocalDateTime expireTime = LocalDateTime.now().minusDays(3);
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(ChatMessage::getCreateTime, expireTime);

        Long count = chatMessageMapper.selectCount(queryWrapper);
        if (count > 0) {
            chatMessageMapper.delete(queryWrapper);
            log.info("清理完成，删除 {} 条过期聊天消息", count);
        } else {
            log.info("清理完成，无过期聊天消息");
        }
    }
}
