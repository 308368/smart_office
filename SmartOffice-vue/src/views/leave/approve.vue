<template>
  <div class="leave-approve-page">
    <div class="page-header">
      <h2>请假审批</h2>
    </div>

    <div class="approve-content">
      <el-table :data="leaveList" v-loading="loading" stripe>
        <el-table-column prop="leaveNo" label="请假编号" width="170" />
        <el-table-column prop="userName" label="申请人" width="80" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="leaveType" label="请假类型" width="80">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.leaveType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请假时间" width="220">
          <template #default="{ row }">
            {{ row.startDate }} ~ {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="天数" width="70" align="center" />
        <el-table-column prop="reason" label="请假原因" min-width="120" show-overflow-tooltip />
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
    <el-dialog v-model="dialogVisible" title="请假审批" width="500px">
      <div class="approve-form" v-if="currentRow">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="申请人">{{ currentRow.userName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ currentRow.deptName }}</el-descriptions-item>
          <el-descriptions-item label="请假类型">{{ currentRow.leaveType }}</el-descriptions-item>
          <el-descriptions-item label="请假时间">{{ currentRow.startDate }} ~ {{ currentRow.endDate }}</el-descriptions-item>
          <el-descriptions-item label="请假天数">{{ currentRow.duration }} 天</el-descriptions-item>
          <el-descriptions-item label="请假原因">{{ currentRow.reason }}</el-descriptions-item>
        </el-descriptions>

        <el-form ref="formRef" :model="form" label-width="80px" style="margin-top: 20px">
          <el-form-item label="审批结果" prop="approve" :rules="{ required: true, message: '请选择审批结果', trigger: 'change' }">
            <el-radio-group v-model="form.approve">
              <el-radio :value="true">通过</el-radio>
              <el-radio :value="false">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审批意见" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="3" :placeholder="form.approve ? '选填，可添加备注' : '请输入拒绝原因'" />
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
import { getPendingLeaveList, approveLeave } from '@/api/office'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const leaveList = ref<any[]>([])
const current = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const currentRow = ref<any>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  approve: true,
  remark: ''
})

const fetchList = async () => {
  try {
    loading.value = true
    const res = await getPendingLeaveList({ current: current.value, size: size.value })
    leaveList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleApprove = (row: any) => {
  currentRow.value = row
  form.approve = true
  form.remark = ''
  dialogVisible.value = true
}

const submitApprove = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!form.approve && !form.remark.trim()) {
        ElMessage.warning('请输入拒绝原因')
        return
      }

      try {
        submitting.value = true
        await approveLeave(currentRow.value.id, {
          approve: form.approve,
          remark: form.remark
        })
        ElMessage.success(form.approve ? '已通过申请' : '已拒绝申请')
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
.leave-approve-page {
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
    .el-descriptions {
      margin-bottom: 10px;
    }
  }
}
</style>
