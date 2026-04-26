<template>
  <div class="expense-approve-page">
    <div class="page-header">
      <h2>报销审批</h2>
    </div>

    <div class="approve-content">
      <el-table :data="expenseList" v-loading="loading" stripe>
        <el-table-column prop="expenseNo" label="报销编号" width="200" />
        <el-table-column prop="userName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="expenseType" label="报销类型" width="80">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.expenseType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="报销金额" width="100">
          <template #default="{ row }">
            <span style="color: #10B981; font-weight: 600">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="费用说明" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleApprove(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="current"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- 审批弹窗 -->
    <el-dialog v-model="dialogVisible" title="报销审批" width="600px">
      <div class="approve-form" v-if="currentRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="报销编号">{{ currentRow.expenseNo }}</el-descriptions-item>
          <el-descriptions-item label="报销类型">{{ currentRow.expenseType }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentRow.userName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ currentRow.deptName }}</el-descriptions-item>
          <el-descriptions-item label="报销总额">
            <span style="color: #10B981; font-weight: 600">¥{{ currentRow.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDateTime(currentRow.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="费用说明" :span="2">{{ currentRow.description || '无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 费用明细 -->
        <div class="expense-items-section" v-if="currentRow.items?.length">
          <h4>费用明细</h4>
          <el-table :data="currentRow.items" size="small" border>
            <el-table-column prop="itemType" label="费用类型" width="100" />
            <el-table-column prop="amount" label="金额" width="90">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column prop="expenseDate" label="日期" width="100" />
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

        <el-form ref="formRef" :model="form" label-width="80px" style="margin-top: 20px">
          <el-form-item label="审批结果" prop="approved" :rules="{ required: true, message: '请选择审批结果', trigger: 'change' }">
            <el-radio-group v-model="form.approved">
              <el-radio :value="true">通过</el-radio>
              <el-radio :value="false">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审批意见" prop="comment">
            <el-input
              v-model="form.comment"
              type="textarea"
              :rows="3"
              :placeholder="form.approved ? '选填，可添加备注' : '请输入拒绝原因'"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getExpensePending, approveExpense, getExpenseDetail } from '@/api/office'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const expenseList = ref<any[]>([])
const current = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const currentRow = ref<any>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  approved: true,
  comment: ''
})

const fetchList = async () => {
  try {
    loading.value = true
    const res = await getExpensePending({ current: current.value, size: size.value })
    expenseList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row: any) => {
  try {
    // 获取完整详情（包含费用明细）
    const res = await getExpenseDetail(row.id)
    currentRow.value = res.data
    form.approved = true
    form.comment = ''
    dialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const getFullUrl = (url: string) => {
  if (!url) return ''
  return url.startsWith('http') ? url : import.meta.env.VITE_MINIO_URL + url
}

const submitApprove = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!form.approved && !form.comment.trim()) {
        ElMessage.warning('请输入拒绝原因')
        return
      }

      try {
        submitting.value = true
        await approveExpense(currentRow.value.id, {
          approved: form.approved,
          comment: form.comment
        })
        ElMessage.success(form.approved ? '已通过申请' : '已拒绝申请')
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.expense-approve-page {
  .approve-content {
    background: #FFFFFF;
    border-radius: 12px;
    padding: 20px;

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .approve-form {
    .expense-items-section {
      margin-top: 15px;

      h4 {
        font-size: 14px;
        font-weight: 600;
        color: #4B5563;
        margin-bottom: 10px;
      }
    }
  }
}
</style>
