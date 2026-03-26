import request from '@/utils/request'

// 请假列表
export const getLeaveList = (params?: any) => {
  return request({
    url: '/office/leave/list',
    method: 'get',
    params
  })
}

// 创建请假
export const createLeave = (data: any) => {
  return request({
    url: '/office/leave/create',
    method: 'post',
    data
  })
}

// 取消请假
export const cancelLeave = (id: number) => {
  return request({
    url: `/office/leave/cancel/${id}`,
    method: 'put'
  })
}

// 请假审批
export const approveLeave = (id: number, data: { approve: boolean; remark?: string }) => {
  return request({
    url: `/office/leave/approve/${id}`,
    method: 'put',
    data
  })
}

// 剩余假期
export const getLeaveBalance = () => {
  return request({
    url: '/office/leave/balance',
    method: 'get'
  })
}

// 通知公告列表
export const getNoticeList = (params?: any) => {
  return request({
    url: '/office/notice/list',
    method: 'get',
    params
  })
}

// 通知公告详情
export const getNoticeDetail = (id: number) => {
  return request({
    url: `/office/notice/${id}`,
    method: 'get'
  })
}

// 发布公告
export const publishNotice = (data: any) => {
  return request({
    url: '/office/notice',
    method: 'post',
    data
  })
}

// 删除公告
export const deleteNotice = (id: number) => {
  return request({
    url: `/office/notice/${id}`,
    method: 'delete'
  })
}

// 消息列表
export const getMessageList = (params?: any) => {
  return request({
    url: '/office/message/list',
    method: 'get',
    params
  })
}

// 标记已读
export const markMessageRead = (id: number) => {
  return request({
    url: `/office/message/read/${id}`,
    method: 'put'
  })
}

// 未读数量
export const getUnreadCount = () => {
  return request({
    url: '/office/message/unreadCount',
    method: 'get'
  })
}
