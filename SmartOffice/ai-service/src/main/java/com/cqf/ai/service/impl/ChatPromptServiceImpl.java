package com.cqf.ai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cqf.ai.model.dto.AddPromptDTO;
import com.cqf.ai.model.po.ChatPrompt;
import com.cqf.ai.mapper.ChatPromptMapper;
import com.cqf.ai.model.vo.ChatPromptVo;
import com.cqf.ai.service.IChatPromptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 提示词模板表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-17
 */
@Service
@RequiredArgsConstructor
public class ChatPromptServiceImpl extends ServiceImpl<ChatPromptMapper, ChatPrompt> implements IChatPromptService {
    private final ChatPromptMapper chatPromptMapper;

    @Override
    public ChatPromptVo addPrompt(AddPromptDTO dto, Long userId) {
        ChatPrompt chatPrompt = new ChatPrompt();
        chatPrompt.setName(dto.getName());
        chatPrompt.setDescription(dto.getDescription());
        chatPrompt.setPrompt(dto.getPrompt());
        chatPrompt.setCategory(dto.getCategory());
        chatPrompt.setIsPublic(dto.getIsPublic());
        chatPrompt.setCreateBy(userId);
        chatPromptMapper.insert(chatPrompt);
        return BeanUtil.copyProperties(chatPrompt, ChatPromptVo.class);
    }

    @Override
    public void updatePrompt(Long id, AddPromptDTO dto) {
        ChatPrompt chatPrompt = chatPromptMapper.selectById(id);
        if (chatPrompt == null)throw new RuntimeException("提示词不存在");
        BeanUtil.copyProperties(dto, chatPrompt);
        chatPromptMapper.updateById(chatPrompt);
    }
}
