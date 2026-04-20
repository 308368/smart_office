package com.cqf.ai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cqf.ai.enums.ChatSessionEnum;
import com.cqf.ai.model.po.ChatMessage;
import com.cqf.ai.model.po.ChatSession;
import com.cqf.ai.mapper.ChatSessionMapper;
import com.cqf.ai.model.vo.SessionVO;
import com.cqf.ai.service.IChatMessageService;
import com.cqf.ai.service.IChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * AI对话会话表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-17
 */
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {
    @Value("${spring.ai.openai.chat.model:qwen-max}")
    private String MODEL;
    private final ChatSessionMapper chatSessionMapper;
    @Resource
    @Lazy
    private IChatMessageService chatMessageService;

    @Override
    public ChatSession getOrCreateSession(String sessionId, Long userId) {
        if (sessionId != null && !sessionId.isBlank()) {
            ChatSession session = chatSessionMapper.selectById(Long.parseLong(sessionId));
            if (session != null && session.getUserId().equals(userId)) {
                return session;
            }
        }
        // 创建新会话
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setModel(MODEL);
        session.setStatus(ChatSessionEnum.PROCESSING.getStatus());
        session.setMessageCount(0);
        chatSessionMapper.insert(session);
        return session;
    }

    @Override
    public void updateSession(ChatSession session, String question, String answer) {
        session.setMessageCount(session.getMessageCount() + 2);
        session.setUpdateTime(LocalDateTime.now());
        String title = session.getTitle();
        if (title == null || title.isBlank()){
            session.setTitle(question.length() > 50 ? question.substring(0, 50) + "..." : question);
        }
        chatSessionMapper.updateById(session);
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        return chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getStatus, ChatSessionEnum.PROCESSING.getStatus()));
    }

    @Override
    public List<SessionVO> history(Long userId) {
        List<ChatSession> sessions = getUserSessions(userId);
        return sessions.stream().map(this::toSessionVO).toList();
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        int update = chatSessionMapper.update(new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId)
                .set(ChatSession::getStatus, ChatSessionEnum.END.getStatus()));
        if (update <= 0){
            throw new RuntimeException("删除会话失败");
        }
        boolean remove = chatMessageService.remove(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId));
        if (!remove){
            throw new RuntimeException("删除会话失败");
        }
    }

    @Override
    public SessionVO createSession(Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setModel(MODEL);
        session.setStatus(ChatSessionEnum.PROCESSING.getStatus());
        chatSessionMapper.insert(session);

        return toSessionVO(session);
    }

    private SessionVO toSessionVO(ChatSession session) {
        SessionVO vo = new SessionVO();
        BeanUtil.copyProperties(session, vo);
        List<ChatMessage> assistant = chatMessageService.lambdaQuery()
                .eq(ChatMessage::getSessionId, session.getId())
                .eq(ChatMessage::getRole, "assistant")
                //找到最后一条消息
                .orderByDesc(ChatMessage::getCreateTime)
                .last("limit 1")
                .list();
        if (!assistant.isEmpty())vo.setLastMessage(assistant.get(0).getContent());
        vo.setSessionNo(generateSessionNo(session.getId()));
        return vo;
    }
    private String generateSessionNo(Long id) {
        return "CS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", id);
    }


}
