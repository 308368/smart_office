import request from '@/utils/request'

// 请假列表
export const getLeaveList = (params?: any) => {
  return request.get('/office/leave/list', { params })
}

// 创建请假
export const createLeave = (data: any) => {
  return request.post('/office/leave/create', data)
}

// 取消请假
export const cancelLeave = (id: number) => {
  return request.put(`/office/leave/cancel/${id}`)
}

// 请假详情
export const getLeaveDetail = (id: number) => {
  return request.get(`/office/leave/${id}`)
}

// 请假审批
export const approveLeave = (id: number, data: { approve: boolean; remark?: string }) => {
  return request.put(`/office/leave/approve/${id}`, data)
}

// 待审批请假列表
export const getPendingLeaveList = (params?: any) => {
  return request.get('/office/leave/pending', { params })
}

// 剩余假期
export const getLeaveBalance = () => {
  return request.get('/office/leave/balance')
}

// 通知公告列表
export const getNoticeList = (params?: any) => {
  return request.get('/office/notice/list', { params })
}

// 通知公告详情
export const getNoticeDetail = (id: number) => {
  return request.get(`/office/notice/${id}`)
}

// 发布公告
export const publishNotice = (data: any) => {
  return request.post('/office/notice', data)
}

// 更新公告
export const updateNotice = (data: any) => {
  return request.put('/office/notice', data)
}

// 删除公告
export const deleteNotice = (id: number) => {
  return request.delete(`/office/notice/${id}`)
}

// 消息列表
export const getMessageList = (params?: any) => {
  return request.get('/office/message/list', { params })
}

// 标记已读
export const markMessageRead = (id: number) => {
  return request.put(`/office/message/read/${id}`)
}

// 未读数量
export const getUnreadCount = () => {
  return request.get('/office/notice/unreadCount')
}

// ========== 报销相关 API ==========

// 创建报销申请
export const createExpense = (data: any) => {
  const formData = new FormData()
  formData.append('expenseType', data.expenseType)
  formData.append('totalAmount', data.totalAmount.toString())
  formData.append('description', data.description)

  // 收集需要上传的文件
  const filesToUpload: File[] = []

  // 将 items 序列化为 JSON 字符串发送（排除 file 属性）
  const itemsJson = JSON.stringify(data.items.filter((item: any) => item.itemType && item.amount > 0).map((item: any) => {
    const { file, ...rest } = item
    return rest
  }))
  formData.append('itemsJson', itemsJson)

  // 收集文件
  data.items.forEach((item: any) => {
    if (item.file) {
      filesToUpload.push(item.file)
    }
  })

  // 添加文件
  filesToUpload.forEach((file: File) => {
    formData.append('files', file)
  })

  return request.post('/office/expense/create', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 我的报销列表
export const getExpenseList = (params?: any) => {
  return request.get('/office/expense/list', { params })
}

// 报销详情
export const getExpenseDetail = (id: number) => {
  return request.get(`/office/expense/${id}`)
}

// 待审批报销列表
export const getExpensePending = (params?: any) => {
  return request.get('/office/expense/pending', { params })
}

// 审批报销
export const approveExpense = (id: number, data: { approved: boolean; comment?: string }) => {
  return request.put(`/office/expense/approve/${id}`, data)
}

// 取消报销
export const cancelExpense = (id: number) => {
  return request.put(`/office/expense/cancel/${id}`)
}
