package com.cqf.knowledge.controller;


import com.cqf.common.domain.dto.ChunkSaveRequest;
import com.cqf.common.result.Result;
import com.cqf.knowledge.model.po.KbDocument;
import com.cqf.knowledge.model.po.KbDocumentChunk;
import com.cqf.knowledge.service.IKbDocumentChunkService;
import com.cqf.knowledge.service.IKbDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 文档向量切片表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
@RestController
@RequestMapping("/knowledge/kb")
@RequiredArgsConstructor
public class KbDocumentChunkController {
    private final IKbDocumentChunkService kbDocumentChunkService;
    @PostMapping("/chunk/batch")
    public Result saveBatch(@RequestBody ChunkSaveRequest request){
        kbDocumentChunkService.uploadChunk( request);

        return Result.success();

    }

}
