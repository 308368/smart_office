# 项目记忆 - SmartOffice 企业智能办公助手

## 项目信息

**项目名称**: SmartOffice (企业智能办公助手)
**项目类型**: Spring Cloud 微服务 + Vue3 前端
**当前状态**: 后端认证服务开发中

---

## 用户技术栈 (已掌握)

| 分类 | 技术 |
|------|------|
| Java | Spring Boot 3.2.5、Spring Cloud 2023.0.0、Spring Security |
| 微服务 | Nacos(注册/配置)、Feign、Sentinel、Seata |
| ORM | MyBatis / MyBatis-Plus 3.5.8 |
| 数据存储 | MySQL、Redis、Elasticsearch |
| 消息队列 | RabbitMQ |
| 任务调度 | XXL-JOB |
| 文件存储 | MinIO / 阿里云OSS |
| 实时通信 | WebSocket |
| AI框架 | Spring AI、LangChain4j |
| 前端 | Vue3 + Element Plus + Vite |
| DevOps | Docker、Git |
| JWT | jjwt 0.12.3 |

---

## 项目功能模块

1. **智能问答** - 基于RAG架构的AI对话，支持多轮对话、答案引用
2. **知识库管理** - 文档上传、解析、向量化存储
3. **工单管理** - 工单提交、审批、流转、AI自动分类
4. **办公辅助** - 请假申请、报销审批、通知公告
5. **系统管理** - 用户、角色、菜单权限(RBAC)

---

## 当前进度

### ✅ 已完成

1. **技术方案设计**
   - 企业智能办公助手开发文档
   - 阶段一开发指南
   - 前端页面设计规格 (11个页面)
   - 完整接口文档

2. **前端项目**
   - Vue3 + Element Plus + Vite 项目
   - 清新绿主题 (#10B981)
   - 13个页面组件
   - 模拟登录功能 (admin/123456)

3. **后端 - 阶段一**
   - Maven父项目 (SmartOffice/pom.xml)
   - 公共模块 (smart-office-common)
   - 网关服务 (smart-office-gateway: 8082)
   - 认证服务 (auth-service: 8081)

4. **认证功能**
   - Spring Security 认证流程
   - 自定义 CustomUserDetailsService
   - JWT Token 生成 (JwtUtil)
   - 登录接口 /ucenter/login (支持JSON)
   - 登录结果枚举 LoginResultEnum

### ⏳ 待开发

- 网关路由配置
- 网关JWT验证过滤器
- 用户服务接口完善

---

## 文件结构

```
biyesheji/
├── docs/                           # 设计文档
│   ├── 企业智能办公助手开发文档.md
│   ├── 阶段一：基础框架与用户权限开发指南.md
│   ├── 前端页面设计规格_简约清新绿.md
│   └── 接口文档.md
│
├── SmartOffice/                    # 后端项目
│   ├── pom.xml                    # Maven父项目
│   ├── smart-office-common/       # 公共模块
│   │   └── src/main/java/com/cqf/common/
│   │       ├── enums/
│   │       │   ├── ResultCode.java
│   │       │   └── LoginResultEnum.java
│   │       ├── result/
│   │       │   ├── Result.java
│   │       │   └── LoginResult.java
│   │       └── utils/
│   │           └── JwtUtil.java
│   │
│   ├── smart-office-gateway/      # 网关服务 (8082)
│   │   └── src/main/java/com/cqf/gateway/
│   │       ├── config/
│   │       │   ├── SecurityConfig.java
│   │       │   ├── TokenConfig.java
│   │       │   └── GatewayAuthFilter.java
│   │       └── SmartOfficeGatewayApplication.java
│   │
│   └── auth-service/              # 认证服务 (8081)
│       └── src/main/java/com/cqf/auth/
│           ├── config/
│           │   └── SecurityConfig.java
│           └── service/impl/
│               └── CustomUserDetailsService.java
│
├── sql/                            # 数据库脚本
│   └── init-databases.sql
│
├── SmartOffice-vue/                # 前端项目
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── router/index.ts
│       ├── styles/index.scss
│       ├── utils/
│       ├── stores/
│       ├── api/
│       └── views/
│
└── .claude/memory/                # 记忆文件
    ├── MEMORY.md
    └── 开发进度.md
```

---

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 网关 | 8080 | 统一入口 |
| 认证服务 | 8081 | 登录/注册 |
| Nacos | 192.168.220.100:8848 | 注册/配置中心 |

---

## JWT配置

- **签名密钥**: SmartOffice2026SecretKeyForJWT256Bit
- **有效期**: 24小时

---

## 数据库

- **Nacos namespace**: d43fb09d-9144-4b48-94a9-06c078581a8b
- **Nacos group**: SmartOffice-project
- **数据库密码**: 123456 (BCrypt加密)

---

## 下次继续

1. 完善网关JWT验证
2. 配置路由规则
3. 实现用户服务接口

---

*最后更新: 2026-03-26*
