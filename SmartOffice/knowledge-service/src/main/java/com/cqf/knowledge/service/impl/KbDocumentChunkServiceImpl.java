package com.cqf.knowledge.service.impl;

import com.cqf.api.client.OfficeClient;
import com.cqf.common.domain.dto.ChunkSaveRequest;
import com.cqf.knowledge.model.po.KbDocument;
import com.cqf.knowledge.model.po.KbDocumentChunk;
import com.cqf.knowledge.mapper.KbDocumentChunkMapper;
import com.cqf.knowledge.service.IKbDocumentChunkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqf.knowledge.service.IKbDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 文档向量切片表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbDocumentChunkServiceImpl extends ServiceImpl<KbDocumentChunkMapper, KbDocumentChunk> implements IKbDocumentChunkService {
    private final KbDocumentChunkMapper kbDocumentChunkMapper;
    private final IKbDocumentService kbDocumentService;
    private final OfficeClient webSocketFeignClient;

    @Override
    public void uploadChunk(ChunkSaveRequest request) {
        List<ChunkSaveRequest.ChunkItem> chunks = request.getChunks();
        List<KbDocumentChunk> kbDocumentChunks = chunks.stream().map(chunk -> {
            KbDocumentChunk kbDocumentChunk = new KbDocumentChunk();
            kbDocumentChunk.setDocId(chunk.getDocumentId());
            kbDocumentChunk.setChunkIndex(chunk.getChunkIndex());
            kbDocumentChunk.setContent(chunk.getChunkContent());
            kbDocumentChunk.setVector("{\"vectorId\":\"" + chunk.getVectorId() + "\"}");
            return kbDocumentChunk;
        }).toList();
        boolean b = kbDocumentChunkMapper.saveBatch(kbDocumentChunks);
        KbDocument document = kbDocumentService.getById(request.getChunks().get(0).getDocumentId());
        if (document == null)throw new RuntimeException("文档不存在");
        document.setStatus(2);//状态 0待处理 1处理中 2已完成 3处理失败
        document.setChunkCount(chunks.size());
        boolean b1 = kbDocumentService.updateById(document);
        if(!b1)throw new RuntimeException("更新文档状态失败");
        //向前端推送解析成功消息 - 通过Feign调用office-service发送WebSocket消息
        String username = request.getUsername();
        webSocketFeignClient.sendChunkComplete(document.getKbId(), document.getId(), document.getTitle(), chunks.size(), username);
    }


}
