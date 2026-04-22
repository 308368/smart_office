<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1 class="welcome-title">欢迎回来，{{ userStore.nickname || userStore.username }}！</h1>
      <p class="welcome-desc">今天的工作从这里开始</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: #ECFDF5;">
          <el-icon :size="28" color="#10B981"><Collection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.docCount }}</div>
          <div class="stat-label">知识库文档</div>
        </div>
        <div class="stat-trend up">↑12% 较上周</div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #FEF3C7;">
          <el-icon :size="28" color="#F59E0B"><Tickets /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingTicket }}</div>
          <div class="stat-label">待办工单</div>
        </div>
        <div class="stat-trend up">↑2 较上周</div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #DBEAFE;">
          <el-icon :size="28" color="#3B82F6"><Calendar /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingLeave }}</div>
          <div class="stat-label">待审批</div>
        </div>
        <div class="stat-trend down">↓1 较上周</div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #FCE7F3;">
          <el-icon :size="28" color="#EC4899"><ChatDotRound /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.chatCount }}</div>
          <div class="stat-label">AI对话次数</div>
        </div>
        <div class="stat-trend up">↑8 较上周</div>
      </div>
    </div>

    <!-- 图表和列表区域 -->
    <div class="content-grid">
      <!-- 对话趋势图 -->
      <div class="page-card chart-card">
        <div class="card-header">
          <h3>📈 最近对话趋势</h3>
        </div>
        <div class="chart-container">
          <div class="simple-chart">
            <div class="chart-bar" v-for="(value, index) in chartData" :key="index" :style="{ height: getBarHeight(value) + '%' }">
              <span class="chart-value">{{ chartValues[index] }}</span>
            </div>
          </div>
          <div class="chart-labels">
            <span v-for="day in chartLabels" :key="day">{{ day }}</span>
          </div>
        </div>
      </div>

      <!-- 待处理工单 -->
      <div class="page-card">
        <div class="card-header">
          <h3>📋 待处理工单</h3>
          <el-button type="primary" link @click="$router.push('/ticket')">查看全部</el-button>
        </div>
        <div class="ticket-list">
          <div class="ticket-item" v-for="item in pendingTickets" :key="item.id" @click="$router.push(`/ticket/detail/${item.id}`)">
            <div class="ticket-info">
              <el-tag :type="getPriorityType(item.priority)" size="small">{{ getPriorityText(item.priority) }}</el-tag>
              <span class="ticket-title">{{ item.title }}</span>
            </div>
            <div class="ticket-meta">
              <span class="ticket-type">{{ item.typeName }}</span>
              <span class="ticket-time">{{ formatDateTime(item.createTime) }}</span>
            </div>
          </div>
          <el-empty v-if="pendingTickets.length === 0" description="暂无待处理工单" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- 底部区域 -->
    <div class="content-grid">
      <!-- 通知公告 -->
      <div class="page-card">
        <div class="card-header">
          <h3>📰 通知公告</h3>
          <el-button type="primary" link @click="$router.push('/notice')">查看全部</el-button>
        </div>
        <div class="notice-list">
          <div class="notice-item" v-for="item in notices" :key="item.id" @click="$router.push('/notice')">
            <div class="notice-title">
              <span class="unread-dot" v-if="!item.isRead"></span>
              {{ item.title }}
            </div>
            <div class="notice-meta">
              <span>{{ item.publisherName }}</span>
              <span>{{ formatDateTime(item.publishTime) }}</span>
            </div>
          </div>
          <el-empty v-if="notices.length === 0" description="暂无公告" :image-size="60" />
        </div>
      </div>

      <!-- AI快捷入口 -->
      <div class="page-card">
        <div class="card-header">
          <h3>🤖 AI 快捷入口</h3>
        </div>
        <div class="ai-quick">
          <div class="quick-input">
            <el-input
              v-model="quickQuestion"
              placeholder="输入问题，快速获取答案..."
              @keyup.enter="goToAI"
            >
              <template #append>
                <el-button :icon="Search" @click="goToAI" />
              </template>
            </el-input>
          </div>
          <div class="quick-questions">
            <div class="quick-item" v-for="q in quickQuestions" :key="q" @click="askQuick(q)">
              {{ q }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import { getDashboardStats, getChatStats, getPendingTicketList, getHomeNotices } from '@/api/dashboard'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
  docCount: 0,
  pendingTicket: 0,
  pendingLeave: 0,
  chatCount: 0
})

const chartData = ref<number[]>([])
const chartValues = ref<number[]>([])
const chartLabels = ref<string[]>([])

const pendingTickets = ref<any[]>([])
const notices = ref<any[]>([])

const loading = ref(false)

const quickQuestion = ref('')
const quickQuestions = ref([
  '年假申请需要什么材料？',
  '如何申请报销？',
  '公司考勤制度是什么？'
])

onMounted(async () => {
  loading.value = true
  try {
    // 获取统计数据
    try {
      const statsRes = await getDashboardStats()
      stats.value = statsRes.data
    } catch (e) {
      console.error('获取统计数据失败', e)
    }

    // 获取图表数据
    try {
      const chartRes = await getChatStats()
      if (chartRes.data && chartRes.data.length > 0) {
        chartData.value = chartRes.data.map((item: any) => item.count)
        chartValues.value = chartRes.data.map((item: any) => item.count)
        chartLabels.value = chartRes.data.map((item: any) => formatChartLabel(item.date))
      }
    } catch (e) {
      console.error('获取图表数据失败', e)
    }

    // 获取待处理工单
    try {
      const ticketRes = await getPendingTicketList({ current: 1, size: 5 })
      pendingTickets.value = ticketRes.data?.records || []
    } catch (e) {
      console.error('获取工单列表失败', e)
    }

    // 获取公告列表
    try {
      const noticeRes = await getHomeNotices({ size: 5 })
      notices.value = noticeRes.data || []
    } catch (e) {
      console.error('获取公告列表失败', e)
    }

  } catch (error) {
    console.error('获取首页数据失败:', error)
  } finally {
    loading.value = false
  }
})

const getPriorityType = (priority: number) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[priority as keyof typeof types] || 'info'
}

const getPriorityText = (priority: number) => {
  const texts = { 1: '紧急', 2: '普通', 3: '低' }
  return texts[priority as keyof typeof texts] || '普通'
}

const formatChartLabel = (dateStr: string) => {
  if (!dateStr) return ''
  // dateStr 格式: "2026-04-22" -> "04-22"
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    return `${parts[1]}-${parts[2]}`
  }
  return dateStr
}

// 根据数据动态计算柱形高度，最小10%保证可见性
const getBarHeight = (value: number) => {
  if (!chartValues.value || chartValues.value.length === 0) return 10
  const max = Math.max(...chartValues.value)
  if (max === 0) return 10
  const height = (value / max) * 100
  return Math.max(height, 10) // 最小10%保证可见
}

const goToAI = () => {
  if (quickQuestion.value) {
    router.push({ path: '/ai-chat', query: { q: quickQuestion.value } })
  } else {
    router.push('/ai-chat')
  }
}

const askQuick = (q: string) => {
  router.push({ path: '/ai-chat', query: { q } })
}
</script>

<style scoped lang="scss">
.dashboard {
  .welcome-section {
    margin-bottom: 24px;

    .welcome-title {
      font-size: 24px;
      font-weight: 600;
      color: #1F2937;
      margin-bottom: 8px;
    }

    .welcome-desc {
      color: #6B7280;
      font-size: 14px;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;

    .stat-card {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: 600;
          color: #1F2937;
        }

        .stat-label {
          font-size: 13px;
          color: #6B7280;
          margin-top: 4px;
        }
      }

      .stat-trend {
        font-size: 12px;
        padding: 4px 8px;
        border-radius: 4px;

        &.up {
          background: #ECFDF5;
          color: #059669;
        }

        &.down {
          background: #FEE2E2;
          color: #DC2626;
        }
      }
    }
  }

  .content-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 24px;
  }

  .page-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #1F2937;
      }
    }
  }

  .chart-card {
    .chart-container {
      height: 200px;
      display: flex;
      flex-direction: column;

      .simple-chart {
        flex: 1;
        display: flex;
        align-items: flex-end;
        justify-content: space-around;
        padding: 0 20px;

        .chart-bar {
          width: 40px;
          background: linear-gradient(180deg, #10B981 0%, #6EE7B7 100%);
          border-radius: 4px 4px 0 0;
          position: relative;
          transition: all 0.3s;

          &:hover {
            opacity: 0.8;
          }

          .chart-value {
            position: absolute;
            top: -24px;
            left: 50%;
            transform: translateX(-50%);
            font-size: 12px;
            color: #4B5563;
          }
        }
      }

      .chart-labels {
        display: flex;
        justify-content: space-around;
        padding: 8px 20px 0;
        font-size: 12px;
        color: #9CA3AF;
      }
    }
  }

  .ticket-list {
    .ticket-item {
      padding: 12px 0;
      border-bottom: 1px solid #F3F4F6;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: #F9FAFB;
        margin: 0 -20px;
        padding: 12px 20px;
      }

      &:last-child {
        border-bottom: none;
      }

      .ticket-info {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;

        .ticket-title {
          color: #1F2937;
          font-size: 14px;
        }
      }

      .ticket-meta {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #9CA3AF;
      }
    }
  }

  .notice-list {
    .notice-item {
      padding: 12px 0;
      border-bottom: 1px solid #F3F4F6;
      cursor: pointer;

      &:hover .notice-title {
        color: #10B981;
      }

      &:last-child {
        border-bottom: none;
      }

      .notice-title {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #1F2937;
        font-size: 14px;
        margin-bottom: 6px;

        .unread-dot {
          width: 8px;
          height: 8px;
          background: #EF4444;
          border-radius: 50%;
        }
      }

      .notice-meta {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #9CA3AF;
      }
    }
  }

  .ai-quick {
    .quick-input {
      margin-bottom: 16px;
    }

    .quick-questions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .quick-item {
        padding: 8px 16px;
        background: #ECFDF5;
        color: #059669;
        border-radius: 20px;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: #D1FAE5;
        }
      }
    }
  }
}
</style>
