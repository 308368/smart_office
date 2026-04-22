<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="collapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <el-icon :size="28" color="#10B981"><ChatDotRound /></el-icon>
        <span v-show="!collapse" class="logo-text">SmartOffice</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="collapse"
        :router="true"
        class="menu"
        background-color="#FFFFFF"
        text-color="#4B5563"
        active-text-color="#10B981"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/knowledge" v-if="hasPermission('knowledge')">
          <el-icon><Collection /></el-icon>
          <span>知识库</span>
        </el-menu-item>

        <el-menu-item index="/ai-chat" v-if="hasPermission('ai')">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI助手</span>
        </el-menu-item>

        <el-menu-item index="/ticket" v-if="hasPermission('ticket')">
          <el-icon><Tickets /></el-icon>
          <span>工单管理</span>
        </el-menu-item>

        <el-menu-item index="/leave" v-if="hasPermission('leave')">
          <el-icon><Calendar /></el-icon>
          <span>请假申请</span>
        </el-menu-item>

        <el-menu-item index="/leave/approve" v-if="hasPermission('leave:approve')">
          <el-icon><DocumentChecked /></el-icon>
          <span>请假审批</span>
        </el-menu-item>

        <el-menu-item index="/notice" v-if="hasPermission('notice')">
          <el-icon><Bell /></el-icon>
          <span>通知公告</span>
        </el-menu-item>

        <el-sub-menu index="system" v-if="isAdmin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主体区域 -->
    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="collapse = !collapse">
            <Fold v-if="!collapse" />
            <Expand v-else />
          </el-icon>
        </div>

        <div class="header-right">
          <el-badge :value="noticeStore.unreadCount" :hidden="noticeStore.unreadCount === 0" class="message-badge">
            <el-icon :size="20" @click="goToNotice"><Bell /></el-icon>
          </el-badge>

          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <span class="username">{{ userStore.nickname || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNoticeStore } from '@/stores/notice'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const noticeStore = useNoticeStore()

const collapse = ref(false)

const activeMenu = computed(() => route.path)

const isAdmin = computed(() => {
  // 检查是否有系统管理相关权限
  return userStore.permissions.some(p => p.startsWith('system:'))
})

// 检查是否有指定前缀的权限（如 knowledge:xxx 任意一个即可显示知识库菜单）
const hasPermission = (prefix: string) => {
  if (prefix.includes(':')) {
    // 精确权限，如 leave:approve
    return userStore.permissions.some(p => p === prefix)
  }
  // 前缀匹配，如 knowledge
  return userStore.permissions.some(p => p.startsWith(prefix + ':'))
}

// 获取未读消息数
const fetchUnreadCount = async () => {
  await noticeStore.fetchUnreadCount()
}

// 跳转到通知公告页面
const goToNotice = async () => {
  // await fetchUnreadCount()
  router.push('/notice')
}

fetchUnreadCount()

// 处理下拉菜单
const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logoutAction()
    router.push('/login')
  } else if (command === 'profile') {
    // 跳转到个人中心
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;

  .aside {
    background: #FFFFFF;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
    transition: width 0.3s;
    overflow: hidden;

    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      border-bottom: 1px solid #E5E7EB;

      .logo-text {
        font-size: 18px;
        font-weight: 600;
        color: #059669;
        white-space: nowrap;
      }
    }

    .menu {
      border-right: none;
      padding-top: 8px;

      .el-menu-item {
        margin: 4px 8px;
        border-radius: 8px;

        &.is-active {
          background: #ECFDF5 !important;
        }
      }
    }
  }

  .header {
    background: #FFFFFF;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    z-index: 10;

    .header-left {
      .collapse-icon {
        font-size: 20px;
        cursor: pointer;
        color: #4B5563;

        &:hover {
          color: #10B981;
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .message-badge {
        cursor: pointer;

        &:hover {
          color: #10B981;
        }
      }

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;

        .username {
          color: #4B5563;
          font-size: 14px;
        }
      }
    }
  }

  .main {
    background: #F3F4F6;
    padding: 20px;
    overflow-y: auto;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
