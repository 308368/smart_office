<template>
  <el-dialog v-model="visible" title="提示词管理" width="900px" @close="handleClose">
    <!-- 提示词列表 -->
    <div v-show="!showForm">
      <div class="list-header">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增提示词
        </el-button>
      </div>

      <el-table :data="promptList" v-if="promptList.length > 0" style="width: 100%" stripe>
        <el-table-column prop="name" label="名称" min-width="120">
          <template #default="{ row }">
            <span class="prompt-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150">
          <template #default="{ row }">
            <span class="prompt-desc">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.category || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isPublic" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isPublic === 1 ? 'success' : 'warning'">
              {{ row.isPublic === 1 ? '公共' : '私有' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleSelect(row)">选用</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="暂无提示词" />

      <!-- 底部选中提示 -->
      <div class="select-tip" v-if="selectedPrompt">
        已选提示词：{{ selectedPrompt.name }}
        <el-button link type="danger" @click="handleClearSelect">清除</el-button>
      </div>
    </div>

    <!-- 新增/编辑表单 -->
    <div v-show="showForm" class="prompt-form">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入提示词名称" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入提示词描述" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" clearable style="width: 100%">
            <el-option label="编程开发" value="编程开发" />
            <el-option label="办公效率" value="办公效率" />
          </el-select>
        </el-form-item>

        <el-form-item label="是否公共" prop="isPublic">
          <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0" :disabled="!isCreator" />
          <span class="form-tip">{{ isCreator ? '公共提示词所有用户可见，私有仅自己可见' : '只有创建者才能修改可见性' }}</span>
        </el-form-item>

        <el-form-item label="提示词内容" prop="prompt">
          <el-input v-model="form.prompt" type="textarea" :rows="8" placeholder="输入系统提示词内容..." />
        </el-form-item>
      </el-form>

      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getPromptList, createPrompt, updatePrompt, deletePrompt } from '@/api/ai'

const emit = defineEmits<{
  (e: 'select', prompt: Prompt | null): void
}>()

interface Prompt {
  id: number
  name: string
  description?: string
  prompt: string
  category?: string
  isPublic: number
  createBy: number
  createTime: string
}

const visible = ref(false)
const showForm = ref(false)
const promptList = ref<Prompt[]>([])
const isEdit = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  name: '',
  description: '',
  prompt: '',
  category: '',
  isPublic: 0
})

const rules = {
  name: [{ required: true, message: '请输入提示词名称', trigger: 'blur' }],
  prompt: [{ required: true, message: '请输入提示词内容', trigger: 'blur' }]
}

const formRef = ref<any>()

// 当前选中的提示词
const selectedPrompt = ref<Prompt | null>(null)
const isCreator = ref(false)
const currentUserId = ref<number | null>(null)

// 获取当前用户ID
const getCurrentUserId = () => {
  const userId = localStorage.getItem('userId')
  return userId ? parseInt(userId) : null
}

// 打开弹窗
const open = (prompt?: Prompt) => {
  visible.value = true
  showForm.value = false
  selectedPrompt.value = null
  if (prompt) {
    selectedPrompt.value = prompt
  }
  loadList()
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  showForm.value = false
  resetForm()
}

// 加载列表
const loadList = async () => {
  try {
    const res = await getPromptList()
    promptList.value = res.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载提示词列表失败')
  }
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  currentUserId.value = getCurrentUserId()
  isCreator.value = true  // 新建时默认是创建者
  showForm.value = true
}

// 编辑
const handleEdit = (p: Prompt) => {
  isEdit.value = true
  editingId.value = p.id
  form.name = p.name
  form.description = p.description || ''
  form.prompt = p.prompt
  form.category = p.category || ''
  form.isPublic = p.isPublic
  currentUserId.value = getCurrentUserId()
  isCreator.value = p.createBy === currentUserId.value
  showForm.value = true
}

// 选用提示词
const handleSelect = (p: Prompt) => {
  selectedPrompt.value = p
  emit('select', p)
  visible.value = false
}

// 清除选中
const handleClearSelect = () => {
  selectedPrompt.value = null
  emit('select', null)
}

// 取消编辑
const handleCancel = () => {
  showForm.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.name = ''
  form.description = ''
  form.prompt = ''
  form.category = ''
  form.isPublic = 0
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    if (isEdit.value && editingId.value) {
      await updatePrompt(editingId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createPrompt(form)
      ElMessage.success('创建成功')
    }
    showForm.value = false
    loadList()
    resetForm()
  } catch (error) {
    console.error(error)
    ElMessage.error(isEdit.value ? '修改失败' : '创建失败')
  }
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该提示词?', '提示', { type: 'warning' })
    await deletePrompt(id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

// 暴露方法
defineExpose({ open, refreshList: loadList, selectedPrompt })
</script>

<style scoped lang="scss">
.list-header {
  margin-bottom: 16px;
}

.prompt-name {
  font-weight: 600;
  color: #1F2937;
}

.prompt-desc {
  color: #6B7280;
  font-size: 13px;
}

.time-text {
  color: #9CA3AF;
  font-size: 12px;
}

.select-tip {
  margin-top: 16px;
  padding: 12px 16px;
  background: #ECFDF5;
  border: 1px solid #10B981;
  border-radius: 8px;
  color: #059669;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.prompt-form {
  .form-tip {
    margin-left: 12px;
    font-size: 12px;
    color: #9CA3AF;
  }

  .form-actions {
    margin-top: 24px;
    text-align: right;
  }
}
</style>