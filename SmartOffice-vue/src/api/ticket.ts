import request from '@/utils/request'

// 工单列表
export const getTicketList = (params: any) => {
  return request.get('/office/ticket/list', { params })
}

// 创建工单
export const createTicket = (data: any) => {
  return request.post('/office/ticket/create', data)
}

// 工单详情
export const getTicketDetail = (id: number) => {
  return request.get(`/office/ticket/${id}`)
}

// 处理工单
export const handleTicket = (id: number, data: { processResult: string }) => {
  return request.put(`/office/ticket/${id}/handle`, data)
}

// 审批工单
export const approveTicket = (id: number, data: { approve: boolean; remark?: string }) => {
  return request.put(`/office/ticket/${id}/approve`, data)
}

// 转派工单
export const transferTicket = (id: number, data: { targetUserId: number; reason?: string }) => {
  return request.put(`/office/ticket/${id}/transfer`, data)
}

// 工单类型列表
export const getTicketTypeList = () => {
  return request.get('/office/ticket/type/list')
}
