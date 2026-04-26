<template>
  <div class="expense-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>费用报销</h2>
    </div>

    <div class="expense-content">
      <!-- 左侧：报销表单 -->
      <div class="expense-form-card">
        <h3>📝 报销申请</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="报销类型" prop="expenseType">
            <el-select v-model="form.expenseType" placeholder="请选择报销类型" style="width: 100%">
              <el-option label="差旅" value="差旅" />
              <el-option label="餐饮" value="餐饮" />
              <el-option label="办公" value="办公" />
              <el-option label="交通" value="交通" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>

          <!-- 费用明细 -->
          <el-form-item label="费用明细">
            <div class="expense-items">
              <div v-for="(item, index) in form.items" :key="index" class="expense-item-row">
                <el-input v-model="item.itemType" placeholder="费用类型" style="width: 90px" />
                <el-input-number v-model="item.amount" :min="0" :precision="2" placeholder="金额" style="width: 130px" />
                <el-date-picker
                  v-model="item.expenseDate"
                  type="date"
                  placeholder="日期"
                  value-format="YYYY-MM-DD"
                  style="width: 150px"
                />
                <el-input v-model="item.description" placeholder="说明" style="width: 120px" />
                <!-- 发票上传 -->
                <el-button size="small" type="primary" link @click="triggerUpload(index)">
                  {{ item.receiptUrl ? '已上传' : '上传发票' }}
                </el-button>
                <input
                  type="file"
                  :data-index="index"
                  style="display: none"
                  accept="image/*"
                  @change="handleFileChange($event, index)"
                />
                <el-button type="danger" link @click="removeItem(index)">删除</el-button>
              </div>
              <el-button type="primary" link @click="addItem">+ 添加明细</el-button>
            </div>
          </el-form-item>

          <el-form-item label="总金额">
            <span class="total-amount">¥{{ totalAmount }}</span>
          </el-form-item>

          <el-form-item label="费用说明" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入费用说明..." />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" style="width: 100%">提交申请</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 右侧：报销记录 -->
      <div class="expense-record-card">
        <h3>📋 报销记录</h3>
        <div class="table-wrap">
          <el-table :data="expenseList" size="small" @row-click="handleView">
            <el-table-column prop="expenseType" label="类型" width="60" />
            <el-table-column prop="totalAmount" label="金额" width="80">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="130">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="55" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" link @click.stop="handleCancel(row)" v-if="row.status === 1">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, prev, pager, next"
            @change="fetchList"
            style="margin-top: 15px; justify-content: flex-end"
          />
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="报销详情" width="650px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="报销编号">{{ detailData.expenseNo }}</el-descriptions-item>
        <el-descriptions-item label="报销类型">{{ detailData.expenseType }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detailData.deptName }}</el-descriptions-item>
        <el-descriptions-item label="报销总额">
          <span style="color: #10B981; font-weight: 600">¥{{ detailData.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="费用说明" :span="2">{{ detailData.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="审批人" v-if="detailData.approverName">{{ detailData.approverName }}</el-descriptions-item>
        <el-descriptions-item label="审批时间" v-if="detailData.approveTime">{{ formatDateTime(detailData.approveTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2" v-if="detailData.approveComment">{{ detailData.approveComment }}</el-descriptions-item>
      </el-descriptions>

      <!-- 费用明细 -->
      <div class="expense-items-section" v-if="detailData?.items?.length">
        <h4>费用明细</h4>
        <el-table :data="detailData.items" size="small" border>
          <el-table-column prop="itemType" label="费用类型" width="100" />
          <el-table-column prop="amount" label="金额" width="90">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column prop="expenseDate" label="日期" width="100">
            <template #default="{ row }">{{ formatDate(row.expenseDate) }}</template>
          </el-table-column>
          <el-table-column prop="description" label="说明" />
          <el-table-column label="发票" width="80">
            <template #default="{ row }">
              <el-image
                v-if="row.receiptUrl"
                :src="getFullUrl(row.receiptUrl)"
                :preview-src-list="[getFullUrl(row.receiptUrl)]"
                fit="cover"
                style="width: 40px; height: 40px; cursor: pointer"
              />
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getExpenseList, createExpense, getExpenseDetail, cancelExpense } from '@/api/office'
import { formatDateTime, formatDate } from '@/utils/format'

const formRef = ref<FormInstance>()

// 每个费用明细包含 file 对象
interface ExpenseItem {
  itemType: string
  amount: number
  expenseDate: string
  description: string
  receiptUrl: string
  file: File | null
  fileIndex: number  // 对应 files 数组的索引，-1表示无文件
}

const form = reactive({
  expenseType: '',
  description: '',
  items: [{ itemType: '', amount: 0, expenseDate: '', description: '', receiptUrl: '', file: null, fileIndex: -1 }] as ExpenseItem[]
})

const rules = {
  expenseType: [{ required: true, message: '请选择报销类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入费用说明', trigger: 'blur' }]
}

const expenseList = ref<any[]>([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const detailVisible = ref(false)
const detailData = ref<any>(null)

const totalAmount = computed(() => {
  return form.items.reduce((sum, item) => sum + (item.amount || 0), 0).toFixed(2)
})

const addItem = () => {
  form.items.push({ itemType: '', amount: 0, expenseDate: '', description: '', receiptUrl: '', file: null, fileIndex: -1 })
}

const removeItem = (index: number) => {
  if (form.items.length > 1) {
    form.items.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一条费用明细')
  }
}

// 触发文件选择
const triggerUpload = (index: number) => {
  const fileInput = document.querySelector(`input[type=file][data-index="${index}"]`) as HTMLInputElement
  if (fileInput) {
    fileInput.click()
  }
}

const handleFileChange = (event: Event, index: number) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files[0]) {
    const file = input.files[0]
    // 校验文件类型和大小
    if (!file.type.startsWith('image/')) {
      ElMessage.error('只能上传图片文件')
      return
    }
    if (file.size / 1024 / 1024 > 5) {
      ElMessage.error('图片大小不能超过 5MB')
      return
    }
    form.items[index].file = file
    // 显示文件名作为已上传标识
    form.items[index].receiptUrl = file.name
    // 设置 fileIndex：计算当前项目在 files 数组中的位置
    // 统计之前已上传的文件数量，即为当前文件的索引
    let fileIndex = 0
    for (let i = 0; i < index; i++) {
      if (form.items[i].file) {
        fileIndex++
      }
    }
    form.items[index].fileIndex = fileIndex
  }
}

const getStatusType = (status: number) => {
  const types: any = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger', 5: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: any = { 1: '待审批', 2: '审批中', 3: '已通过', 4: '已拒绝', 5: '已取消' }
  return texts[status] || '未知'
}

const getFullUrl = (url: string) => {
  if (!url) return ''
  return url.startsWith('http') ? url : import.meta.env.VITE_MINIO_URL + url
}

const fetchList = async () => {
  try {
    const res = await getExpenseList({ current: pagination.current, size: pagination.size })
    expenseList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 检查是否有有效的费用明细
      const hasValidItem = form.items.some(item => item.itemType && item.amount > 0)
      if (!hasValidItem) {
        ElMessage.warning('请至少添加一条有效的费用明细')
        return
      }

      try {
        await createExpense({
          expenseType: form.expenseType,
          totalAmount: parseFloat(totalAmount.value),
          description: form.description,
          items: form.items
        })
        ElMessage.success('报销申请提交成功')
        fetchList()
        // 重置表单
        form.expenseType = ''
        form.description = ''
        form.items = [{ itemType: '', amount: 0, expenseDate: '', description: '', receiptUrl: '', file: null, fileIndex: -1 }]
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleView = async (row: any) => {
  try {
    const res = await getExpenseDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消该报销申请吗？', '提示', {
      type: 'warning'
    })
    await cancelExpense(row.id)
    ElMessage.success('已取消')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.expense-page {
  .expense-content {
    display: grid;
    grid-template-columns: 1fr 455px;
    gap: 20px;

    .expense-form-card {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 24px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 20px;
      }

      .expense-items {
        .expense-item-row {
          display: flex;
          gap: 8px;
          margin-bottom: 10px;
          align-items: center;
          flex-wrap: wrap;
        }
      }

      .total-amount {
        font-size: 20px;
        font-weight: 600;
        color: #10B981;
      }
    }

    .expense-record-card {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 20px;

      h3 {
        font-size: 15px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 16px;
      }

      .table-wrap {
        max-height: calc(100vh - 280px);
        overflow-y: auto;
      }
    }
  }

  .expense-items-section {
    margin-top: 20px;

    h4 {
      font-size: 14px;
      font-weight: 600;
      color: #4B5563;
      margin-bottom: 10px;
    }
  }
}
</style>
