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
    <div class="search-bar">
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

    <!-- 标签页 -->
    <div class="notice-tabs" v-if="isAdmin">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="已发布" name="published" />
        <el-tab-pane label="草稿箱" name="draft" />
      </el-tabs>
    </div>

    <!-- 公告列表 -->
    <div class="notice-list">
      <div
        class="notice-item"
        :class="{ unread: !item.isRead && activeTab !== 'draft' }"
        v-for="item in noticeList"
        :key="item.id"
        @click="activeTab === 'draft' ? handleEdit(item) : handleView(item)"
      >
        <div class="notice-icon" :class="'type-' + item.noticeType">
          <span v-if="!item.isRead && activeTab !== 'draft'" class="unread-dot"></span>
          <span v-else-if="item.noticeType === 1">📢</span>
          <span v-else>📋</span>
        </div>
        <div class="notice-content">
          <div class="notice-title">
            <span class="notice-type" :class="'type-' + item.noticeType">
              {{ item.noticeType === 1 ? '通知' : '公告' }}
            </span>
            <span class="priority-tag" :class="'priority-' + item.priority">
              {{ item.priority === 1 ? '高' : '普通' }}
            </span>
            {{ item.title }}
          </div>
          <div class="notice-meta">
            <span>{{ item.publisherName }}</span>
            <span>{{ formatDateTime(item.publishTime) }}</span>
            <span>👁 {{ item.viewCount }}</span>
            <el-button
              type="danger"
              link
              v-if="userStore.hasPermission('notice:remove')"
              @click.stop="handleDelete(item)"
            >
              删除
            </el-button>
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
          <span>发布时间：{{ formatDateTime(currentNotice.publishTime) }}</span>
          <span>阅读量：{{ currentNotice.viewCount }}</span>
        </div>
        <div class="detail-content" v-html="currentNotice.content"></div>
      </div>
    </el-dialog>

    <!-- 发布/编辑弹窗 -->
    <el-dialog v-model="publishVisible" :title="isEditMode ? '编辑公告' : '发布公告'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容..." />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio :label="1">通知</el-radio>
            <el-radio :label="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio :label="1">高</el-radio>
            <el-radio :label="2">普通</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="handlePublish">立即发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getNoticeList, getNoticeDetail, publishNotice, updateNotice, deleteNotice } from '@/api/office'
import { useUserStore } from '@/stores/user'
import { useNoticeStore } from '@/stores/notice'
import { formatDateTime } from '@/utils/format'

const userStore = useUserStore()
const noticeStore = useNoticeStore()

const noticeList = ref<any[]>([])
const detailVisible = ref(false)
const publishVisible = ref(false)
const formRef = ref<FormInstance>()
const activeTab = ref('all')
const isEditMode = ref(false)
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
  id: undefined as number | undefined,
  title: '',
  content: '',
  noticeType: 1,
  priority: 2,
  publishStatus: 1
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

// 获取公告列表
const fetchList = async () => {
  try {
    const params: any = { ...searchForm, ...pagination }
    // 根据标签页筛选
    if (activeTab.value === 'published' || activeTab.value === 'all') {
      params.publishStatus = 1
    } else if (activeTab.value === 'draft') {
      params.publishStatus = 0
    }
    const res = await getNoticeList(params)
    let list = res.data.records || []
    // 未读公告置顶，多条未读按 priority 排序，再按发布日期排序
    list.sort((a, b) => {
      // 先按是否未读排序（未读在前）
      if (a.isRead !== b.isRead) {
        return a.isRead ? 1 : -1
      }
      // 再按 priority 排序（1高优先级在前）
      if (a.priority !== b.priority) {
        return (a.priority || 2) - (b.priority || 2)
      }
      // 最后按发布日期排序（新的在前）
      return new Date(b.publishTime).getTime() - new Date(a.publishTime).getTime()
    })
    noticeList.value = list
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

// 标签页切换
const handleTabChange = () => {
  pagination.current = 1
  fetchList()
}

onMounted(() => {
  fetchList()
})

const handleView = async (item: any) => {
  try {
    const res = await getNoticeDetail(item.id)
    await noticeStore.fetchUnreadCount()
    Object.assign(currentNotice, res.data)
    detailVisible.value = true
    // 刷新列表更新已读状态
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

const handleCreate = () => {
  isEditMode.value = false
  form.id = undefined
  form.title = ''
  form.content = ''
  form.noticeType = 1
  form.priority = 2
  form.publishStatus = 0
  publishVisible.value = true
}

const handleEdit = async (item: any) => {
  try {
    const res = await getNoticeDetail(item.id)
    const data = res.data
    isEditMode.value = true
    form.id = data.id
    form.title = data.title
    form.content = data.content || ''
    form.noticeType = data.noticeType
    form.priority = data.priority
    form.publishStatus = data.publishStatus
    publishVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handlePublish = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        form.publishStatus = 1
        if (isEditMode.value) {
          await updateNotice(form)
        } else {
          await publishNotice(form)
        }
        await noticeStore.fetchUnreadCount()
        ElMessage.success('发布成功')
        publishVisible.value = false
        fetchList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleSaveDraft = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        form.publishStatus = 0
        if (isEditMode.value) {
          await updateNotice(form)
        } else {
          await publishNotice(form)
        }
        ElMessage.success('草稿保存成功')
        publishVisible.value = false
        fetchList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleDelete = async (item: any) => {
  try {
    await ElMessageBox.confirm('确定删除该公告吗？', '提示', { type: 'warning' })
    await deleteNotice(item.id)
    await noticeStore.fetchUnreadCount()
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    console.error(error)
  }
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

  .notice-tabs {
    background: #FFFFFF;
    padding: 0 20px;
    border-radius: 12px;
    margin-top: 16px;
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

        &.type-1 {
          background: #EFF6FF;
          border-radius: 8px;
        }

        &.type-2 {
          background: #FEF3C7;
          border-radius: 8px;
        }
      }

      .notice-content {
        flex: 1;

        .notice-title {
          font-size: 15px;
          color: #1F2937;
          font-weight: 500;
          margin-bottom: 8px;

          .notice-type {
            display: inline-block;
            padding: 2px 8px;
            font-size: 12px;
            border-radius: 4px;
            margin-right: 8px;

            &.type-1 {
              background: #DBEAFE;
              color: #1D4ED8;
            }

            &.type-2 {
              background: #FEF3C7;
              color: #B45309;
            }
          }

          .priority-tag {
            display: inline-block;
            padding: 2px 8px;
            font-size: 12px;
            border-radius: 4px;
            margin-right: 8px;

            &.priority-1 {
              background: #FEE2E2;
              color: #DC2626;
              font-weight: bold;
            }

            &.priority-2 {
              background: #F3F4F6;
              color: #6B7280;
            }
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
