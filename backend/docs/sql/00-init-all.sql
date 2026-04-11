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
    deleted INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_permission IS '权限/菜单表';
COMMENT ON COLUMN eh_permission.parent_id IS '父权限 ID（0 表示顶级）';
COMMENT ON COLUMN eh_permission.perm_name IS '权限/菜单名称';
COMMENT ON COLUMN eh_permission.perm_code IS '权限编码（唯一标识）';
COMMENT ON COLUMN eh_permission.perm_type IS '类型：MENU-菜单 BUTTON-按钮 API-接口';
COMMENT ON COLUMN eh_permission.path IS '前端路由路径';
COMMENT ON COLUMN eh_permission.status IS '状态（0-禁用 1-正常）';

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
-- 九、初始化菜单数据
-- ----------------------------------------------------------------------------
-- 说明：创建后台管理界面的菜单结构
-- 菜单类型：MENU-菜单项，BUTTON-按钮（Phase 2 实现）
-- ----------------------------------------------------------------------------

-- 9.1 一级菜单（目录）
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time)
VALUES
    (1, 0, '系统管理', 'system', 'MENU', '/system', 'Setting', 1, 1, NOW(), NOW()),
    (2, 0, '知识库管理', 'kb', 'MENU', '/kb', 'Files', 2, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 9.2 二级菜单（功能）
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time)
VALUES
    -- 系统管理下的子菜单
    (3, 1, '用户管理', 'system:user:list', 'MENU', '/system/users', 'User', 1, 1, NOW(), NOW()),
    (4, 1, '部门管理', 'system:dept:list', 'MENU', '/system/dept', 'Dept', 2, 1, NOW(), NOW()),
    (5, 1, '模型配置', 'system:model:list', 'MENU', '/system/models', 'Model', 3, 1, NOW(), NOW()),
    (6, 1, '系统配置', 'system:config', 'MENU', '/system/config', 'Config', 4, 1, NOW(), NOW()),

    -- 知识库管理下的子菜单
    (7, 2, '文档管理', 'kb:doc:list', 'MENU', '/kb/docs', 'Doc', 1, 1, NOW(), NOW()),
    (8, 2, '知识库列表', 'kb:list', 'MENU', '/kb/list', 'List', 2, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 十、初始化角色菜单关联
-- ----------------------------------------------------------------------------
-- 说明：为不同角色分配可访问的菜单
-- ----------------------------------------------------------------------------

-- 10.1 超级管理员（SUPER_ADMIN）- 拥有全部菜单
INSERT INTO eh_role_permission (role_id, permission_id, create_time, update_time)
VALUES
    -- 系统管理
    (1, 1, NOW(), NOW()), (1, 3, NOW(), NOW()), (1, 4, NOW(), NOW()), (1, 5, NOW(), NOW()), (1, 6, NOW(), NOW()),
    -- 知识库管理
    (1, 2, NOW(), NOW()), (1, 7, NOW(), NOW()), (1, 8, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 10.2 普通管理员（ADMIN）- 部分菜单（去掉模型配置和系统配置）
INSERT INTO eh_role_permission (role_id, permission_id, create_time, update_time)
VALUES
    -- 系统管理（只有用户管理和部门管理）
    (4, 1, NOW(), NOW()), (4, 3, NOW(), NOW()), (4, 4, NOW(), NOW()),
    -- 知识库管理（全部）
    (4, 2, NOW(), NOW()), (4, 7, NOW(), NOW()), (4, 8, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 十一、初始化默认超级管理员账号
-- ----------------------------------------------------------------------------
-- 说明：创建默认管理员账号，用于首次登录
-- 用户名：admin
-- 密码：admin123（BCrypt 加密后的哈希值）
-- ----------------------------------------------------------------------------

-- 生成 BCrypt 密码哈希（密码：admin123）
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi

INSERT INTO eh_user (id, username, password, nickname, email, dept_id, status, create_time, update_time)
VALUES
    (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@evolvehub.com', 1, 1, NOW(), NOW())
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
-- 4. 菜单说明：
--    - 系统管理：用户管理、部门管理、模型配置、系统配置
--    - 知识库管理：文档管理、知识库列表
--    - SUPER_ADMIN 可以看到全部菜单
--    - ADMIN 只能看到用户管理、部门管理和知识库管理菜单
--
-- 5. 数据权限（data_scope）说明：
--    - 1：全部数据（可看所有部门数据）
--    - 2：本部门及子部门数据
--    - 3：仅本部门数据
--    - 4：仅本人数据
--    - 5：自定义部门数据
--
-- ============================================================================
