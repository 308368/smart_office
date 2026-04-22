<template>
  <div class="ai-chat-page">
    <!-- 左侧会话列表 -->
    <div class="session-sidebar">
      <div class="sidebar-header">
        <span>会话列表</span>
        <el-button type="primary" link @click="handleNewSession">
          <el-icon><Plus /></el-icon>
          新会话
        </el-button>
      </div>
      <div class="session-list">
        <div
          v-for="session in sessionList"
          :key="session.id"
          class="session-item"
          :class="{ active: currentSessionId === session.id }"
          @click="handleSelectSession(session)"
        >
          <div class="session-info">
            <div class="session-title">{{ session.title || '新会话' }}</div>
            <div class="session-time">{{ formatTime(session.updateTime) }}</div>
          </div>
          <el-button
            type="danger"
            link
            class="delete-btn"
            @click.stop="handleDeleteSession(session.id)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <div v-if="sessionList.length === 0" class="empty-tip">
          暂无会话记录
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-main">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAIKnowledgeList, getChatHistory, getChatMessages, deleteChatHistory, createChatSession } from '@/api/ai'
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
}

interface Session {
  id: number
  sessionNo: string
  title: string
  model: string
  status: number
  messageCount: number
  lastMessage: string
  createTime: string
  updateTime: string
}

const docList = ref<DocItem[]>([])
const selectedKbId = ref<number | null>(null)
const selectedDocIds = ref<number[]>([])
const messages = reactive<AIMessage[]>([])
const inputText = ref('')
const loading = ref(false)
const thinking = ref(false)
const chatContainer = ref<HTMLElement>()

// 会话相关
const sessionList = ref<Session[]>([])
const currentSessionId = ref<number | null>(null)

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

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

// 加载会话列表
const loadSessionList = async () => {
  try {
    const res = await getChatHistory()
    const list = res.data || []
    // 按更新时间倒序，新的在上面
    list.sort((a: Session, b: Session) => new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime())
    sessionList.value = list
  } catch (error) {
    console.error(error)
  }
}

// 创建新会话
const handleNewSession = async () => {
  try {
    const res = await createChatSession()
    const newSession = res.data as Session
    sessionList.value.unshift(newSession)
    handleSelectSession(newSession)
  } catch (error) {
    console.error(error)
    ElMessage.error('创建会话失败')
  }
}

// 选择会话
const handleSelectSession = async (session: Session) => {
  currentSessionId.value = session.id
  messages.length = 0

  try {
    const res = await getChatMessages(session.id)
    const historyMessages = res.data || []
    historyMessages.forEach((msg: any) => {
      messages.push({
        role: msg.role,
        content: msg.content
      })
    })
    scrollToBottom()
  } catch (error) {
    console.error(error)
    ElMessage.error('加载会话消息失败')
  }
}

// 删除会话
const handleDeleteSession = async (sessionId: number) => {
  try {
    await deleteChatHistory(sessionId)
    sessionList.value = sessionList.value.filter(s => s.id !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.length = 0
    }
    ElMessage.success('删除成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('删除失败')
  }
}

// 初始化
onMounted(async () => {
  // 获取知识库列表
  const isOwe = userStore.permissions.includes('knowledge:list') ? 1 : 0
  try {
    const res = await getAIKnowledgeList(isOwe)
    docList.value = res.data || []
    if (groupedKbList.value.length > 0) {
      selectedKbId.value = groupedKbList.value[0].kbId
      if (groupedKbList.value[0].docs.length > 0) {
        selectedDocIds.value = [groupedKbList.value[0].docs[0].docId]
      }
    }
  } catch (error) {
    console.error(error)
  }

  // 加载会话列表
  await loadSessionList()

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
const handleKbChange = () => {}

// 选择文档
const handleDocChange = () => {}

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
        sessionId: currentSessionId.value
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
            // 更新当前会话ID
            if (!currentSessionId.value) {
              currentSessionId.value = parseInt(json.sessionId)
              // 刷新会话列表
              loadSessionList()
            }
          }
        } catch {
          // 解析失败忽略
        }
      }

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
  return content.replace(/\n/g, '<br>')
}
</script>

<style scoped lang="scss">
.ai-chat-page {
  display: flex;
  height: calc(100vh - 140px);
  background: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;

  .session-sidebar {
    width: 260px;
    background: #F9FAFB;
    border-right: 1px solid #E5E7EB;
    display: flex;
    flex-direction: column;

    .sidebar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #E5E7EB;

      span {
        font-weight: 600;
        color: #1F2937;
      }
    }

    .session-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;

      .session-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        margin-bottom: 4px;
        transition: background 0.2s;

        &:hover {
          background: #E5E7EB;

          .delete-btn {
            opacity: 1;
          }
        }

        &.active {
          background: #10B981;

          .session-title, .session-time {
            color: #FFFFFF;
          }
        }

        .session-info {
          flex: 1;
          overflow: hidden;

          .session-title {
            font-size: 14px;
            color: #1F2937;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .session-time {
            font-size: 12px;
            color: #9CA3AF;
            margin-top: 4px;
          }
        }

        .delete-btn {
          opacity: 0;
          transition: opacity 0.2s;
        }
      }

      .empty-tip {
        text-align: center;
        color: #9CA3AF;
        font-size: 14px;
        padding: 20px;
      }
    }
  }

  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

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
