<template>
  <div class="ticket-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>工单管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建工单
      </el-button>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="status-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane :label="`待处理(${stats.pending})`" name="1" />
      <el-tab-pane :label="`处理中(${stats.processing})`" name="2" />
      <el-tab-pane :label="`已解决(${stats.completed})`" name="3" />
      <el-tab-pane :label="`已关闭(${stats.closed})`" name="4" />
    </el-tabs>

    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.title"
        placeholder="搜索工单编号/标题..."
        clearable
        style="width: 240px"
        @keyup.enter="fetchList"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="searchForm.typeId" placeholder="工单类型" clearable style="width: 140px">
        <el-option v-for="t in typeList" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-button @click="fetchList">搜索</el-button>
    </div>

    <!-- 工单列表 -->
    <el-table :data="ticketList" @row-click="goToDetail" row-class-name="ticket-row">
      <el-table-column prop="ticketNo" label="工单编号" width="180" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="category" label="类型" width="100" />
      <el-table-column prop="priority" label="优先级" width="80">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" size="small">
            {{ getPriorityText(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submitterName" label="创建人" width="100" />
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click.stop="goToDetail(row.id)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @change="fetchList"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 新建工单弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建工单" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" />
        </el-form-item>
        <el-form-item label="工单类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择类型" style="width: 100%">
            <el-option v-for="t in typeList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio :label="1">紧急</el-radio>
            <el-radio :label="2">普通</el-radio>
            <el-radio :label="3">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="工单内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请详细描述问题..." />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            v-model:file-list="fileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :auto-upload="false"
            :limit="5"
            multiple
            accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.txt"
          >
            <el-button type="info" plain>
              <el-icon><Upload /></el-icon>
              上传附件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">支持图片、PDF、Word、Excel文件，单个不超过10MB，最多5个</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import { getTicketList, createTicket, getTicketTypeList, uploadTicketFile, getTicketStats } from '@/api/ticket'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const activeTab = ref('all')
const ticketList = ref<any[]>([])
const typeList = ref<any[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const fileList = ref<any[]>([])
const uploadUrl = '/api/office/upload'
const uploadHeaders = ref({})

const stats = ref({ pending: 0, processing: 0, completed: 0, closed: 0 })

const searchForm = reactive({
  title: '',
  typeId: undefined as number | undefined,
  status: undefined as number | undefined
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  title: '',
  typeId: undefined as number | undefined,
  typeName: '',
  content: '',
  priority: 2
})

const rules = {
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入工单内容', trigger: 'blur' }]
}

// 获取工单列表
const fetchList = async () => {
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    if (activeTab.value !== 'all') {
      params.status = Number(activeTab.value)
    }
    const res = await getTicketList(params)
    ticketList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

// 获取工单类型
const fetchTypes = async () => {
  try {
    const res = await getTicketTypeList()
    typeList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 获取工单统计
const fetchStats = async () => {
  try {
    const res = await getTicketStats()
    stats.value = res.data || { pending: 0, processing: 0, completed: 0, closed: 0 }
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchList()
  fetchTypes()
  fetchStats()
})

const handleTabChange = () => {
  pagination.current = 1
  fetchList()
}

const goToDetail = (id: number) => {
  router.push(`/ticket/detail/${id}`)
}

const getPriorityType = (priority: number) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[priority as keyof typeof types] || 'info'
}

const getPriorityText = (priority: number) => {
  const texts = { 1: '紧急', 2: '普通', 3: '低' }
  return texts[priority as keyof typeof texts] || '普通'
}

const getStatusType = (status: number) => {
  const types: any = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: any = { 1: '待处理', 2: '处理中', 3: '已解决', 4: '已关闭', 5: '已驳回' }
  return texts[status] || '未知'
}

const handleCreate = () => {
  form.title = ''
  form.typeId = undefined
  form.typeName = ''
  form.content = ''
  form.priority = 2
  fileList.value = []
  dialogVisible.value = true
}

// 上传前检查
const beforeUpload = (file: any) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('单个文件大小不能超过10MB')
    return false
  }
}

// 上传成功
const handleUploadSuccess = (response: any, uploadFile: any) => {
  if (response.code === 200) {
    const url = response.data
    uploadFile.url = url
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 移除文件
const handleRemove = (uploadFile: any) => {
  const index = fileList.value.findIndex(f => f.uid === uploadFile.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const typeItem = typeList.value.find(t => t.id === form.typeId)
      const submitData = {
        ...form,
        category: typeItem?.name || ''
      }
      try {
        // 1. 先创建工单，获取工单ID
        const res = await createTicket(submitData)
        const ticketId = res.data?.id
        if (!ticketId) {
          ElMessage.error('创建工单失败')
          return
        }

        // 2. 循环上传附件，传入工单ID
        for (const file of fileList.value) {
          if (file.raw) {
            try {
              await uploadTicketFile(file.raw, ticketId)
            } catch (error) {
              console.error(`文件 ${file.name} 上传失败`, error)
            }
          }
        }

        ElMessage.success('创建成功')
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}
</script>

<style scoped lang="scss">
.ticket-page {
  .status-tabs {
    margin-bottom: 16px;
    background: #FFFFFF;
    padding: 0 20px;
    border-radius: 12px;
  }

  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    background: #FFFFFF;
    padding: 16px 20px;
    border-radius: 12px;
  }

  .el-table {
    border-radius: 12px;

    :deep(.ticket-row) {
      cursor: pointer;
    }
  }
}
</style>
