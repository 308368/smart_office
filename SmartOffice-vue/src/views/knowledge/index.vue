<template>
  <div class="knowledge-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>知识库管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增知识库
      </el-button>
    </div>

    <!-- 知识库列表 -->
    <div class="kb-grid">
      <div
        class="kb-card"
        v-for="item in kbList"
        :key="item.id"
        @click="goToDetail(item.id)"
      >
        <div class="kb-icon">
          <el-icon :size="40" color="#10B981"><FolderOpened /></el-icon>
        </div>
        <div class="kb-info">
          <h3 class="kb-name">{{ item.name }}</h3>
          <p class="kb-desc">{{ item.description || '暂无描述' }}</p>
          <div class="kb-meta">
            <span><el-icon><Document /></el-icon> {{ item.docCount }} 文档</span>
            <span>
              <el-tag :type="item.isPublic ? 'success' : 'warning'" size="small">
                {{ item.isPublic ? '公开' : '私有' }}
              </el-tag>
            </span>
          </div>
        </div>
        <div class="kb-actions" @click.stop>
          <el-button type="primary" link @click="handleEdit(item)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(item)">删除</el-button>
        </div>
      </div>

      <!-- 新建卡片 -->
      <div class="kb-card add-card" @click="handleCreate">
        <el-icon :size="40" color="#9CA3AF"><Plus /></el-icon>
        <span>新建知识库</span>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑知识库' : '新增知识库'"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="公开">
          <el-switch v-model="form.isPublic" active-text="公开" inactive-text="私有" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getKnowledgeList, createKnowledge, updateKnowledge, deleteKnowledge } from '@/api/knowledge'

const router = useRouter()

const kbList = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  isPublic: true
})

const rules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }]
}

// 获取知识库列表
const fetchList = async () => {
  try {
    const res = await getKnowledgeList({ current: 1, size: 100 })
    kbList.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchList()
})

// 跳转到详情
const goToDetail = (id: number) => {
  router.push(`/knowledge/detail/${id}`)
}

// 新建
const handleCreate = () => {
  form.id = undefined
  form.name = ''
  form.description = ''
  form.isPublic = true
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑
const handleEdit = (item: any) => {
  form.id = item.id
  form.name = item.name
  form.description = item.description
  form.isPublic = item.isPublic
  isEdit.value = true
  dialogVisible.value = true
}

// 删除
const handleDelete = async (item: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除知识库"${item.name}"吗？`, '提示', {
      type: 'warning'
    })
    await deleteKnowledge(item.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    // 用户取消或删除失败
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateKnowledge(form)
          ElMessage.success('修改成功')
        } else {
          await createKnowledge(form)
          ElMessage.success('创建成功')
        }
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
.knowledge-page {
  .kb-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;

    .kb-card {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 24px;
      cursor: pointer;
      transition: all 0.3s;
      position: relative;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);

        .kb-actions {
          opacity: 1;
        }
      }

      .kb-icon {
        margin-bottom: 16px;
      }

      .kb-info {
        .kb-name {
          font-size: 16px;
          font-weight: 600;
          color: #1F2937;
          margin-bottom: 8px;
        }

        .kb-desc {
          font-size: 13px;
          color: #6B7280;
          margin-bottom: 12px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .kb-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 13px;
          color: #9CA3AF;

          span {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }

      .kb-actions {
        position: absolute;
        top: 12px;
        right: 12px;
        opacity: 0;
        transition: opacity 0.2s;
      }

      &.add-card {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        border: 2px dashed #E5E7EB;
        background: #F9FAFB;
        min-height: 200px;

        &:hover {
          border-color: #10B981;
          background: #ECFDF5;
        }

        span {
          margin-top: 12px;
          color: #9CA3AF;
          font-size: 14px;
        }
      }
    }
  }
}
</style>
