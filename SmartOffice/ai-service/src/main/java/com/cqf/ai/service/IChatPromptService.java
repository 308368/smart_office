package com.cqf.ai.service;

import com.cqf.ai.model.dto.AddPromptDTO;
import com.cqf.ai.model.po.ChatPrompt;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.ai.model.vo.ChatPromptVo;

/**
 * <p>
 * 提示词模板表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-17
 */
public interface IChatPromptService extends IService<ChatPrompt> {

    ChatPromptVo addPrompt(AddPromptDTO dto, Long userId);

    void updatePrompt(Long id, AddPromptDTO dto);
}
