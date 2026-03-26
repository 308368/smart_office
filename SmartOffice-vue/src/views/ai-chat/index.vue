<template>
  <div class="ai-chat-page">
    <!-- 顶部知识库选择 -->
    <div class="chat-header">
      <h2>🤖 AI 智能助手</h2>
      <div class="kb-selector">
        <span class="label">请选择知识库：</span>
        <el-checkbox-group v-model="selectedKbIds" @change="handleKbChange">
          <el-checkbox v-for="kb in kbList" :key="kb.id" :label="kb.id">
            {{ kb.name }}
          </el-checkbox>
        </el-checkbox-group>
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

        <!-- 加载中 -->
        <div class="message ai-message" v-if="loading">
          <div class="avatar">🤖</div>
          <div class="message-content">
            <div class="loading">
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
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chat, getAIKnowledgeList } from '@/api/ai'

const route = useRoute()

const kbList = ref<any[]>([])
const selectedKbIds = ref<number[]>([])
const messages = reactive<any[]>([])
const inputText = ref('')
const loading = ref(false)
const chatContainer = ref<HTMLElement>()

// 初始化
onMounted(async () => {
  // 获取知识库列表
  try {
    const res = await getAIKnowledgeList()
    kbList.value = res.data || []
    // 默认选择第一个
    if (kbList.value.length > 0) {
      selectedKbIds.value = [kbList.value[0].id]
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
  // 可以在这里保存用户的选择
}

// 发送消息
const handleSend = async () => {
  if (!inputText.value.trim()) return
  if (selectedKbIds.value.length === 0) {
    ElMessage.warning('请先选择知识库')
    return
  }

  const question = inputText.value.trim()
  const sessionId = messages.length > 0 ? messages[0].sessionId : undefined

  // 添加用户消息
  messages.push({ role: 'user', content: question })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = await chat({
      kbIds: selectedKbIds.value,
      question,
      sessionId
    })

    const data = res.data
    messages.push({
      role: 'ai',
      content: data.answer,
      references: data.references,
      sessionId: data.sessionId
    })
    scrollToBottom()
  } catch (error) {
    console.error(error)
    ElMessage.error('请求失败，请稍后重试')
  } finally {
    loading.value = false
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
      gap: 12px;

      .label {
        font-size: 14px;
        color: #6B7280;
      }

      .el-checkbox {
        margin-right: 16px;
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

      .loading {
        display: flex;
        gap: 4px;

        .dot {
          width: 8px;
          height: 8px;
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
