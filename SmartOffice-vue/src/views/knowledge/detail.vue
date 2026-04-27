<template>
  <div class="knowledge-detail">
    <!-- 头部 -->
    <div class="detail-header">
      <el-button @click="$router.back()" text>
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>{{ kbInfo.name }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openUpload">
          <el-icon><Upload /></el-icon>
          上传文档
        </el-button>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="detail-content">
      <!-- 左侧：目录树 -->
      <div class="left-panel">
        <div class="panel-title">📂 文档列表</div>
        <div class="doc-tree">
          <div
            class="doc-item"
            v-for="doc in docList"
            :key="doc.id"
            @click="selectDoc(doc)"
          >
            <el-icon><Document /></el-icon>
            <span class="doc-name">{{ doc.title }}</span>
            <el-tag :type="getStatusType(doc.status)" size="small">
              {{ getStatusText(doc.status) }}
            </el-tag>
          </div>
          <el-empty v-if="docList.length === 0" description="暂无文档" :image-size="60" />
        </div>
      </div>

      <!-- 右侧：文档详情 -->
      <div class="right-panel">
        <div class="panel-title">
          📄 文档详情
          <span v-if="selectedDoc" class="doc-count">共 {{ selectedDoc.chunkCount || 0 }} 个切片</span>
        </div>
        <div class="doc-detail" v-if="selectedDoc">
          <div class="detail-item">
            <span class="label">文件名：</span>
            <span class="value">{{ selectedDoc.title }}</span>
          </div>
          <div class="detail-item">
            <span class="label">文件大小：</span>
            <span class="value">{{ formatSize(selectedDoc.fileSize) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">解析状态：</span>
            <el-tag :type="getStatusType(selectedDoc.status)">
              {{ getStatusText(selectedDoc.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="label">切片数量：</span>
            <span class="value">{{ selectedDoc.chunkCount || 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="label">Token数量：</span>
            <span class="value">{{ selectedDoc.tokenCount || 0 }}</span>
          </div>
          <div class="detail-actions">
            <el-button type="danger" size="small" @click="handleDelete">
              删除文档
            </el-button>
          </div>
        </div>
        <el-empty v-else description="请选择左侧文档查看详情" :image-size="80" />
      </div>
    </div>

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传文档" width="500px">
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :action="`/api/knowledge/kb/${kbId}/doc/upload`"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 PDF、Word、Excel、PPT、Markdown 等格式
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getKnowledgeDetail, getDocumentList, deleteDocument } from '@/api/knowledge'
import { addDocumentListener } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const kbId = Number(route.params.id)

const kbInfo = ref<any>({})
const docList = ref<any[]>([])
const selectedDoc = ref<any>(null)
const uploadVisible = ref(false)
const uploadRef = ref()
const fileList = ref<any[]>([])

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

// 获取知识库详情
const fetchKBDetail = async () => {
  try {
    const res = await getKnowledgeDetail(kbId)
    kbInfo.value = res.data
  } catch (error) {
    console.error(error)
  }
}

// 获取文档列表
const fetchDocList = async () => {
  try {
    const res = await getDocumentList(kbId, { current: 1, size: 100 })
    docList.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchKBDetail()
  fetchDocList()

  // 监听文档分块完成消息
  removeDocumentListener = addDocumentListener((data) => {
    if (data.kbId === kbId) {
      // 如果是当前知识库的文档，刷新列表
      fetchDocList()
    }
  })
})

onUnmounted(() => {
  if (removeDocumentListener) {
    removeDocumentListener()
  }
})

let removeDocumentListener = () => {}

const selectDoc = (doc: any) => {
  selectedDoc.value = doc
}

// 打开上传弹窗时清空之前的选择
const openUpload = () => {
  fileList.value = []
  uploadVisible.value = true
}

const getStatusType = (status: number) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status as keyof typeof types] || 'info'
}

const getStatusText = (status: number) => {
  const texts = { 0: '待处理', 1: '解析中', 2: '已完成', 3: '解析失败' }
  return texts[status as keyof typeof texts] || '未知'
}

const formatSize = (size: number) => {
  if (!size) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(2)} ${units[i]}`
}

const submitUpload = () => {
  uploadRef.value?.submit()
}

const handleUploadSuccess = () => {
  ElMessage.success('上传成功，正在解析...')
  uploadVisible.value = false
  fileList.value = []
  fetchDocList()
}

// 文件选择时，只保留最后一个文件
const handleFileChange = (_file: any, files: any[]) => {
  if (files.length > 1) {
    fileList.value = [files[files.length - 1]]
  } else {
    fileList.value = files
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleDelete = async () => {
  if (!selectedDoc.value) return
  try {
    await deleteDocument(kbId, selectedDoc.value.id)
    ElMessage.success('删除成功')
    selectedDoc.value = null
    fetchDocList()
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped lang="scss">
.knowledge-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #FFFFFF;
    border-radius: 12px;

    h2 {
      flex: 1;
      font-size: 18px;
      font-weight: 600;
      color: #1F2937;
    }
  }

  .detail-content {
    display: grid;
    grid-template-columns: 350px 1fr;
    gap: 20px;

    .left-panel, .right-panel {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 20px;
      min-height: 500px;

      .panel-title {
        font-size: 15px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #E5E7EB;

        .doc-count {
          font-weight: normal;
          font-size: 13px;
          color: #9CA3AF;
          margin-left: 12px;
        }
      }
    }

    .doc-tree {
      .doc-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: #F9FAFB;
        }

        .doc-name {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          color: #4B5563;
        }
      }
    }

    .doc-detail {
      .detail-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #F3F4F6;

        .label {
          width: 100px;
          color: #6B7280;
          font-size: 14px;
        }

        .value {
          color: #1F2937;
          font-size: 14px;
        }
      }

      .detail-actions {
        margin-top: 24px;
        display: flex;
        gap: 12px;
      }
    }
  }
}
</style>
