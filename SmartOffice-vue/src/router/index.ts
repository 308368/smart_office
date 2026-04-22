import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House' }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/index.vue'),
        meta: { title: '知识库', icon: 'Collection' }
      },
      {
        path: 'knowledge/detail/:id',
        name: 'KnowledgeDetail',
        component: () => import('@/views/knowledge/detail.vue'),
        meta: { title: '知识库详情', hidden: true }
      },
      {
        path: 'ai-chat',
        name: 'AIChat',
        component: () => import('@/views/ai-chat/index.vue'),
        meta: { title: 'AI助手', icon: 'ChatDotRound' }
      },
      {
        path: 'ticket',
        name: 'Ticket',
        component: () => import('@/views/ticket/index.vue'),
        meta: { title: '工单管理', icon: 'Tickets' }
      },
      {
        path: 'ticket/detail/:id',
        name: 'TicketDetail',
        component: () => import('@/views/ticket/detail.vue'),
        meta: { title: '工单详情', hidden: true }
      },
      {
        path: 'leave',
        name: 'Leave',
        component: () => import('@/views/leave/index.vue'),
        meta: { title: '请假申请', icon: 'Calendar' }
      },
      {
        path: 'leave/approve',
        name: 'LeaveApprove',
        component: () => import('@/views/leave/approve.vue'),
        meta: { title: '请假审批', icon: 'DocumentChecked', permission: 'leave:approve' }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/views/notice/index.vue'),
        meta: { title: '通知公告', icon: 'Bell' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['SUPER_ADMIN'] }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', roles: ['SUPER_ADMIN'] }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu', roles: ['SUPER_ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()

  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    next()
  } else {
    if (token) {
      // 检查权限
      const userStore = useUserStore()
      // 只有非 mock-token 的真实 token 才调用 getUserInfo
      if (!userStore.userId && !token.startsWith('mock-token-')) {
        userStore.getUserInfo().then(() => {
          next()
        }).catch(() => {
          localStorage.removeItem('token')
          localStorage.removeItem('userId')
          next('/login')
        })
      } else if (!userStore.userId && token.startsWith('mock-token-')) {
        // mock-token 使用模拟数据，直接放行
        next()
      } else {
        next()
      }
    } else {
      next('/login')
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
