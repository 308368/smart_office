package com.cqf.ai.controller;

import cn.hutool.core.bean.BeanUtil;
import com.cqf.ai.model.dto.AddPromptDTO;
import com.cqf.ai.model.po.ChatPrompt;
import com.cqf.ai.model.vo.ChatPromptVo;
import com.cqf.ai.service.IChatPromptService;
import com.cqf.api.client.AuthClient;
import com.cqf.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/ai/prompt")
@RequiredArgsConstructor
public class ChatPromptController {
    private final IChatPromptService chatPromptService;
    private final AuthClient authClient;
    @GetMapping("/list")
    public Result<List<ChatPromptVo>> list() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        List<ChatPrompt> chatPrompts = chatPromptService.lambdaQuery()
                .eq(ChatPrompt::getCreateBy, userId)
                .or()
                .eq(ChatPrompt::getIsPublic, 1)
                .list();
        List<ChatPromptVo> chatPromptVos = BeanUtil.copyToList(chatPrompts, ChatPromptVo.class);
        return Result.success(chatPromptVos);
    }
    @PostMapping("/create")
    public Result<ChatPromptVo> create(@RequestBody  AddPromptDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        return Result.success(chatPromptService.addPrompt(dto, userId));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody  AddPromptDTO dto) {
        chatPromptService.updatePrompt(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        chatPromptService.removeById(id);
        return Result.success();
    }
}
