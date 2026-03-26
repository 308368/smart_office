import request from '@/utils/request'

// AI问答（非流式）
export const chat = (data: {
  kbIds: number[]
  question: string
  sessionId?: string
}) => {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

// AI问答（流式）
export const chatStream = (params: {
  kbIds: string
  question: string
  sessionId?: string
}) => {
  return request({
    url: '/ai/chat/stream',
    method: 'get',
    params,
    responseType: 'text'
  })
}

// 对话历史列表
export const getChatHistory = (params?: { sessionId?: string; current?: number; size?: number }) => {
  return request({
    url: '/ai/history',
    method: 'get',
    params
  })
}

// 删除对话历史
export const deleteChatHistory = (id: number) => {
  return request({
    url: `/ai/history/${id}`,
    method: 'delete'
  })
}

// 获取知识库列表（供AI对话选择）
export const getAIKnowledgeList = () => {
  return request({
    url: '/ai/kb/list',
    method: 'get'
  })
}
