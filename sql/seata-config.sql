-- ============================================
-- Seata 分布式事务配置
-- ============================================

-- Seata 服务端配置 (Nacos)
-- 命名空间: d43fb09d-9144-4b48-94a9-06c078581a8b
-- 组: SmartOffice-project
-- 服务地址: 192.168.220.100:8091

-- ============================================
-- undo_log 表 (每个需要分布式事务的数据库都需要创建)
-- ============================================

-- 注意：必须在每个参与分布式事务的数据库中创建undo_log表
-- 当前库: so-knowledge

CREATE TABLE IF NOT EXISTS `undo_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `branch_id` bigint NOT NULL COMMENT '分支事务ID',
  `xid` varchar(100) NOT NULL COMMENT '全局事务ID',
  `context` varchar(128) NOT NULL COMMENT '上下文',
  `rollback_info` longblob NOT NULL COMMENT '回滚信息',
  `log_status` int NOT NULL COMMENT '日志状态: 0-正常, 1-待处理',
  `log_created` datetime NOT NULL COMMENT '创建时间',
  `log_modified` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata回滚日志表';