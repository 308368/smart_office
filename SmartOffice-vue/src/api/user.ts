import request from '@/utils/request'

// 登录
// export const login = (data: { username: string; password: string }) => {
//   return request({
//     url: '/ucenter/login',
//     method: 'post',
//     data
//   })
// }
// 登录
export const login = (params: any) => {
  return request.post('/ucenter/login', null, { params })
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/ucenter/info')
}

// 登出
export const logout = () => {
  return request.post('/ucenter/logout')
}

// 用户列表
export const getUserList = (params: any) => {
  return request.get('/system/user/list', { params })
}

// 获取用户详情
export const getUserById = (id: number) => {
  return request.get(`/system/user/${id}`)
}

// 部门列表
export const getDeptList = () => {
  return request.get('/system/dept/list')
}

// 新增用户
export const addUser = (data: any) => {
  return request.post('/system/user', data)
}

// 修改用户
export const updateUser = (data: any) => {
  return request.put('/system/user', data)
}

// 删除用户
export const deleteUser = (id: number) => {
  return request.delete(`/system/user/${id}`)
}

// 用户分配角色
export const assignUserRoles = (userId: number, roleIds: number[]) => {
  return request.put(`/system/user/${userId}/roles`, roleIds)
}

// 获取用户角色
export const getUserRoles = (userId: number) => {
  return request.get(`/system/user/${userId}/roles`)
}

// 重置密码
export const resetPassword = (id: number) => {
  return request.put(`/system/user/resetPwd/${id}`)
}

// 角色列表
export const getRoleList = () => {
  return request.get('/system/role/list')
}

// 新增角色
export const addRole = (data: any) => {
  return request.post('/system/role', data)
}

// 修改角色
export const updateRole = (data: any) => {
  return request.put('/system/role', data)
}

// 删除角色
export const deleteRole = (id: number) => {
  return request.delete(`/system/role/${id}`)
}

// 获取角色菜单
export const getRoleMenus = (id: number) => {
  return request.get(`/system/role/${id}/menus`)
}

// 分配菜单
export const assignMenus = (id: number, menuIds: number[]) => {
  return request.put(`/system/role/${id}/menus`, menuIds)
}

// 获取用户菜单路由
export const getRoutes = () => {
  return request.get('/system/menu/routes')
}

// 菜单列表
export const getMenuList = () => {
  return request.get('/system/menu/list')
}

// 新增菜单
export const addMenu = (data: any) => {
  return request.post('/system/menu', data)
}

// 更新菜单
export const updateMenu = (data: any) => {
  return request.put('/system/menu', data)
}

// 删除菜单
export const deleteMenu = (id: number) => {
  return request.delete(`/system/menu/${id}`)
}
