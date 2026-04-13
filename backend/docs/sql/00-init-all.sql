-- ============================================================================
-- EvolveHub MVP v1.0 - 完整数据库初始化脚本
-- ============================================================================
-- 功能：创建 RBAC 系统所需的所有表并初始化基础数据
-- 执行时机：数据库创建后首次部署时执行（一键初始化）
-- 作者：EvolveHub Team
-- 日期：2026-04-11
-- ============================================================================

-- ============================================================================
-- 第一部分：表结构创建
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 一、用户表 (eh_user)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar TEXT,
    dept_id BIGINT,
    status INTEGER DEFAULT 1,
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_user IS '用户表';
COMMENT ON COLUMN eh_user.username IS '用户名（登录账号）';
COMMENT ON COLUMN eh_user.password IS '密码（BCrypt 加密）';
COMMENT ON COLUMN eh_user.nickname IS '昵称';
COMMENT ON COLUMN eh_user.dept_id IS '所属部门 ID';
COMMENT ON COLUMN eh_user.status IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN eh_user.deleted IS '逻辑删除（0-未删除 1-已删除）';

CREATE INDEX idx_user_username ON eh_user(username);
CREATE INDEX idx_user_dept_id ON eh_user(dept_id);

-- ----------------------------------------------------------------------------
-- 二、角色表 (eh_role)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_role (
    id BIGINT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    data_scope INTEGER DEFAULT 4,
    sort INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    remark VARCHAR(255),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_role IS '角色表';
COMMENT ON COLUMN eh_role.role_name IS '角色名称';
COMMENT ON COLUMN eh_role.role_code IS '角色编码（唯一）';
COMMENT ON COLUMN eh_role.data_scope IS '数据权限：1-全部 2-部门及子部门 3-本部门 4-本人 5-自定义';
COMMENT ON COLUMN eh_role.status IS '状态（0-禁用 1-正常）';

-- ----------------------------------------------------------------------------
-- 三、权限/菜单表 (eh_permission)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_permission (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    perm_name VARCHAR(50) NOT NULL,
    perm_code VARCHAR(100) NOT NULL UNIQUE,
    perm_type VARCHAR(20) NOT NULL,
    path VARCHAR(255),
    icon VARCHAR(50),
    sort INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    -- 桌面图标专用字段
    gradient TEXT,
    default_width INTEGER DEFAULT 800,
    default_height INTEGER DEFAULT 600,
    min_width INTEGER DEFAULT 640,
    min_height INTEGER DEFAULT 400,
    dock_order INTEGER DEFAULT -1,
    is_desktop_icon INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_permission IS '权限/菜单表';
COMMENT ON COLUMN eh_permission.parent_id IS '父权限 ID（0 表示顶级）';
COMMENT ON COLUMN eh_permission.perm_name IS '权限/菜单名称';
COMMENT ON COLUMN eh_permission.perm_code IS '权限编码（唯一标识）';
COMMENT ON COLUMN eh_permission.perm_type IS '类型：MENU-菜单 BUTTON-按钮 API-接口';
COMMENT ON COLUMN eh_permission.path IS '前端路由路径';
COMMENT ON COLUMN eh_permission.status IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN eh_permission.gradient IS 'CSS 渐变色（如 linear-gradient）';
COMMENT ON COLUMN eh_permission.default_width IS '默认窗口宽度';
COMMENT ON COLUMN eh_permission.default_height IS '默认窗口高度';
COMMENT ON COLUMN eh_permission.min_width IS '最小窗口宽度';
COMMENT ON COLUMN eh_permission.min_height IS '最小窗口高度';
COMMENT ON COLUMN eh_permission.dock_order IS 'Dock 栏顺序，-1 不显示';
COMMENT ON COLUMN eh_permission.is_desktop_icon IS '桌面图标标记：0-否 1-是';

-- ----------------------------------------------------------------------------
-- 四、部门表 (eh_dept)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_dept (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    dept_name VARCHAR(50) NOT NULL,
    sort INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_dept IS '部门表（树形结构）';
COMMENT ON COLUMN eh_dept.parent_id IS '父部门 ID（0 表示顶级）';
COMMENT ON COLUMN eh_dept.dept_name IS '部门名称';
COMMENT ON COLUMN eh_dept.status IS '状态（0-禁用 1-正常）';

-- ----------------------------------------------------------------------------
-- 五、用户角色关联表 (eh_user_role)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_user_role (
    id BIGINT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    PRIMARY KEY (user_id, role_id)
);

COMMENT ON TABLE eh_user_role IS '用户角色关联表';

-- ----------------------------------------------------------------------------
-- 六、角色权限关联表 (eh_role_permission)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_role_permission (
    id BIGINT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    PRIMARY KEY (role_id, permission_id)
);

COMMENT ON TABLE eh_role_permission IS '角色权限关联表';

-- ----------------------------------------------------------------------------
-- 七、角色数据权限表 (eh_role_data_scope)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_role_data_scope (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE eh_role_data_scope IS '角色数据权限表（用于 data_scope=5 的自定义部门）';

CREATE INDEX idx_role_data_scope_role_id ON eh_role_data_scope(role_id);

-- ============================================================================
-- 第二部分：基础数据初始化
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 八、初始化角色数据
-- ----------------------------------------------------------------------------
-- 说明：创建 5 个预定义角色，涵盖系统的不同权限等级
-- ----------------------------------------------------------------------------

INSERT INTO eh_role (id, role_name, role_code, data_scope, sort, status, remark, create_time, update_time)
VALUES
    (1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '拥有系统所有权限，不受任何限制', NOW(), NOW()),
    (2, '高层领导', 'LEADER', 2, 2, 1, '公司高层领导，可查看跨部门信息', NOW(), NOW()),
    (3, '部门负责人', 'DEPT_HEAD', 3, 3, 1, '部门负责人，管理本部门数据', NOW(), NOW()),
    (4, '普通管理员', 'ADMIN', 3, 4, 1, '普通管理员，管理用户和部门', NOW(), NOW()),
    (5, '普通员工', 'USER', 4, 5, 1, '普通员工，只能访问个人相关功能', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 九、初始化桌面图标数据（parent_id = 0，扁平结构）
-- ----------------------------------------------------------------------------
-- 说明：桌面图标直接挂在根下（parent_id = 0），不构成菜单树
-- perm_type = MENU 表示这是一个桌面图标
-- icon 字段对应 lucide-vue-next 图标名称
-- ----------------------------------------------------------------------------

INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (9,  0, 'AI 对话',   'app:chat',      'MENU', '/app/chat',      'MessageSquare', 1,  1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #5E5CE6)', 900, 640, 700, 480, 0, 1),
    (10, 0, '知识库',    'app:knowledge', 'MENU', '/app/knowledge', 'BookOpen',      2,  1, NOW(), NOW(), 'linear-gradient(135deg, #30D158, #34C759)', 880, 600, 700, 460, 1, 1),
    (11, 0, '模型管理',  'app:model',     'MENU', '/app/model',     'Bot',           3,  1, NOW(), NOW(), 'linear-gradient(135deg, #BF5AF2, #9B59B6)', 800, 560, 640, 400, -1, 1),
    (12, 0, '用户管理',  'app:users',     'MENU', '/app/users',     'Users',         4,  1, NOW(), NOW(), 'linear-gradient(135deg, #FF9F0A, #FF6B00)', 900, 640, 700, 480, -1, 1),
    (13, 0, 'MCP 工具',  'app:mcp',       'MENU', '/app/mcp',       'Wrench',        5,  1, NOW(), NOW(), 'linear-gradient(135deg, #64D2FF, #5AC8FA)', 920, 600, 720, 460, 2, 1),
    (14, 0, '记忆管理',  'app:memory',    'MENU', '/app/memory',    'Zap',           6,  1, NOW(), NOW(), 'linear-gradient(135deg, #FFD60A, #FF9F0A)', 800, 560, 640, 400, -1, 1),
    (15, 0, '系统设置',  'app:settings',  'MENU', '/app/settings',  'Settings',      7,  1, NOW(), NOW(), 'linear-gradient(135deg, #8E8E93, #636366)', 780, 560, 640, 400, 3, 1),
    (16, 0, '数据大屏',  'app:dashboard', 'MENU', '/app/dashboard', 'Monitor',      8,  1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #30D158)', 1280, 800, 1024, 600, 4, 1),
    (17, 0, '宠物管理',  'app:pets',      'MENU', '/app/pets',      'Cat',           9,  1, NOW(), NOW(), 'linear-gradient(135deg, #FF6B9D, #BF5AF2)', 900, 640, 700, 480, -1, 1),
    (18, 0, '部门管理',  'app:dept',     'MENU', '/app/dept',     'Building',     10,  1, NOW(), NOW(), 'linear-gradient(135deg, #64D2FF, #5AC8FA)', 800, 600, 640, 400, -1, 1),
    (19, 0, '图标管理',  'app:desktopicon','MENU', '/app/desktopicon','Grid',         11,  1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #5E5CE6)', 900, 640, 700, 480, -1, 1),
    (26, 0, '角色管理',  'app:role',      'MENU', '/app/role',      'Shield',       12,  1, NOW(), NOW(), 'linear-gradient(135deg, #BF5AF2, #9B59B6)', 800, 600, 640, 400, -1, 1),
    (27, 0, '权限管理',  'app:permission','MENU', '/app/permission', 'Key',          13,  1, NOW(), NOW(), 'linear-gradient(135deg, #FFD60A, #FF9F0A)', 800, 600, 640, 400, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 九.2、初始化桌面图标管理接口权限（perm_type = BUTTON）
-- ----------------------------------------------------------------------------
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (20, 0, '桌面图标查询', 'desktop-icon:query',   'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (21, 0, '桌面图标列表', 'desktop-icon:list',     'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (22, 0, '桌面图标创建', 'desktop-icon:create',  'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (23, 0, '桌面图标更新', 'desktop-icon:update',  'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (24, 0, '桌面图标删除', 'desktop-icon:delete',  'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 十、创建序列（供 eh_role_permission.id 使用）
-- ----------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS seq_role_permission START 1;

-- ----------------------------------------------------------------------------
-- 十一、初始化角色桌面图标关联
-- ----------------------------------------------------------------------------
-- 说明：根据前端 appDefinitions.roles 配置各角色可见的桌面图标
-- SUPER_ADMIN: 全部图标
-- ADMIN:       chat, knowledge, memory, settings, dept
-- DEPT_HEAD:    chat, knowledge, memory, settings, dept
-- LEADER:       chat, knowledge, memory, settings, dashboard
-- USER:         chat, knowledge, memory, settings
-- ----------------------------------------------------------------------------

-- 10.1 超级管理员（SUPER_ADMIN）- 全部桌面图标
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 11, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 12, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 13, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 16, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 17, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 18, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 19, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 20, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 21, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 22, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 23, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 24, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 26, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 27, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 10.2 普通管理员（ADMIN）- 部分桌面图标
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 4, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 18, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 10.3 部门负责人（DEPT_HEAD）- 部分桌面图标
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 3, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 18, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 10.4 高层领导（LEADER）- 部分桌面图标
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 2, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 16, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 10.5 普通员工（USER）- 最小集桌面图标
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 5, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 15, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 十一、初始化默认超级管理员账号
-- ----------------------------------------------------------------------------
-- 说明：创建默认管理员账号，用于首次登录
-- 用户名：admin
-- 密码：admin123（BCrypt 加密后的哈希值）
-- ----------------------------------------------------------------------------

-- 生成 BCrypt 密码哈希（密码：admin123）
-- $2a$10$zv.OUJsmMI1kuebzfoQf2.j.9MNW5c0t1gmm3RW/EWk7d95nov0fu

INSERT INTO eh_user (id, username, password, nickname, email, dept_id, status, create_time, update_time)
VALUES
    (1, 'admin', '$2a$10$zv.OUJsmMI1kuebzfoQf2.j.9MNW5c0t1gmm3RW/EWk7d95nov0fu', '系统管理员', 'admin@evolvehub.com', 1, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 为默认管理员分配超级管理员角色
INSERT INTO eh_user_role (user_id, role_id, create_time, update_time)
VALUES (1, 1, NOW(), NOW())
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 十二、初始化默认部门
-- ----------------------------------------------------------------------------
-- 说明：创建默认的部门结构，供用户关联
-- ----------------------------------------------------------------------------

INSERT INTO eh_dept (id, parent_id, dept_name, sort, status, create_time, update_time)
VALUES
    (1, 0, '总公司', 1, 1, NOW(), NOW()),
    (2, 1, '技术部', 1, 1, NOW(), NOW()),
    (3, 1, '产品部', 2, 1, NOW(), NOW()),
    (4, 1, '市场部', 3, 1, NOW(), NOW()),
    (5, 1, '行政部', 4, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 使用说明
-- ============================================================================
--
-- 1. 默认账号信息：
--    用户名：admin
--    密码：admin123
--    角色：超级管理员
--    权限：全部功能
--
-- 2. 首次登录后建议：
--    - 修改默认管理员密码
--    - 创建部门和用户
--    - 根据需要调整角色权限
--
-- 3. 角色说明：
--    - SUPER_ADMIN（超级管理员）：拥有所有权限
--    - LEADER（高层领导）：可查看跨部门信息
--    - DEPT_HEAD（部门负责人）：管理本部门数据
--    - ADMIN（普通管理员）：管理用户和部门
--    - USER（普通员工）：只能访问个人相关功能
--
-- 4. 桌面图标说明（eh_permission id 9-18，parent_id = 0）：
--    - AI 对话(app:chat)、知识库(app:knowledge)、模型管理(app:model)
--    - 用户管理(app:users)、MCP 工具(app:mcp)、记忆管理(app:memory)
--    - 系统设置(app:settings)、数据大屏(app:dashboard)、宠物管理(app:pets)
--    - 部门管理(system:dept)
--    - SUPER_ADMIN：全部 10 个图标
--    - ADMIN/DEPT_HEAD：chat, knowledge, memory, settings, dept
--    - LEADER：chat, knowledge, memory, settings, dashboard
--    - USER：chat, knowledge, memory, settings
--
-- 5. 数据权限（data_scope）说明：
--    - 1：全部数据（可看所有部门数据）
--    - 2：本部门及子部门数据
--    - 3：仅本部门数据
--    - 4：仅本人数据
--    - 5：自定义部门数据
--
-- ============================================================================
