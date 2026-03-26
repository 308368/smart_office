import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getMockResponse } from './mock'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    // 添加用户ID
    const userId = localStorage.getItem('userId')
    if (userId && config.headers) {
      config.headers['X-User-Id'] = userId
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      router.push('/login')
      return Promise.reject(new Error(res.msg || '登录已过期'))
    } else if (res.code === 403) {
      ElMessage.error('没有权限')
      return Promise.reject(new Error(res.msg || '没有权限'))
    } else {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  (error) => {
    // 网络错误时检查是否有模拟数据
    const originalRequest = error.config
    if (originalRequest) {
      const mockData = getMockResponse(originalRequest.url || '', originalRequest.method || 'get')
      if (mockData) {
        console.log(`[Mock] ${originalRequest.method?.toUpperCase()} ${originalRequest.url}`)
        return Promise.resolve(mockData)
      }
    }
    ElMessage.error(error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

// 封装请求方法，支持模拟数据
const request = async (config: AxiosRequestConfig): Promise<any> => {
  // 先检查是否有模拟数据
  const mockData = getMockResponse(config.url || '', config.method || 'get')
  if (mockData) {
    console.log(`[Mock] ${config.method?.toUpperCase()} ${config.url}`)
    return Promise.resolve(mockData)
  }

  // 没有模拟数据，发起真实请求
  return service(config)
}

// 导出封装后的请求方法
export default {
  get: (url: string, config?: AxiosRequestConfig) => request({ ...config, url, method: 'get' }),
  post: (url: string, data?: any, config?: AxiosRequestConfig) => request({ ...config, url, method: 'post', data }),
  put: (url: string, data?: any, config?: AxiosRequestConfig) => request({ ...config, url, method: 'put', data }),
  delete: (url: string, config?: AxiosRequestConfig) => request({ ...config, url, method: 'delete' })
}
