package com.cqf.ai.service;

import com.cqf.ai.model.dto.ChatRequest;
import com.cqf.ai.model.po.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.ai.model.vo.ChatCount;
import com.cqf.ai.model.vo.MessageVO;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * <p>
 * AI对话消息表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-17
 */
public interface IChatMessageService extends IService<ChatMessage> {

    Flux<String> chatStream(ChatRequest request);

    List<Message> getChatHistory(Long sessionId);

    List<MessageVO> getMessage(Long sessionId);

    List<ChatCount> chatCount();
}
