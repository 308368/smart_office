package com.cqf.knowledge.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.common.domain.PageResult;
import com.cqf.common.result.Result;
import com.cqf.knowledge.model.dto.DocQueryParam;
import com.cqf.knowledge.model.po.KbDocument;
import com.cqf.knowledge.model.vo.DocumentVo;
import com.cqf.knowledge.model.vo.KnowledgeVO;
import com.cqf.knowledge.service.IKbDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        DocumentVo documentVo=kbDocumentService.upload(kbId,file);
        return Result.success(documentVo);
    }


}
