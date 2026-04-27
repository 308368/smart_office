package com.cqf.knowledge.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.vo.DocumentVo;
import com.cqf.common.enums.KnowledgeStatusEnum;
import com.cqf.common.result.Result;
import com.cqf.knowledge.model.dto.DocQueryParam;
import com.cqf.knowledge.model.po.KbDocument;
import com.cqf.knowledge.model.po.KbKnowledgeBase;
import com.cqf.knowledge.service.IKbDocumentService;
import com.cqf.knowledge.service.IKbKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 知识库文档表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
@RestController
@RequestMapping("/knowledge/kb")
@RequiredArgsConstructor
public class KbDocumentController {
    private final IKbDocumentService kbDocumentService;
    private final IKbKnowledgeBaseService kbKnowledgeBaseService;
    private final AuthClient authClient;
//    private final RabbitMqHelper rabbitMqHelper;
    @GetMapping("/{kbId}/doc/list")
    public Result<PageResult<DocumentVo>> listDoc(@PathVariable("kbId") Long kbId, DocQueryParam docQueryParam) {
        Page<KbDocument> kbDocumentPage = kbDocumentService.lambdaQuery()
                .eq(KbDocument::getKbId, kbId)
                .like(StringUtils.isNotBlank(docQueryParam.getTitle()), KbDocument::getTitle, docQueryParam.getTitle())
                .page(new Page<>(docQueryParam.getCurrent(), docQueryParam.getSize()));
        List<KbDocument> records = kbDocumentPage.getRecords();
        List<DocumentVo> documentVos = BeanUtil.copyToList(records, DocumentVo.class);
        PageResult<DocumentVo> pageResult = new PageResult<>();
        pageResult.setCurrent((int) kbDocumentPage.getCurrent());
        pageResult.setSize((int) kbDocumentPage.getSize());
        pageResult.setTotal((int) kbDocumentPage.getTotal());
        pageResult.setPages((int) kbDocumentPage.getPages());
        pageResult.setRecords(documentVos);
        return Result.success(pageResult);
    }
    @PostMapping(value = "/{kbId}/doc/upload",consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('knowledge:upload')")
    public Result<DocumentVo> upload(@PathVariable("kbId") Long kbId, @RequestParam("file") MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        DocumentVo documentVo=kbDocumentService.upload(kbId,file,username);
        return Result.success(documentVo);
    }
    @DeleteMapping("/{kbId}/doc/{docId}")
    @PreAuthorize("hasAuthority('knowledge:doc:remove')")
    public Result deleteDoc(@PathVariable("kbId") Long kbId, @PathVariable("docId") Long docId) {
        kbDocumentService.removeDoc(kbId,docId);
        return Result.success();
    }
    @GetMapping("userDoc/{isOwe}")
    public List<DocumentVo> userDoc(@PathVariable("isOwe") Integer isOwe) {
        List<KbDocument> kbDocuments=null;
        if (isOwe==1){
            kbDocuments = kbDocumentService.list();
        }else {
            SecurityContext context = SecurityContextHolder.getContext();
            String username = context.getAuthentication().getName();
            Long userId = authClient.getUserId(username);
            kbDocuments = kbDocumentService.lambdaQuery()
                    .eq(KbDocument::getCreateBy, userId)
                    .or()
                    .eq(KbDocument::getStatus, KnowledgeStatusEnum.PUBLIC.getStatus())
                    .list();
        }
        Set<Long> kbIds = kbDocuments.stream().map(KbDocument::getKbId).collect(Collectors.toSet());
        List<DocumentVo> documentVos = BeanUtil.copyToList(kbDocuments, DocumentVo.class);
        if (kbDocuments!=null && kbIds.size()>0){
            List<KbKnowledgeBase> kbKnowledgeBases = kbKnowledgeBaseService.list(new LambdaQueryWrapper<KbKnowledgeBase>()
                    .in(KbKnowledgeBase::getId, kbIds));
            Map<Long, String> knowledge = kbKnowledgeBases.stream().collect(Collectors.toMap(KbKnowledgeBase::getId, KbKnowledgeBase::getName));
            documentVos.forEach(documentVo -> documentVo.setKbName(knowledge.get(documentVo.getKbId())));
        }
        return documentVos;
    }
    @GetMapping("/total")
    public Long total() {
        List<KbDocument> list = kbDocumentService.list();
        return (long) list.size();
    }


}
