import request from '@/utils/request'

// 工单列表
export const getTicketList = (params: any) => {
  return request({
    url: '/office/ticket/list',
    method: 'get',
    params
  })
}

// 创建工单
export const createTicket = (data: any) => {
  return request({
    url: '/office/ticket/create',
    method: 'post',
    data
  })
}

// 工单详情
export const getTicketDetail = (id: number) => {
  return request({
    url: `/office/ticket/${id}`,
    method: 'get'
  })
}

// 处理工单
export const handleTicket = (id: number, data: { processResult: string }) => {
  return request({
    url: `/office/ticket/${id}/handle`,
    method: 'put',
    data
  })
}

// 审批工单
export const approveTicket = (id: number, data: { approve: boolean; remark?: string }) => {
  return request({
    url: `/office/ticket/${id}/approve`,
    method: 'put',
    data
  })
}

// 转派工单
export const transferTicket = (id: number, data: { targetUserId: number; reason?: string }) => {
  return request({
    url: `/office/ticket/${id}/transfer`,
    method: 'put',
    data
  })
}

// 工单类型列表
export const getTicketTypeList = () => {
  return request({
    url: '/office/ticket/type/list',
    method: 'get'
  })
}
