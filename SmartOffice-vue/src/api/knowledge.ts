import request from '@/utils/request'

// 知识库列表
export const getKnowledgeList = (params: any) => {
  return request.get('/knowledge/kb/list', { params })
}

// 创建知识库
export const createKnowledge = (data: any) => {
  return request.post('/knowledge/kb/create', data)
}

// 获取知识库详情
export const getKnowledgeDetail = (id: number) => {
  return request.get(`/knowledge/kb/${id}`)
}

// 修改知识库
export const updateKnowledge = (data: any) => {
  return request.put('/knowledge/kb', data)
}

// 删除知识库
export const deleteKnowledge = (id: number) => {
  return request.delete(`/knowledge/kb/${id}`)
}

// 上传文档
export const uploadDocument = (kbId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/knowledge/kb/${kbId}/doc/upload`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 文档列表
export const getDocumentList = (kbId: number, params?: any) => {
  return request.get(`/knowledge/kb/${kbId}/doc/list`, { params: { kbId, ...params } })
}

// 删除文档
export const deleteDocument = (kbId: number, docId: number) => {
  return request.delete(`/knowledge/kb/${kbId}/doc/${docId}`)
}

// 重建索引
export const rebuildIndex = (kbId: number) => {
  return request.post(`/knowledge/kb/${kbId}/rebuild`)
}
