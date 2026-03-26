-- ============================================================
-- 企业智能办公助手 - 数据库初始化脚本
-- 数据库命名规范: so-业务名称
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `so-system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `so-knowledge` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `so-ticket` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `so-office` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `so-chat` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `so-system`;

-- ============================================================
-- so-system: 系统模块（用户、角色、菜单、部门）
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `username`        VARCHAR(50)     NOT NULL UNIQUE COMMENT '用户名',
    `password`        VARCHAR(200)    NOT NULL COMMENT '密码',
    `nickname`        VARCHAR(50)     COMMENT '昵称',
    `email`           VARCHAR(100)    COMMENT '邮箱',
    `phone`           VARCHAR(20)     COMMENT '手机号',
    `avatar`          VARCHAR(500)    COMMENT '头像URL',
    `dept_id`         BIGINT          COMMENT '部门ID',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 0禁用 1正常',
    `login_ip`        VARCHAR(50)     COMMENT '最后登录IP',
    `login_date`      DATETIME        COMMENT '最后登录时间',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_username (`username`),
    INDEX idx_dept_id (`dept_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `name`            VARCHAR(50)     NOT NULL COMMENT '角色名称',
    `code`            VARCHAR(50)     NOT NULL UNIQUE COMMENT '角色编码',
    `description`    VARCHAR(200)    COMMENT '描述',
    `status`         TINYINT         DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_code (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `parent_id`      BIGINT          DEFAULT 0 COMMENT '父菜单ID',
    `name`            VARCHAR(50)     NOT NULL COMMENT '菜单名称',
    `path`            VARCHAR(200)    COMMENT '路由路径',
    `component`       VARCHAR(255)    COMMENT '组件路径',
    `menu_type`       CHAR(1)         COMMENT 'M菜单 C目录 F按钮',
    `icon`            VARCHAR(50)     COMMENT '图标',
    `sort`            INT             DEFAULT 0 COMMENT '排序',
    `visible`        TINYINT         DEFAULT 1 COMMENT '是否显示 0否 1是',
    `permission`      VARCHAR(100)    COMMENT '权限标识',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (`parent_id`),
    INDEX idx_menu_type (`menu_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 部门表
CREATE TABLE IF NOT EXISTS `sys_dept` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `parent_id`       BIGINT          DEFAULT 0 COMMENT '父部门ID',
    `name`            VARCHAR(50)     NOT NULL COMMENT '部门名称',
    `sort`            INT             DEFAULT 0 COMMENT '排序',
    `leader`          VARCHAR(50)     COMMENT '负责人',
    `phone`           VARCHAR(20)     COMMENT '联系电话',
    `email`           VARCHAR(100)    COMMENT '邮箱',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (`parent_id`),
    INDEX idx_name (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id`         BIGINT          NOT NULL COMMENT '用户ID',
    `role_id`         BIGINT          NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id`         BIGINT          NOT NULL COMMENT '角色ID',
    `menu_id`         BIGINT          NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS `sys_log` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id`         BIGINT          COMMENT '操作人ID',
    `username`        VARCHAR(50)     COMMENT '操作人用户名',
    `module`          VARCHAR(50)     COMMENT '操作模块',
    `operation`       VARCHAR(50)     COMMENT '操作类型',
    `method`          VARCHAR(200)    COMMENT '请求方法',
    `params`          TEXT COMMENT '请求参数',
    `ip`              VARCHAR(50)     COMMENT 'IP地址',
    `result`          TINYINT         COMMENT '操作结果 0失败 1成功',
    `error_msg`       VARCHAR(500)    COMMENT '错误信息',
    `cost_time`       BIGINT          COMMENT '耗时(毫秒)',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- ============================================================
-- 初始化系统数据
-- ============================================================

-- 初始化用户 (密码: 123456 已BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `phone`, `dept_id`, `status`, `login_ip`, `login_date`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@smartoffice.com', '13800138000', 1, 1, '127.0.0.1', NOW()),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三', 'zhangsan@smartoffice.com', '13800138001', 2, 1, '192.168.1.100', NOW()),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李四', 'lisi@smartoffice.com', '13800138002', 3, 1, '192.168.1.101', NOW()),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王五', 'wangwu@smartoffice.com', '13800138003', 2, 1, '192.168.1.102', NOW()),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '赵六', 'zhaoliu@smartoffice.com', '13800138004', 4, 1, NULL, NULL),
('sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '孙七', 'sunqi@smartoffice.com', '13800138005', 2, 1, NULL, NULL),
('zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '周八', 'zhouba@smartoffice.com', '13800138006', 3, 0, NULL, NULL);

-- 初始化角色
INSERT INTO `sys_role` (`name`, `code`, `description`, `status`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('部门经理', 'DEPT_MANAGER', '部门管理权限', 1),
('研发组长', 'DEV_LEADER', '研发组管理权限', 1),
('普通员工', 'USER', '普通用户权限', 1),
('测试人员', 'TESTER', '测试权限', 1);

-- 初始化部门
INSERT INTO `sys_dept` (`parent_id`, `name`, `sort`, `leader`, `phone`, `email`, `status`) VALUES
(0, '总公司', 0, 'admin', '400-888-8888', 'hq@smartoffice.com', 1),
(1, '技术部', 1, 'zhangsan', '400-888-8889', 'tech@smartoffice.com', 1),
(1, '市场部', 2, 'lisi', '400-888-8890', 'market@smartoffice.com', 1),
(1, '人力资源部', 3, 'zhaoliu', '400-888-8891', 'hr@smartoffice.com', 1),
(1, '财务部', 4, 'zhouba', '400-888-8892', 'finance@smartoffice.com', 1),
(2, '研发组', 5, 'wangwu', '400-888-8893', 'dev@smartoffice.com', 1),
(2, '测试组', 6, 'sunqi', '400-888-8894', 'qa@smartoffice.com', 1),
(2, '运维组', 7, NULL, '400-888-8895', 'ops@smartoffice.com', 1);

-- 初始化菜单
INSERT INTO `sys_menu` (`parent_id`, `name`, `path`, `component`, `menu_type`, `icon`, `sort`, `visible`, `permission`) VALUES
-- 一级菜单
(0, '工作台', '/dashboard', 'dashboard/index', 'C', 'HomeFilled', 0, 1, ''),
(0, '知识库', '/knowledge', 'knowledge/index', 'C', 'Reading', 10, 1, ''),
(0, 'AI助手', '/ai-chat', 'ai-chat/index', 'C', 'ChatDotRound', 20, 1, ''),
(0, '工单中心', '/ticket', 'ticket/index', 'C', 'Ticket', 30, 1, ''),
(0, '审批办公', '/office', 'office/index', 'C', 'Checked', 40, 1, ''),
(0, '系统管理', '/system', 'system/index', 'C', 'Setting', 100, 1, ''),

-- 工作台子菜单
(1, '工作台首页', '/dashboard', 'dashboard/index', 'M', 'HomeFilled', 0, 1, 'dashboard:view'),

-- 知识库子菜单
(2, '知识库列表', '/knowledge', 'knowledge/index', 'M', 'Reading', 10, 1, 'knowledge:list'),
(2, '知识库详情', '/knowledge/detail', 'knowledge/detail', 'M', NULL, 11, 0, 'knowledge:detail'),

-- AI助手子菜单
(3, 'AI对话', '/ai-chat', 'ai-chat/index', 'M', 'ChatDotRound', 20, 1, 'ai:chat'),
(3, '对话历史', '/ai-chat/history', 'ai-chat/history', 'M', 'Clock', 21, 1, 'ai:history'),

-- 工单中心子菜单
(4, '工单列表', '/ticket', 'ticket/index', 'M', 'Ticket', 30, 1, 'ticket:list'),
(4, '工单详情', '/ticket/detail', 'ticket/detail', 'M', NULL, 31, 0, 'ticket:detail'),
(4, '创建工单', '/ticket/create', 'ticket/create', 'M', 'Plus', 32, 1, 'ticket:create'),

-- 审批办公子菜单
(5, '请假申请', '/office/leave', 'leave/index', 'M', 'Calendar', 40, 1, 'leave:list'),
(5, '报销申请', '/office/expense', 'expense/index', 'M', 'Money', 41, 1, 'expense:list'),
(5, '通知公告', '/office/notice', 'notice/index', 'M', 'Bell', 42, 1, 'notice:list'),

-- 系统管理子菜单
(6, '用户管理', '/system/user', 'system/user', 'M', 'User', 100, 1, 'system:user'),
(6, '角色管理', '/system/role', 'system/role', 'M', 'UserFilled', 101, 1, 'system:role'),
(6, '菜单管理', '/system/menu', 'system/menu', 'M', 'Menu', 102, 1, 'system:menu'),
(6, '部门管理', '/system/dept', 'system/dept', 'M', 'Office', 103, 1, 'system:dept');

-- 用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin 超级管理员
(2, 2),  -- zhangsan 部门经理
(3, 2),  -- lisi 部门经理
(4, 3),  -- wangwu 研发组长
(5, 2),  -- zhaoliu 部门经理
(6, 4),  -- sunqi 普通员工
(7, 5);  -- zhouba 测试人员

-- 角色菜单关联 (超级管理员拥有所有菜单)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu`;

-- 部门经理角色菜单 (工作台、知识库、AI对话、工单、办公)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, id FROM `sys_menu` WHERE id IN (1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);

-- 研发组长菜单 (除系统管理外的所有菜单)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 3, id FROM `sys_menu` WHERE parent_id != 6;

-- 普通员工菜单 (工作台、知识库、AI对话)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 4, id FROM `sys_menu` WHERE id IN (1, 2, 3, 7, 8, 9, 10);

-- 测试人员菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 5, id FROM `sys_menu` WHERE id IN (1, 4, 12, 13);

-- 初始化系统日志
INSERT INTO `sys_log` (`user_id`, `username`, `module`, `operation`, `method`, `ip`, `result`, `cost_time`, `create_time`) VALUES
(1, 'admin', '用户', '登录', 'POST /ucenter/login', '127.0.0.1', 1, 156, NOW() - INTERVAL 1 DAY),
(1, 'admin', '用户', '查询用户列表', 'GET /ucenter/user/list', '127.0.0.1', 1, 89, NOW() - INTERVAL 12 HOUR),
(1, 'admin', '角色', '查询角色列表', 'GET /ucenter/role/list', '127.0.0.1', 1, 45, NOW() - INTERVAL 11 HOUR),
(2, 'zhangsan', '用户', '登录', 'POST /ucenter/login', '192.168.1.100', 1, 203, NOW() - INTERVAL 10 HOUR),
(2, 'zhangsan', '工单', '创建工单', 'POST /ticket/create', '192.168.1.100', 1, 320, NOW() - INTERVAL 8 HOUR),
(3, 'lisi', '用户', '登录', 'POST /ucenter/login', '192.168.1.101', 1, 178, NOW() - INTERVAL 6 HOUR),
(4, 'wangwu', '用户', '登录', 'POST /ucenter/login', '192.168.1.102', 1, 145, NOW() - INTERVAL 3 HOUR),
(1, 'admin', '菜单', '查询菜单列表', 'GET /ucenter/menu/list', '127.0.0.1', 1, 67, NOW() - INTERVAL 2 HOUR),
(1, 'admin', '部门', '查询部门列表', 'GET /ucenter/dept/list', '127.0.0.1', 1, 52, NOW() - INTERVAL 1 HOUR);

-- ============================================================
-- so-knowledge: 知识库模块
-- ============================================================

USE `so-knowledge`;

-- 知识库表
CREATE TABLE IF NOT EXISTS `kb_knowledge_base` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `name`            VARCHAR(100)    NOT NULL COMMENT '知识库名称',
    `description`     VARCHAR(500)    COMMENT '知识库描述',
    `cover_image`     VARCHAR(500)    COMMENT '封面图',
    `doc_count`       INT             DEFAULT 0 COMMENT '文档数量',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 0私密 1公开',
    `create_by`       BIGINT          COMMENT '创建人ID',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_name (`name`),
    INDEX idx_create_by (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 知识库文档表
CREATE TABLE IF NOT EXISTS `kb_document` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `kb_id`           BIGINT          NOT NULL COMMENT '知识库ID',
    `title`           VARCHAR(200)    NOT NULL COMMENT '文档标题',
    `content`         LONGTEXT        COMMENT '文档内容',
    `file_url`        VARCHAR(500)    COMMENT '文件URL',
    `file_type`       VARCHAR(50)     COMMENT '文件类型',
    `file_size`       BIGINT          COMMENT '文件大小(字节)',
    `chunk_count`     INT             DEFAULT 0 COMMENT '切片数量',
    `status`          TINYINT         DEFAULT 0 COMMENT '状态 0待处理 1处理中 2已完成 3处理失败',
    `create_by`       BIGINT          COMMENT '创建人ID',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_kb_id (`kb_id`),
    INDEX idx_status (`status`),
    INDEX idx_create_by (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 文档向量切片表
CREATE TABLE IF NOT EXISTS `kb_document_chunk` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `doc_id`          BIGINT          NOT NULL COMMENT '文档ID',
    `chunk_index`     INT             NOT NULL COMMENT '切片索引',
    `content`         TEXT            NOT NULL COMMENT '切片内容',
    `vector`          JSON COMMENT '向量数据',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_doc_id (`doc_id`),
    INDEX idx_chunk_index (`chunk_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档向量切片表';

-- 初始化知识库数据
INSERT INTO `kb_knowledge_base` (`name`, `description`, `cover_image`, `doc_count`, `status`, `create_by`, `create_time`) VALUES
('公司规章制度', '包含公司日常办公各项规章制度文档', 'https://picsum.photos/seed/kb1/400/200', 5, 1, 1, NOW() - INTERVAL 30 DAY),
('产品文档', '产品功能说明、使用手册、技术文档', 'https://picsum.photos/seed/kb2/400/200', 8, 1, 1, NOW() - INTERVAL 25 DAY),
('技术规范', '开发规范、编码规范、架构设计文档', 'https://picsum.photos/seed/kb3/400/200', 6, 1, 2, NOW() - INTERVAL 20 DAY),
('FAQ常见问题', '用户常见问题及解答汇总', 'https://picsum.photos/seed/kb4/400/200', 12, 1, 1, NOW() - INTERVAL 15 DAY),
('培训资料', '新员工培训、业务培训、技术培训资料', 'https://picsum.photos/seed/kb5/400/200', 4, 1, 2, NOW() - INTERVAL 10 DAY),
('内部私密文档', '仅内部员工可查看的机密文档', 'https://picsum.photos/seed/kb6/400/200', 3, 0, 1, NOW() - INTERVAL 5 DAY);

-- 初始化知识库文档
INSERT INTO `kb_document` (`kb_id`, `title`, `content`, `file_type`, `file_size`, `chunk_count`, `status`, `create_by`, `create_time`) VALUES
(1, '办公用品领用规定', '一、领用范围\n本公司员工可在行政部领取办公用品，包括笔、纸、文件夹等基本用品。\n二、领用流程\n1. 员工填写《办公用品领用表》\n2. 部门负责人签字确认\n3. 行政部审核发放\n三、注意事项\n1. 办公用品每人每月限领2次\n2. 特殊用品需提前申请\n3. 离职时需归还公司财产', 'txt', 2048, 3, 2, 1, NOW() - INTERVAL 28 DAY),
(1, '考勤管理制度', '一、工作时间\n周一至周五 9:00-18:00，午休12:00-13:00\n二、考勤方式\n采用钉钉打卡制度，需每天上下班打卡\n三、请假规则\n1. 请假需提前1天申请\n2. 紧急情况需电话通知部门负责人\n四、迟到早退\n每月迟到超过3次扣除当月绩效100元', 'txt', 1536, 2, 2, 1, NOW() - INTERVAL 27 DAY),
(1, '费用报销流程', '一、报销范围\n因公产生的差旅费、餐饮费、办公采购费等可报销\n二、报销流程\n1. 费用发生后5个工作日内提交\n2. 粘贴发票并填写报销单\n3. 部门负责人审批\n4. 财务部审核打款\n三、注意事项\n1. 发票必须为正规发票\n2. 报销金额需与发票一致', 'txt', 1792, 2, 2, 1, NOW() - INTERVAL 26 DAY),
(2, '产品功能介绍', '企业智能办公助手是一款专为企业打造的智能化办公平台。\n主要功能包括：\n1. 知识库管理：支持文档上传、智能问答\n2. AI助手：基于大语言模型的智能对话\n3. 工单系统：在线提交、处理工单\n4. 审批办公：请假、报销等审批流程\n5. 通知公告：企业内信息发布', 'txt', 1024, 2, 2, 1, NOW() - INTERVAL 22 DAY),
(2, '用户操作手册', '一、登录系统\n访问系统地址，输入用户名密码登录\n二、个人中心\n可查看个人信息、修改密码、设置头像\n三、我的工单\n查看提交的工单进度\n四、审批中心\n处理待审批的请假、报销申请', 'txt', 896, 1, 2, 1, NOW() - INTERVAL 21 DAY),
(3, 'Java开发规范', '一、命名规范\n1. 类名使用UpperCamelCase\n2. 方法名、变量名使用lowerCamelCase\n3. 常量使用UPPER_SNAKE_CASE\n二、代码格式\n1. 缩进使用4个空格\n2. 每行不超过120个字符\n3. 方法参数不超过5个\n三、注释规范\n1. 类和方法必须有JavaDoc注释\n2. 关键逻辑需添加行内注释', 'txt', 2560, 3, 2, 2, NOW() - INTERVAL 18 DAY),
(3, '前端代码规范', '一、命名规范\n1. 组件名使用kebab-case\n2. CSS类名使用BEM或kebab-case\n3. 变量使用camelCase\n二、Vue开发规范\n1. 使用Composition API\n2. 组件props需定义类型\n3. 使用Pinia进行状态管理', 'txt', 2048, 2, 2, 2, NOW() - INTERVAL 17 DAY),
(4, '如何修改密码', 'Q: 如何修改登录密码？\nA: 1. 登录系统后，点击右上角头像\n2. 选择"个人设置"\n3. 点击"修改密码"\n4. 输入原密码和新密码\n5. 点击保存即可', 'txt', 512, 1, 2, 1, NOW() - INTERVAL 12 DAY),
(4, '如何提交工单', 'Q: 怎么提交工单？\nA: 1. 点击"工单中心"菜单\n2. 点击"创建工单"按钮\n3. 填写工单标题、类型、描述\n4. 如有附件可上传\n5. 点击提交即可', 'txt', 480, 1, 2, 1, NOW() - INTERVAL 11 DAY),
(5, '新员工入职指南', '欢迎加入我们的团队！\n一、入职第一天\n1. 9:00到人事部报到\n2. 领取工牌和办公用品\n3. 开通OA账号和邮箱\n4. 参加入职培训\n二、第一周任务\n1. 了解公司规章制度\n2. 熟悉办公环境\n3. 认识部门同事\n4. 完成入职测试', 'txt', 1536, 2, 2, 2, NOW() - INTERVAL 8 DAY);

-- 初始化文档切片
INSERT INTO `kb_document_chunk` (`doc_id`, `chunk_index`, `content`) VALUES
(1, 0, '一、领用范围本公司员工可在行政部领取办公用品，包括笔、纸、文件夹等基本用品。'),
(1, 1, '二、领用流程1. 员工填写《办公用品领用表》2. 部门负责人签字确认3. 行政部审核发放'),
(1, 2, '三、注意事项1. 办公用品每人每月限领2次2. 特殊用品需提前申请3. 离职时需归还公司财产'),
(2, 0, '一、工作时间周一至周五 9:00-18:00，午休12:00-13:00二、考勤方式采用钉钉打卡制度'),
(2, 1, '三、请假规则1. 请假需提前1天申请2. 紧急情况需电话通知部门负责人四、迟到早退每月迟到超过3次扣除当月绩效100元'),
(3, 0, '一、报销范围因公产生的差旅费、餐饮费、办公采购费等可报销二、报销流程1. 费用发生后5个工作日内提交2. 粘贴发票并填写报销单'),
(4, 0, '企业智能办公助手是一款专为企业打造的智能化办公平台。主要功能包括：1. 知识库管理2. AI助手3. 工单系统4. 审批办公5. 通知公告'),
(5, 0, '一、登录系统访问系统地址，输入用户名密码登录二、个人中心可查看个人信息、修改密码'),
(6, 0, '一、命名规范1. 类名使用UpperCamelCase2. 方法名、变量名使用lowerCamelCase'),
(6, 1, '二、代码格式1. 缩进使用4个空格2. 每行不超过120个字符三、注释规范1. 类和方法必须有JavaDoc注释'),
(7, 0, '一、命名规范1. 组件名使用kebab-case2. CSS类名使用BEM三、Vue开发规范1. 使用Composition API2. 使用Pinia进行状态管理');

-- ============================================================
-- so-ticket: 工单模块
-- ============================================================

USE `so-ticket`;

-- 工单表
CREATE TABLE IF NOT EXISTS `tk_ticket` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `ticket_no`       VARCHAR(50)     NOT NULL UNIQUE COMMENT '工单编号',
    `title`           VARCHAR(200)    NOT NULL COMMENT '工单标题',
    `content`         TEXT            COMMENT '工单内容',
    `category`        VARCHAR(50)     COMMENT '工单分类',
    `priority`        TINYINT         DEFAULT 2 COMMENT '优先级 1紧急 2高 3中 4低',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 1待处理 2处理中 3已解决 4已关闭 5已驳回',
    `submitter_id`    BIGINT          NOT NULL COMMENT '提交人ID',
    `submitter_name`  VARCHAR(50)     COMMENT '提交人姓名',
    `handler_id`      BIGINT          COMMENT '处理人ID',
    `handler_name`    VARCHAR(50)     COMMENT '处理人姓名',
    `resolve_time`    DATETIME        COMMENT '解决时间',
    `close_time`      DATETIME        COMMENT '关闭时间',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_ticket_no (`ticket_no`),
    INDEX idx_status (`status`),
    INDEX idx_submitter_id (`submitter_id`),
    INDEX idx_handler_id (`handler_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 工单处理记录表
CREATE TABLE IF NOT EXISTS `tk_ticket_reply` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `ticket_id`       BIGINT          NOT NULL COMMENT '工单ID',
    `content`         TEXT            NOT NULL COMMENT '回复内容',
    `reply_type`      TINYINT         DEFAULT 1 COMMENT '回复类型 1用户回复 2客服回复 3系统回复',
    `user_id`         BIGINT          COMMENT '回复人ID',
    `user_name`       VARCHAR(50)     COMMENT '回复人姓名',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ticket_id (`ticket_id`),
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单处理记录表';

-- 工单附件表
CREATE TABLE IF NOT EXISTS `tk_ticket_attachment` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `ticket_id`       BIGINT          NOT NULL COMMENT '工单ID',
    `file_name`       VARCHAR(200)    NOT NULL COMMENT '文件名',
    `file_url`        VARCHAR(500)    NOT NULL COMMENT '文件URL',
    `file_size`       BIGINT          COMMENT '文件大小',
    `file_type`       VARCHAR(50)     COMMENT '文件类型',
    `upload_by`       BIGINT          COMMENT '上传人ID',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ticket_id (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单附件表';

-- 初始化工单数据
INSERT INTO `tk_ticket` (`ticket_no`, `title`, `content`, `category`, `priority`, `status`, `submitter_id`, `submitter_name`, `handler_id`, `handler_name`, `create_time`) VALUES
('TK20260325001', '电脑无法开机', '今天早上来到公司，发现电脑按下电源键完全没有反应，指示灯也不亮。尝试拔插电源线后依然无效。', '硬件故障', 1, 2, 4, '王五', 2, '张三', NOW() - INTERVAL 2 DAY),
('TK20260325002', '申请开通VPN账号', '由于需要在家办公，需要开通公司VPN账号以便远程访问内网资源。', '账号权限', 2, 3, 6, '孙七', 2, '张三', NOW() - INTERVAL 3 DAY),
('TK20260325003', '打印机无法连接', '办公区3号打印机显示脱机状态，无法打印文件。已尝试重启打印机无效。', '设备故障', 2, 1, 3, '李四', NULL, NULL, NOW() - INTERVAL 1 DAY),
('TK20260325004', '需要安装开发工具', '新入职员工需要安装 IntelliJ IDEA、Git、VS Code 等开发工具，请协助安装。', '软件安装', 3, 2, 6, '孙七', 4, '王五', NOW() - INTERVAL 4 DAY),
('TK20260325005', '会议室预约系统异常', '通过系统预约会议室成功后，到达会议室发现已被其他部门占用。', '系统Bug', 1, 4, 3, '李四', 2, '张三', NOW() - INTERVAL 5 DAY),
('TK20260325006', '申请增加服务器资源', '当前测试环境服务器资源不足，部署项目经常失败，需要增加内存和硬盘。', '资源申请', 2, 1, 4, '王五', NULL, NULL, NOW() - INTERVAL 6 HOUR),
('TK20260325007', '邮箱容量不足', '公司邮箱容量已满，无法接收新邮件，请帮忙扩容。', '账号权限', 2, 3, 3, '李四', 2, '张三', NOW() - INTERVAL 7 DAY);

-- 初始化工单回复
INSERT INTO `tk_ticket_reply` (`ticket_id`, `content`, `reply_type`, `user_id`, `user_name`, `create_time`) VALUES
(1, '已收到工单，正在安排技术人员处理。', 2, 2, '张三', NOW() - INTERVAL 2 DAY),
(1, '经检查发现电脑电源线损坏，已更换新电源线，电脑恢复正常。', 2, 2, '张三', NOW() - INTERVAL 1 DAY),
(2, 'VPN账号已开通，用户名为工号，初始密码已发送至手机短信。', 2, 2, '张三', NOW() - INTERVAL 2 DAY),
(3, '已联系IT人员前往查看，请稍等。', 2, 2, '张三', NOW() - INTERVAL 1 DAY),
(4, '开发工具已安装完成，请检查能否正常使用。', 2, 4, '王五', NOW() - INTERVAL 3 DAY),
(5, '非常抱歉给您带来不便，经核实是会议室管理系统缓存问题，已修复。', 2, 2, '张三', NOW() - INTERVAL 4 DAY),
(5, '问题已解决，感谢您的反馈。', 1, 3, '李四', NOW() - INTERVAL 4 DAY),
(7, '邮箱容量已扩容至50GB，请重新登录查看。', 2, 2, '张三', NOW() - INTERVAL 6 DAY);

-- ============================================================
-- so-office: 办公模块
-- ============================================================

USE `so-office`;

-- 请假申请表
CREATE TABLE IF NOT EXISTS `of_leave` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `leave_no`        VARCHAR(50)     NOT NULL UNIQUE COMMENT '请假编号',
    `user_id`         BIGINT          NOT NULL COMMENT '申请人ID',
    `user_name`       VARCHAR(50)     COMMENT '申请人姓名',
    `dept_id`         BIGINT          COMMENT '部门ID',
    `dept_name`       VARCHAR(50)     COMMENT '部门名称',
    `leave_type`      VARCHAR(20)     NOT NULL COMMENT '请假类型: 年假/病假/事假/婚假/产假/其他',
    `start_date`      DATE            NOT NULL COMMENT '开始日期',
    `end_date`        DATE            NOT NULL COMMENT '结束日期',
    `duration`        DECIMAL(5,1)    COMMENT '请假时长(天)',
    `reason`          VARCHAR(500)    COMMENT '请假原因',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 1待审批 2审批中 3已通过 4已拒绝 5已取消',
    `approver_id`     BIGINT          COMMENT '审批人ID',
    `approver_name`   VARCHAR(50)     COMMENT '审批人姓名',
    `approve_time`    DATETIME        COMMENT '审批时间',
    `approve_comment` VARCHAR(500)    COMMENT '审批意见',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_leave_no (`leave_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 报销申请表
CREATE TABLE IF NOT EXISTS `of_expense` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `expense_no`      VARCHAR(50)     NOT NULL UNIQUE COMMENT '报销编号',
    `user_id`         BIGINT          NOT NULL COMMENT '申请人ID',
    `user_name`       VARCHAR(50)     COMMENT '申请人姓名',
    `dept_id`         BIGINT          COMMENT '部门ID',
    `dept_name`       VARCHAR(50)     COMMENT '部门名称',
    `expense_type`    VARCHAR(20)     NOT NULL COMMENT '报销类型: 差旅/餐饮/办公/交通/其他',
    `total_amount`    DECIMAL(10,2)   NOT NULL COMMENT '报销总额',
    `description`     VARCHAR(500)    COMMENT '费用说明',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 1待审批 2审批中 3已通过 4已拒绝 5已取消',
    `approver_id`     BIGINT          COMMENT '审批人ID',
    `approver_name`   VARCHAR(50)     COMMENT '审批人姓名',
    `approve_time`    DATETIME        COMMENT '审批时间',
    `approve_comment` VARCHAR(500)    COMMENT '审批意见',
    `pay_time`        DATETIME        COMMENT '支付时间',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_expense_no (`expense_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销申请表';

-- 报销明细表
CREATE TABLE IF NOT EXISTS `of_expense_item` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `expense_id`      BIGINT          NOT NULL COMMENT '报销ID',
    `item_type`      VARCHAR(20)     NOT NULL COMMENT '费用类型',
    `amount`          DECIMAL(10,2)   NOT NULL COMMENT '金额',
    `description`     VARCHAR(200)    COMMENT '费用说明',
    `expense_date`    DATE            COMMENT '费用日期',
    `receipt_url`     VARCHAR(500)    COMMENT '发票/收据URL',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_expense_id (`expense_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销明细表';

-- 通知公告表
CREATE TABLE IF NOT EXISTS `of_notice` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `title`           VARCHAR(200)    NOT NULL COMMENT '公告标题',
    `content`         LONGTEXT        COMMENT '公告内容',
    `notice_type`     TINYINT         DEFAULT 1 COMMENT '公告类型 1通知 2公告',
    `priority`        TINYINT         DEFAULT 2 COMMENT '优先级 1高 2普通',
    `publish_status` TINYINT         DEFAULT 0 COMMENT '发布状态 0草稿 1已发布',
    `publish_time`    DATETIME        COMMENT '发布时间',
    `publisher_id`    BIGINT          COMMENT '发布人ID',
    `publisher_name` VARCHAR(50)     COMMENT '发布人姓名',
    `view_count`      INT             DEFAULT 0 COMMENT '阅读次数',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_publish_status (`publish_status`),
    INDEX idx_publish_time (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- 初始化请假数据
INSERT INTO `of_leave` (`leave_no`, `user_id`, `user_name`, `dept_id`, `dept_name`, `leave_type`, `start_date`, `end_date`, `duration`, `reason`, `status`, `approver_id`, `approver_name`, `approve_time`, `approve_comment`) VALUES
('LV20260325001', 4, '王五', 2, '技术部-研发组', '年假', '2026-03-20', '2026-03-22', 3.0, '准备清明节提前出行回家祭祖', 3, 2, '张三', '2026-03-18 10:00:00', '已确认，同意休假'),
('LV20260325002', 6, '孙七', 2, '技术部-研发组', '病假', '2026-03-24', '2026-03-24', 1.0, '感冒发烧，需要休息一天', 3, 2, '张三', '2026-03-24 08:30:00', '注意身体，好好休息'),
('LV20260325003', 3, '李四', 3, '市场部', '事假', '2026-03-28', '2026-03-28', 1.0, '家中有事需要处理', 2, 2, '张三', NULL, NULL),
('LV20260325004', 4, '王五', 2, '技术部-研发组', '婚假', '2026-04-01', '2026-04-10', 10.0, '结婚大喜，筹备婚礼', 1, NULL, NULL, NULL, NULL),
('LV20260325005', 6, '孙七', 2, '技术部-研发组', '年假', '2026-04-05', '2026-04-07', 3.0, '清明节小长假出行', 1, NULL, NULL, NULL, NULL);

-- 初始化报销数据
INSERT INTO `of_expense` (`expense_no`, `user_id`, `user_name`, `dept_id`, `dept_name`, `expense_type`, `total_amount`, `description`, `status`, `approver_id`, `approver_name`, `approve_time`, `approve_comment`) VALUES
('EX20260325001', 4, '王五', 2, '技术部-研发组', '差旅', 2680.00, '上周去北京出差客户拜访交通住宿费用', 3, 2, '张三', '2026-03-22 15:00:00', '费用合理，已通过'),
('EX20260325002', 3, '李四', 3, '市场部', '餐饮', 580.00, '接待客户商务晚宴', 3, 2, '张三', '2026-03-23 10:00:00', '已通过'),
('EX20260325003', 6, '孙七', 2, '技术部-研发组', '办公', 320.00, '购买办公鼠标键盘', 2, 2, '张三', NULL, NULL),
('EX20260325004', 4, '王五', 2, '技术部-研发组', '交通', 156.00, '日常上下班地铁交通费', 1, NULL, NULL, NULL, NULL);

-- 初始化报销明细
INSERT INTO `of_expense_item` (`expense_id`, `item_type`, `amount`, `description`, `expense_date`, `receipt_url`) VALUES
(1, '交通', 680.00, '北京往返高铁票', '2026-03-15', 'https://picsum.photos/seed/t1/200/200'),
(1, '住宿', 1500.00, '北京建国酒店2晚', '2026-03-16', 'https://picsum.photos/seed/t2/200/200'),
(1, '餐饮', 500.00, '客户接待晚餐', '2026-03-16', 'https://picsum.photos/seed/t3/200/200'),
(2, '餐饮', 580.00, '商务晚宴', '2026-03-20', 'https://picsum.photos/seed/t4/200/200'),
(3, '办公', 320.00, '罗技无线鼠标', '2026-03-23', 'https://picsum.photos/seed/t5/200/200'),
(4, '交通', 156.00, '3月地铁充值卡', '2026-03-23', 'https://picsum.photos/seed/t6/200/200');

-- 初始化通知公告
INSERT INTO `of_notice` (`title`, `content`, `notice_type`, `priority`, `publish_status`, `publish_time`, `publisher_id`, `publisher_name`, `view_count`, `create_time`) VALUES
('关于2026年清明节放假安排的通知', '各位同事：根据国务院办公厅关于2026年部分节假日安排的通知，现将我司清明节放假安排通知如下：\n一、放假时间：4月4日（周六）至4月6日（周一），共3天。\n二、调休上班：4月7日（周二）正常上班。\n三、注意事项：\n1. 请各部门在放假前完成手头工作\n2. 外出人员注意安全，保持通讯畅通\n3. 值班安排请查看内部系统', 1, 1, 1, '2026-03-20 09:00:00', 1, '系统管理员', 156, '2026-03-20 08:30:00'),
('系统升级维护公告', '为了提供更好的服务，系统将于2026年3月28日凌晨2:00-6:00进行升级维护。\n维护期间系统将暂停使用，请各位同事提前做好准备。\n如有紧急事务请联系IT部门。\n感谢您的理解与支持！', 2, 1, 1, '2026-03-22 10:00:00', 1, '系统管理员', 89, '2026-03-22 09:30:00'),
('新员工入职欢迎', '欢迎以下新同事加入我们的团队：\n- 研发部：张伟（前端开发工程师）\n- 市场部：李娜（市场专员）\n请各位同事相互认识，积极帮助新同事融入团队。', 1, 2, 1, '2026-03-21 14:00:00', 5, '赵六', 234, '2026-03-21 13:00:00'),
('办公区域调整通知', '由于公司业务发展需要，技术部将从3楼搬迁至5楼新办公区。\n搬迁时间：3月30日（周日）\n请各位同事配合搬迁工作，妥善保管个人物品。\n新办公区配备全新办公设施，期待大家入驻！', 2, 1, 1, '2026-03-24 11:00:00', 1, '系统管理员', 67, '2026-03-24 10:30:00'),
('关于开展2026年度体检的通知', '公司将于4月10日-4月20日开展年度员工体检，具体安排如下：\n一、体检医院：美年大健康体检中心\n二、体检项目：血常规、尿常规、心电图、B超等\n三、报名方式：请于3月31日前在OA系统报名\n四、注意事项：\n1. 体检当天需空腹\n2. 请携带身份证\n3. 如有特殊情况请联系人事部', 1, 2, 0, NULL, 5, '赵六', 0, '2026-03-24 16:00:00');

-- ============================================================
-- so-chat: AI对话模块
-- ============================================================

USE `so-chat`;

-- AI对话会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id`         BIGINT          NOT NULL COMMENT '用户ID',
    `title`           VARCHAR(200)    COMMENT '会话标题',
    `model`           VARCHAR(50)     DEFAULT 'qwen-plus' COMMENT '使用模型',
    `status`          TINYINT         DEFAULT 1 COMMENT '状态 0结束 1进行中',
    `message_count`   INT             DEFAULT 0 COMMENT '消息数量',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- AI对话消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `session_id`      BIGINT          NOT NULL COMMENT '会话ID',
    `role`            VARCHAR(20)     NOT NULL COMMENT '角色 user/assistant/system',
    `content`         LONGTEXT        NOT NULL COMMENT '消息内容',
    `model`           VARCHAR(50)     COMMENT '使用的模型',
    `tokens`          INT             COMMENT '消耗的token',
    `cost_time`       BIGINT          COMMENT '响应耗时(毫秒)',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (`session_id`),
    INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

-- 提示词模板表
CREATE TABLE IF NOT EXISTS `chat_prompt` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `name`            VARCHAR(100)    NOT NULL COMMENT '模板名称',
    `description`    VARCHAR(500)    COMMENT '模板描述',
    `prompt`          TEXT            NOT NULL COMMENT '提示词内容',
    `category`        VARCHAR(50)     COMMENT '分类',
    `is_public`       TINYINT         DEFAULT 0 COMMENT '是否公共模板 0私有 1公共',
    `create_by`       BIGINT          COMMENT '创建人ID',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_create_by (`create_by`),
    INDEX idx_is_public (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板表';

-- 初始化AI对话数据
INSERT INTO `chat_session` (`user_id`, `title`, `model`, `status`, `message_count`, `create_time`) VALUES
(1, 'Java代码优化咨询', 'qwen-plus', 0, 6, NOW() - INTERVAL 3 DAY),
(1, '如何提升团队协作效率', 'qwen-plus', 0, 4, NOW() - INTERVAL 2 DAY),
(1, '当前会话', 'qwen-plus', 1, 2, NOW() - INTERVAL 1 HOUR),
(2, 'Spring Cloud微服务问题', 'qwen-plus', 0, 8, NOW() - INTERVAL 1 DAY),
(4, '前端框架选型建议', 'qwen-plus', 0, 5, NOW() - INTERVAL 2 DAY),
(6, 'Python数据分析问题', 'qwen-turbo', 0, 3, NOW() - INTERVAL 4 DAY);

-- 初始化AI消息
INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`, `tokens`, `cost_time`, `create_time`) VALUES
-- Session 1: Java代码优化咨询
(1, 'user', '如何优化Java代码性能？', 'qwen-plus', 50, 1200, NOW() - INTERVAL 3 DAY),
(1, 'assistant', '优化Java代码性能可以从以下几个方面入手：\n\n1. **数据结构选择**：根据实际需求选择合适的集合类，如ArrayList适合随机访问，LinkedList适合频繁插入删除。\n\n2. **减少对象创建**：尽量重用对象，使用对象池技术，避免在循环中创建对象。\n\n3. **字符串操作**：使用StringBuilder代替字符串拼接，特别是在循环中。\n\n4. **数据库优化**：使用预编译语句、批量操作、连接池。\n\n5. **并发优化**：合理使用多线程，考虑使用线程池。\n\n6. **JVM调优**：根据应用特点调整堆大小、垃圾回收器等参数。\n\n需要我针对某个具体场景详细说明吗？', 'qwen-plus', 320, 1800, NOW() - INTERVAL 3 DAY + INTERVAL 1 MINUTE),
(1, 'user', '那关于数据库连接池有什么建议？', 'qwen-plus', 80, 0, NOW() - INTERVAL 3 DAY),
(1, 'assistant', '数据库连接池优化的建议：\n\n1. **合理设置连接池大小**：根据数据库服务器性能和并发量设置，通常建议CPU核心数的2-3倍。\n\n2. **最小连接数**：设置合理的最小连接数，避免频繁创建销毁连接的开销。\n\n3. **最大连接数**：根据业务峰值和数据库承载能力设置，避免连接过多导致数据库压力过大。\n\n4. **连接超时**：设置合理的获取连接超时时间。\n\n5. **常用连接池**：HikariCP（性能最优）、Druid（阿里开源，功能丰富）、C3P0（老牌）。\n\n推荐使用HikariCP，它是目前性能最好的连接池。', 'qwen-plus', 280, 1500, NOW() - INTERVAL 3 DAY + INTERVAL 2 MINUTE),
(1, 'user', '好的，明白了', 'qwen-plus', 30, 0, NOW() - INTERVAL 3 DAY + INTERVAL 3 MINUTE),
(1, 'assistant', '不客气！还有其他问题随时问我。', 'qwen-plus', 50, 800, NOW() - INTERVAL 3 DAY + INTERVAL 4 MINUTE),

-- Session 2: 如何提升团队协作效率
(2, 'user', '如何提升团队协作效率？', 'qwen-plus', 60, 0, NOW() - INTERVAL 2 DAY),
(2, 'assistant', '提升团队协作效率可以从以下几个方面着手：\n\n1. **明确目标和职责**：确保每个成员都清楚团队目标和自己的职责。\n\n2. **建立沟通机制**：定期召开站会、周会，使用即时通讯工具保持沟通。\n\n3. **使用协作工具**：如飞书、钉钉、企业微信，以及项目管理工具如Jira、Trello。\n\n4. **代码规范统一**：使用统一的编码规范和代码审查流程。\n\n5. **知识共享**：建立知识库，定期进行技术分享。\n\n6. **敏捷开发**：采用Scrum或Kanban等敏捷方法。\n\n需要我针对某个方面展开详细说明吗？', 'qwen-plus', 300, 2000, NOW() - INTERVAL 2 DAY + INTERVAL 1 MINUTE),
(2, 'user', '知识库怎么建设？', 'qwen-plus', 40, 0, NOW() - INTERVAL 2 DAY),
(2, 'assistant', '建设团队知识库的建议：\n\n1. **内容分类**：技术文档、流程规范、常见问题、项目文档等。\n\n2. **工具选择**：Confluence、语雀、Notion等。\n\n3. **维护机制**：指定专人负责，定期更新和审核。\n\n4. **激励机制**：鼓励成员贡献知识，可设置奖励。\n\n5. **搜索便捷**：确保知识易搜索，建立良好的目录结构。', 'qwen-plus', 220, 1600, NOW() - INTERVAL 2 DAY + INTERVAL 2 MINUTE),
(2, 'user', '收到，感谢', 'qwen-plus', 20, 0, NOW() - INTERVAL 2 DAY + INTERVAL 3 MINUTE),

-- Session 3: 当前会话
(3, 'user', '你好，请介绍一下你自己', 'qwen-plus', 50, 0, NOW() - INTERVAL 1 HOUR),
(3, 'assistant', '您好！我是企业智能办公助手的AI助手，基于通义千问大语言模型构建。\n\n我可以帮助您：\n- 解答技术问题\n- 协助代码开发\n- 提供业务咨询\n- 知识库问答\n- 文档撰写和润色\n\n有什么我可以帮助您的吗？', 'qwen-plus', 180, 1200, NOW() - INTERVAL 59 MINUTE),
(3, 'user', '帮我写一个快速排序算法', 'qwen-plus', 40, 0, NOW() - INTERVAL 30 MINUTE),
(3, 'assistant', '好的，这是Java实现的一个快速排序算法：\n\n```java\npublic class QuickSort {\n    public static void quickSort(int[] arr, int left, int right) {\n        if (left >= right) return;\n        \n        int pivot = partition(arr, left, right);\n        quickSort(arr, left, pivot - 1);\n        quickSort(arr, pivot + 1, right);\n    }\n    \n    private static int partition(int[] arr, int left, int right) {\n        int pivot = arr[left];\n        while (left < right) {\n            while (left < right && arr[right] >= pivot) right--;\n            arr[left] = arr[right];\n            while (left < right && arr[left] <= pivot) left++;\n            arr[right] = arr[left];\n        }\n        arr[left] = pivot;\n        return left;\n    }\n    \n    public static void main(String[] args) {\n        int[] arr = {64, 34, 25, 12, 22, 11, 90};\n        quickSort(arr, 0, arr.length - 1);\n        System.out.println(Arrays.toString(arr));\n    }\n}\n```\n\n时间复杂度：平均O(nlogn)，最差O(n²)\n空间复杂度：O(logn)\n\n需要我解释代码或提供其他语言版本吗？', 'qwen-plus', 380, 2000, NOW() - INTERVAL 29 MINUTE),

-- Session 4
(4, 'user', 'Spring Cloud微服务如何配置负载均衡？', 'qwen-plus', 70, 0, NOW() - INTERVAL 1 DAY),
(4, 'assistant', 'Spring Cloud中配置负载均衡主要有以下方式：\n\n1. **Ribbon**：传统的客户端负载均衡器\n2. **Spring Cloud LoadBalancer**：新一代负载均衡（推荐）\n3. **Nginx**：服务端负载均衡\n\n推荐使用Spring Cloud LoadBalancer，配置示例：\n\n```yaml\nspring:\n  cloud:\n    loadbalancer:\n      ribbon:\n        enabled: false  # 禁用Ribbon\n```\n\n服务调用时使用@LoadBalanced注解的RestTemplate或Feign。', 'qwen-plus', 280, 1800, NOW() - INTERVAL 1 DAY + INTERVAL 1 MINUTE),

-- Session 5
(5, 'user', '前端框架选哪个好？Vue还是React？', 'qwen-plus', 60, 0, NOW() - INTERVAL 2 DAY),
(5, 'assistant', 'Vue和React都是优秀的前端框架，选择建议：\n\n**Vue**\n- 上手简单，文档友好\n- 双向绑定直观\n- 适合中小型项目\n- 生态完善（Vuex、Vue Router）\n\n**React**\n- 灵活性强\n- 大型项目首选\n- 社区活跃\n- JSX语法强大\n\n**建议**：\n- 新人入门：Vue\n- 大型复杂项目：React\n- 快速开发：Vue\n- 追求灵活：React\n\n您的团队擅长哪个就选哪个！', 'qwen-plus', 320, 2200, NOW() - INTERVAL 2 DAY + INTERVAL 1 MINUTE),

-- Session 6
(6, 'user', 'Python怎么做数据分析？', 'qwen-turbo', 50, 0, NOW() - INTERVAL 4 DAY),
(6, 'assistant', 'Python数据分析常用库：\n\n1. **NumPy**：数值计算\n2. **Pandas**：数据处理分析\n3. **Matplotlib/Seaborn**：数据可视化\n4. **Scikit-learn**：机器学习\n\n基本流程：数据加载→清洗→分析→可视化→建模\n\n需要具体示例吗？', 'qwen-turbo', 200, 1500, NOW() - INTERVAL 4 DAY + INTERVAL 1 MINUTE);

-- 初始化提示词模板
INSERT INTO `chat_prompt` (`name`, `description`, `prompt`, `category`, `is_public`, `create_by`) VALUES
('代码审查助手', '帮助审查代码并提供优化建议', '你是一位资深的代码审查专家。请审查以下代码，指出潜在问题并提供优化建议。\n\n代码语言：{language}\n代码内容：\n```{language}\n{code}\n```\n\n请从以下维度分析：\n1. 代码规范性\n2. 性能优化\n3. 安全隐患\n4. 可维护性', '编程开发', 1, 1),
('周报生成器', '根据工作内容自动生成周报', '请根据以下工作内容，生成一份简洁的工作周报。\n\n本周工作内容：\n{content}\n\n请按以下格式输出：\n1. 本周工作概述\n2. 完成的任务\n3. 遇到的问题及解决方案\n4. 下周计划', '办公效率', 1, 1),
('SQL优化助手', '分析SQL语句并提供优化建议', '请分析以下SQL语句的性能，并提供优化建议。\n\nSQL语句：\n{sql}\n\n请分析：\n1. 索引使用情况\n2. 查询效率\n3. 潜在性能问题\n4. 优化建议', '编程开发', 1, 1),
('会议纪要生成', '根据会议内容生成正式会议纪要', '请根据以下会议记录，生成一份正式的会议纪要。\n\n会议主题：{topic}\n会议时间：{time}\n参会人员：{attendees}\n会议内容：{content}\n\n请包含：\n1. 会议基本信息\n2. 讨论要点\n3. 决议事项\n4. 行动计划', '办公效率', 1, 1);

-- ============================================================
-- 完成
-- ============================================================

SELECT '数据库初始化完成!' AS result;
