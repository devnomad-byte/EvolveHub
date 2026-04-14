-- ============================================================================
-- EvolveHub v1.0 - 完整数据库初始化脚本（PostgreSQL）
-- ============================================================================
-- 功能：删除旧表 → 创建全部表 → 初始化基础数据
-- 包含：RBAC 权限体系 + 资源管理 + 模型提供商 + 模型配置种子数据
-- 执行时机：数据库重建或首次部署时执行（一键初始化）
-- 作者：EvolveHub Team
-- 日期：2026-04-14
-- ============================================================================

-- ############################################################################
-- 第零部分：清空旧表（按外键依赖倒序删除）
-- ############################################################################

DROP TABLE IF EXISTS eh_resource_grant  CASCADE;
DROP TABLE IF EXISTS eh_api_key         CASCADE;
DROP TABLE IF EXISTS eh_mcp_config      CASCADE;
DROP TABLE IF EXISTS eh_skill_config    CASCADE;
DROP TABLE IF EXISTS eh_model_config    CASCADE;
DROP TABLE IF EXISTS eh_model_provider  CASCADE;
DROP TABLE IF EXISTS eh_role_data_scope CASCADE;
DROP TABLE IF EXISTS eh_role_permission CASCADE;
DROP TABLE IF EXISTS eh_user_role       CASCADE;
DROP TABLE IF EXISTS eh_permission      CASCADE;
DROP TABLE IF EXISTS eh_dept            CASCADE;
DROP TABLE IF EXISTS eh_role            CASCADE;
DROP TABLE IF EXISTS eh_user            CASCADE;
DROP SEQUENCE IF EXISTS seq_role_permission;

-- ############################################################################
-- 第一部分：RBAC 权限体系表
-- ############################################################################

-- ----------------------------------------------------------------------------
-- 一、用户表 (eh_user)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_user (
    id          BIGINT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(50),
    email       VARCHAR(100),
    phone       VARCHAR(20),
    avatar      TEXT,
    dept_id     BIGINT,
    status      INTEGER DEFAULT 1,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_user          IS '用户表';
COMMENT ON COLUMN eh_user.username IS '用户名（登录账号）';
COMMENT ON COLUMN eh_user.password IS '密码（BCrypt 加密）';
COMMENT ON COLUMN eh_user.nickname IS '昵称';
COMMENT ON COLUMN eh_user.email    IS '邮箱';
COMMENT ON COLUMN eh_user.phone    IS '手机号';
COMMENT ON COLUMN eh_user.avatar   IS '头像 URL';
COMMENT ON COLUMN eh_user.dept_id  IS '所属部门 ID';
COMMENT ON COLUMN eh_user.status   IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN eh_user.deleted  IS '逻辑删除（0-未删除 1-已删除）';

CREATE INDEX idx_user_username ON eh_user(username);
CREATE INDEX idx_user_dept_id  ON eh_user(dept_id);

-- ----------------------------------------------------------------------------
-- 二、角色表 (eh_role)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_role (
    id          BIGINT PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL,
    role_code   VARCHAR(50) NOT NULL UNIQUE,
    data_scope  INTEGER DEFAULT 4,
    sort        INTEGER DEFAULT 0,
    status      INTEGER DEFAULT 1,
    remark      VARCHAR(255),
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_role            IS '角色表';
COMMENT ON COLUMN eh_role.role_name  IS '角色名称';
COMMENT ON COLUMN eh_role.role_code  IS '角色编码（唯一）';
COMMENT ON COLUMN eh_role.data_scope IS '数据权限：1-全部 2-部门及子部门 3-本部门 4-本人 5-自定义';
COMMENT ON COLUMN eh_role.sort       IS '排序号';
COMMENT ON COLUMN eh_role.status     IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN eh_role.remark     IS '备注';

-- ----------------------------------------------------------------------------
-- 三、权限/菜单表 (eh_permission)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_permission (
    id              BIGINT PRIMARY KEY,
    parent_id       BIGINT  DEFAULT 0,
    perm_name       VARCHAR(50)  NOT NULL,
    perm_code       VARCHAR(100) NOT NULL UNIQUE,
    perm_type       VARCHAR(20)  NOT NULL,
    path            VARCHAR(255),
    icon            VARCHAR(50),
    sort            INTEGER DEFAULT 0,
    status          INTEGER DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         INTEGER DEFAULT 0,
    -- 桌面图标专用字段
    gradient        TEXT,
    default_width   INTEGER DEFAULT 800,
    default_height  INTEGER DEFAULT 600,
    min_width       INTEGER DEFAULT 640,
    min_height      INTEGER DEFAULT 400,
    dock_order      INTEGER DEFAULT -1,
    is_desktop_icon INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_permission                 IS '权限/菜单表';
COMMENT ON COLUMN eh_permission.parent_id       IS '父权限 ID（0 表示顶级）';
COMMENT ON COLUMN eh_permission.perm_name       IS '权限/菜单名称';
COMMENT ON COLUMN eh_permission.perm_code       IS '权限编码（唯一标识）';
COMMENT ON COLUMN eh_permission.perm_type       IS '类型：MENU-菜单 BUTTON-按钮 API-接口';
COMMENT ON COLUMN eh_permission.path            IS '前端路由路径';
COMMENT ON COLUMN eh_permission.icon            IS '菜单图标（lucide-vue-next 图标名）';
COMMENT ON COLUMN eh_permission.status          IS '状态（0-禁用 1-正常）';
COMMENT ON COLUMN eh_permission.gradient        IS 'CSS 渐变色（如 linear-gradient）';
COMMENT ON COLUMN eh_permission.default_width   IS '默认窗口宽度';
COMMENT ON COLUMN eh_permission.default_height  IS '默认窗口高度';
COMMENT ON COLUMN eh_permission.min_width       IS '最小窗口宽度';
COMMENT ON COLUMN eh_permission.min_height      IS '最小窗口高度';
COMMENT ON COLUMN eh_permission.dock_order      IS 'Dock 栏顺序，-1 不显示';
COMMENT ON COLUMN eh_permission.is_desktop_icon IS '桌面图标标记：0-否 1-是';

-- ----------------------------------------------------------------------------
-- 四、部门表 (eh_dept)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_dept (
    id          BIGINT PRIMARY KEY,
    parent_id   BIGINT  DEFAULT 0,
    dept_name   VARCHAR(50) NOT NULL,
    sort        INTEGER DEFAULT 0,
    status      INTEGER DEFAULT 1,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_dept           IS '部门表（树形结构）';
COMMENT ON COLUMN eh_dept.parent_id IS '父部门 ID（0 表示顶级）';
COMMENT ON COLUMN eh_dept.dept_name IS '部门名称';
COMMENT ON COLUMN eh_dept.status    IS '状态（0-禁用 1-正常）';

-- ----------------------------------------------------------------------------
-- 五、用户角色关联表 (eh_user_role)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_user_role (
    id          BIGINT,
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0,
    PRIMARY KEY (user_id, role_id)
);

COMMENT ON TABLE eh_user_role IS '用户角色关联表';

-- ----------------------------------------------------------------------------
-- 六、角色权限关联表 (eh_role_permission)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_role_permission (
    id            BIGINT,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_by     BIGINT,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       INTEGER DEFAULT 0,
    PRIMARY KEY (role_id, permission_id)
);

COMMENT ON TABLE eh_role_permission IS '角色权限关联表';

-- ----------------------------------------------------------------------------
-- 七、角色数据权限表 (eh_role_data_scope)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_role_data_scope (
    id          BIGINT PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    dept_id     BIGINT NOT NULL,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_role_data_scope IS '角色数据权限表（用于 data_scope=5 的自定义部门）';

CREATE INDEX idx_role_data_scope_role_id ON eh_role_data_scope(role_id);

-- ############################################################################
-- 第二部分：资源管理表
-- ############################################################################

-- ----------------------------------------------------------------------------
-- 八、模型提供商表 (eh_model_provider)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_model_provider (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(50)  NOT NULL UNIQUE,
    logo_url         VARCHAR(500),
    default_base_url VARCHAR(500),
    sort             INTEGER DEFAULT 0,
    enabled          INTEGER DEFAULT 1,
    create_by        BIGINT,
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted          INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_model_provider               IS '模型提供商维度表';
COMMENT ON COLUMN eh_model_provider.name           IS '提供商名称（如 OpenAI / DeepSeek / Anthropic）';
COMMENT ON COLUMN eh_model_provider.logo_url       IS '厂商 Logo URL';
COMMENT ON COLUMN eh_model_provider.default_base_url IS '默认 Base URL';
COMMENT ON COLUMN eh_model_provider.sort           IS '排序';
COMMENT ON COLUMN eh_model_provider.enabled        IS '状态（1=启用 0=禁用）';

CREATE INDEX idx_model_provider_enabled ON eh_model_provider(enabled);

-- ----------------------------------------------------------------------------
-- 九、模型配置表 (eh_model_config)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_model_config (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    provider    VARCHAR(50),
    api_key     VARCHAR(255),
    base_url    VARCHAR(500),
    enabled     INTEGER DEFAULT 1,
    model_type  VARCHAR(20),
    scope       VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    owner_id    BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_model_config            IS '模型配置表';
COMMENT ON COLUMN eh_model_config.name       IS '模型名称';
COMMENT ON COLUMN eh_model_config.provider   IS '提供商（openai / deepseek / anthropic 等）';
COMMENT ON COLUMN eh_model_config.api_key    IS 'API 密钥';
COMMENT ON COLUMN eh_model_config.base_url   IS '模型 API 基址';
COMMENT ON COLUMN eh_model_config.enabled    IS '是否启用（1=启用 0=禁用）';
COMMENT ON COLUMN eh_model_config.model_type IS '模型类型（chat / embedding）';
COMMENT ON COLUMN eh_model_config.scope      IS '资源范围：SYSTEM-系统级 USER-用户级';
COMMENT ON COLUMN eh_model_config.owner_id   IS '资源所有者 ID，scope=SYSTEM 时为 NULL';

CREATE INDEX idx_model_config_scope    ON eh_model_config(scope);
CREATE INDEX idx_model_config_owner_id ON eh_model_config(owner_id);

-- ----------------------------------------------------------------------------
-- 十、技能配置表 (eh_skill_config)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_skill_config (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    skill_type  VARCHAR(20),
    config      JSONB,
    enabled     INTEGER DEFAULT 1,
    scope       VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    owner_id    BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_skill_config            IS '技能配置表';
COMMENT ON COLUMN eh_skill_config.name       IS '技能名称';
COMMENT ON COLUMN eh_skill_config.description IS '技能描述';
COMMENT ON COLUMN eh_skill_config.skill_type IS '技能类型';
COMMENT ON COLUMN eh_skill_config.config     IS '配置信息（JSON）';
COMMENT ON COLUMN eh_skill_config.enabled    IS '是否启用（1=启用 0=禁用）';
COMMENT ON COLUMN eh_skill_config.scope      IS '资源范围：SYSTEM-系统级 USER-用户级';
COMMENT ON COLUMN eh_skill_config.owner_id   IS '资源所有者 ID，scope=SYSTEM 时为 NULL';

CREATE INDEX idx_skill_config_scope ON eh_skill_config(scope);
CREATE INDEX idx_skill_config_owner ON eh_skill_config(owner_id);

-- ----------------------------------------------------------------------------
-- 十一、MCP 服务配置表 (eh_mcp_config)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_mcp_config (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    server_url  VARCHAR(500) NOT NULL,
    protocol    VARCHAR(20) DEFAULT 'stdio',
    config      JSONB,
    enabled     INTEGER DEFAULT 1,
    scope       VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    owner_id    BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_mcp_config            IS 'MCP 服务配置表';
COMMENT ON COLUMN eh_mcp_config.name       IS '服务名称';
COMMENT ON COLUMN eh_mcp_config.description IS '服务描述';
COMMENT ON COLUMN eh_mcp_config.server_url IS '服务地址';
COMMENT ON COLUMN eh_mcp_config.protocol   IS '传输协议（stdio / sse）';
COMMENT ON COLUMN eh_mcp_config.config     IS '配置信息（JSON）';
COMMENT ON COLUMN eh_mcp_config.enabled    IS '是否启用（1=启用 0=禁用）';
COMMENT ON COLUMN eh_mcp_config.scope      IS '资源范围：SYSTEM-系统级 USER-用户级';
COMMENT ON COLUMN eh_mcp_config.owner_id   IS '资源所有者 ID，scope=SYSTEM 时为 NULL';

CREATE INDEX idx_mcp_config_scope ON eh_mcp_config(scope);
CREATE INDEX idx_mcp_config_owner ON eh_mcp_config(owner_id);

-- ----------------------------------------------------------------------------
-- 十二、用户 API Key 表 (eh_api_key)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_api_key (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    api_key     VARCHAR(64) NOT NULL,
    status      INTEGER DEFAULT 1,
    expired_at  TIMESTAMP,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_api_key            IS '用户 API Key 表（一个用户一个 Key）';
COMMENT ON COLUMN eh_api_key.user_id    IS '绑定的用户 ID（一对一）';
COMMENT ON COLUMN eh_api_key.api_key    IS '密钥值，格式：sk-{32位随机hex}';
COMMENT ON COLUMN eh_api_key.status     IS '状态：1-正常 0-禁用';
COMMENT ON COLUMN eh_api_key.expired_at IS '过期时间，NULL 表示永不过期';

CREATE UNIQUE INDEX uk_api_key      ON eh_api_key(api_key)  WHERE deleted = 0;
CREATE UNIQUE INDEX uk_api_key_user ON eh_api_key(user_id)  WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 十三、资源授权表 (eh_resource_grant)
-- ----------------------------------------------------------------------------

CREATE TABLE eh_resource_grant (
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    resource_id   BIGINT NOT NULL,
    create_by     BIGINT,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_resource_grant               IS '资源授权表（管理员将系统级资源授权给用户）';
COMMENT ON COLUMN eh_resource_grant.user_id       IS '被授权用户 ID';
COMMENT ON COLUMN eh_resource_grant.resource_type IS '资源类型：MODEL-模型 SKILL-技能 MCP-MCP服务';
COMMENT ON COLUMN eh_resource_grant.resource_id   IS '资源 ID（指向具体资源表主键）';

CREATE INDEX  idx_resource_grant_user     ON eh_resource_grant(user_id, resource_type);
CREATE INDEX  idx_resource_grant_resource ON eh_resource_grant(resource_type, resource_id);
CREATE UNIQUE INDEX uk_resource_grant     ON eh_resource_grant(user_id, resource_type, resource_id) WHERE deleted = 0;

-- ############################################################################
-- 第三部分：序列
-- ############################################################################

CREATE SEQUENCE seq_role_permission START 1;

-- ############################################################################
-- 第四部分：基础数据初始化
-- ############################################################################

-- ----------------------------------------------------------------------------
-- 1. 初始化角色
-- ----------------------------------------------------------------------------

INSERT INTO eh_role (id, role_name, role_code, data_scope, sort, status, remark, create_time, update_time)
VALUES
    (1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '拥有系统所有权限，不受任何限制', NOW(), NOW()),
    (2, '高层领导',   'LEADER',      2, 2, 1, '公司高层领导，可查看跨部门信息', NOW(), NOW()),
    (3, '部门负责人', 'DEPT_HEAD',   3, 3, 1, '部门负责人，管理本部门数据',     NOW(), NOW()),
    (4, '普通管理员', 'ADMIN',       3, 4, 1, '普通管理员，管理用户和部门',     NOW(), NOW()),
    (5, '普通员工',   'USER',        4, 5, 1, '普通员工，只能访问个人相关功能', NOW(), NOW());

-- ----------------------------------------------------------------------------
-- 2. 初始化部门
-- ----------------------------------------------------------------------------

INSERT INTO eh_dept (id, parent_id, dept_name, sort, status, create_time, update_time)
VALUES
    (1, 0, '总公司', 1, 1, NOW(), NOW()),
    (2, 1, '技术部', 1, 1, NOW(), NOW()),
    (3, 1, '产品部', 2, 1, NOW(), NOW()),
    (4, 1, '市场部', 3, 1, NOW(), NOW()),
    (5, 1, '行政部', 4, 1, NOW(), NOW());

-- ----------------------------------------------------------------------------
-- 3. 初始化默认超级管理员账号
-- ----------------------------------------------------------------------------
-- 用户名：admin  密码：admin123（BCrypt 哈希）

INSERT INTO eh_user (id, username, password, nickname, email, dept_id, status, create_time, update_time)
VALUES
    (1, 'admin', '$2a$10$zv.OUJsmMI1kuebzfoQf2.j.9MNW5c0t1gmm3RW/EWk7d95nov0fu', '系统管理员', 'admin@evolvehub.com', 1, 1, NOW(), NOW());

INSERT INTO eh_user_role (user_id, role_id, create_time, update_time)
VALUES (1, 1, NOW(), NOW());

-- ----------------------------------------------------------------------------
-- 4. 初始化桌面图标（perm_type = MENU，parent_id = 0）
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
    (16, 0, '数据大屏',  'app:dashboard', 'MENU', '/app/dashboard', 'Monitor',       8,  1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #30D158)', 1280, 800, 1024, 600, 4, 1),
    (17, 0, '宠物管理',  'app:pets',      'MENU', '/app/pets',      'Cat',           9,  1, NOW(), NOW(), 'linear-gradient(135deg, #FF6B9D, #BF5AF2)', 900, 640, 700, 480, -1, 1),
    (18, 0, '部门管理',  'app:dept',      'MENU', '/app/dept',      'Building',     10,  1, NOW(), NOW(), 'linear-gradient(135deg, #64D2FF, #5AC8FA)', 800, 600, 640, 400, -1, 1),
    (19, 0, '图标管理',  'app:desktopicon','MENU', '/app/desktopicon','Grid',        11,  1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #5E5CE6)', 900, 640, 700, 480, -1, 1),
    (26, 0, '角色管理',  'app:role',      'MENU', '/app/role',      'Shield',       12,  1, NOW(), NOW(), 'linear-gradient(135deg, #BF5AF2, #9B59B6)', 800, 600, 640, 400, -1, 1),
    (27, 0, '权限管理',  'app:permission','MENU', '/app/permission', 'Key',          13,  1, NOW(), NOW(), 'linear-gradient(135deg, #FFD60A, #FF9F0A)', 800, 600, 640, 400, -1, 1);

-- ----------------------------------------------------------------------------
-- 5. 初始化接口权限（perm_type = BUTTON）
-- ----------------------------------------------------------------------------

INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (20, 0, '桌面图标查询', 'desktop-icon:query',  'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (21, 0, '桌面图标列表', 'desktop-icon:list',   'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (22, 0, '桌面图标创建', 'desktop-icon:create', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (23, 0, '桌面图标更新', 'desktop-icon:update', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (24, 0, '桌面图标删除', 'desktop-icon:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0);

-- ----------------------------------------------------------------------------
-- 6. 初始化角色-权限关联
-- ----------------------------------------------------------------------------
-- SUPER_ADMIN: 全部图标
-- ADMIN:       chat, knowledge, memory, settings, dept
-- DEPT_HEAD:   chat, knowledge, memory, settings, dept
-- LEADER:      chat, knowledge, memory, settings, dashboard
-- USER:        chat, knowledge, memory, settings

-- 6.1 超级管理员（SUPER_ADMIN）- 全部
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
    (nextval('seq_role_permission'), 1, 27, NOW(), NOW());

-- 6.2 普通管理员（ADMIN）
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 4, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 18, NOW(), NOW());

-- 6.3 部门负责人（DEPT_HEAD）
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 3, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 18, NOW(), NOW());

-- 6.4 高层领导（LEADER）
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 2, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 16, NOW(), NOW());

-- 6.5 普通员工（USER）
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 5, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 15, NOW(), NOW());

-- ----------------------------------------------------------------------------
-- 7. 初始化模型提供商
-- ----------------------------------------------------------------------------

INSERT INTO eh_model_provider (name, default_base_url, sort, enabled, create_time, update_time)
VALUES
    ('OpenAI',     'https://api.openai.com/v1',                          1,  1, NOW(), NOW()),
    ('Anthropic',  'https://api.anthropic.com/v1',                       2,  1, NOW(), NOW()),
    ('DeepSeek',   'https://api.deepseek.com/v1',                        3,  1, NOW(), NOW()),
    ('硅基流动',    'https://api.siliconflow.cn/v1',                      4,  1, NOW(), NOW()),
    ('阿里云',     'https://dashscope.aliyuncs.com/compatible-mode/v1',  5,  1, NOW(), NOW()),
    ('百度千帆',   'https://qwenlm.cn/api/v1',                           6,  1, NOW(), NOW()),
    ('腾讯混元',   'https://hunyuan.cloud.tencent.com/v1',               7,  1, NOW(), NOW()),
    ('智谱 GLM',   'https://open.bigmodel.cn/api/paas/v4',                8,  1, NOW(), NOW()),
    ('Groq',       'https://api.groq.com/openai/v1',                     9,  1, NOW(), NOW()),
    ('Ollama',     'http://localhost:11434/v1',                           10, 1, NOW(), NOW()),
    ('LM Studio',  'http://localhost:1234/v1',                           11, 1, NOW(), NOW()),
    ('LocalAI',    'http://localhost:8080/v1',                           12, 1, NOW(), NOW()),
    ('Azure OpenAI','https://{your-resource-name}.openai.azure.com/v1',  13, 1, NOW(), NOW()),
    ('Mistral',    'https://api.mistral.ai/v1',                          14, 1, NOW(), NOW()),
    ('Cohere',     'https://api.cohere.ai/v1',                           15, 1, NOW(), NOW());

-- ----------------------------------------------------------------------------
-- 8. 初始化模型配置（测试种子数据）
-- ----------------------------------------------------------------------------

-- 8.1 系统级对话模型（SYSTEM LLM）
INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    (1,  'GPT-4o',         'OpenAI',    'sk-test-placeholder-openai-gpt4o',        'https://api.openai.com/v1',         1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (2,  'GPT-4o-mini',    'OpenAI',    'sk-test-placeholder-openai-gpt4o-mini',  'https://api.openai.com/v1',         1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (3,  'GPT-4-turbo',    'OpenAI',    'sk-test-placeholder-openai-gpt4-turbo', 'https://api.openai.com/v1',         1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (4,  'Claude 3.5 Sonnet', 'Anthropic', 'sk-test-placeholder-anthropic-sonnet', 'https://api.anthropic.com/v1',    1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (5,  'Claude 3 Opus',    'Anthropic', 'sk-test-placeholder-anthropic-opus',    'https://api.anthropic.com/v1',    1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (6,  'DeepSeek Chat',   'DeepSeek',  'sk-test-placeholder-deepseek-chat',      'https://api.deepseek.com/v1',      1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (7,  'DeepSeek Coder',  'DeepSeek',  'sk-test-placeholder-deepseek-coder',     'https://api.deepseek.com/v1',      1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (8,  '通义千问 Plus',   '阿里云',    'sk-test-placeholder-aliyun-qwen-plus',   'https://dashscope.aliyuncs.com/compatible-mode/v1', 1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (9,  '通义千问 Turbo',  '阿里云',    'sk-test-placeholder-aliyun-qwen-turbo', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (10, '硅基 Qwen2.5',    '硅基流动',  'sk-test-placeholder-siliconflow-qwen',   'https://api.siliconflow.cn/v1',    1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (11, '硅基 GLM-4',      '硅基流动',  'sk-test-placeholder-siliconflow-glm4',   'https://api.siliconflow.cn/v1',    1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (12, 'GLM-4-Plus',     '智谱 GLM',  'sk-test-placeholder-zhipu-glm4-plus',  'https://open.bigmodel.cn/api/paas/v4', 1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (13, 'Groq Llama3.1',   'Groq',      'sk-test-placeholder-groq-llama',        'https://api.groq.com/openai/v1',  1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW());

-- 8.2 系统级向量模型（SYSTEM EMBEDDING）
INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    (21, 'text-embedding-3-small', 'OpenAI',   'sk-test-placeholder-openai-embed',   'https://api.openai.com/v1',              1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (22, 'text-embedding-3-large', 'OpenAI',   'sk-test-placeholder-openai-embed-3l','https://api.openai.com/v1',              1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (23, 'text-embedding-ada-002',  'OpenAI',   'sk-test-placeholder-openai-ada',    'https://api.openai.com/v1',              1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (24, 'embedding-v1.5',         '智谱 GLM',  'sk-test-placeholder-zhipu-embed',   'https://open.bigmodel.cn/api/paas/v4',   1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (25, 'bge-large-zh-v1.5',      '硅基流动',  'sk-test-placeholder-silicon-bge',   'https://api.siliconflow.cn/v1',          1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW());

-- 8.3 个人模型（USER 级，owner_id = 1 即 admin 用户）
INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    (31, '我的 GPT-4o',        'OpenAI',   'sk-personal-openai-gpt4o-test',     'https://api.openai.com/v1',              1, 'LLM', 'USER', 1, 1, NOW(), NOW()),
    (32, '我的 Claude',        'Anthropic', 'sk-personal-anthropic-test',        'https://api.anthropic.com/v1',            1, 'LLM', 'USER', 1, 1, NOW(), NOW()),
    (33, '我的 DeepSeek',      'DeepSeek',  'sk-personal-deepseek-test',         'https://api.deepseek.com/v1',             1, 'LLM', 'USER', 1, 1, NOW(), NOW()),
    (34, '我的 Embedding',     'OpenAI',   'sk-personal-openai-embed-test',    'https://api.openai.com/v1',              1, 'EMBEDDING', 'USER', 1, 1, NOW(), NOW()),
    (35, 'Ollama Llama3',     'Ollama',    'sk-personal-ollama-llama3',         'http://localhost:11434/v1',              1, 'LLM', 'USER', 1, 1, NOW(), NOW()),
    (36, 'Ollama Qwen2.5',    'Ollama',    'sk-personal-ollama-qwen',           'http://localhost:11434/v1',              1, 'LLM', 'USER', 1, 1, NOW(), NOW()),
    (37, 'LM Studio GPT4',    'LM Studio', 'sk-personal-lmstudio-gpt4',         'http://localhost:1234/v1',                1, 'LLM', 'USER', 1, 1, NOW(), NOW());

-- 8.4 已禁用模型
INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    (40, '已禁用的 GPT-4',   'OpenAI',   'sk-disabled-openai-gpt4',            'https://api.openai.com/v1',              0, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW());

-- ############################################################################
-- 使用说明
-- ############################################################################
--
-- 1. 默认账号：admin / admin123（超级管理员，拥有全部权限）
--
-- 2. 角色说明：
--    SUPER_ADMIN - 超级管理员（全部数据）
--    LEADER      - 高层领导（部门及子部门数据）
--    DEPT_HEAD   - 部门负责人（本部门数据）
--    ADMIN       - 普通管理员（本部门数据）
--    USER        - 普通员工（仅本人数据）
--
-- 3. 数据权限（data_scope）：
--    1=全部 2=部门及子部门 3=本部门 4=本人 5=自定义部门
--
-- 4. 资源范围（scope）：
--    SYSTEM - 系统级资源，管理员创建，全局可见
--    USER   - 用户级资源，仅 owner_id 本人可见
--
-- 5. API Key 使用：
--    管理员通过 POST /api/admin/api-key/generate 为用户生成密钥
--    用户调用资源时携带 X-Api-Key 请求头
--
-- 6. 资源授权：
--    POST /api/admin/resource-grant/assign  授权
--    POST /api/admin/resource-grant/revoke  撤销
--
-- ############################################################################
