# 接口文档 - 首页Dashboard

> 更新日期: 2026-04-21

---

## 1. 首页统计数据

**接口路径**: `GET /dashboard/stats`

**请求参数**: 无

**响应参数**:

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码 |
| msg | string | 消息 |
| data | object | 数据对象 |

**data 对象**:

| 字段 | 类型 | 说明 |
|------|------|------|
| docCount | int | 知识库文档总数 |
| pendingTicket | int | 待处理工单数 |
| pendingLeave | int | 待审批请假数 |
| chatCount | int | AI对话总次数 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "docCount": 128,
    "pendingTicket": 5,
    "pendingLeave": 3,
    "chatCount": 156
  }
}
```

**后端实现说明**:
- `docCount`: 统计 `kb_document` 表总记录数（或 `kb_knowledge_base` 的 doc_count 之和）
- `pendingTicket`: 统计 `tk_ticket` 表 status IN (0, 1) 的记录数
- `pendingLeave`: 统计 `of_leave` 表 status = 1 的记录数（待审批）
- `chatCount`: 统计 `chat_message` 表总记录数

---

## 2. AI对话统计（最近7天趋势）

**接口路径**: `GET /ai/chat/stats`

**请求参数**: 无

**响应参数**:

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码 |
| msg | string | 消息 |
| data | array | 每日统计数据 |

**data 数组元素**:

| 字段 | 类型 | 说明 |
|------|------|------|
| date | string | 日期 (yyyy-MM-dd) |
| count | int | 当日对话次数 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {"date": "2026-04-15", "count": 20},
    {"date": "2026-04-16", "count": 35},
    {"date": "2026-04-17", "count": 45},
    {"date": "2026-04-18", "count": 60},
    {"date": "2026-04-19", "count": 40},
    {"date": "2026-04-20", "count": 55},
    {"date": "2026-04-21", "count": 70}
  ]
}
```

**后端实现说明**:
- 统计 `chat_message` 表最近7天的消息数
- 按日期分组，日期格式 yyyy-MM-dd
- 如果某天没有数据，count 返回 0

---

## 3. 待处理工单列表

**接口路径**: `GET /office/ticket/pending`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认5 |

**响应参数**:

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码 |
| msg | string | 消息 |
| data.records | array | 工单列表 |
| data.total | int | 总数 |

**records 数组元素**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 工单ID |
| title | string | 工单标题 |
| typeName | string | 工单类型名称 |
| priority | int | 优先级 (1紧急/2普通/3低) |
| createTime | string | 创建时间 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {"id": 1, "title": "打印机故障", "typeName": "IT支持", "priority": 1, "createTime": "2026-04-21 10:30:00"},
      {"id": 2, "title": "网络无法连接", "typeName": "IT支持", "priority": 1, "createTime": "2026-04-21 09:45:00"}
    ],
    "total": 5
  }
}
```

**后端实现说明**:
- 筛选条件: status IN (0, 1) 表示待处理状态
- 可按 create_time DESC 排序
- 限制返回最多5条

---

## 3.5 工单统计

**接口路径**: `GET /office/ticket/stats`

**请求参数**: 无

**响应参数**:

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码 |
| msg | string | 消息 |
| data | object | 统计数据 |

**data 对象**:

| 字段 | 类型 | 说明 |
|------|------|------|
| pending | int | 待处理数量 |
| processing | int | 处理中数量 |
| completed | int | 已解决数量 |
| closed | int | 已关闭数量 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "pending": 5,
    "processing": 3,
    "completed": 45,
    "closed": 12
  }
}
```

**后端实现说明**:
- pending: status = 0
- processing: status = 1
- completed: status = 2
- closed: status = 3

---

## 4. 通知公告列表（首页展示）

**接口路径**: `GET /office/notice/home`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| size | int | 否 | 返回条数，默认5 |

**响应参数**:

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码 |
| msg | string | 消息 |
| data | array | 公告列表 |

**data 数组元素**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 公告ID |
| title | string | 公告标题 |
| publisherName | string | 发布人 |
| publishTime | string | 发布时间 |
| isRead | boolean | 是否已读 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {"id": 1, "title": "关于2024年清明节放假安排的通知", "publisherName": "人事行政部", "publishTime": "2026-04-20", "isRead": false},
    {"id": 2, "title": "2024年度员工体检通知", "publisherName": "人事行政部", "publishTime": "2026-04-18", "isRead": true}
  ]
}
```

**后端实现说明**:
- 筛选条件: publish_status = 1（已发布）
- 按 publish_time DESC 排序
- 需要关联查询 of_notice_read 表确定 isRead 状态

---

## 5. 接口实现优先级

| 优先级 | 接口 | 说明 |
|--------|------|------|
| P0 | `GET /dashboard/stats` | 首页统计卡片数据 |
| P0 | `GET /office/ticket/pending` | 待处理工单列表 |
| P0 | `GET /office/notice/home` | 首页公告列表 |
| P1 | `GET /ai/chat/stats` | AI对话趋势（图表用） |

---

## 6. 前端对接说明

**dashboard/index.vue 需要修改的API调用**:

```typescript
// 新增API
import { getDashboardStats, getChatStats, getPendingTicketList, getHomeNotices } from '@/api/dashboard'

// 替换假数据
const stats = ref({
  docCount: 0,
  pendingTicket: 0,
  pendingLeave: 0,
  chatCount: 0
})

const chartData = ref<number[]>([])

// onMounted 中调用
onMounted(async () => {
  // 获取统计数据
  const statsRes = await getDashboardStats()
  stats.value = statsRes.data

  // 获取图表数据
  const chartRes = await getChatStats()
  chartData.value = chartRes.data.map((item: any) => item.count)

  // 获取待处理工单
  const ticketRes = await getPendingTicketList({ size: 5 })
  pendingTickets.value = ticketRes.data.records

  // 获取公告列表
  const noticeRes = await getHomeNotices({ size: 5 })
  notices.value = noticeRes.data
})
```

**新增文件**: `SmartOffice-vue/src/api/dashboard.ts`

```typescript
import request from '@/utils/request'

// 首页统计数据
export const getDashboardStats = () => {
  return request.get('/dashboard/stats')
}

// AI对话统计（最近7天）
export const getChatStats = () => {
  return request.get('/ai/chat/stats')
}

// 待处理工单列表
export const getPendingTicketList = (params?: any) => {
  return request.get('/office/ticket/pending', { params })
}

// 首页公告列表
export const getHomeNotices = (params?: any) => {
  return request.get('/office/notice/home', { params })
}
```