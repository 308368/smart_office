<template>
  <div class="ai-chat-page">
    <!-- 顶部知识库选择 -->
    <div class="chat-header">
      <h2>🤖 AI 智能助手</h2>
      <div class="kb-selector">
        <el-select v-model="selectedKbId" placeholder="请选择知识库" clearable @change="handleKbChange">
          <el-option
            v-for="kb in groupedKbList"
            :key="kb.kbId"
            :label="kb.kbName"
            :value="kb.kbId"
          >
            <span>{{ kb.kbName }}</span>
            <span class="doc-count">({{ kb.docs.length }}篇)</span>
          </el-option>
        </el-select>
        <el-checkbox-group v-model="selectedDocIds" @change="handleDocChange">
          <el-checkbox
            v-for="doc in currentKbDocs"
            :key="doc.docId"
            :label="doc.docId"
          >
            {{ doc.docTitle }}
            <el-tag size="small" type="info">{{ doc.fileType }}</el-tag>
          </el-checkbox>
        </el-checkbox-group>
        <el-button link type="primary" @click="handleClear" :disabled="selectedDocIds.length === 0">
          清空已选
        </el-button>
      </div>
    </div>

    <!-- 对话区域 -->
    <div class="chat-container" ref="chatContainer">
      <div class="chat-messages">
        <!-- 欢迎消息 -->
        <div class="message ai-message" v-if="messages.length === 0">
          <div class="avatar">🤖</div>
          <div class="message-content">
            <p>您好！我是您的智能办公助手</p>
            <p>我可以帮您解答公司制度、流程等问题，也可以帮您查找知识库中的文档内容。</p>
            <p>请在上方选择知识库，然后开始提问吧~</p>
          </div>
        </div>

        <!-- 消息列表 -->
        <div
          class="message"
          :class="msg.role === 'user' ? 'user-message' : 'ai-message'"
          v-for="(msg, index) in messages"
          :key="index"
        >
          <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="message-content">
            <div class="content-text" v-html="formatContent(msg.content)"></div>
            <!-- AI思考中 -->
            <div class="thinking" v-if="msg.thinking && !msg.content">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
            <!-- 引用来源 -->
            <div class="references" v-if="msg.references && msg.references.length > 0">
              <div class="ref-title">📎 参考来源：</div>
              <div class="ref-item" v-for="ref in msg.references" :key="ref.docId">
                {{ ref.docTitle }}
                <span class="score">相似度: {{ (ref.score * 100).toFixed(1) }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="请输入问题，按Enter发送..."
        resize="none"
        :disabled="loading"
        @keydown.enter.exact.prevent="handleSend"
      />
      <el-button type="primary" :loading="loading" @click="handleSend">
        <el-icon><Promotion /></el-icon>
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAIKnowledgeList } from '@/api/ai'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

interface DocItem {
  docId: number
  docTitle: string
  kbId: number
  kbName: string
  fileType: string
}

interface KbGroup {
  kbId: number
  kbName: string
  docs: DocItem[]
}

interface AIMessage {
  role: string
  content: string
  thinking?: boolean
  sessionId?: string
  references?: any[]
}

const docList = ref<DocItem[]>([])
const selectedKbId = ref<number | null>(null)
const selectedDocIds = ref<number[]>([])
const messages = reactive<AIMessage[]>([])
const inputText = ref('')
const loading = ref(false)
const thinking = ref(false)
const chatContainer = ref<HTMLElement>()

// 按知识库分组
const groupedKbList = computed(() => {
  const map = new Map<number, KbGroup>()
  for (const doc of docList.value) {
    if (!map.has(doc.kbId)) {
      map.set(doc.kbId, { kbId: doc.kbId, kbName: doc.kbName, docs: [] })
    }
    map.get(doc.kbId)!.docs.push(doc)
  }
  return Array.from(map.values())
})

// 当前选中知识库的文档列表
const currentKbDocs = computed(() => {
  if (!selectedKbId.value) return []
  const kb = groupedKbList.value.find(k => k.kbId === selectedKbId.value)
  return kb ? kb.docs : []
})

// 初始化
onMounted(async () => {
  // 获取知识库列表
  // 有 knowledge:list 权限传1，否则传0
  const isOwe = userStore.permissions.includes('knowledge:list') ? 1 : 0
  try {
    const res = await getAIKnowledgeList(isOwe)
    docList.value = res.data || []
    // 默认选择第一个知识库
    if (groupedKbList.value.length > 0) {
      selectedKbId.value = groupedKbList.value[0].kbId
      // 默认选择第一篇文档
      if (groupedKbList.value[0].docs.length > 0) {
        selectedDocIds.value = [groupedKbList.value[0].docs[0].docId]
      }
    }
  } catch (error) {
    console.error(error)
  }

  // 检查是否有传递问题
  if (route.query.q) {
    inputText.value = route.query.q as string
    handleSend()
  }
})

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// 选择知识库
const handleKbChange = () => {
  // 切换知识库时不清空之前的文档选择
}

// 选择文档
const handleDocChange = () => {
  // 可以在这里保存用户的选择
}

// 清空已选
const handleClear = () => {
  selectedDocIds.value = []
}

// 发送消息（流式）
const handleSend = async () => {
  if (!inputText.value.trim()) return
  if (selectedDocIds.value.length === 0) {
    ElMessage.warning('请先选择文档')
    return
  }

  const question = inputText.value.trim()
  const sessionId = messages.length > 0 ? messages[0].sessionId : undefined

  // 添加用户消息
  messages.push({ role: 'user', content: question })
  inputText.value = ''
  loading.value = true
  thinking.value = true
  scrollToBottom()

  // 创建 AI 消息占位
  const aiMessage = reactive<AIMessage>({
    role: 'ai',
    content: '',
    thinking: true
  })
  messages.push(aiMessage)

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        docIds: selectedDocIds.value,
        question,
        sessionId
      })
    })

    if (!response.ok) {
      throw new Error('请求失败')
    }

    if (!response.body) {
      throw new Error('响应体为空')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 提取所有 data: 开头的内容
      const regex = /data:\s*(\{[^}]+\})/g
      let match: RegExpExecArray | null
      while ((match = regex.exec(buffer)) !== null) {
        try {
          const json = JSON.parse(match[1])
          if (json.content) {
            aiMessage.content += json.content
            scrollToBottom()
          }
          if (json.sessionId) {
            aiMessage.sessionId = json.sessionId
          }
        } catch {
          // 解析失败忽略
        }
      }

      // 清理已处理的数据
      const lastDataIndex = buffer.lastIndexOf('}')
      if (lastDataIndex !== -1) {
        buffer = buffer.substring(lastDataIndex + 1)
      }
    }

    aiMessage.thinking = false
    scrollToBottom()
  } catch (error) {
    console.error(error)
    ElMessage.error('请求失败，请稍后重试')
    // 移除失败的 AI 消息
    const index = messages.indexOf(aiMessage)
    if (index > -1) {
      messages.splice(index, 1)
    }
  } finally {
    loading.value = false
    thinking.value = false
  }
}

// 格式化内容
const formatContent = (content: string) => {
  if (!content) return ''
  // 换行处理
  return content.replace(/\n/g, '<br>')
}
</script>

<style scoped lang="scss">
.ai-chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  background: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;

  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #E5E7EB;

    h2 {
      font-size: 18px;
      font-weight: 600;
      color: #1F2937;
    }

    .kb-selector {
      display: flex;
      align-items: center;
      gap: 16px;
      flex: 1;

      .el-select {
        width: 200px;
      }

      .doc-count {
        color: #999;
        font-size: 12px;
      }

      .el-checkbox-group {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }

      .el-checkbox {
        margin-right: 0;
      }

      .el-tag {
        margin-left: 4px;
      }
    }
  }

  .chat-container {
    flex: 1;
    overflow-y: auto;
    padding: 20px;

    .chat-messages {
      .message {
        display: flex;
        gap: 12px;
        margin-bottom: 20px;

        .avatar {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          flex-shrink: 0;
        }

        .message-content {
          max-width: 70%;
          padding: 12px 16px;
          border-radius: 12px;
          font-size: 14px;
          line-height: 1.6;

          .content-text {
            word-break: break-word;
          }

          .references {
            margin-top: 12px;
            padding-top: 12px;
            border-top: 1px solid #E5E7EB;

            .ref-title {
              font-size: 12px;
              color: #6B7280;
              margin-bottom: 8px;
            }

            .ref-item {
              font-size: 12px;
              color: #10B981;
              margin-bottom: 4px;

              .score {
                color: #9CA3AF;
                margin-left: 8px;
              }
            }
          }
        }

        &.ai-message {
          .avatar {
            background: #ECFDF5;
          }

          .message-content {
            background: #F9FAFB;
            color: #1F2937;
          }
        }

        &.user-message {
          flex-direction: row-reverse;

          .avatar {
            background: #10B981;
          }

          .message-content {
            background: #10B981;
            color: #FFFFFF;

            .references {
              border-top-color: rgba(255, 255, 255, 0.3);

              .ref-title, .ref-item {
                color: rgba(255, 255, 255, 0.8);
              }
            }
          }
        }
      }

      .thinking {
        display: flex;
        gap: 4px;
        margin-top: 8px;

        .dot {
          width: 6px;
          height: 6px;
          background: #10B981;
          border-radius: 50%;
          animation: bounce 1.4s infinite ease-in-out both;

          &:nth-child(1) { animation-delay: -0.32s; }
          &:nth-child(2) { animation-delay: -0.16s; }
        }
      }

      @keyframes bounce {
        0%, 80%, 100% { transform: scale(0); }
        40% { transform: scale(1); }
      }
    }
  }

  .chat-input {
    display: flex;
    gap: 12px;
    padding: 16px 20px;
    border-top: 1px solid #E5E7EB;
    background: #F9FAFB;

    .el-textarea {
      flex: 1;
    }

    .el-button {
      height: auto;
      padding: 12px 24px;
    }
  }
}
</style>
