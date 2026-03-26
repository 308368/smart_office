<template>
  <div class="notice-page">
    <!-- 页面头部 -->
    <div class="page-header" v-if="isAdmin">
      <h2>通知公告</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        发布公告
      </el-button>
    </div>
    <div class="page-header" v-else>
      <h2>通知公告</h2>
    </div>

    <!-- 搜索 -->
    <div class="search-bar" v-if="isAdmin">
      <el-input
        v-model="searchForm.title"
        placeholder="搜索公告标题..."
        clearable
        style="width: 240px"
        @keyup.enter="fetchList"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button @click="fetchList">搜索</el-button>
    </div>

    <!-- 公告列表 -->
    <div class="notice-list">
      <div
        class="notice-item"
        :class="{ unread: !item.isRead }"
        v-for="item in noticeList"
        :key="item.id"
        @click="handleView(item)"
      >
        <div class="notice-icon">
          <span v-if="!item.isRead" class="unread-dot"></span>
          <span v-else>📄</span>
        </div>
        <div class="notice-content">
          <div class="notice-title">
            <span class="is-top" v-if="item.isTop">置顶</span>
            {{ item.title }}
          </div>
          <div class="notice-meta">
            <span>{{ item.publisherName }}</span>
            <span>{{ item.publishTime }}</span>
            <span>👁 {{ item.viewCount }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="noticeList.length === 0" description="暂无公告" />
    </div>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @change="fetchList"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentNotice.title" width="700px">
      <div class="notice-detail">
        <div class="detail-meta">
          <span>发布人：{{ currentNotice.publisherName }}</span>
          <span>发布时间：{{ currentNotice.publishTime }}</span>
          <span>阅读量：{{ currentNotice.viewCount }}</span>
        </div>
        <div class="detail-content" v-html="currentNotice.content"></div>
      </div>
    </el-dialog>

    <!-- 发布弹窗 -->
    <el-dialog v-model="publishVisible" title="发布公告" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容..." />
        </el-form-item>
        <el-form-item label="发布范围">
          <el-radio-group v-model="form.publishRange">
            <el-radio label="all">全公司</el-radio>
            <el-radio label="dept">指定部门</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.isTop" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, FormInstance } from 'element-plus'
import { getNoticeList, getNoticeDetail, publishNotice, deleteNotice } from '@/api/office'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const noticeList = ref<any[]>([])
const detailVisible = ref(false)
const publishVisible = ref(false)
const formRef = ref<FormInstance>()
const currentNotice = reactive<any>({
  title: '',
  content: '',
  publisherName: '',
  publishTime: '',
  viewCount: 0
})

const isAdmin = computed(() => userStore.roles.includes('SUPER_ADMIN'))

const searchForm = reactive({ title: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const form = reactive({
  title: '',
  content: '',
  publishRange: 'all',
  isTop: false
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

// 获取公告列表
const fetchList = async () => {
  try {
    const res = await getNoticeList({ ...searchForm, ...pagination })
    noticeList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchList()
})

const handleView = async (item: any) => {
  try {
    const res = await getNoticeDetail(item.id)
    Object.assign(currentNotice, res.data)
    detailVisible.value = true
    // 刷新列表更新已读状态
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

const handleCreate = () => {
  form.title = ''
  form.content = ''
  form.publishRange = 'all'
  form.isTop = false
  publishVisible.value = true
}

const handlePublish = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await publishNotice(form)
        ElMessage.success('发布成功')
        publishVisible.value = false
        fetchList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}
</script>

<style scoped lang="scss">
.notice-page {
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    background: #FFFFFF;
    padding: 16px 20px;
    border-radius: 12px;
  }

  .notice-list {
    background: #FFFFFF;
    border-radius: 12px;
    padding: 0 20px;

    .notice-item {
      display: flex;
      gap: 16px;
      padding: 20px 0;
      border-bottom: 1px solid #F3F4F6;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: #F9FAFB;
        margin: 0 -20px;
        padding: 20px;
      }

      &:last-child {
        border-bottom: none;
      }

      &.unread {
        background: #F0FDF4;
        margin: 0 -20px;
        padding: 20px;
        border-left: 3px solid #10B981;
      }

      .notice-icon {
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        flex-shrink: 0;

        .unread-dot {
          width: 10px;
          height: 10px;
          background: #EF4444;
          border-radius: 50%;
        }
      }

      .notice-content {
        flex: 1;

        .notice-title {
          font-size: 15px;
          color: #1F2937;
          font-weight: 500;
          margin-bottom: 8px;

          .is-top {
            display: inline-block;
            padding: 2px 8px;
            background: #FEE2E2;
            color: #DC2626;
            font-size: 12px;
            border-radius: 4px;
            margin-right: 8px;
          }
        }

        .notice-meta {
          display: flex;
          gap: 16px;
          font-size: 13px;
          color: #9CA3AF;
        }
      }
    }
  }

  .notice-detail {
    .detail-meta {
      display: flex;
      gap: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #E5E7EB;
      margin-bottom: 16px;
      font-size: 13px;
      color: #6B7280;
    }

    .detail-content {
      line-height: 1.8;
      color: #4B5563;
    }
  }
}
</style>
