<template>
  <div class="ticket-detail">
    <!-- 头部 -->
    <div class="detail-header">
      <el-button @click="$router.back()" text>
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="header-info">
        <h2>{{ ticketInfo.title }}</h2>
        <div class="header-tags">
          <el-tag :type="getStatusType(ticketInfo.status)">{{ getStatusText(ticketInfo.status) }}</el-tag>
          <el-tag :type="getPriorityType(ticketInfo.priority)">{{ getPriorityText(ticketInfo.priority) }}</el-tag>
        </div>
      </div>
      <div class="header-actions">
        <el-button v-if="ticketInfo.status === 0" type="primary" @click="handleVisible = true">处理</el-button>
        <el-button v-if="ticketInfo.status !== 3" @click="transferVisible = true">转派</el-button>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="detail-content">
      <!-- 左侧：工单信息 -->
      <div class="left-panel">
        <el-descriptions title="工单信息" :column="1" border>
          <el-descriptions-item label="工单编号">{{ ticketInfo.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="工单类型">{{ ticketInfo.typeName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ getPriorityText(ticketInfo.priority) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(ticketInfo.status) }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ ticketInfo.creatorName }} ({{ ticketInfo.creatorDept }})</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ ticketInfo.createTime }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ ticketInfo.handlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ ticketInfo.completeTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="content-section">
          <h3>📝 工单内容</h3>
          <div class="content-text">{{ ticketInfo.content }}</div>
        </div>

        <div class="content-section" v-if="ticketInfo.processResult">
          <h3>✅ 处理结果</h3>
          <div class="content-text">{{ ticketInfo.processResult }}</div>
        </div>
      </div>

      <!-- 右侧：处理记录 -->
      <div class="right-panel">
        <h3>📋 处理记录</h3>
        <div class="flow-timeline">
          <div class="flow-item" v-for="flow in ticketInfo.flows" :key="flow.id">
            <div class="flow-dot"></div>
            <div class="flow-content">
              <div class="flow-header">
                <span class="flow-action">{{ flow.action }}</span>
                <span class="flow-time">{{ flow.createTime }}</span>
              </div>
              <div class="flow-user">{{ flow.operatorName }}</div>
              <div class="flow-desc" v-if="flow.content">{{ flow.content }}</div>
            </div>
          </div>
        </div>

        <!-- 回复区域 -->
        <div class="reply-section">
          <h3>💬 沟通记录</h3>
          <div class="reply-list">
            <div class="reply-item" v-for="reply in ticketInfo.replies" :key="reply.id">
              <div class="reply-user">{{ reply.userName }}</div>
              <div class="reply-content">{{ reply.content }}</div>
              <div class="reply-time">{{ reply.createTime }}</div>
            </div>
          </div>
          <div class="reply-input">
            <el-input
              v-model="replyText"
              type="textarea"
              :rows="2"
              placeholder="添加回复..."
            />
            <el-button type="primary" @click="handleReply" :disabled="!replyText.trim()">发送</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleVisible" title="处理工单" width="500px">
      <el-form label-width="100px">
        <el-form-item label="处理结果">
          <el-input v-model="handleResult" type="textarea" :rows="4" placeholder="请输入处理结果..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">提交</el-button>
      </template>
    </el-dialog>

    <!-- 转派弹窗 -->
    <el-dialog v-model="transferVisible" title="转派工单" width="500px">
      <el-form label-width="100px">
        <el-form-item label="转派原因">
          <el-input v-model="transferReason" type="textarea" :rows="2" placeholder="请输入转派原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketDetail, handleTicket, transferTicket } from '@/api/ticket'

const route = useRoute()
const id = Number(route.params.id)

const ticketInfo = reactive<any>({
  ticketNo: '',
  title: '',
  typeName: '',
  content: '',
  priority: 2,
  status: 0,
  creatorName: '',
  creatorDept: '',
  handlerName: '',
  createTime: '',
  completeTime: '',
  processResult: '',
  flows: [],
  replies: []
})

const handleVisible = ref(false)
const transferVisible = ref(false)
const handleResult = ref('')
const transferReason = ref('')
const replyText = ref('')

// 获取详情
const fetchDetail = async () => {
  try {
    const res = await getTicketDetail(id)
    Object.assign(ticketInfo, res.data)
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchDetail()
})

const getPriorityType = (priority: number) => {
  const types: any = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[priority] || 'info'
}

const getPriorityText = (priority: number) => {
  const texts: any = { 1: '紧急', 2: '普通', 3: '低' }
  return texts[priority] || '普通'
}

const getStatusType = (status: number) => {
  const types: any = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: any = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return texts[status] || '未知'
}

const submitHandle = async () => {
  if (!handleResult.value.trim()) {
    ElMessage.warning('请输入处理结果')
    return
  }
  try {
    await handleTicket(id, { processResult: handleResult.value })
    ElMessage.success('处理成功')
    handleVisible.value = false
    fetchDetail()
  } catch (error) {
    console.error(error)
  }
}

const submitTransfer = async () => {
  try {
    await transferTicket(id, { targetUserId: 1, reason: transferReason.value })
    ElMessage.success('转派成功')
    transferVisible.value = false
  } catch (error) {
    console.error(error)
  }
}

const handleReply = () => {
  // TODO: 实现回复功能
  replyText.value = ''
}
</script>

<style scoped lang="scss">
.ticket-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #FFFFFF;
    border-radius: 12px;

    .header-info {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 12px;

      h2 {
        font-size: 18px;
        font-weight: 600;
        color: #1F2937;
      }
    }
  }

  .detail-content {
    display: grid;
    grid-template-columns: 1fr 400px;
    gap: 20px;

    .left-panel {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 20px;

      .content-section {
        margin-top: 20px;

        h3 {
          font-size: 15px;
          font-weight: 600;
          color: #1F2937;
          margin-bottom: 12px;
        }

        .content-text {
          padding: 12px;
          background: #F9FAFB;
          border-radius: 8px;
          color: #4B5563;
          line-height: 1.6;
        }
      }
    }

    .right-panel {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 20px;

      h3 {
        font-size: 15px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 16px;
      }

      .flow-timeline {
        .flow-item {
          display: flex;
          gap: 12px;
          padding-bottom: 20px;
          position: relative;

          &:not(:last-child)::after {
            content: '';
            position: absolute;
            left: 5px;
            top: 16px;
            bottom: 0;
            width: 2px;
            background: #E5E7EB;
          }

          .flow-dot {
            width: 12px;
            height: 12px;
            background: #10B981;
            border-radius: 50%;
            flex-shrink: 0;
            margin-top: 4px;
          }

          .flow-content {
            flex: 1;

            .flow-header {
              display: flex;
              justify-content: space-between;
              margin-bottom: 4px;

              .flow-action {
                font-size: 14px;
                color: #1F2937;
                font-weight: 500;
              }

              .flow-time {
                font-size: 12px;
                color: #9CA3AF;
              }
            }

            .flow-user {
              font-size: 13px;
              color: #6B7280;
            }
          }
        }
      }

      .reply-section {
        margin-top: 24px;
        border-top: 1px solid #E5E7EB;
        padding-top: 16px;

        .reply-list {
          max-height: 200px;
          overflow-y: auto;

          .reply-item {
            padding: 12px;
            background: #F9FAFB;
            border-radius: 8px;
            margin-bottom: 8px;

            .reply-user {
              font-size: 13px;
              font-weight: 500;
              color: #1F2937;
              margin-bottom: 4px;
            }

            .reply-content {
              font-size: 14px;
              color: #4B5563;
            }

            .reply-time {
              font-size: 12px;
              color: #9CA3AF;
              margin-top: 4px;
            }
          }
        }

        .reply-input {
          margin-top: 12px;
          display: flex;
          gap: 8px;
          align-items: flex-end;
        }
      }
    }
  }
}
</style>
