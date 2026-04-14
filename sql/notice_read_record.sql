-- ============================================================
-- 通知公告阅读记录表
-- 用于记录用户对公告的阅读状态，实现已读/未读功能
-- ============================================================

-- 用户公告阅读记录表
CREATE TABLE IF NOT EXISTS `of_notice_read` (
    `id`              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `user_id`         BIGINT          NOT NULL COMMENT '用户ID',
    `notice_id`       BIGINT          NOT NULL COMMENT '公告ID',
    `read_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    `create_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_notice` (`user_id`, `notice_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_notice_id (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户公告阅读记录表';

-- ============================================================
-- 初始化阅读记录数据
-- 说明：admin用户ID=1, zhangsan用户ID=2, lisi用户ID=3
-- 公告ID: 1=放假通知, 2=系统升级, 3=新员工欢迎, 4=办公区域调整, 5=体检通知
-- ============================================================

-- 管理员(admin)已阅读所有公告
INSERT INTO `of_notice_read` (`user_id`, `notice_id`, `read_time`) VALUES
(1, 1, '2026-03-25 09:30:00'),
(1, 2, '2026-03-22 11:00:00'),
(1, 3, '2026-03-21 14:30:00'),
(1, 4, '2026-03-24 15:00:00');

-- 张三(zhangsan)已阅读部分公告
INSERT INTO `of_notice_read` (`user_id`, `notice_id`, `read_time`) VALUES
(2, 1, '2026-03-25 10:00:00'),
(2, 2, '2026-03-22 14:00:00'),
(2, 3, '2026-03-21 15:30:00');

-- 李四(lisi)已阅读部分公告（未读办公区域调整）
INSERT INTO `of_notice_read` (`user_id`, `notice_id`, `read_time`) VALUES
(3, 1, '2026-03-25 08:45:00'),
(3, 2, '2026-03-22 16:00:00'),
(3, 4, '2026-03-24 12:00:00');

-- 王五(wangwu)只读了放假通知
INSERT INTO `of_notice_read` (`user_id`, `notice_id`, `read_time`) VALUES
(4, 1, '2026-03-25 11:20:00');

-- 孙七(sunqi)读了放假通知和系统升级公告
INSERT INTO `of_notice_read` (`user_id`, `notice_id`, `read_time`) VALUES
(6, 1, '2026-03-25 09:00:00'),
(6, 2, '2026-03-22 10:30:00');

-- 赵六(zhaoliu)作为发布者，已阅读自己发布的公告
INSERT INTO `of_notice_read` (`user_id`, `notice_id`, `read_time`) VALUES
(5, 3, '2026-03-21 14:00:00'),
(5, 5, '2026-03-24 16:10:00');

-- ============================================================
-- 查询各用户未读公告数量（供参考）
-- SELECT user_id,
--        (SELECT COUNT(*) FROM of_notice WHERE publish_status = 1) -
--        (SELECT COUNT(*) FROM of_notice_read r WHERE r.notice_id IN
--          (SELECT id FROM of_notice WHERE publish_status = 1) AND r.user_id = sys_user.id) as unread_count
-- FROM sys_user;
-- ============================================================
