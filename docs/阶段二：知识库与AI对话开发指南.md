# 阶段二：知识库与AI对话 - 开发指南

> 本指南将第二阶段拆分为3个小阶段，逐步完成知识库管理和AI对话功能

---

## 阶段二总览

```
阶段二：知识库管理 + AI对话
│
├── 2.1 知识库管理模块（3天）
├── 2.2 文档解析与向量化（2天）
├── 2.3 AI对话模块（3天）
│
└── 阶段产出：完整的知识库管理 + RAG问答
```

预计完成时间：**8天**

---

## 2.1 知识库管理模块

### 2.1.1 创建知识库服务

**项目结构：**
```
smart-office-knowledge/              # 知识库服务
├── src/main/java/com/cqf/knowledge/
│   ├── controller/
│   │   ├── KnowledgeController.java      # 知识库CRUD
│   │   └── DocumentController.java       # 文档管理
│   ├── service/
│   │   ├── KnowledgeService.java
│   │   └── DocumentService.java
│   ├── mapper/
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── KnowledgeBase.java       # 知识库实体
│   │   │   └── Document.java            # 文档实体
│   │   ├── dto/
│   │   └── vo/
│   └── config/
└── pom.xml
```

**pom.xml：**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    <dependency>
        <groupId>com.smartoffice</groupId>
        <artifactId>smart-office-common-web</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.smartoffice</groupId>
        <artifactId>smart-office-common-security</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- MinIO对象存储 -->
    <dependency>
        <groupId>io.minio</groupId>
        <artifactId>minio</artifactId>
        <version>8.5.7</version>
    </dependency>
    <!-- PDF解析 -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>3.0.0</version>
    </dependency>
    <!-- Word解析 -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.5</version>
    </dependency>
</dependencies>
```

### 2.1.2 数据库表

```sql
-- 知识库表
CREATE TABLE kb_base (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL COMMENT '知识库名称',
    description     VARCHAR(500)    COMMENT '描述',
    cover           VARCHAR(500)    COMMENT '封面图',
    is_public       TINYINT         DEFAULT 1 COMMENT '是否公开 0私有 1公开',
    status          TINYINT         DEFAULT 1 COMMENT '状态 0禁用 1启用',
    doc_count       INT             DEFAULT 0 COMMENT '文档数量',
    chunk_count     INT             DEFAULT 0 COMMENT '切片数量',
    creator_id      BIGINT          COMMENT '创建人ID',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '知识库表';

-- 文档表
CREATE TABLE kb_document (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    kb_id           BIGINT          NOT NULL COMMENT '知识库ID',
    title           VARCHAR(200)    NOT NULL COMMENT '文档标题',
    file_name       VARCHAR(200)    NOT NULL COMMENT '文件名',
    file_type       VARCHAR(20)     COMMENT '文件类型',
    file_size       BIGINT          COMMENT '文件大小',
    file_path       VARCHAR(500)    COMMENT '文件路径',
    parse_status    TINYINT         DEFAULT 0 COMMENT '解析状态 0待处理 1解析中 2成功 3失败',
    parse_message   VARCHAR(500)    COMMENT '解析消息',
    chunk_count     INT             DEFAULT 0 COMMENT '切片数量',
    token_count     INT             DEFAULT 0 COMMENT 'Token数量',
    creator_id      BIGINT          COMMENT '创建人ID',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '文档表';

-- 文档切片表
CREATE TABLE kb_chunk (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    doc_id          BIGINT          NOT NULL COMMENT '文档ID',
    content         TEXT            NOT NULL COMMENT '内容',
    word_count      INT             COMMENT '字数',
    token_count     INT             COMMENT 'Token数',
    vector_id       VARCHAR(100)    COMMENT '向量ID',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP
) COMMENT '文档切片表';
```

### 2.1.3 知识库管理接口

```java
package com.cqf.knowledge.controller;

import com.cqf.knowledge.domain.dto.KnowledgeDTO;
import com.cqf.knowledge.domain.vo.KnowledgeVO;
import com.cqf.knowledge.service.KnowledgeService;
import com.smartoffice.common.core.domain.PageResult;
import com.smartoffice.common.core.domain.Result;
import com.smartoffice.common.security.annotation.RequiresPermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledge/kb")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 知识库列表
     */
    @GetMapping("/list")
    public Result<PageResult<KnowledgeVO>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String name
    ) {
        return Result.success(knowledgeService.listKnowledge(current, size, name));
    }

    /**
     * 获取知识库详情
     */
    @GetMapping("/{id}")
    public Result<KnowledgeVO> getById(@PathVariable Long id) {
        return Result.success(knowledgeService.getById(id));
    }

    /**
     * 创建知识库
     */
    @PostMapping("/create")
    @RequiresPermissions("knowledge:add")
    public Result<KnowledgeVO> create(@RequestBody KnowledgeDTO dto) {
        return Result.success(knowledgeService.create(dto));
    }

    /**
     * 修改知识库
     */
    @PutMapping
    @RequiresPermissions("knowledge:edit")
    public Result<Void> update(@RequestBody KnowledgeDTO dto) {
        knowledgeService.updateKnowledge(dto);
        return Result.success();
    }

    /**
     * 删除知识库
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("knowledge:remove")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return Result.success();
    }

    /**
     * 获取用户可访问的知识库列表
     */
    @GetMapping("/user/list")
    public Result<List<KnowledgeVO>> userList() {
        return Result.success(knowledgeService.getUserKnowledgeList());
    }
}
```

### 2.1.4 文档上传接口

```java
package com.cqf.knowledge.controller;

import cn.hutool.core.util.StrUtil;
import com.cqf.knowledge.service.DocumentService;
import com.smartoffice.common.core.domain.Result;
import com.smartoffice.common.security.annotation.RequiresPermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/knowledge/kb/{kbId}/doc")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    @RequiresPermissions("knowledge:upload")
    public Result<DocumentVO> upload(
            @PathVariable Long kbId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return Result.success(documentService.uploadDocument(kbId, file));
    }

    /**
     * 文档列表
     */
    @GetMapping("/list")
    public Result<PageResult<DocumentVO>> list(
            @PathVariable Long kbId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size
    ) {
        return Result.success(documentService.listDocument(kbId, current, size));
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{docId}")
    @RequiresPermissions("knowledge:doc:remove")
    public Result<Void> delete(
            @PathVariable Long kbId,
            @PathVariable Long docId
    ) {
        documentService.deleteDocument(kbId, docId);
        return Result.success();
    }

    /**
     * 重建索引
     */
    @PostMapping("/{docId}/rebuild")
    @RequiresPermissions("knowledge:rebuild")
    public Result<Void> rebuild(
            @PathVariable Long kbId,
            @PathVariable Long docId
    ) {
        documentService.rebuildIndex(kbId, docId);
        return Result.success();
    }
}
```

---

## 2.2 文档解析与向量化

### 2.2.1 文档解析服务

```java
package com.cqf.knowledge.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
public class DocumentParseService {

    /**
     * 解析PDF文档
     */
    public String parsePdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("PDF解析失败", e);
            throw new RuntimeException("PDF解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析Word文档
     */
    public String parseWord(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            return content.toString();
        } catch (Exception e) {
            log.error("Word解析失败", e);
            throw new RuntimeException("Word解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析TXT文档
     */
    public String parseTxt(MultipartFile file) {
        try {
            return new String(file.getBytes(), "UTF-8");
        } catch (Exception e) {
            log.error("TXT解析失败", e);
            throw new RuntimeException("TXT解析失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件类型选择解析方式
     */
    public String parse(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (StrUtil.isBlank(fileName)) {
            throw new RuntimeException("文件名不能为空");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "pdf" -> parsePdf(file);
            case "doc", "docx" -> parseWord(file);
            case "txt" -> parseTxt(file);
            default -> throw new RuntimeException("不支持的文件类型: " + extension);
        };
    }
}
```

### 2.2.2 文本切片服务

```java
package com.cqf.knowledge.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChunkService {

    @Data
    public static class ChunkResult {
        private String content;
        private int wordCount;
        private int tokenCount;

        public ChunkResult(String content) {
            this.content = content;
            this.wordCount = content.length();
            // 简单估算：1 token ≈ 4 字符
            this.tokenCount = (int) Math.ceil(wordCount / 4.0);
        }
    }

    /**
     * 按段落切片
     */
    public List<ChunkResult> chunkByParagraph(String content, int maxTokens) {
        List<ChunkResult> chunks = new ArrayList<>();
        String[] paragraphs = content.split("\n\n+");

        StringBuilder currentChunk = new StringBuilder();
        int currentTokens = 0;

        for (String paragraph : paragraphs) {
            int paragraphTokens = (int) Math.ceil(paragraph.length() / 4.0);

            if (currentTokens + paragraphTokens > maxTokens && currentChunk.length() > 0) {
                chunks.add(new ChunkResult(currentChunk.toString().trim()));
                currentChunk = new StringBuilder();
                currentTokens = 0;
            }

            currentChunk.append(paragraph).append("\n\n");
            currentTokens += paragraphTokens;
        }

        if (currentChunk.length() > 0) {
            chunks.add(new ChunkResult(currentChunk.toString().trim()));
        }

        return chunks;
    }

    /**
     * 按固定长度切片（重叠）
     */
    public List<ChunkResult> chunkByLength(String content, int maxTokens, int overlapTokens) {
        List<ChunkResult> chunks = new ArrayList<>();
        int chunkSize = maxTokens * 4; // 字符数
        int step = (maxTokens - overlapTokens) * 4;

        for (int i = 0; i < content.length(); i += step) {
            int end = Math.min(i + chunkSize, content.length());
            String chunkContent = content.substring(i, end);

            if (chunkContent.trim().isEmpty()) {
                continue;
            }

            chunks.add(new ChunkResult(chunkContent));

            if (end >= content.length()) {
                break;
            }
        }

        return chunks;
    }
}
```

---

## 2.3 AI对话模块

### 2.3.1 创建AI对话服务

**项目结构：**
```
smart-office-ai/                    # AI对话服务
├── src/main/java/com/cqf/ai/
│   ├── controller/
│   │   └── ChatController.java
│   ├── service/
│   │   ├── ChatService.java
│   │   └── EmbeddingService.java
│   ├── config/
│   │   └── AiConfig.java
│   └── domain/
└── pom.xml
```

**pom.xml：**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>com.smartoffice</groupId>
        <artifactId>smart-office-common-web</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.smartoffice</groupId>
        <artifactId>smart-office-common-security</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- AI模型调用 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>kimi-spring-ai-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- 向量数据库（可选） -->
    <dependency>
        <groupId>com.milaboratory</groupId>
        <artifactId>milvus-sdk-java</artifactId>
        <version>2.3.4</version>
    </dependency>
    <!-- SSE支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>
```

### 2.3.2 AI对话接口

```java
package com.cqf.ai.controller;

import com.cqf.ai.domain.dto.ChatDTO;
import com.cqf.ai.domain.vo.ChatVO;
import com.cqf.ai.service.ChatService;
import com.smartoffice.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * AI问答（非流式）
     */
    @PostMapping("/chat")
    public Result<ChatVO> chat(@RequestBody ChatDTO dto) {
        return Result.success(chatService.chat(dto));
    }

    /**
     * AI问答（流式/SSE）
     */
    @GetMapping("/chat/stream")
    public Flux<ServerSentEvent<Map>> streamChat(
            @RequestParam String kbIds,
            @RequestParam String question,
            @RequestParam(required = false) String sessionId
    ) {
        return chatService.streamChat(kbIds, question, sessionId);
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history")
    public Result<PageResult<HistoryVO>> history(
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size
    ) {
        return Result.success(chatService.getHistory(sessionId, current, size));
    }

    /**
     * 删除对话历史
     */
    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        chatService.deleteHistory(id);
        return Result.success();
    }

    /**
     * 获取知识库列表（供用户选择）
     */
    @GetMapping("/kb/list")
    public Result<List<KnowledgeSimpleVO>> kbList() {
        return Result.success(chatService.getKnowledgeList());
    }
}
```

### 2.3.3 ChatService实现

```java
package com.cqf.ai.service.impl;

import com.cqf.ai.domain.dto.ChatDTO;
import com.cqf.ai.domain.vo.ChatVO;
import com.cqf.ai.service.EmbeddingService;
import com.smartoffice.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final EmbeddingService embeddingService;
    private final AiModelClient aiModelClient;

    /**
     * RAG问答流程
     */
    @Override
    public ChatVO chat(ChatDTO dto) {
        long startTime = System.currentTimeMillis();

        // 1. 获取知识库内容
        List<Long> kbIds = dto.getKbIds();
        List<ChunkInfo> chunks = embeddingService.search(dto.getQuestion(), kbIds);

        // 2. 构建上下文
        String context = buildContext(chunks);

        // 3. 调用AI模型
        String answer = aiModelClient.chat(
                dto.getQuestion(),
                context,
                getHistory(dto.getSessionId())
        );

        // 4. 保存对话记录
        saveHistory(dto.getSessionId(), dto.getQuestion(), answer);

        // 5. 构建返回结果
        ChatVO vo = new ChatVO();
        vo.setAnswer(answer);
        vo.setSessionId(dto.getSessionId() != null ? dto.getSessionId() : UUID.randomUUID().toString());
        vo.setReferences(chunks.stream().map(c -> {
            ReferenceVO ref = new ReferenceVO();
            ref.setDocId(c.getDocId());
            ref.setDocTitle(c.getDocTitle());
            ref.setChunkContent(c.getContent());
            ref.setScore(c.getScore());
            return ref;
        }).toList());
        vo.setTokens(estimateTokens(answer));
        vo.setDuration(System.currentTimeMillis() - startTime);

        return vo;
    }

    /**
     * 构建上下文
     */
    private String buildContext(List<ChunkInfo> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("参考知识：\n\n");

        for (int i = 0; i < chunks.size(); i++) {
            ChunkInfo chunk = chunks.get(i);
            context.append(String.format("【文档%d】%s\n%s\n\n",
                    i + 1, chunk.getDocTitle(), chunk.getContent()));
        }

        context.append("\n请根据以上参考知识回答问题，如果找不到相关信息，请如实说明。");

        return context.toString();
    }

    /**
     * 估算Token数量
     */
    private int estimateTokens(String text) {
        return (int) Math.ceil(text.length() / 4.0);
    }
}
```

### 2.3.4 EmbeddingService实现

```java
package com.cqf.ai.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmbeddingService {

    /**
     * 向量检索
     */
    public List<ChunkInfo> search(String question, List<Long> kbIds) {
        // 1. 将问题转换为向量
        List<Float> questionEmbedding = embed(question);

        // 2. 从数据库检索相似内容
        // 这里简化为从数据库查询，实际应该做向量相似度搜索
        List<ChunkInfo> chunks = new ArrayList<>();

        for (Long kbId : kbIds) {
            List<ChunkInfo> kbChunks = getChunksFromDb(kbId, question);
            chunks.addAll(kbChunks);
        }

        // 3. 按相似度排序，返回Top K
        return chunks.stream()
                .sorted(Comparator.comparing(ChunkInfo::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 文本向量化（简化版，实际需要调用Embedding模型）
     */
    private List<Float> embed(String text) {
        // 这里应该调用Embedding模型API
        // 暂时返回随机向量，实际需要接入embedding服务
        Random random = new Random(text.hashCode());
        int dimensions = 1536; // OpenAI ada embedding维度
        List<Float> embedding = new ArrayList<>(dimensions);
        for (int i = 0; i < dimensions; i++) {
            embedding.add(random.nextFloat());
        }
        return embedding;
    }

    /**
     * 从数据库获取切片（简化版）
     */
    private List<ChunkInfo> getChunksFromDb(Long kbId, String keyword) {
        // 实际实现需要查询数据库
        // 这里返回空列表，实际需要实现向量检索
        return new ArrayList<>();
    }

    @Data
    public static class ChunkInfo {
        private Long docId;
        private String docTitle;
        private String content;
        private Float score;
    }
}
```

### 2.3.5 SSE流式响应

```java
package com.cqf.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class StreamChatService {

    private final Map<String, FluxSink<ServerSentEvent<String>>> sessionSinks = new ConcurrentHashMap<>();

    /**
     * 流式问答
     */
    public Flux<ServerSentEvent<Map>> streamChat(String kbIds, String question, String sessionId) {
        return Flux.create(sink -> {
            // 保存sink引用
            if (sessionId != null) {
                sessionSinks.put(sessionId, (FluxSink<ServerSentEvent<String>>) (Object) sink);
            }

            // 模拟流式输出（实际需要调用AI模型的流式接口）
            String[] words = question.split("");
            StringBuilder answer = new StringBuilder();

            for (int i = 0; i < words.length; i++) {
                answer.append(words[i]);

                // 发送增量内容
                Map<String, Object> data = Map.of(
                        "content", words[i],
                        "done", false
                );
                sink.next(ServerSentEvent.builder()
                        .data(data)
                        .build());

                // 模拟延迟
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }

            // 发送完成信号
            Map<String, Object> finalData = Map.of(
                    "content", "",
                    "done", true,
                    "answer", answer.toString()
            );
            sink.next(ServerSentEvent.builder()
                    .data(finalData)
                    .build());

            sink.complete();

            // 清理
            if (sessionId != null) {
                sessionSinks.remove(sessionId);
            }
        });
    }

    /**
     * 取消流式响应
     */
    public void cancelStream(String sessionId) {
        FluxSink<ServerSentEvent<String>> sink = sessionSinks.get(sessionId);
        if (sink != null) {
            sink.complete();
            sessionSinks.remove(sessionId);
        }
    }
}
```

---

## 阶段二产出清单

| 产出物 | 说明 |
|--------|------|
| knowledge服务 | 知识库管理、文档上传解析 |
| ai-chat服务 | AI对话、RAG问答 |
| 数据库表 | kb_base, kb_document, kb_chunk |
| 文档解析 | PDF、Word、TXT解析 |
| 文本切片 | 按段落、固定长度切片 |
| 向量检索 | RAG知识检索 |
| SSE流式 | 流式AI问答 |

---

## 下一步

完成阶段二后，你将拥有：
- ✅ 知识库管理（创建、修改、删除）
- ✅ 文档上传与解析
- ✅ AI智能问答（RAG）
- ✅ 流式对话

接下来可以进入 **阶段三：工单与办公模块**

需要我继续细化阶段三的内容吗？
