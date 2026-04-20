import request from '@/utils/request'

// AI问答（非流式）
export const chat = (data: {
  kbIds: number[]
  question: string
  sessionId?: string
}) => {
  return request.post('/ai/chat', data)
}

// AI问答（流式）
export const chatStream = (params: {
  kbIds: string
  question: string
  sessionId?: string
}) => {
  return request.get('/ai/chat/stream', { params, responseType: 'text' })
}

// 对话历史列表
export const getChatHistory = (params?: { sessionId?: string; current?: number; size?: number }) => {
  return request.get('/ai/history', { params })
}

// 删除对话历史
export const deleteChatHistory = (id: number) => {
  return request.delete(`/ai/history/${id}`)
}

// 获取知识库列表（供AI对话选择）
// isOwe: 1-有knowledge:list权限，可看所有知识库文档；0-只能看公开和自己创建的
export const getAIKnowledgeList = (isOwe: number) => {
  return request.get(`/ai/kb/list/${isOwe}`)
}
