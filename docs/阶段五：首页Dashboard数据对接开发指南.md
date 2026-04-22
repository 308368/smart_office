# 阶段五：首页Dashboard数据对接 - 开发指南（后端）

> 本文档仅包含后端接口开发部分，前端已在前一个阶段完成

---

## 阶段五总览

```
阶段五：首页Dashboard数据对接（后端）
│
├── 5.1 首页统计数据接口 (/dashboard/stats)
├── 5.2 AI对话趋势接口 (/ai/chat/stats)
├── 5.3 待处理工单接口 (/office/ticket/pending)
└── 5.4 首页公告接口 (/office/notice/home)
```

预计完成时间：**2天**

---

## 5.1 首页统计数据接口

**接口路径**: `GET /dashboard/stats`

**接口说明**: 返回首页4个统计卡片的数据

**实现位置**: office-service (新建 DashboardController)

**统计数据来源**:

| 字段 | 数据来源 | 说明 |
|------|----------|------|
| docCount | kb_document 表 | 知识库文档总数 |
| pendingTicket | tk_ticket 表 status IN (0,1) | 待处理工单数 |
| pendingLeave | of_leave 表 status = 1 | 待审批请假数 |
| chatCount | chat_message 表 | AI对话总次数 |

**注意**: docCount 在 so-knowledge 库，chatCount 在 so-chat 库，需要跨库查询

**接口实现**:

```java
// DashboardController.java
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private IDashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(dashboardService.getStats());
    }
}

// DashboardServiceImpl.java
@Override
public DashboardStatsVO getStats() {
    DashboardStatsVO vo = new DashboardStatsVO();

    // 知识库文档数 (调用 knowledge-service 或直接查)
    vo.setDocCount(kbDocumentMapper.selectCount(null));

    // 待处理工单数
    vo.setPendingTicket(tkTicketMapper.selectCount(
        new LambdaQueryWrapper<TkTicket>().in(TkTicket::getStatus, 0, 1)
    ));

    // 待审批请假数
    vo.setPendingLeave(ofLeaveMapper.selectCount(
        new LambdaQueryWrapper<OfLeave>().eq(OfLeave::getStatus, 1)
    ));

    // AI对话次数 (调用 ai-service 或直接查 so-chat 库)
    vo.setChatCount(chatMessageMapper.selectCount(null));

    return vo;
}
```

**跨库查询方案**:

方案一：直接连接多个数据库（office-service 添加 so-knowledge 和 so-chat 数据源）
方案二：通过 Feign 调用其他服务聚合数据（推荐）

```java
// 方案二示例：调用 knowledge-service 和 ai-service
@Resource
private KnowledgeFeignClient knowledgeFeignClient;

@Resource
private ChatFeignClient chatFeignClient;
```

**VO定义**:

```java
@Data
public class DashboardStatsVO {
    private Integer docCount;
    private Integer pendingTicket;
    private Integer pendingLeave;
    private Integer chatCount;
}
```

---

## 5.2 AI对话趋势接口

**接口路径**: `GET /ai/chat/stats`

**接口说明**: 返回最近7天每天的AI对话次数

**实现位置**: ai-service (ChatController)

**SQL实现**:

```sql
-- 统计最近7天每天的消息数
SELECT DATE(create_time) as date, COUNT(*) as count
FROM chat_message
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(create_time)
ORDER BY date ASC
```

**接口实现**:

```java
// ChatController.java
@GetMapping("/stats")
public Result<List<ChatStatsVO>> getChatStats() {
    return Result.success(chatService.getStats());
}

// ChatServiceImpl.java
@Override
public List<ChatStatsVO> getStats() {
    List<ChatMessage> messages = chatMessageMapper.selectStats();
    // 转换为每日统计列表
    // 注意：如果某天没有数据，需要填充 count=0
}
```

**VO定义**:

```java
@Data
public class ChatStatsVO {
    private String date;  // yyyy-MM-dd
    private Integer count;
}
```

---

## 5.3 待处理工单接口

**接口路径**: `GET /office/ticket/pending`

**接口说明**: 返回待处理的工单列表（首页展示用，最多5条）

**实现位置**: office-service (TicketController)

**接口实现**:

```java
@GetMapping("/pending")
public Result<IPage<TkTicketVO>> getPendingTickets(
    @RequestParam(defaultValue = "1") Integer current,
    @RequestParam(defaultValue = "5") Integer size
) {
    IPage<TkTicket> page = ticketService.lambdaQuery()
        .in(TkTicket::getStatus, 0, 1)  // 待处理状态
        .orderByDesc(TkTicket::getCreateTime)
        .page(new Page<>(current, size));

    // 转换为 VO
    Page<TkTicketVO> voPage = new Page<>();
    BeanUtils.copyProperties(page, voPage);
    voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());

    return Result.success(voPage);
}
```

**响应字段**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 工单ID |
| title | string | 工单标题 |
| typeName | string | 工单类型名称 |
| priority | int | 优先级 (1紧急/2普通/3低) |
| createTime | string | 创建时间 |

---

## 5.4 工单统计接口

**接口路径**: `GET /office/ticket/stats`

**接口说明**: 返回工单各状态的统计数量（工单管理页标签页用）

**实现位置**: office-service (TicketController)

**接口实现**:

```java
@GetMapping("/stats")
public Result<Map<String, Integer>> getStats() {
    Map<String, Integer> stats = new HashMap<>();

    // 待处理: status = 0
    stats.put("pending", tkTicketMapper.selectCount(
        new LambdaQueryWrapper<TkTicket>().eq(TkTicket::getStatus, 0)
    ));

    // 处理中: status = 1
    stats.put("processing", tkTicketMapper.selectCount(
        new LambdaQueryWrapper<TkTicket>().eq(TkTicket::getStatus, 1)
    ));

    // 已解决: status = 2
    stats.put("completed", tkTicketMapper.selectCount(
        new LambdaQueryWrapper<TkTicket>().eq(TkTicket::getStatus, 2)
    ));

    // 已关闭: status = 3
    stats.put("closed", tkTicketMapper.selectCount(
        new LambdaQueryWrapper<TkTicket>().eq(TkTicket::getStatus, 3)
    ));

    return Result.success(stats);
}
```

---

## 5.5 首页公告接口

**接口路径**: `GET /office/notice/home`

**接口说明**: 返回首页展示的公告列表（最多5条）

**实现位置**: office-service (NoticeController)

**接口实现**:

```java
@GetMapping("/home")
public Result<List<NoticeHomeVO>> getHomeNotices(
    @RequestParam(defaultValue = "5") Integer size
) {
    // 查询已发布的公告
    List<OfNotice> notices = noticeService.lambdaQuery()
        .eq(OfNotice::getPublishStatus, 1)
        .orderByDesc(OfNotice::getPublishTime)
        .list();

    // 转换为 VO，判断已读状态
    Long userId = getCurrentUserId();
    List<NoticeHomeVO> voList = notices.stream().limit(size).map(notice -> {
        NoticeHomeVO vo = new NoticeHomeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setPublisherName(notice.getPublisherName());
        vo.setPublishTime(notice.getPublishTime().toString().substring(0, 10));

        // 查询是否已读
        boolean isRead = noticeReadRecordService.lambdaQuery()
            .eq(NoticeReadRecord::getUserId, userId)
            .eq(NoticeReadRecord::getNoticeId, notice.getId())
            .exists();
        vo.setRead(isRead);

        return vo;
    }).toList();

    return Result.success(voList);
}
```

**注意**:
- 需要判断当前用户的已读状态（查询 of_notice_read 表）
- isRead = true 表示已读，false 表示未读

---

## 接口清单汇总

| 接口路径 | 方法 | 说明 | 实现位置 |
|----------|------|------|----------|
| /dashboard/stats | GET | 首页统计 | office-service |
| /ai/chat/stats | GET | AI对话趋势 | ai-service |
| /office/ticket/pending | GET | 待处理工单 | office-service |
| /office/ticket/stats | GET | 工单统计 | office-service |
| /office/notice/home | GET | 首页公告 | office-service |

---

## 后端文件修改清单

| 文件 | 操作 | 说明 |
|------|------|------|
| office-service/.../controller/DashboardController.java | 新增 | 首页统计 |
| office-service/.../service/IDashboardService.java | 新增 | 服务接口 |
| office-service/.../service/impl/DashboardServiceImpl.java | 新增 | 服务实现 |
| office-service/.../model/vo/DashboardStatsVO.java | 新增 | VO类 |
| office-service/.../controller/TicketController.java | 修改 | 添加 pending、stats 接口 |
| office-service/.../controller/NoticeController.java | 修改 | 添加 home 接口 |
| ai-service/.../controller/ChatController.java | 修改 | 添加 stats 接口 |
| ai-service/.../service/IChatService.java | 修改 | 添加 getStats 方法 |
| ai-service/.../service/impl/ChatServiceImpl.java | 修改 | 实现 getStats |
| ai-service/.../model/vo/ChatStatsVO.java | 新增 | VO类 |

---

## 注意事项

### 跨库查询

Dashboard需要查询多个数据库：
- `so-office` - 工单、请假、公告
- `so-chat` - AI对话统计
- `so-knowledge` - 知识库文档数

### 已读状态判断

公告的 isRead 状态需要查询 of_notice_read 表，该表结构：

```sql
CREATE TABLE of_notice_read (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notice_id BIGINT NOT NULL,
    read_time DATETIME,
    UNIQUE KEY uk_user_notice (user_id, notice_id)
);
```