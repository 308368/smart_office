-- ============================================================
-- 企业智能办公助手 - 权限数据增量补充
-- 为 admin 角色补充缺失的权限标识
-- ============================================================

USE `so-system`;

-- 查看当前菜单权限
SELECT id, parent_id, name, menu_type, permission FROM sys_menu ORDER BY id;

-- 补充缺失的菜单权限（按钮类型 F）
-- 知识库权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(2, '创建知识库', '', '', 'F', NULL, 12, 1, 'knowledge:add'),
(2, '编辑知识库', '', '', 'F', NULL, 13, 1, 'knowledge:edit'),
(2, '删除知识库', '', '', 'F', NULL, 14, 1, 'knowledge:remove'),
(2, '上传文档', '', '', 'F', NULL, 15, 1, 'knowledge:upload'),
(2, '删除文档', '', '', 'F', NULL, 16, 1, 'knowledge:doc:remove')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- AI助手权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(3, '清空历史', '', '', 'F', NULL, 22, 1, 'ai:history:clear')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 工单中心权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(4, '处理工单', '', '', 'F', NULL, 33, 1, 'ticket:handle'),
(4, '审批工单', '', '', 'F', NULL, 34, 1, 'ticket:approve'),
(4, '转派工单', '', '', 'F', NULL, 35, 1, 'ticket:transfer'),
(4, '删除工单', '', '', 'F', NULL, 36, 1, 'ticket:remove')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 请假权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(5, '创建请假', '', '', 'F', NULL, 41, 1, 'leave:add'),
(5, '审批请假', '', '', 'F', NULL, 42, 1, 'leave:approve'),
(5, '取消请假', '', '', 'F', NULL, 43, 1, 'leave:cancel')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 报销权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(5, '创建报销', '', '', 'F', NULL, 45, 1, 'expense:add'),
(5, '审批报销', '', '', 'F', NULL, 46, 1, 'expense:approve')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 通知公告权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(5, '发布公告', '', '', 'F', NULL, 48, 1, 'notice:add'),
(5, '编辑公告', '', '', 'F', NULL, 49, 1, 'notice:edit'),
(5, '删除公告', '', '', 'F', NULL, 50, 1, 'notice:remove')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 用户管理权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(6, '新增用户', '', '', 'F', NULL, 101, 1, 'system:user:add'),
(6, '编辑用户', '', '', 'F', NULL, 102, 1, 'system:user:edit'),
(6, '删除用户', '', '', 'F', NULL, 103, 1, 'system:user:remove'),
(6, '重置密码', '', '', 'F', NULL, 104, 1, 'system:user:resetPwd')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 角色管理权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(6, '新增角色', '', '', 'F', NULL, 106, 1, 'system:role:add'),
(6, '编辑角色', '', '', 'F', NULL, 107, 1, 'system:role:edit'),
(6, '删除角色', '', '', 'F', NULL, 108, 1, 'system:role:remove'),
(6, '分配菜单', '', '', 'F', NULL, 109, 1, 'system:role:menu')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 菜单管理权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(6, '新增菜单', '', '', 'F', NULL, 111, 1, 'system:menu:add'),
(6, '编辑菜单', '', '', 'F', NULL, 112, 1, 'system:menu:edit'),
(6, '删除菜单', '', '', 'F', NULL, 113, 1, 'system:menu:remove')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 部门管理权限
INSERT INTO sys_menu (parent_id, name, path, component, menu_type, icon, sort, visible, permission) VALUES
(6, '新增部门', '', '', 'F', NULL, 115, 1, 'system:dept:add'),
(6, '编辑部门', '', '', 'F', NULL, 116, 1, 'system:dept:edit'),
(6, '删除部门', '', '', 'F', NULL, 117, 1, 'system:dept:remove')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 更新系统管理子菜单的权限标识（修正原来不规范的权限）
UPDATE sys_menu SET permission = 'system:user:list' WHERE parent_id = 6 AND name = '用户管理';
UPDATE sys_menu SET permission = 'system:role:list' WHERE parent_id = 6 AND name = '角色管理';
UPDATE sys_menu SET permission = 'system:menu:list' WHERE parent_id = 6 AND name = '菜单管理';
UPDATE sys_menu SET permission = 'system:dept:list' WHERE parent_id = 6 AND name = '部门管理';

-- 为 admin（role_id=1）补充新增按钮的权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE permission IN (
    'knowledge:add', 'knowledge:edit', 'knowledge:remove', 'knowledge:upload', 'knowledge:doc:remove',
    'ai:history:clear',
    'ticket:handle', 'ticket:approve', 'ticket:transfer', 'ticket:remove',
    'leave:add', 'leave:approve', 'leave:cancel',
    'expense:add', 'expense:approve',
    'notice:add', 'notice:edit', 'notice:remove',
    'system:user:add', 'system:user:edit', 'system:user:remove', 'system:user:resetPwd',
    'system:role:add', 'system:role:edit', 'system:role:remove', 'system:role:menu',
    'system:menu:add', 'system:menu:edit', 'system:menu:remove',
    'system:dept:add', 'system:dept:edit', 'system:dept:remove'
) AND id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- 验证：admin 角色现在拥有的权限
SELECT GROUP_CONCAT(m.permission ORDER BY m.parent_id SEPARATOR ', ') AS admin_permissions
FROM sys_menu m
INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
WHERE rm.role_id = 1 AND m.permission != '';

-- ============================================================
SELECT '权限数据补充完成!' AS result;
