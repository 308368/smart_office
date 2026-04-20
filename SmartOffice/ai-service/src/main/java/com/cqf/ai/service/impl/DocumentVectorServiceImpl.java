package com.cqf.ai.service.impl;

import com.cqf.ai.service.IDocumentVectorService;
import com.cqf.api.client.KbDocumentClient;
import com.cqf.common.domain.dto.ChunkSaveRequest;
import com.cqf.common.domain.dto.DocumentChunkMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentVectorServiceImpl implements IDocumentVectorService {
    private final KbDocumentClient kbChunkFeignClient;
    private final VectorStore vectorStore;

    static {
        System.setProperty("pdfbox.fontcache", "false");
        System.setProperty("org.apache.pdfbox.pdmodel.font.useFontCache", "false");
    }
    @Override
    public void processDocument(DocumentChunkMsg msg) {
        log.info("开始处理文档: documentId={}, fileName={}", msg.getDocumentId(), msg.getFileName());
        try {
            // 1. 获取文件资源
            Resource resource = new UrlResource("http://192.168.220.100:9000"+msg.getFileUrl());

            // 2. 使用 Spring AI 的文档读取器读取 PDF（每页作为一个 Document）
            //创建PDF的读取器
            PagePdfDocumentReader reader = new PagePdfDocumentReader(
                    resource, // 文件源
                    PdfDocumentReaderConfig.builder()
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                            .withPagesPerDocument(1) // 每1页PDF作为一个Document
                            .build()
            );
            //读取PDF文档，拆分为Document
            List<Document> documents = reader.read();
            documents.forEach(document -> document.getMetadata().put("doc_id", msg.getDocumentId()));
            //写入向量库
            vectorStore.add(documents);

            // 4. 保存 chunk 记录到 knowledge-service
            List<ChunkSaveRequest.ChunkItem> chunkItems = new ArrayList<>();
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                ChunkSaveRequest.ChunkItem item = new ChunkSaveRequest.ChunkItem();
                item.setDocumentId(msg.getDocumentId());
                item.setChunkContent(doc.getText());
                item.setChunkIndex(i);
                item.setVectorId(doc.getId());
                chunkItems.add(item);
            }

            ChunkSaveRequest request = new ChunkSaveRequest();
            request.setChunks(chunkItems);
            kbChunkFeignClient.saveBatch(request);

            log.info("文档处理完成: documentId={}, chunkCount={}", msg.getDocumentId(), documents.size());

        } catch (MalformedURLException e) {
            log.error("文件URL格式错误: {}", msg.getFileUrl(), e);
        } catch (Exception e) {
            log.error("文档处理失败: documentId={}", msg.getDocumentId(), e);
        }
    }

    @Override
    public void deleteByDocId(Long documentId) {
        log.info("开始删除文档向量: documentId={}", documentId);
    }
}
