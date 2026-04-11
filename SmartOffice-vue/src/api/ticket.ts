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

// 处理工单（接单，状态变为处理中）
export const handleTicket = (id: number, data: { handlerId: number; handlerName: string; remark?: string }) => {
  return request.put(`/office/ticket/${id}/handle`, data)
}

// 已解决（提交处理结果，状态变为已解决）
export const resolveTicket = (id: number, data: { remark: string }) => {
  return request.put(`/office/ticket/${id}/resolve`, data)
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

// 回复工单
export const replyTicket = (id: number, data: { content: string }) => {
  return request.post(`/office/ticket/${id}/reply`, data)
}

// 关闭工单
export const closeTicket = (id: number) => {
  return request.put(`/office/ticket/${id}/close`)
}

// 上传工单附件
export const uploadTicketFile = (file: File, ticketId: number) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('ticketId', ticketId.toString())
  return request.post('/office/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
