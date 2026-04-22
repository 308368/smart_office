import request from '@/utils/request'

// 首页统计数据
export const getDashboardStats = () => {
  return request.get('/dashboard/stats')
}

// AI对话统计（最近7天）
export const getChatStats = () => {
  return request.get('/ai/chat/stats')
}

// 待处理工单列表
export const getPendingTicketList = (params?: any) => {
  return request.get('/office/ticket/pending', { params })
}

// 首页公告列表
export const getHomeNotices = (params?: any) => {
  return request.get('/office/notice/home', { params })
}