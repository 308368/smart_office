# 阶段四：AI对话模块 - 开发指南

> 阶段三完成后，进入第四阶段：AI对话 + RAG检索

---

## 阶段四总览

```
阶段四：AI对话 + RAG检索
│
├── 4.1 AI服务搭建 (ai-service:8085)
├── 4.2 数据库表设计（会话、消息）
├── 4.3 AI对话接口开发
├── 4.4 RAG检索增强
├── 4.5 前端对话界面
│
└── 阶段产出：智能问答 + 会话管理 + RAG检索
```

预计完成时间：**10天**

---

## 4.1 创建AI服务

### 4.1.1 服务结构

```
smart-office-ai/                   # AI服务 (8085)
├── pom.xml
└── src/main/java/com/cqf/ai/
    ├── controller/
    │   └── ChatController.java     # AI对话
    ├── service/
    │   ├── ChatService.java
    │   └── RAGService.java         # RAG检索
    ├── model/
    │   ├── entity/
    │   ├── dto/
    │   └── vo/
    ├── config/
    │   └── SpringAIConfig.java     # AI配置
    └── mapper/
```

### 4.1.2 pom.xml依赖

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
        <artifactId>smart-office-common</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Spring AI -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-qianwen</artifactId>
        <version>1.0.0.M5</version>
    </dependency>

    <!-- 向量数据库（可选：若使用Milvus/PgVector） -->
    <!--
    <dependency>
        <groupId>io.milvus</groupId>
        <artifactId>milvus-sdk-java</artifactId>
        <version>2.3.4</version>
    </dependency>
    -->
</dependencies>
```

### 4.1.3 配置文件 (bootstrap.yml)

```yaml
server:
  port: 8085

spring:
  application:
    name: ai-service
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.220.100:8848
        namespace: d43fb09d-9144-4b48-94a9-06c078581a8b
      config:
        server-addr: 192.168.220.100:8848
        namespace: d43fb09d-9144-4b48-94a9-06c078581a8b
        file-extension: yaml
        shared-configs:
          - data-id: common.yaml
            group: SmartOffice-project
            refresh: true

  datasource:
    url: jdbc:mysql://localhost:3306/smart-office?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456

  ai:
    qianwen:
      api-key: ${QW_API_KEY}  # 阿里云百炼API Key
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      chat-options:
        model: qwen-plus  # 或 qwen-turbo、qwen-max 等
```

---

## 4.2 数据库表设计

### 4.2.1 AI对话相关表

```sql
-- 对话会话表
CREATE TABLE chat_session (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    session_no      VARCHAR(50)    NOT NULL UNIQUE COMMENT '会话编号',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    title           VARCHAR(200)    COMMENT '会话标题',
    model           VARCHAR(50)     COMMENT '使用的模型',
    status          TINYINT         DEFAULT 0 COMMENT '状态 0结束 1进行中',
    message_count   INT             DEFAULT 0 COMMENT '消息条数',
    last_message    TEXT            COMMENT '最后一条消息摘要',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT 'AI对话会话表';

-- 对话消息表
CREATE TABLE chat_message (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    session_id      BIGINT          NOT NULL COMMENT '会话ID',
    role            VARCHAR(20)     NOT NULL COMMENT '角色 user/assistant/system',
    content         TEXT            NOT NULL COMMENT '消息内容',
    references      JSON            COMMENT '引用的知识片段',
    tokens          INT             COMMENT '消耗的token数',
    duration        INT             COMMENT '响应耗时(ms)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) COMMENT 'AI对话消息表';

-- 提示词模板表
CREATE TABLE chat_prompt (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(50)    NOT NULL COMMENT '模板名称',
    content         TEXT            NOT NULL COMMENT '系统提示词',
    variables       VARCHAR(500)    COMMENT '变量列表，逗号分隔',
    status          TINYINT         DEFAULT 1 COMMENT '状态',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP
) COMMENT 'AI提示词模板表';

-- 初始化提示词模板
INSERT INTO chat_prompt (name, content, variables) VALUES
('默认助手', '你是公司的智能办公助手，可以回答关于公司制度、流程等问题。如果不确定答案，请如实说明。', '');
```

### 4.2.2 RAG相关表（复用知识库）

知识库的文档分片表 `kb_document_chunk` 已设计好，支持向量存储：

```sql
-- 文档分片表（已在阶段二创建）
CREATE TABLE kb_document_chunk (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    document_id     BIGINT          NOT NULL COMMENT '文档ID',
    content         TEXT            NOT NULL COMMENT '分片内容',
    vector          JSON            COMMENT '向量数据',
    token_count     INT             DEFAULT 0 COMMENT 'token数',
    status          TINYINT         DEFAULT 0 COMMENT '状态 0待处理 1已向量化 2失败',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_document_id (document_id),
    INDEX idx_status (status)
) COMMENT '文档分片表';
```

---

## 4.3 AI对话接口

### 4.3.1 后端Controller

```java
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final RAGService ragService;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody @Valid ChatRequest request) {
        return Result.success(chatService.chat(request));
    }

    @GetMapping("/chat/stream")
    public SseEmitter chatStream(@RequestParam String question,
                                  @RequestParam String kbIds,
                                  @RequestParam(required = false) String sessionId) {
        return chatService.chatStream(question, kbIds, sessionId);
    }

    @GetMapping("/history")
    public Result<PageResult<SessionVO>> history(QueryParam param) {
        return Result.success(chatService.getHistory(param));
    }

    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        chatService.deleteHistory(id);
        return Result.success();
    }

    @GetMapping("/kb/list")
    public Result<List<KbSimpleVO>> listKbs() {
        return Result.success(ragService.listAvailableKbs());
    }
}
```

### 4.3.2 核心服务实现

```java
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final RAGService ragService;
    private final ChatMemory chatMemory;
    private final QianwenChatModel chatModel;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. 获取或创建会话
        ChatSession session = getOrCreateSession(request.getSessionId(), username);

        // 2. RAG检索：获取相关知识片段
        List<Document> documents = ragService.retrieve(request.getKbIds(), request.getQuestion());

        // 3. 构建提示词（包含检索到的知识）
        String systemPrompt = buildSystemPrompt(documents);
        UserMessage userMessage = new UserMessage(request.getQuestion());

        // 4. 调用LLM
        ChatResponse response = new ChatResponse();
        long startTime = System.currentTimeMillis();

        String answer = chatModel.call(new Prompt(
            List.of(new SystemMessage(systemPrompt), userMessage)
        )).getResult().getOutput().getContent();

        long duration = System.currentTimeMillis() - startTime;

        // 5. 保存消息
        saveMessages(session.getId(), request.getQuestion(), answer, documents, duration);

        response.setAnswer(answer);
        response.setReferences(documents.stream().map(doc ->
            ReferenceVO.builder()
                .docId(doc.getMetadata("docId"))
                .docTitle(doc.getMetadata("title"))
                .chunkContent(doc.getContent())
                .score(doc.getMetadata("score"))
                .build()
        ).toList());
        response.setSessionId(session.getSessionNo());
        response.setTokens(estimateTokens(answer));
        response.setDuration(duration);

        return response;
    }

    // 流式响应（SSE）
    public SseEmitter chatStream(String question, String kbIds, String sessionId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5分钟超时

        CompletableFuture.runAsync(() -> {
            try {
                // RAG检索
                List<Document> documents = ragService.retrieve(kbIds, question);

                // 构建提示词
                String systemPrompt = buildSystemPrompt(documents);

                // 流式调用
                chatModel.stream(new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(question)
                ))).subscribe(chunk -> {
                    try {
                        emitter.send(SseEmitter.event()
                            .data("{\"content\":\"" + chunk.getResult().getOutput().getContent() + "\",\"done\":false}"));
                    } catch (IOException e) {
                        log.error("SSE发送失败", e);
                    }
                });

                // 发送完成信号
                emitter.send(SseEmitter.event()
                    .data("{\"done\":true,\"references\":" + toJson(documents) + "}"));
                emitter.complete();

            } catch (Exception e) {
                log.error("流式对话异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String buildSystemPrompt(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "你是公司的智能办公助手，可以回答关于公司制度、流程等问题。如果不确定答案，请如实说明。";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能助手，请根据以下参考知识回答用户问题。\n\n");
        prompt.append("【参考知识】：\n");
        documents.forEach(doc -> {
            prompt.append("- ").append(doc.getContent()).append("\n");
        });
        prompt.append("\n请基于上述知识回答，如果不在知识范围内，请说明\"该信息不在知识库中\"。");

        return prompt.toString();
    }
}
```

### 4.3.3 RAG服务实现

```java
@Service
@RequiredArgsConstructor
public class RAGService {

    private final KbDocumentChunkMapper chunkMapper;
    private final EmbeddingModel embeddingModel;

    /**
     * 检索知识库，返回相关文档片段
     */
    public List<Document> retrieve(String kbIds, String question) {
        if (StringUtils.isBlank(kbIds)) {
            return Collections.emptyList();
        }

        // 1. 将问题向量化
        EmbeddingRequest embeddingRequest = new EmbeddingRequest(
            EmbeddingInput.of(Collections.singletonList(question))
        );
        EmbeddingResponse embeddingResponse = embeddingModel.call(embeddingRequest);
        List<Float> questionVector = embeddingResponse.getResults().get(0).getEmbedding();

        // 2. 在数据库中搜索相似片段（简化的向量相似度搜索）
        // 实际生产环境建议使用Milvus/PgVector等向量数据库
        List<KbDocumentChunk> chunks = chunkMapper.searchSimilar(
            Arrays.stream(kbIds.split(","))
                .map(Long::parseLong)
                .toList(),
            questionVector,
            5  // 返回前5条
        );

        // 3. 构建Document对象
        return chunks.stream().map(chunk -> {
            Document doc = new Document(chunk.getContent());
            doc.putMetadata("docId", chunk.getDocumentId().toString());
            doc.putMetadata("title", chunk.getDocumentTitle());
            doc.putMetadata("score", chunk.getSimilarity());
            return doc;
        }).toList();
    }

    /**
     * 文档向量化（文档解析时调用）
     */
    @Transactional
    public void embedChunk(KbDocumentChunk chunk) {
        try {
            // 调用Embedding模型
            EmbeddingRequest request = new EmbeddingRequest(
                EmbeddingInput.of(Collections.singletonList(chunk.getContent()))
            );
            EmbeddingResponse response = embeddingModel.call(request);
            List<Float> vector = response.getResults().get(0).getEmbedding();

            // 存入数据库
            chunk.setVector(toJson(vector));
            chunk.setTokenCount(estimateTokens(chunk.getContent()));
            chunk.setStatus(1); // 已向量化
            chunkMapper.updateById(chunk);

        } catch (Exception e) {
            log.error("向量化失败", e);
            chunk.setStatus(2); // 失败
            chunkMapper.updateById(chunk);
        }
    }
}
```

---

## 4.4 数据库向量搜索（简化实现）

对于没有专用向量数据库的场景，可以使用MySQL的JSON类型存储向量，并使用近似算法进行搜索：

### 4.4.1 KbDocumentChunkMapper

```java
@Mapper
public interface KbDocumentChunkMapper extends BaseMapper<KbDocumentChunk> {

    @Select("<script>" +
        "SELECT c.*, " +
        "  (SELECT MAX(vec_close_point(vector, #{vector})) FROM kb_document_chunk WHERE document_id IN " +
        "    (SELECT id FROM kb_document WHERE kb_id IN <foreach collection='kbIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>)) " +
        "  ) as similarity " +
        "FROM kb_document_chunk c " +
        "WHERE c.document_id IN (SELECT id FROM kb_document WHERE kb_id IN " +
        "  <foreach collection='kbIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>) " +
        "ORDER BY similarity DESC LIMIT #{topK}" +
        "</script>")
    List<KbDocumentChunk> searchSimilar(@Param("kbIds") List<Long> kbIds,
                                        @Param("vector") List<Float> vector,
                                        @Param("topK") int topK);
}
```

**注意**：MySQL 8.0+ 内置了 `vec_close_point` 函数用于向量相似度计算。

---

## 4.5 前端对话界面

### 4.5.1 路由配置

```typescript
// router/index.ts
{
  path: '/ai',
  children: [
    { path: 'chat', component: '@/views/ai/chat/index.vue', meta: { title: 'AI助手' } }
  ]
}
```

### 4.5.2 页面结构

```
src/views/ai/chat/
├── index.vue        # AI对话主页面
└── components/
    ├── ChatSidebar.vue    # 会话列表侧边栏
    ├── ChatMessage.vue    # 消息组件
    └── KbSelector.vue     # 知识库选择器
```

### 4.5.3 核心页面实现

```vue
<template>
  <div class="ai-chat-page">
    <!-- 侧边栏：会话列表 -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <span>会话记录</span>
        <el-button text @click="createSession">+ 新对话</el-button>
      </div>
      <div class="session-list">
        <div v-for="session in sessions"
             :key="session.id"
             :class="['session-item', { active: session.id === currentSessionId }]"
             @click="selectSession(session)">
          <span class="title">{{ session.title || '新对话' }}</span>
          <el-button text size="small" @click.stop="deleteSession(session.id)">删除</el-button>
        </div>
      </div>
    </aside>

    <!-- 主聊天区域 -->
    <main class="chat-main">
      <!-- 知识库选择 -->
      <div class="kb-selector">
        <el-select v-model="selectedKbIds" multiple placeholder="选择知识库" style="width: 300px">
          <el-option v-for="kb in kbList" :key="kb.id" :label="kb.name" :value="kb.id" />
        </el-select>
      </div>

      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <div v-for="msg in messages" :key="msg.id" :class="['message', msg.role]">
          <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
          <div class="content">
            <div v-if="msg.role === 'assistant'" v-html="renderMarkdown(msg.content)" />
            <div v-else>{{ msg.content }}</div>

            <!-- 引用来源 -->
            <div v-if="msg.references && msg.references.length" class="references">
              <div class="ref-title">参考知识：</div>
              <div v-for="ref in msg.references" :key="ref.docId" class="ref-item">
                <span class="doc-title">{{ ref.docTitle }}</span>
                <span class="chunk-content">{{ ref.chunkContent }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          placeholder="输入问题，按Enter发送..."
          @keyup.enter.ctrl="sendMessage"
        />
        <el-button type="primary" @click="sendMessage" :loading="sending">发送</el-button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { chat, getHistory, deleteHistory, listKbs } from '@/api/ai'
import { marked } from 'marked'

const inputText = ref('')
const sending = ref(false)
const messages = ref<any[]>([])
const sessions = ref<any[]>([])
const currentSessionId = ref<string | null>(null)
const selectedKbIds = ref<number[]>([])
const kbList = ref<any[]>([])
const messageListRef = ref<HTMLElement>()

// 加载知识库列表
const loadKbList = async () => {
  const res = await listKbs()
  kbList.value = res.data || []
}

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim() || sending.value) return

  const question = inputText.value.trim()
  inputText.value = ''

  // 添加用户消息
  messages.value.push({ role: 'user', content: question })
  scrollToBottom()

  try {
    sending.value = true
    const res = await chat({
      question,
      kbIds: selectedKbIds.value,
      sessionId: currentSessionId.value
    })

    // 添加AI回复
    messages.value.push({
      role: 'assistant',
      content: res.data.answer,
      references: res.data.references
    })
    currentSessionId.value = res.data.sessionId

    scrollToBottom()
  } catch (error) {
    console.error(error)
  } finally {
    sending.value = false
  }
}

// Markdown渲染
const renderMarkdown = (content: string) => {
  return marked.parse(content)
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  loadKbList()
  // 加载会话历史
})
</script>

<style scoped lang="scss">
.ai-chat-page {
  display: flex;
  height: 100%;
  background: #f5f5f5;

  .chat-sidebar {
    width: 250px;
    background: #fff;
    border-right: 1px solid #e0e0e0;
    padding: 16px;

    .session-list {
      .session-item {
        padding: 12px;
        cursor: pointer;
        border-radius: 8px;
        margin-bottom: 8px;

        &.active {
          background: #e3f2fd;
        }

        &:hover {
          background: #f5f5f5;
        }
      }
    }
  }

  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;

    .message-list {
      flex: 1;
      overflow-y: auto;
      padding: 20px;

      .message {
        display: flex;
        margin-bottom: 20px;

        &.user {
          flex-direction: row-reverse;

          .content {
            background: #1976d2;
            color: #fff;
          }
        }

        &.assistant .content {
          background: #fff;
        }

        .content {
          max-width: 70%;
          padding: 12px 16px;
          border-radius: 12px;
        }

        .references {
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px solid #e0e0e0;
          font-size: 12px;

          .ref-item {
            padding: 8px;
            background: #f5f5f5;
            margin-top: 4px;
            border-radius: 4px;
          }
        }
      }
    }

    .input-area {
      padding: 16px;
      background: #fff;
      display: flex;
      gap: 12px;

      .el-textarea {
        flex: 1;
      }
    }
  }
}
</style>
```

---

## 4.6 API接口定义

### 4.6.1 AI问答（非流式）

```
POST /ai/chat
Content-Type: application/json
Authorization: Bearer <token>

{
    "kbIds": [1, 2],
    "question": "年假申请需要哪些材料？",
    "sessionId": "abc123"
}

Response:
{
    "code": 200,
    "data": {
        "answer": "年假申请需要以下材料：...",
        "references": [...],
        "sessionId": "abc123",
        "tokens": 150,
        "duration": 2000
    }
}
```

### 4.6.2 AI问答（流式/SSE）

```
GET /ai/chat/stream?kbIds=1,2&question=年假申请&sessionId=abc123

Response: text/event-stream

data: {"content": "年", "done": false}
data: {"content": "假", "done": false}
...
data: {"content": "", "done": true, "references": [...], "tokens": 150}
```

### 4.6.3 会话历史

```
GET /ai/history?current=1&size=10

Response:
{
    "code": 200,
    "data": {
        "records": [
            {
                "id": 1,
                "sessionNo": "CS20260416001",
                "title": "Java代码优化咨询",
                "messageCount": 6,
                "lastMessage": "谢谢解答",
                "createTime": "2026-04-16 10:00:00"
            }
        ],
        "total": 50
    }
}
```

### 4.6.4 删除会话

```
DELETE /ai/history/{id}
```

### 4.6.5 知识库列表（供选择）

```
GET /ai/kb/list

Response:
{
    "code": 200,
    "data": [
        {"id": 1, "name": "员工手册", "docCount": 23},
        {"id": 2, "name": "考勤制度", "docCount": 15}
    ]
}
```

---

## 4.7 权限配置

### 4.7.1 菜单SQL

```sql
-- AI对话菜单
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, permission, status, create_time) VALUES
(0, 'AI助手', '/ai', NULL, 'C', 'ChatDotted', 6, NULL, 1, NOW());

INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, permission, status, create_time) VALUES
(<parent_id>, 'AI对话', '/ai/chat', 'ai/chat/index', 'M', 'ChatLine', 1, 'ai:chat', 1, NOW()),
(<parent_id>, '会话记录', '/ai/history', 'ai/history/index', 'M', 'History', 2, 'ai:history', 1, NOW());
```

### 4.7.2 权限说明

| 权限标识 | 说明 |
|---------|------|
| ai:chat | AI问答 |
| ai:history | 查看会话历史 |
| ai:history:clear | 删除会话 |

---

## 阶段四产出清单

| 产出物 | 说明 |
|--------|------|
| ai-service | 端口8085，AI对话服务 |
| 数据库表 | chat_session, chat_message, chat_prompt |
| 后端接口 | AI问答、流式响应、会话管理 |
| 前端页面 | AI对话界面、会话列表 |
| RAG检索 | 向量化文档检索 |

---

## 下一步

完成阶段四后，项目将包含：
- ✅ 用户权限管理
- ✅ 知识库与文档解析
- ✅ 工单全流程管理
- ✅ 请假申请与审批
- ✅ 通知公告发布
- ✅ AI智能问答 + RAG检索

---

## 阶段五预告（可选）：系统优化与扩展

```
阶段五：系统优化与扩展
│
├── 5.1 数据统计与看板
├── 5.2 消息通知中心（完善）
├── 5.3 移动端适配
└── 5.4 性能优化
```

---

*最后更新: 2026-04-16*
