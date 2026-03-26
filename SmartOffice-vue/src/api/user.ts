import request from '@/utils/request'

// 登录
export const login = (data: { username: string; password: string }) => {
  return request({
    url: '/ucenter/login',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/ucenter/info',
    method: 'get'
  })
}

// 登出
export const logout = () => {
  return request({
    url: '/ucenter/logout',
    method: 'post'
  })
}

// 用户列表
export const getUserList = (params: any) => {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

// 新增用户
export const addUser = (data: any) => {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

// 修改用户
export const updateUser = (data: any) => {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

// 删除用户
export const deleteUser = (id: number) => {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}

// 重置密码
export const resetPassword = (id: number) => {
  return request({
    url: `/system/user/resetPwd/${id}`,
    method: 'put'
  })
}

// 角色列表
export const getRoleList = () => {
  return request({
    url: '/system/role/list',
    method: 'get'
  })
}

// 新增角色
export const addRole = (data: any) => {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

// 修改角色
export const updateRole = (data: any) => {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

// 删除角色
export const deleteRole = (id: number) => {
  return request({
    url: `/system/role/${id}`,
    method: 'delete'
  })
}

// 获取角色菜单
export const getRoleMenus = (id: number) => {
  return request({
    url: `/system/role/${id}/menus`,
    method: 'get'
  })
}

// 分配菜单
export const assignMenus = (id: number, menuIds: number[]) => {
  return request({
    url: `/system/role/${id}/menus`,
    method: 'put',
    data: menuIds
  })
}

// 获取用户菜单路由
export const getRoutes = () => {
  return request({
    url: '/system/menu/routes',
    method: 'get'
  })
}

// 菜单列表
export const getMenuList = () => {
  return request({
    url: '/system/menu/list',
    method: 'get'
  })
}
