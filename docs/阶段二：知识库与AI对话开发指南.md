# 阶段二：知识库与AI对话 - 开发指南

> 本指南严格按照原始sql/init-databases.sql中的表结构设计

---

## 阶段二总览

```
阶段二：知识库管理 + AI对话
│
├── 2.1 知识库管理模块（知识库服务:8083）
├── 2.2 文档解析与向量化
├── 2.3 AI对话模块（AI服务:8084）
│
└── 阶段产出：完整的知识库管理 + RAG问答
```

---

## 2.1 知识库管理模块

### 2.1.1 项目结构

```
smart-office-knowledge/              # 知识库服务 (8083)
├── src/main/java/com/cqf/knowledge/
│   ├── controller/
│   │   ├── KnowledgeBaseController.java   # 知识库CRUD
│   │   └── DocumentController.java        # 文档管理
│   ├── service/
│   │   ├── KnowledgeBaseService.java
│   │   └── DocumentService.java
│   ├── mapper/
│   │   ├── KnowledgeBaseMapper.java
│   │   ├── DocumentMapper.java
│   │   └── DocumentChunkMapper.java
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── KnowledgeBase.java          # 知识库实体
│   │   │   ├── Document.java               # 文档实体
│   │   │   └── DocumentChunk.java          # 切片实体
│   │   ├── dto/
│   │   └── vo/
│   └── config/
└── pom.xml
```

### 2.1.2 数据库表（严格按原始SQL）

**so-knowledge 库 - 知识库表**

```sql
-- 知识库表 kb_knowledge_base
CREATE TABLE `kb_knowledge_base` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `name`            VARCHAR(100)    NOT NULL COMMENT '知识库名称',
    `description`     VARCHAR(500)    COMMENT '知识库描述',
    `cover_image`     VARCHAR(500)    COMMENT '封面图',
    `doc_count`       INT             DEFAULT 0 COMMENT '文档数量',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 0私密 1公开',
    `create_by`       BIGINT          COMMENT '创建人ID',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_name (`name`),
    INDEX idx_create_by (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| name | VARCHAR(100) | 知识库名称，必填 |
| description | VARCHAR(500) | 知识库描述 |
| cover_image | VARCHAR(500) | 封面图URL |
| doc_count | INT | 文档数量，默认0 |
| status | TINYINT | 状态：0私密/1公开，默认1 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除：0否/1是 |

---

**so-knowledge 库 - 文档表**

```sql
-- 知识库文档表 kb_document
CREATE TABLE `kb_document` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `kb_id`           BIGINT          NOT NULL COMMENT '知识库ID',
    `title`           VARCHAR(200)    NOT NULL COMMENT '文档标题',
    `content`         LONGTEXT        COMMENT '文档内容',
    `file_url`        VARCHAR(500)    COMMENT '文件URL',
    `file_type`       VARCHAR(50)     COMMENT '文件类型',
    `file_size`       BIGINT          COMMENT '文件大小(字节)',
    `chunk_count`     INT             DEFAULT 0 COMMENT '切片数量',
    `status`          TINYINT         DEFAULT 0 COMMENT '状态 0待处理 1处理中 2已完成 3处理失败',
    `create_by`       BIGINT          COMMENT '创建人ID',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_kb_id (`kb_id`),
    INDEX idx_status (`status`),
    INDEX idx_create_by (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| kb_id | BIGINT | 所属知识库ID，必填 |
| title | VARCHAR(200) | 文档标题，必填 |
| content | LONGTEXT | 文档内容文本 |
| file_url | VARCHAR(500) | 原始文件存储URL |
| file_type | VARCHAR(50) | 文件类型(pdf/doc/txt等) |
| file_size | BIGINT | 文件大小(字节) |
| chunk_count | INT | 切片数量，默认0 |
| status | TINYINT | 处理状态：0待处理/1处理中/2已完成/3失败 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

---

**so-knowledge 库 - 文档切片表**

```sql
-- 文档向量切片表 kb_document_chunk
CREATE TABLE `kb_document_chunk` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `doc_id`          BIGINT          NOT NULL COMMENT '文档ID',
    `chunk_index`     INT             NOT NULL COMMENT '切片索引',
    `content`         TEXT            NOT NULL COMMENT '切片内容',
    `vector`          JSON COMMENT '向量数据',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_doc_id (`doc_id`),
    INDEX idx_chunk_index (`chunk_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档向量切片表';
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| doc_id | BIGINT | 文档ID，必填 |
| chunk_index | INT | 切片索引序号 |
| content | TEXT | 切片内容文本 |
| vector | JSON | 向量数据(Embedding) |
| create_time | DATETIME | 创建时间 |

---

### 2.1.3 实体类设计

**KnowledgeBase.java**
```java
@Data
@TableName("kb_knowledge_base")
public class KnowledgeBase {
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private Integer docCount;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
```

**Document.java**
```java
@Data
@TableName("kb_document")
public class Document {
    private Long id;
    private Long kbId;
    private String title;
    private String content;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Integer chunkCount;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
```

**DocumentChunk.java**
```java
@Data
@TableName("kb_document_chunk")
public class DocumentChunk {
    private Long id;
    private Long docId;
    private Integer chunkIndex;
    private String content;
    private String vector;
    private LocalDateTime createTime;
}
```

---

### 2.1.4 接口设计

**知识库CRUD接口**

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 知识库列表 | GET | /knowledge/kb/list | 分页查询知识库 |
| 知识库详情 | GET | /knowledge/kb/{id} | 获取知识库详情 |
| 创建知识库 | POST | /knowledge/kb/create | 创建新知识库 |
| 修改知识库 | PUT | /knowledge/kb | 修改知识库 |
| 删除知识库 | DELETE | /knowledge/kb/{id} | 删除知识库 |
| 用户知识库列表 | GET | /knowledge/kb/user/list | 获取用户可访问的知识库 |

**文档管理接口**

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 上传文档 | POST | /knowledge/kb/{kbId}/doc/upload | 上传文档到知识库 |
| 文档列表 | GET | /knowledge/kb/{kbId}/doc/list | 获取知识库文档列表 |
| 删除文档 | DELETE | /knowledge/kb/{kbId}/doc/{docId} | 删除文档 |
| 重建索引 | POST | /knowledge/kb/{kbId}/doc/{docId}/rebuild | 重建文档索引 |

---

## 2.2 文档解析与向量化

### 2.2.1 文档解析服务

支持的文件类型：
- PDF (.pdf) - 使用Apache PDFBox
- Word (.doc/.docx) - 使用Apache POI
- TXT (.txt) - 直接读取文本

**DocumentParseService.java**
```java
@Service
public class DocumentParseService {

    /**
     * 解析PDF文档
     */
    public String parsePdf(MultipartFile file) { ... }

    /**
     * 解析Word文档
     */
    public String parseWord(MultipartFile file) { ... }

    /**
     * 解析TXT文档
     */
    public String parseTxt(MultipartFile file) { ... }

    /**
     * 根据文件类型选择解析方式
     */
    public String parse(MultipartFile file) { ... }
}
```

### 2.2.2 文本切片服务

**ChunkService.java**
```java
@Service
public class ChunkService {

    /**
     * 按段落切片
     */
    public List<ChunkResult> chunkByParagraph(String content, int maxTokens) { ... }

    /**
     * 按固定长度切片（重叠）
     */
    public List<ChunkResult> chunkByLength(String content, int maxTokens, int overlapTokens) { ... }

    @Data
    public static class ChunkResult {
        private String content;
        private int wordCount;
        private int tokenCount;
    }
}
```

---

## 2.3 AI对话模块

### 2.3.1 项目结构

```
smart-office-ai/                    # AI对话服务 (8084)
├── src/main/java/com/cqf/ai/
│   ├── controller/
│   │   └── ChatController.java
│   ├── service/
│   │   ├── ChatService.java
│   │   └── EmbeddingService.java
│   ├── mapper/
│   │   ├── ChatSessionMapper.java
│   │   ├── ChatMessageMapper.java
│   │   └── ChatPromptMapper.java
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── ChatSession.java
│   │   │   ├── ChatMessage.java
│   │   │   └── ChatPrompt.java
│   │   └── dto/
│   └── config/
└── pom.xml
```

### 2.3.2 数据库表（严格按原始SQL）

**so-chat 库 - 会话表**

```sql
-- AI对话会话表 chat_session
CREATE TABLE `chat_session` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id`         BIGINT          NOT NULL COMMENT '用户ID',
    `title`           VARCHAR(200)    COMMENT '会话标题',
    `model`           VARCHAR(50)     DEFAULT 'qwen-plus' COMMENT '使用模型',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 0结束 1进行中',
    `message_count`   INT             DEFAULT 0 COMMENT '消息数量',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (`user_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';
```

---

**so-chat 库 - 消息表**

```sql
-- AI对话消息表 chat_message
CREATE TABLE `chat_message` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `session_id`      BIGINT          NOT NULL COMMENT '会话ID',
    `role`            VARCHAR(20)     NOT NULL COMMENT '角色 user/assistant/system',
    `content`         LONGTEXT        NOT NULL COMMENT '消息内容',
    `model`           VARCHAR(50)     COMMENT '使用的模型',
    `tokens`          INT             COMMENT '消耗的token',
    `cost_time`       BIGINT          COMMENT '响应耗时(毫秒)',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (`session_id`),
    INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';
```

---

**so-chat 库 - 提示词模板表**

```sql
-- 提示词模板表 chat_prompt
CREATE TABLE `chat_prompt` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `name`            VARCHAR(100)    NOT NULL COMMENT '模板名称',
    `description`    VARCHAR(500)    COMMENT '模板描述',
    `prompt`          TEXT            NOT NULL COMMENT '提示词内容',
    `category`        VARCHAR(50)     COMMENT '分类',
    `is_public`       TINYINT         DEFAULT 0 COMMENT '是否公共模板 0私有 1公共',
    `create_by`       BIGINT          COMMENT '创建人ID',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_create_by (`create_by`),
    INDEX idx_is_public (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板表';
```

---

### 2.3.3 实体类设计

**ChatSession.java**
```java
@Data
@TableName("chat_session")
public class ChatSession {
    private Long id;
    private Long userId;
    private String title;
    private String model;
    private Integer status;
    private Integer messageCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**ChatMessage.java**
```java
@Data
@TableName("chat_message")
public class ChatMessage {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String model;
    private Integer tokens;
    private Long costTime;
    private LocalDateTime createTime;
}
```

**ChatPrompt.java**
```java
@Data
@TableName("chat_prompt")
public class ChatPrompt {
    private Long id;
    private String name;
    private String description;
    private String prompt;
    private String category;
    private Integer isPublic;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

---

### 2.3.4 接口设计

**AI对话接口**

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| AI问答 | POST | /ai/chat | 非流式AI问答 |
| AI问答(流式) | GET | /ai/chat/stream | SSE流式AI问答 |
| 对话历史 | GET | /ai/history | 获取对话历史记录 |
| 删除历史 | DELETE | /ai/history/{id} | 删除对话记录 |
| 知识库列表 | GET | /ai/kb/list | 获取可用的知识库列表 |

---

## 阶段二产出清单

| 产出物 | 说明 |
|--------|------|
| knowledge服务 | 知识库管理、文档上传解析 (端口8083) |
| ai-service服务 | AI对话、RAG问答 (端口8084) |
| 数据库表 | kb_knowledge_base, kb_document, kb_document_chunk |
| 数据库表 | chat_session, chat_message, chat_prompt |
| 文档解析 | PDF、Word、TXT解析 |
| 文本切片 | 按段落、固定长度切片 |
| 向量检索 | RAG知识检索 |
| SSE流式 | 流式AI问答 |

---

## 服务端口规划

| 服务 | 端口 | 说明 |
|------|------|------|
| 网关 | 8080 | 统一入口 |
| 认证服务 | 8081 | 登录/注册 |
| 网关(备用) | 8082 | 备用入口 |
| 知识库服务 | 8083 | 知识库管理 |
| AI服务 | 8084 | AI对话 |

---

## 下一步

完成阶段二后，你将拥有：
- ✅ 知识库管理（创建、修改、删除）
- ✅ 文档上传与解析
- ✅ AI智能问答（RAG）
- ✅ 流式对话

接下来可以进入 **阶段三：工单与办公模块**

---

*最后更新: 2026-03-31*