// 模拟数据配置
// 后端开发完成后可以删除此文件

export const mockData: Record<string, any> = {
  // 知识库列表
  'GET|/api/knowledge/kb/list': {
    code: 200,
    msg: 'success',
    data: {
      records: [
        { id: 1, name: '员工手册', description: '公司员工手册及制度文档', docCount: 23, chunkCount: 456, status: 1, createTime: '2024-03-20 10:00:00' },
        { id: 2, name: '考勤制度', description: '员工考勤管理制度', docCount: 15, chunkCount: 234, status: 1, createTime: '2024-03-18 10:00:00' },
        { id: 3, name: '财务制度', description: '财务报销管理制度', docCount: 18, chunkCount: 345, status: 0, createTime: '2024-03-15 10:00:00' },
        { id: 4, name: '技术文档', description: '技术开发文档', docCount: 42, chunkCount: 890, status: 0, createTime: '2024-03-10 10:00:00' }
      ],
      total: 4,
      current: 1,
      size: 10,
      pages: 1
    }
  },

  // 知识库详情
  'GET|/api/knowledge/kb/1': {
    code: 200,
    msg: 'success',
    data: { id: 1, name: '员工手册', description: '公司员工手册及制度文档', docCount: 23, chunkCount: 456, status: 1, createTime: '2024-03-20 10:00:00' }
  },

  // 创建知识库
  'POST|/api/knowledge/kb/create': {
    code: 200,
    msg: '创建成功',
    data: { id: 5, name: '', description: '', docCount: 0, status: 1, createTime: '2024-03-24 21:00:00' }
  },

  // 修改知识库
  'PUT|/api/knowledge/kb': {
    code: 200,
    msg: '修改成功',
    data: null
  },

  // 删除知识库
  'DELETE|/api/knowledge/kb/1': {
    code: 200,
    msg: '删除成功',
    data: null
  },

  // 文档列表
  'GET|/api/knowledge/kb/1/doc/list': {
    code: 200,
    msg: 'success',
    data: {
      records: [
        { id: 1, title: '公司制度手册.pdf', fileName: '公司制度手册.pdf', fileType: 'pdf', fileSize: 2048000, parseStatus: 2, chunkCount: 45, tokenCount: 12000, createTime: '2024-03-20 10:00:00' },
        { id: 2, title: '员工行为规范.doc', fileName: '员工行为规范.doc', fileType: 'doc', fileSize: 1024000, parseStatus: 2, chunkCount: 32, tokenCount: 8000, createTime: '2024-03-19 10:00:00' },
        { id: 3, title: '薪酬体系表格.xlsx', fileName: '薪酬体系表格.xlsx', fileType: 'xlsx', fileSize: 512000, parseStatus: 2, chunkCount: 25, tokenCount: 6000, createTime: '2024-03-18 10:00:00' },
        { id: 4, title: '绩效考核方案.pptx', fileName: '绩效考核方案.pptx', fileType: 'pptx', fileSize: 3072000, parseStatus: 1, chunkCount: 0, tokenCount: 0, createTime: '2024-03-17 10:00:00' }
      ],
      total: 4,
      current: 1,
      size: 10,
      pages: 1
    }
  },

  // AI知识库列表
  'GET|/api/ai/kb/list': {
    code: 200,
    msg: 'success',
    data: [
      { id: 1, name: '员工手册', docCount: 23 },
      { id: 2, name: '考勤制度', docCount: 15 },
      { id: 3, name: '财务制度', docCount: 18 }
    ]
  },

  // 工单列表
  'GET|/api/office/ticket/list': {
    code: 200,
    msg: 'success',
    data: {
      records: [
        { id: 1, ticketNo: 'TK2024030001', title: '打印机故障', typeName: 'IT支持', priority: 1, status: 0, creatorName: '张三', createTime: '2024-03-20 10:30:00' },
        { id: 2, ticketNo: 'TK2024030002', title: '申请年假', typeName: '人事', priority: 2, status: 1, creatorName: '李四', createTime: '2024-03-20 09:15:00' },
        { id: 3, ticketNo: 'TK2024030003', title: '报销差旅费', typeName: '财务', priority: 2, status: 2, creatorName: '王五', createTime: '2024-03-19 16:00:00' },
        { id: 4, ticketNo: 'TK2024030004', title: '网络无法连接', typeName: 'IT支持', priority: 1, status: 0, creatorName: '赵六', createTime: '2024-03-19 14:30:00' },
        { id: 5, ticketNo: 'TK2024030005', title: '门禁卡补办', typeName: '行政', priority: 3, status: 2, creatorName: '钱七', createTime: '2024-03-18 11:00:00' }
      ],
      total: 128,
      current: 1,
      size: 10,
      pages: 13
    }
  },

  // 工单类型
  'GET|/api/office/ticket/type/list': {
    code: 200,
    msg: 'success',
    data: [
      { id: 1, name: 'IT支持', icon: 'Monitor' },
      { id: 2, name: '人事行政', icon: 'User' },
      { id: 3, name: '财务报销', icon: 'Money' },
      { id: 4, name: '其他', icon: 'More' }
    ]
  },

  // 工单详情
  'GET|/api/office/ticket/1': {
    code: 200,
    msg: 'success',
    data: {
      id: 1,
      ticketNo: 'TK2024030001',
      title: '打印机故障',
      typeName: 'IT支持',
      content: '三楼会议室的打印机无法使用，显示错误代码E005，请安排人员维修。',
      priority: 1,
      status: 1,
      creatorId: 2,
      creatorName: '张三',
      creatorDept: '研发部',
      handlerId: 3,
      handlerName: 'IT小李',
      processResult: '已安排技术人员前往维修',
      createTime: '2024-03-20 10:30:00',
      completeTime: '2024-03-20 11:00:00',
      flows: [
        { id: 1, action: '创建工单', operatorName: '张三', operatorType: 'creator', content: '提交工单', createTime: '2024-03-20 10:30:00' },
        { id: 2, action: '系统分配', operatorName: '系统', operatorType: 'system', content: 'AI智能分类 → IT支持 → 已分配IT部门', createTime: '2024-03-20 10:35:00' },
        { id: 3, action: '开始处理', operatorName: 'IT小李', operatorType: 'handler', content: '已安排技术人员前往维修', createTime: '2024-03-20 10:45:00' }
      ],
      replies: [
        { id: 1, userId: 2, userName: '张三', content: '请问什么时候可以修好？', createTime: '2024-03-20 10:40:00' },
        { id: 2, userId: 3, userName: 'IT小李', content: '收到，正在安排技术人员过去，预计11点前处理好。', createTime: '2024-03-20 10:45:00' }
      ]
    }
  },

  // 请假列表
  'GET|/api/office/leave/list': {
    code: 200,
    msg: 'success',
    data: {
      records: [
        { id: 1, leaveNo: 'LV2024030001', leaveType: '年假', startDate: '2024-03-25', endDate: '2024-03-27', days: 3, status: 1, approveTime: '2024-03-20 14:00:00' },
        { id: 2, leaveNo: 'LV2024030002', leaveType: '病假', startDate: '2024-02-15', endDate: '2024-02-15', days: 1, status: 1, approveTime: '2024-02-15 09:00:00' },
        { id: 3, leaveNo: 'LV2024030003', leaveType: '事假', startDate: '2024-01-20', endDate: '2024-01-21', days: 2, status: 1, approveTime: '2024-01-19 16:00:00' }
      ],
      total: 3,
      current: 1,
      size: 10,
      pages: 1
    }
  },

  // 剩余假期
  'GET|/api/office/leave/balance': {
    code: 200,
    msg: 'success',
    data: { '年假': 10, '病假': 5, '事假': 3 }
  },

  // 通知公告列表
  'GET|/api/office/notice/list': {
    code: 200,
    msg: 'success',
    data: {
      records: [
        { id: 1, title: '关于2024年清明节放假安排的通知', publisherName: '人事行政部', publishTime: '2024-03-20 09:00:00', viewCount: 256, isRead: false, isTop: true },
        { id: 2, title: '2024年度员工体检通知', publisherName: '人事行政部', publishTime: '2024-03-18 09:00:00', viewCount: 189, isRead: true, isTop: false },
        { id: 3, title: '系统升级维护通知', publisherName: '技术部', publishTime: '2024-03-15 09:00:00', viewCount: 342, isRead: true, isTop: false },
        { id: 4, title: '考勤系统使用培训通知', publisherName: '人事行政部', publishTime: '2024-03-10 09:00:00', viewCount: 156, isRead: true, isTop: false }
      ],
      total: 4,
      current: 1,
      size: 10,
      pages: 1
    }
  },

  // 通知公告详情
  'GET|/api/office/notice/1': {
    code: 200,
    msg: 'success',
    data: {
      id: 1,
      title: '关于2024年清明节放假安排的通知',
      content: '各位同事：\n\n根据国家法定节假日规定，现将2024年清明节放假安排通知如下：\n\n一、放假时间：4月4日至4月6日，共3天\n二、注意事项：\n1. 请各部门在放假前完成各项工作\n2. 值班安排请查看公司内部通知\n3. 如有紧急事宜，请联系值班人员\n\n祝大家清明节安康！',
      publisherName: '人事行政部',
      publishTime: '2024-03-20 09:00:00',
      viewCount: 256,
      isRead: true
    }
  },

  // 用户列表
  'GET|/api/system/user/list': {
    code: 200,
    msg: 'success',
    data: {
      records: [
        { id: 1, username: 'admin', nickname: '系统管理员', email: 'admin@company.com', phone: '13800138000', deptId: 1, deptName: '技术部', status: 1, createTime: '2024-01-01 10:00:00' },
        { id: 2, username: 'zhangsan', nickname: '张三', email: 'zhangsan@company.com', phone: '13800138001', deptId: 2, deptName: '研发部', status: 1, createTime: '2024-01-05 10:00:00' },
        { id: 3, username: 'lisi', nickname: '李四', email: 'lisi@company.com', phone: '13800138002', deptId: 3, deptName: '销售部', status: 1, createTime: '2024-01-10 10:00:00' },
        { id: 4, username: 'wangwu', nickname: '王五', email: 'wangwu@company.com', phone: '13800138003', deptId: 4, deptName: '市场部', status: 0, createTime: '2024-01-15 10:00:00' }
      ],
      total: 4,
      current: 1,
      size: 10,
      pages: 1
    }
  },

  // 角色列表
  'GET|/api/system/role/list': {
    code: 200,
    msg: 'success',
    data: [
      { id: 1, name: '超级管理员', code: 'SUPER_ADMIN', description: '拥有所有权限', status: 1, createTime: '2024-01-01 10:00:00' },
      { id: 2, name: '普通用户', code: 'USER', description: '普通用户权限', status: 1, createTime: '2024-01-01 10:00:00' },
      { id: 3, name: '部门管理员', code: 'DEPT_ADMIN', description: '部门管理员权限', status: 1, createTime: '2024-01-05 10:00:00' }
    ]
  },

  // 菜单列表
  'GET|/api/system/menu/list': {
    code: 200,
    msg: 'success',
    data: [
      {
        id: 1,
        name: '系统管理',
        path: '/system',
        component: 'Layout',
        menuType: 'C',
        icon: 'Setting',
        sort: 100,
        children: [
          { id: 11, name: '用户管理', path: '/system/user', component: 'system/user/index', menuType: 'M', icon: 'User', sort: 1 },
          { id: 12, name: '角色管理', path: '/system/role', component: 'system/role/index', menuType: 'M', icon: 'UserFilled', sort: 2 },
          { id: 13, name: '菜单管理', path: '/system/menu', component: 'system/menu/index', menuType: 'M', icon: 'Menu', sort: 3 }
        ]
      }
    ]
  },

  // 菜单路由
  'GET|/api/system/menu/routes': {
    code: 200,
    msg: 'success',
    data: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: 'dashboard/index',
        meta: { title: '首页', icon: 'House' }
      },
      {
        path: '/knowledge',
        name: 'Knowledge',
        component: 'knowledge/index',
        meta: { title: '知识库', icon: 'Collection' }
      },
      {
        path: '/ai-chat',
        name: 'AIChat',
        component: 'ai-chat/index',
        meta: { title: 'AI助手', icon: 'ChatDotRound' }
      },
      {
        path: '/ticket',
        name: 'Ticket',
        component: 'ticket/index',
        meta: { title: '工单管理', icon: 'Tickets' }
      },
      {
        path: '/leave',
        name: 'Leave',
        component: 'leave/index',
        meta: { title: '请假申请', icon: 'Calendar' }
      },
      {
        path: '/notice',
        name: 'Notice',
        component: 'notice/index',
        meta: { title: '通知公告', icon: 'Bell' }
      },
      {
        path: '/system',
        name: 'System',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          { path: '/system/user', name: 'SystemUser', component: 'system/user', meta: { title: '用户管理' } },
          { path: '/system/role', name: 'SystemRole', component: 'system/role', meta: { title: '角色管理' } }
        ]
      }
    ]
  },

  // 未读消息数
  'GET|/api/office/message/unreadCount': {
    code: 200,
    msg: 'success',
    data: 3
  }
}

// 检查是否是模拟请求
export function getMockResponse(url: string, method: string): any | null {
  const key = `${method.toUpperCase()}|${url}`
  return mockData[key] || null
}
