import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { login, getUserInfo as getUserInfoApi, logout } from '@/api/user'

interface UserInfo {
  userId: number
  username: string
  nickname: string
  email?: string
  phone?: string
  avatar?: string
  deptId?: number
  deptName?: string
  roles: string[]
  permissions: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userId = ref<number>(Number(localStorage.getItem('userId')) || 0)
  const username = ref<string>('')
  const nickname = ref<string>('')
  const avatar = ref<string>('')
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])
  const routes = ref<RouteRecordRaw[]>([])

  // 登录（模拟登录 - 后端开发完成后替换为真实接口）
  const loginAction = async (username: string, password: string) => {
    // 模拟登录验证
    if (username === 'admin' && password === '123456') {
      const mockToken = 'mock-token-' + Date.now()
      const mockData = {
        token: mockToken,
        userId: 1,
        username: 'admin',
        nickname: '系统管理员',
        roles: ['SUPER_ADMIN'],
        permissions: [
          'system:user:list', 'system:user:add', 'system:user:edit', 'system:user:remove',
          'system:role:list', 'system:role:add', 'system:role:edit', 'system:role:remove',
          'system:menu:list', 'system:menu:add', 'system:menu:edit', 'system:menu:remove',
          'knowledge:kb:list', 'knowledge:kb:add', 'knowledge:kb:edit', 'knowledge:kb:remove',
          'knowledge:doc:add', 'knowledge:doc:remove',
          'office:ticket:list', 'office:ticket:add', 'office:ticket:handle', 'office:ticket:approve',
          'leave:list', 'leave:add', 'leave:approve',
          'office:notice:list', 'office:notice:add', 'office:notice:remove'
        ]
      }

      token.value = mockToken
      userId.value = mockData.userId
      nickname.value = mockData.nickname
      roles.value = mockData.roles || []
      permissions.value = mockData.permissions || []

      localStorage.setItem('token', mockToken)
      localStorage.setItem('userId', String(mockData.userId))

      return mockData
    } else {
      throw new Error('用户名或密码错误')
    }

    // 真实接口调用（后端开发完成后使用）
    // const res = await login({ username, password })
    // token.value = res.data.token
    // userId.value = res.data.userId
    // nickname.value = res.data.nickname
    // roles.value = res.data.roles || []
    // localStorage.setItem('token', res.data.token)
    // localStorage.setItem('userId', String(res.data.userId))
    // return res.data
  }

  // 获取用户信息（模拟 - 后端开发完成后替换为真实接口）
  const getUserInfo = async () => {
    // 检查是否有模拟token
    const storedToken = localStorage.getItem('token')
    if (storedToken && storedToken.startsWith('mock-token-')) {
      // 使用模拟数据
      const mockData = {
        userId: 1,
        username: 'admin',
        nickname: '系统管理员',
        email: 'admin@company.com',
        phone: '13800138000',
        avatar: '',
        deptId: 1,
        deptName: '技术部',
        roles: ['SUPER_ADMIN'],
        permissions: [
          'system:user:list', 'system:user:add', 'system:user:edit', 'system:user:remove',
          'system:role:list', 'system:role:add', 'system:role:edit', 'system:role:remove',
          'system:menu:list', 'system:menu:add', 'system:menu:edit', 'system:menu:remove',
          'knowledge:kb:list', 'knowledge:kb:add', 'knowledge:kb:edit', 'knowledge:kb:remove',
          'knowledge:doc:add', 'knowledge:doc:remove',
          'office:ticket:list', 'office:ticket:add', 'office:ticket:handle', 'office:ticket:approve',
          'leave:list', 'leave:add', 'leave:approve',
          'office:notice:list', 'office:notice:add', 'office:notice:remove'
        ]
      }
      userId.value = mockData.userId
      username.value = mockData.username
      nickname.value = mockData.nickname
      avatar.value = mockData.avatar || ''
      roles.value = mockData.roles || []
      permissions.value = mockData.permissions || []
      return mockData
    }

    // 真实接口调用（后端开发完成后使用）
    try {
      const res = await getUserInfoApi()
      
      const data = res.data
      userId.value = data.userId
      username.value = data.username
      nickname.value = data.nickname
      avatar.value = data.avatar || ''
      roles.value = data.roles || []
      permissions.value = data.permissions || []
      return data
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 登出（模拟 - 后端开发完成后替换为真实接口）
  const logoutAction = async () => {
    // 清除本地存储
    token.value = ''
    userId.value = 0
    username.value = ''
    nickname.value = ''
    avatar.value = ''
    roles.value = []
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userId')

    // 真实接口调用（后端开发完成后使用）
    // try {
    //   await logout()
    // } finally {
    //   token.value = ''
    //   userId.value = 0
    //   username.value = ''
    //   nickname.value = ''
    //   avatar.value = ''
    //   roles.value = []
    //   permissions.value = []
    //   localStorage.removeItem('token')
    //   localStorage.removeItem('userId')
    // }
  }

  // 是否有某个权限
  const hasPermission = (permission: string) => {
    return permissions.value.includes(permission)
  }

  // 是否有某个角色
  const hasRole = (role: string) => {
    return roles.value.includes(role)
  }

  return {
    token,
    userId,
    username,
    nickname,
    avatar,
    roles,
    permissions,
    routes,
    loginAction,
    getUserInfo,
    logoutAction,
    hasPermission,
    hasRole
  }
})
