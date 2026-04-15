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
 * 4. 收到消息后，解析并通过 Element Plus Notification 弹窗展示
 */

import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { ElNotification } from 'element-plus'

// STOMP 客户端实例，用于维护 WebSocket 连接
let stompClient: any = null

/**
 * 建立 WebSocket 连接
 *
 * 调用时机：应用启动时（在 main.ts 中调用）
 * 连接地址：通过 Vite 代理 /ws → http://localhost:8084/ws
 *
 * 连接成功后会：
 * 1. 订阅 /topic/notice 公告通知主题
 * 2. 当后端推送新公告时，自动弹窗显示
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
    {},  // 连接头，这里为空
    () => {
      // 5. 连接成功后，订阅公告通知主题
      //    /topic/ 是 STOMP 的广播地址前缀
      //    /topic/notice 是我们自定义的公告通知主题
      stompClient.subscribe('/topic/notice', (message: any) => {
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
