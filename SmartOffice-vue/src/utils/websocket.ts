/**
 * WebSocket 工具模块
 *
 * 功能说明：
 * 1. 使用 SockJS + STOMP 协议实现 WebSocket 通信
 * 2. SockJS：解决浏览器 WebSocket 兼容性问题，在不支持 WS 的环境下自动降级
 * 3. STOMP：简单的消息传输协议，方便订阅/发布主题
 *
 * 连接流程：
 * 1. 创建 SockJS 连接到 /ws 端点（后端提供）
 * 2. 通过 Stomp.over(socket) 创建 STOMP 客户端
 * 3. 连接成功后，订阅 /topic/notice 主题（接收公告推送）
 * 4. 订阅 /user/queue/document 个人文档通知主题
 * 5. 收到消息后，解析并通过 Element Plus Notification 弹窗展示
 */

import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { ElNotification } from 'element-plus'

// STOMP 客户端实例，用于维护 WebSocket 连接
let stompClient: any = null

// 挂到 window 方便调试
window.stompClient = stompClient

// 导出用于调试
export const getStompClient = () => stompClient

// 文档消息监听器列表
type DocumentMessageCallback = (data: {
  type: string
  kbId: number
  docId: number
  docTitle: string
  chunkCount: number
  timestamp: number
}) => void
const documentListeners: DocumentMessageCallback[] = []

/**
 * 添加文档消息监听器
 * @param callback 收到文档消息时的回调函数
 * @returns 取消监听的函数
 */
export const addDocumentListener = (callback: DocumentMessageCallback): (() => void) => {
  documentListeners.push(callback)
  return () => {
    const index = documentListeners.indexOf(callback)
    if (index > -1) {
      documentListeners.splice(index, 1)
    }
  }
}

/**
 * 建立 WebSocket 连接
 *
 * 调用时机：应用启动时（在 main.ts 中调用）
 * 连接地址：通过 Vite 代理 /ws → http://localhost:8084/ws
 *
 * 连接成功后会：
 * 1. 订阅 /topic/notice 公告通知主题
 * 2. 订阅 /user/queue/document 个人文档通知主题
 * 3. 当后端推送新公告时，自动弹窗显示
 */
export const connectWebSocket = () => {
  // 1. 创建 SockJS 实例，连接后端的 /ws 端点
  //    SockJS 会自动处理浏览器兼容性问题
  // const socket = new SockJS('http://localhost:8084/ws')
  const socket = new SockJS('/ws')

  // 2. 使用 STOMP 协议包装 SockJS 连接
  //    STOMP 比原生 WebSocket 更好用，支持订阅/发布模式
  stompClient = Stomp.over(socket)

  // 3. 禁用 STOMP 的 debug 日志（生产环境可移除）
  stompClient.debug = () => {}

  // 4. 建立连接
  stompClient.connect(
    { token: localStorage.getItem('token') },  // 传递 token
    () => {
      console.log('=== WebSocket 连接成功 ===')
      window.stompClient = stompClient  // 挂到 window 方便调试

      // 5. 连接成功后，订阅公告通知主题（广播，所有人可见）
      //    /topic/ 是 STOMP 的广播地址前缀
      //    /topic/notice 是我们自定义的公告通知主题
      stompClient.subscribe('/topic/notice', (message: any) => {
        console.log('=== 收到公告消息 ===')
        console.log('原始消息:', message)
        console.log('body:', message.body)
        console.log('headers:', message.headers)
        // 6. 收到消息时，解析消息体（JSON 格式）
        const data = JSON.parse(message.body)

        // 7. 弹窗通知用户
        // 内容超过50字截断，显示省略号
        const content = data.content || ''
        const displayContent = content.length > 50
          ? content.substring(0, 50) + '...'
          : content
        ElNotification({
          title: '📢 ' + (data.title || '新公告'),
          message: displayContent,
          type: 'info',
          position: 'top-right',
          duration: 5000
        })
      })

      // 8. 订阅个人文档通知主题（点对点，只有自己可见）
      //    /user/ 是 STOMP 的用户专属地址前缀
      //    /queue/document 是文档通知队列
      //    Spring 会自动将消息路由到对应用户的队列
      const docSub = stompClient.subscribe('/user/queue/document', (message: any) => {
        console.log('=== WebSocket 收到文档消息 ===')
        console.log('原始消息:', message)
        console.log('body:', message.body)
        const data = JSON.parse(message.body)

        // 根据消息类型处理
        if (data.type === 'chunk_complete') {
          // 文档分块完成通知
          ElNotification({
            title: '📄 文档处理完成',
            message: `"${data.docTitle}" 已完成分块，共 ${data.chunkCount} 个片段`,
            type: 'success',
            position: 'top-right',
            duration: 5000
          })

          // 通知所有监听器
          documentListeners.forEach(callback => callback(data))
        }
      })
    },
    (error: any) => {
      // 连接错误回调
      console.error('WebSocket 连接失败:', error)
    }
  )
}

/**
 * 断开 WebSocket 连接
 *
 * 调用时机：应用关闭时（可选，用于清理资源）
 * 调用方式：disconnectWebSocket()
 */
export const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.disconnect()
    stompClient = null
  }
}
