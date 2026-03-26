import request from '@/utils/request'

// 知识库列表
export const getKnowledgeList = (params: any) => {
  return request({
    url: '/knowledge/kb/list',
    method: 'get',
    params
  })
}

// 创建知识库
export const createKnowledge = (data: any) => {
  return request({
    url: '/knowledge/kb/create',
    method: 'post',
    data
  })
}

// 获取知识库详情
export const getKnowledgeDetail = (id: number) => {
  return request({
    url: `/knowledge/kb/${id}`,
    method: 'get'
  })
}

// 修改知识库
export const updateKnowledge = (data: any) => {
  return request({
    url: '/knowledge/kb',
    method: 'put',
    data
  })
}

// 删除知识库
export const deleteKnowledge = (id: number) => {
  return request({
    url: `/knowledge/kb/${id}`,
    method: 'delete'
  })
}

// 上传文档
export const uploadDocument = (kbId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/knowledge/kb/${kbId}/doc/upload`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 文档列表
export const getDocumentList = (kbId: number, params?: any) => {
  return request({
    url: `/knowledge/kb/${kbId}/doc/list`,
    method: 'get',
    params: { kbId, ...params }
  })
}

// 删除文档
export const deleteDocument = (kbId: number, docId: number) => {
  return request({
    url: `/knowledge/kb/${kbId}/doc/${docId}`,
    method: 'delete'
  })
}

// 重建索引
export const rebuildIndex = (kbId: number) => {
  return request({
    url: `/knowledge/kb/${kbId}/rebuild`,
    method: 'post'
  })
}
