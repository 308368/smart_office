<template>
  <div class="leave-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>请假申请</h2>
    </div>

    <div class="leave-content">
      <!-- 左侧：请假表单 -->
      <div class="leave-form-card">
        <h3>📝 请假申请</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="请假类型" prop="leaveType">
            <el-radio-group v-model="form.leaveType">
              <el-radio label="年假">年假</el-radio>
              <el-radio label="病假">病假</el-radio>
              <el-radio label="事假">事假</el-radio>
              <el-radio label="婚假">婚假</el-radio>
              <el-radio label="产假">产假</el-radio>
              <el-radio label="其他">其他</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="开始日期" prop="startDate">
            <el-date-picker
              v-model="form.startDate"
              type="date"
              placeholder="选择开始日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="结束日期" prop="endDate">
            <el-date-picker
              v-model="form.endDate"
              type="date"
              placeholder="选择结束日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="请假天数">
            <div class="days-display">{{ calculateDays }} 天</div>
          </el-form-item>
          <el-form-item label="请假原因" prop="reason">
            <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入请假原因..." />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit" style="width: 100%">提交申请</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 右侧：剩余假期和请假记录 -->
      <div class="leave-right">
        <!-- 剩余假期 -->
        <div class="balance-card">
          <h3>📊 剩余假期</h3>
          <div class="balance-list">
            <div class="balance-item" v-for="(value, key) in leaveBalance" :key="key">
              <span class="balance-type">{{ key }}</span>
              <span class="balance-value">{{ value }} 天</span>
            </div>
          </div>
        </div>

        <!-- 请假记录 -->
        <div class="record-card">
          <h3>📋 请假记录</h3>
          <div class="table-wrap">
            <el-table :data="leaveList" size="small" @row-click="handleView">
              <el-table-column prop="leaveType" label="类型" width="55" />
              <el-table-column prop="startDate" label="开始" width="90" />
              <el-table-column prop="endDate" label="结束" width="90" />
              <el-table-column prop="days" label="天数" width="45" />
              <el-table-column prop="status" label="状态" width="70">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="55" fixed="right">
                <template #default="{ row }">
                  <el-button
                    type="danger"
                    size="small"
                    link
                    @click.stop="handleCancel(row)"
                    v-if="row.status === 1 || row.status === 2"
                  >
                    取消
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="请假详情" width="500px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="请假编号">{{ detailData.leaveNo }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">{{ detailData.leaveType }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detailData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detailData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="请假天数">{{ detailData.duration }} 天</el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请假原因" :span="2">{{ detailData.reason }}</el-descriptions-item>
        <el-descriptions-item label="审批人" v-if="detailData.approverName">{{ detailData.approverName }}</el-descriptions-item>
        <el-descriptions-item label="审批时间" v-if="detailData.approveTime">{{ formatDate(detailData.approveTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2" v-if="detailData.approveComment">{{ detailData.approveComment }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getLeaveList, createLeave, getLeaveBalance, cancelLeave, getLeaveDetail } from '@/api/office'

const formRef = ref<FormInstance>()

const form = reactive({
  leaveType: '年假',
  startDate: '',
  endDate: '',
  reason: ''
})

const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

const leaveBalance = ref<any>({ 年假: 10, 病假: 5, 事假: 3 })
const leaveList = ref<any[]>([])
const detailVisible = ref(false)
const detailData = ref<any>(null)

// 计算请假天数
const calculateDays = computed(() => {
  if (!form.startDate || !form.endDate) return 0
  const start = new Date(form.startDate)
  const end = new Date(form.endDate)
  const diff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
  return diff > 0 ? diff : 0
})

// 获取剩余假期
const fetchBalance = async () => {
  try {
    const res = await getLeaveBalance()
    leaveBalance.value = res.data || {}
  } catch (error) {
    console.error(error)
  }
}

// 获取请假列表
const fetchList = async () => {
  try {
    const res = await getLeaveList({ current: 1, size: 10 })
    leaveList.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchBalance()
  fetchList()
})

const getStatusType = (status: number) => {
  const types: any = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger', 5: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: any = { 1: '待审批', 2: '审批中', 3: '已通过', 4: '已拒绝', 5: '已取消' }
  return texts[status] || '未知'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 查看详情
const handleView = async (row: any) => {
  try {
    const res = await getLeaveDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await createLeave(form)
        ElMessage.success('提交成功')
        fetchList()
        fetchBalance()
        form.startDate = ''
        form.endDate = ''
        form.reason = ''
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消该请假申请吗？', '提示', {
      type: 'warning'
    })
    await cancelLeave(row.id)
    ElMessage.success('已取消')
    fetchList()
    fetchBalance()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}
</script>

<style scoped lang="scss">
.leave-page {
  .leave-content {
    display: grid;
    grid-template-columns: 1fr 455px;
    gap: 20px;

    .leave-form-card {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 24px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 20px;
      }

      .el-radio-group {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
      }

      .days-display {
        font-size: 18px;
        font-weight: 600;
        color: #10B981;
      }
    }

    .leave-right {
      display: flex;
      flex-direction: column;
      gap: 20px;

      .balance-card, .record-card {
        background: #FFFFFF;
        border-radius: 12px;
        padding: 20px;

        h3 {
          font-size: 15px;
          font-weight: 600;
          color: #1F2937;
          margin-bottom: 16px;
        }
      }

      .balance-card {
        .balance-list {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .balance-item {
            display: flex;
            justify-content: space-between;
            padding: 12px;
            background: #ECFDF5;
            border-radius: 8px;

            .balance-type {
              color: #4B5563;
            }

            .balance-value {
              font-weight: 600;
              color: #059669;
            }
          }
        }
      }

      .record-card {
        .table-wrap {
          max-height: 260px;
          overflow-y: auto;
        }
      }
    }
  }
}
</style>
