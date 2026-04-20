package com.cqf.ai.service;

import com.cqf.ai.model.po.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.ai.model.vo.SessionVO;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * <p>
 * AI对话会话表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-17
 */
public interface IChatSessionService extends IService<ChatSession> {

    ChatSession getOrCreateSession(String sessionId, Long userId);

    void updateSession(ChatSession session, String question, String answer);

    List<ChatSession> getUserSessions(Long userId);

    List<SessionVO> history(Long userId);

    void deleteSession(Long sessionId);

    SessionVO createSession(Long userId);
}
