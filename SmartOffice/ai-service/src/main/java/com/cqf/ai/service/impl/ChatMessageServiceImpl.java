package com.cqf.ai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.ai.mapper.ChatPromptMapper;
import com.cqf.ai.model.dto.ChatRequest;
import com.cqf.ai.model.po.ChatMessage;
import com.cqf.ai.mapper.ChatMessageMapper;
import com.cqf.ai.model.po.ChatSession;
import com.cqf.ai.model.vo.ChatCount;
import com.cqf.ai.model.vo.MessageVO;
import com.cqf.ai.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqf.ai.service.IChatSessionService;
import com.cqf.api.client.AuthClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.FILTER_EXPRESSION;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * <p>
 * AI对话消息表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {
    private final ChatClient pdfChatClient;
    private final ChatMessageMapper chatMessageMapper;
    private final IChatSessionService chatSessionService;
    private final ObjectMapper objectMapper;
    private final ChatPromptMapper chatPromptMapper;
    @Resource
    @Lazy
    private IChatMessageService chatMessageService;
    private final AuthClient authClient;
    @Value("${spring.ai.openai.chat.model:qwen-max}")
    private String MODEL;

    @Override
    @Transactional
    public Flux<String> chatStream(ChatRequest request) {
        // 1. 获取当前用户ID
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);

        // 2. 获取或创建会话
        ChatSession session = chatSessionService.getOrCreateSession(request.getSessionId(), userId);
        Long sessionId = session.getId();

        // 3. 获取会话历史消息
        List<Message> history = chatMessageService.getChatHistory(sessionId);

        // 默认提示词
        String promptContent = "你是公司的智能办公助手，可以回答关于公司制度、流程等问题。使用纯文本格式回答，不要使用markdown。如果不确定答案，请如实说明。";

        // 获取自定义提示词
        Long promptId = request.getPromptId();
        if (promptId != null) {
            var prompt = chatPromptMapper.selectById(promptId);
            if (prompt != null && StringUtils.hasText(prompt.getPrompt())) {
                // 融合：默认 + 自定义
                promptContent = prompt.getPrompt()+ "\n\n---\n注意：以上是你的专属配置，优先级高于下面的默认规则。\n\n" + promptContent ;
            }
        }

        // 在 history 之前添加系统消息
        List<Message> messages = new ArrayList<>();
        if (StringUtils.hasText(promptContent)) {
            messages.add(new SystemMessage(promptContent));
        }
        messages.addAll(history);

        // 4. 保存用户消息
        saveMessage(sessionId, "user", request.getQuestion(), null, 0L);

        // 5. 获取 AI 流式响应
        Integer[] docIds = request.getDocIds();
        String question = request.getQuestion();
        String finalSessionId = String.valueOf(sessionId);

        Flux<String> aiFlux;
        if (docIds == null || docIds.length == 0) return null;
        String idsStr = Arrays.stream(docIds)
                .map(String::valueOf)
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));
        String filterExpr = "doc_id IN [" + idsStr + "]";

        aiFlux = pdfChatClient.prompt()
                .messages(messages)
                .user(question)
                .advisors(a -> a.param(CONVERSATION_ID, sessionId))
                .advisors(a -> a.param(FILTER_EXPRESSION, filterExpr))
                .stream()
                .content()
                .map(chunk -> {
                    try {
                        return "data: " + objectMapper.writeValueAsString(new StreamData(chunk, finalSessionId)) + "\n\n";
                    } catch (Exception e) {
                        return "data: " + chunk + "\n\n";
                    }
                });


        // 6. 流结束后保存 AI 消息并更新会话
        StringBuilder fullAnswer = new StringBuilder();
        return aiFlux
                .doOnNext(chunk -> {
                    // 提取 content 填充完整答案
                    try {
                        String json = chunk.substring(6, chunk.length() - 2);
                        StreamData data = objectMapper.readValue(json, StreamData.class);
                        fullAnswer.append(data.getContent());
                    } catch (Exception ignored) {
                    }
                })
                .doOnComplete(() -> {
                    long duration = System.currentTimeMillis();
                    saveMessage(sessionId, "assistant", fullAnswer.toString(), null, duration);
                    chatSessionService.updateSession(session, question, fullAnswer.toString());
                })
                .doOnError(e -> log.error("流式输出异常", e));
    }

    // 流式数据传输对象
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class StreamData {
        private String content;
        private String sessionId;
    }

    private String generateSessionNo(Long sessionId) {
        return "CS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", sessionId);
    }

    private void saveMessage(Long sessionId, String role, String content, Integer tokens, Long costTime) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        chatMessage.setModel(MODEL);
        chatMessage.setTokens(tokens);
        chatMessage.setCostTime(costTime);
        chatMessageMapper.insert(chatMessage);
    }

    @Override
    public List<Message> getChatHistory(Long sessionId) {
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId));
        return chatMessages.stream()
                .map(msg -> {
                    if ("user".equals(msg.getRole())) {
                        return new UserMessage(msg.getContent());
                    } else {
                        return new AssistantMessage(msg.getContent());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageVO> getMessage(Long sessionId) {
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId));
        return chatMessages.stream().map(msg -> {
            MessageVO vo = new MessageVO();
            BeanUtil.copyProperties(msg, vo);
            return vo;
        }).toList();
    }

    @Override
    public List<ChatCount> chatCount() {
        return chatMessageMapper.selectDailyChatCount();
    }
}
