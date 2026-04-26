import request from '@/utils/request'

// AI问答（流式）
export const chatStream = (params: {
  kbIds: string
  question: string
  sessionId?: string
}) => {
  return request.get('/ai/chat/stream', { params, responseType: 'text' })
}

// 获取会话历史列表
export const getChatHistory = () => {
  return request.get('/ai/history')
}

// 获取指定会话的消息列表
export const getChatMessages = (sessionId: number) => {
  return request.get(`/ai/history/${sessionId}/messages`)
}

// 删除会话
export const deleteChatHistory = (sessionId: number) => {
  return request.delete(`/ai/history/${sessionId}`)
}

// 创建新会话
export const createChatSession = () => {
  return request.post('/ai/session')
}

// 获取知识库列表（供AI对话选择）
// isOwe: 1-有knowledge:list权限，可看所有知识库文档；0-只能看公开和自己创建的
export const getAIKnowledgeList = (isOwe: number) => {
  return request.get(`/ai/kb/list/${isOwe}`)
}

// ============ 提示词管理 API ============

// 获取提示词列表
export const getPromptList = () => {
  return request.get('/ai/prompt/list')
}

// 创建提示词
export const createPrompt = (data: {
  name: string
  description?: string
  prompt: string
  category?: string
  isPublic?: number
}) => {
  return request.post('/ai/prompt/create', data)
}

// 修改提示词
export const updatePrompt = (id: number, data: {
  name: string
  description?: string
  prompt: string
  category?: string
  isPublic?: number
}) => {
  return request.put(`/ai/prompt/${id}`, data)
}

// 删除提示词
export const deletePrompt = (id: number) => {
  return request.delete(`/ai/prompt/${id}`)
}
