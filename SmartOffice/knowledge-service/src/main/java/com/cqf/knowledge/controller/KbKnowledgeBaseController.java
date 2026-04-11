package com.cqf.knowledge.controller;


import cn.hutool.core.bean.BeanUtil;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.result.Result;
import com.cqf.knowledge.model.dto.KnowledgeQueryParam;
import com.cqf.knowledge.model.po.KbKnowledgeBase;
import com.cqf.knowledge.model.vo.KnowledgeVo;
import com.cqf.knowledge.service.IKbKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 知识库表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
@RestController
@RequestMapping("/knowledge/kb")
@RequiredArgsConstructor
public class KbKnowledgeBaseController {
    private final IKbKnowledgeBaseService kbKnowledgeBaseService;
    private final AuthClient authClient;
    /**
     * 知识库列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('knowledge:list')")
    public Result<PageResult<KnowledgeVo>> list(KnowledgeQueryParam  knowledgeQueryParam) {
        return Result.success(kbKnowledgeBaseService.listKnowledge(knowledgeQueryParam));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('knowledge:detail')")
    public Result<KnowledgeVo> detail(@PathVariable("id") Long id) {
        KbKnowledgeBase knowledgeBase = kbKnowledgeBaseService.getById(id);
        KnowledgeVo knowledgeVO = new KnowledgeVo();
        BeanUtil.copyProperties(knowledgeBase, knowledgeVO);
        return Result.success(knowledgeVO);
    }
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('knowledge:add')")
    public Result<KnowledgeVo> create(@RequestBody KbKnowledgeBase knowledgeBase) {
        SecurityContext context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        knowledgeBase.setCreateBy(userId);
        knowledgeBase.setDocCount(0);
        kbKnowledgeBaseService.save(knowledgeBase);
        KnowledgeVo knowledgeVO = BeanUtil.copyProperties(knowledgeBase, KnowledgeVo.class);
        return Result.success(knowledgeVO);
    }
    @PutMapping()
    @PreAuthorize("hasAuthority('knowledge:edit')")
    public Result update(@RequestBody KbKnowledgeBase knowledgeBase) {
        kbKnowledgeBaseService.updateById(knowledgeBase);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('knowledge:remove')")
    public Result delete(@PathVariable("id") Long id) {
        kbKnowledgeBaseService.removeById(id);
        return Result.success();
    }
    @GetMapping("/user/list")
    public Result<PageResult<KnowledgeVo>> userList(KnowledgeQueryParam  knowledgeQueryParam) {
        SecurityContext context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        return Result.success(kbKnowledgeBaseService.userListKnowledge(userId,knowledgeQueryParam));
    }

}
