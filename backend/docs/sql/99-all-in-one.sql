-- ============================================================================
-- EvolveHub v1.4 - 完整数据库初始化脚本（PostgreSQL）
-- ============================================================================
-- 功能：一键初始化系统所有表和基础数据
-- 包含：RBAC权限体系 + 资源管理（模型/技能/MCP）+ MCP工具表 + MCP实例表
-- 执行时机：数据库创建后首次部署时执行（一键初始化）
-- 作者：EvolveHub Team
-- 日期：2026-04-15
-- ============================================================================

-- =============================================================================
-- 第一部分：00-init-all.sql - RBAC 权限体系表 + 资源管理表
-- =============================================================================

-- ----------------------------------------------------------------------------
-- 一、用户表 (eh_user)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_user (
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

CREATE INDEX IF NOT EXISTS idx_user_username ON eh_user(username);
CREATE INDEX IF NOT EXISTS idx_user_dept_id  ON eh_user(dept_id);

-- ----------------------------------------------------------------------------
-- 二、角色表 (eh_role)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_role (
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

CREATE TABLE IF NOT EXISTS eh_permission (
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

CREATE TABLE IF NOT EXISTS eh_dept (
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

CREATE TABLE IF NOT EXISTS eh_user_role (
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

CREATE TABLE IF NOT EXISTS eh_role_permission (
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

CREATE TABLE IF NOT EXISTS eh_role_data_scope (
    id          BIGINT PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    dept_id     BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  eh_role_data_scope IS '角色数据权限表（用于 data_scope=5 的自定义部门）';

CREATE INDEX IF NOT EXISTS idx_role_data_scope_role_id ON eh_role_data_scope(role_id);

-- ----------------------------------------------------------------------------
-- 八、模型配置表 (eh_model_config)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_model_config (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    provider    VARCHAR(50),
    api_key     VARCHAR(255),
    base_url    VARCHAR(500),
    enabled     INTEGER DEFAULT 1,
    model_type  VARCHAR(20),
    scope       VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    dept_id     BIGINT,
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
COMMENT ON COLUMN eh_model_config.scope      IS '资源范围：SYSTEM-系统级 DEPT-部门级 USER-指定用户级';
COMMENT ON COLUMN eh_model_config.dept_id    IS '部门 ID，scope=DEPT 时必填';
COMMENT ON COLUMN eh_model_config.owner_id   IS '创建者 ID，USER 时记录创建者（可见性由 eh_resource_grant 决定）';

CREATE INDEX IF NOT EXISTS idx_model_config_scope    ON eh_model_config(scope);
CREATE INDEX IF NOT EXISTS idx_model_config_dept_id  ON eh_model_config(dept_id);
CREATE INDEX IF NOT EXISTS idx_model_config_owner_id ON eh_model_config(owner_id);

-- ----------------------------------------------------------------------------
-- 九、技能配置表 (eh_skill_config) - 基础结构（后续会扩展）
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_skill_config (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    skill_type      VARCHAR(20),
    content         TEXT,
    package_path    VARCHAR(500),
    source          VARCHAR(20) DEFAULT 'MANUAL',
    source_url      VARCHAR(500),
    tags            JSONB,
    config          JSONB,
    workspace_path  VARCHAR(500),
    enabled         INTEGER DEFAULT 1,
    scope           VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    dept_id         BIGINT,
    owner_id        BIGINT,
    create_by       BIGINT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_skill_config                IS '技能配置表';
COMMENT ON COLUMN eh_skill_config.name           IS '技能名称';
COMMENT ON COLUMN eh_skill_config.description    IS '技能描述';
COMMENT ON COLUMN eh_skill_config.skill_type     IS '技能类型';
COMMENT ON COLUMN eh_skill_config.content        IS 'SKILL.md 内容（Markdown 格式）';
COMMENT ON COLUMN eh_skill_config.package_path   IS 'ZIP 包路径（上传模式）';
COMMENT ON COLUMN eh_skill_config.source         IS '来源：MANUAL-手动创建 HUB-Hub安装 BUILTIN-内置';
COMMENT ON COLUMN eh_skill_config.source_url     IS 'Hub 安装时的来源 URL';
COMMENT ON COLUMN eh_skill_config.tags           IS '标签数组';
COMMENT ON COLUMN eh_skill_config.config         IS '配置信息（JSON）';
COMMENT ON COLUMN eh_skill_config.workspace_path IS 'S3 工作区路径，如 skills/{id}/';
COMMENT ON COLUMN eh_skill_config.enabled        IS '是否启用（1=启用 0=禁用）';
COMMENT ON COLUMN eh_skill_config.scope          IS '资源范围：SYSTEM-系统级 DEPT-部门级 USER-指定用户级';
COMMENT ON COLUMN eh_skill_config.dept_id        IS '部门 ID，scope=DEPT 时必填';
COMMENT ON COLUMN eh_skill_config.owner_id       IS '创建者 ID，USER 时记录创建者（可见性由 eh_resource_grant 决定）';

CREATE INDEX IF NOT EXISTS idx_skill_config_scope          ON eh_skill_config(scope);
CREATE INDEX IF NOT EXISTS idx_skill_config_dept_id        ON eh_skill_config(dept_id);
CREATE INDEX IF NOT EXISTS idx_skill_config_owner          ON eh_skill_config(owner_id);
CREATE INDEX IF NOT EXISTS idx_skill_config_workspace_path ON eh_skill_config(workspace_path);

-- ----------------------------------------------------------------------------
-- 十、MCP 服务配置表 (eh_mcp_config) - 基础结构（后续会扩展）
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_mcp_config (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    transport_type  VARCHAR(20) DEFAULT 'UPLOADED',
    server_url      VARCHAR(500),
    package_path    VARCHAR(500),
    command         VARCHAR(500),
    args            TEXT,
    env             TEXT,
    work_dir        VARCHAR(500),
    protocol        VARCHAR(20) DEFAULT 'stdio',
    config          JSONB,
    enabled         INTEGER DEFAULT 1,
    scope           VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    dept_id         BIGINT,
    owner_id        BIGINT,
    create_by       BIGINT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_mcp_config                IS 'MCP 服务配置表';
COMMENT ON COLUMN eh_mcp_config.name           IS '服务名称';
COMMENT ON COLUMN eh_mcp_config.description    IS '服务描述';
COMMENT ON COLUMN eh_mcp_config.transport_type IS '传输类型：UPLOADED-本地上传 REMOTE-远程URL';
COMMENT ON COLUMN eh_mcp_config.server_url     IS '服务器地址（REMOTE 模式必填）';
COMMENT ON COLUMN eh_mcp_config.package_path   IS 'zip 包存储路径（MinIO/S3，UPLOADED 模式使用）';
COMMENT ON COLUMN eh_mcp_config.command        IS '启动命令（如 node server.js）';
COMMENT ON COLUMN eh_mcp_config.args           IS '命令参数（JSON 数组）';
COMMENT ON COLUMN eh_mcp_config.env            IS '环境变量（JSON）';
COMMENT ON COLUMN eh_mcp_config.work_dir       IS '工作目录（STDIO 模式）';
COMMENT ON COLUMN eh_mcp_config.protocol       IS '协议类型（STDIO / SSE）';
COMMENT ON COLUMN eh_mcp_config.config         IS '配置信息（JSON）';
COMMENT ON COLUMN eh_mcp_config.enabled        IS '是否启用（1=启用 0=禁用）';
COMMENT ON COLUMN eh_mcp_config.scope          IS '资源范围：SYSTEM-系统级 DEPT-部门级 USER-指定用户级';
COMMENT ON COLUMN eh_mcp_config.dept_id        IS '部门 ID，scope=DEPT 时必填';
COMMENT ON COLUMN eh_mcp_config.owner_id       IS '创建者 ID，USER 时记录创建者（可见性由 eh_resource_grant 决定）';

CREATE INDEX IF NOT EXISTS idx_mcp_config_scope    ON eh_mcp_config(scope);
CREATE INDEX IF NOT EXISTS idx_mcp_config_dept_id  ON eh_mcp_config(dept_id);
CREATE INDEX IF NOT EXISTS idx_mcp_config_owner    ON eh_mcp_config(owner_id);

-- ----------------------------------------------------------------------------
-- 十一、用户 API Key 表 (eh_api_key)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_api_key (
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

CREATE UNIQUE INDEX IF NOT EXISTS uk_api_key      ON eh_api_key(api_key)  WHERE deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_api_key_user ON eh_api_key(user_id)  WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 十二、资源授权表 (eh_resource_grant)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_resource_grant (
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT,
    resource_type VARCHAR(20) NOT NULL,
    resource_id   BIGINT NOT NULL,
    dept_id       BIGINT,
    role_id       BIGINT,
    create_by     BIGINT,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_resource_grant               IS '资源授权表（控制 USER 级资源的可见用户、DEPT/ROLE 级批量授权）';
COMMENT ON COLUMN eh_resource_grant.user_id       IS '被授权用户 ID';
COMMENT ON COLUMN eh_resource_grant.resource_type IS '资源类型：MODEL-模型 SKILL-技能 MCP-MCP服务';
COMMENT ON COLUMN eh_resource_grant.resource_id   IS '资源 ID（指向具体资源表主键）';
COMMENT ON COLUMN eh_resource_grant.dept_id       IS '被授权部门 ID，DEPT 级别授权时使用';
COMMENT ON COLUMN eh_resource_grant.role_id       IS '被授权角色 ID，ROLE 级别授权时使用';

CREATE INDEX  IF NOT EXISTS idx_resource_grant_user     ON eh_resource_grant(user_id, resource_type);
CREATE INDEX  IF NOT EXISTS idx_resource_grant_resource ON eh_resource_grant(resource_type, resource_id);
CREATE INDEX  IF NOT EXISTS idx_resource_grant_dept     ON eh_resource_grant(dept_id) WHERE deleted = 0;
CREATE INDEX  IF NOT EXISTS idx_resource_grant_role     ON eh_resource_grant(role_id) WHERE deleted = 0;

-- 三种授权类型分别设唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_resource_grant_user ON eh_resource_grant(user_id, resource_type, resource_id) WHERE deleted = 0 AND user_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_resource_grant_dept ON eh_resource_grant(dept_id, resource_type, resource_id) WHERE deleted = 0 AND dept_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_resource_grant_role ON eh_resource_grant(role_id, resource_type, resource_id) WHERE deleted = 0 AND role_id IS NOT NULL;

-- ----------------------------------------------------------------------------
-- 序列
-- ----------------------------------------------------------------------------

CREATE SEQUENCE IF NOT EXISTS seq_role_permission START 1;

-- ----------------------------------------------------------------------------
-- 基础数据初始化：角色
-- ----------------------------------------------------------------------------

INSERT INTO eh_role (id, role_name, role_code, data_scope, sort, status, remark, create_time, update_time)
VALUES
    (1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '拥有系统所有权限，不受任何限制', NOW(), NOW()),
    (2, '高层领导',   'LEADER',      2, 2, 1, '公司高层领导，可查看跨部门信息', NOW(), NOW()),
    (3, '部门负责人', 'DEPT_HEAD',   3, 3, 1, '部门负责人，管理本部门数据',     NOW(), NOW()),
    (4, '普通管理员', 'ADMIN',       3, 4, 1, '普通管理员，管理用户和部门',     NOW(), NOW()),
    (5, '普通员工',   'USER',        4, 5, 1, '普通员工，只能访问个人相关功能', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 基础数据初始化：部门
-- ----------------------------------------------------------------------------

INSERT INTO eh_dept (id, parent_id, dept_name, sort, status, create_time, update_time)
VALUES
    (1, 0, '总公司', 1, 1, NOW(), NOW()),
    (2, 1, '技术部', 1, 1, NOW(), NOW()),
    (3, 1, '产品部', 2, 1, NOW(), NOW()),
    (4, 1, '市场部', 3, 1, NOW(), NOW()),
    (5, 1, '行政部', 4, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 基础数据初始化：超级管理员账号
-- 用户名：admin  密码：admin123（BCrypt 哈希）
-- ----------------------------------------------------------------------------

INSERT INTO eh_user (id, username, password, nickname, email, dept_id, status, create_time, update_time)
VALUES
    (1, 'admin', '$2a$10$zv.OUJsmMI1kuebzfoQf2.j.9MNW5c0t1gmm3RW/EWk7d95nov0fu', '系统管理员', 'admin@evolvehub.com', 1, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO eh_user_role (user_id, role_id, create_time, update_time)
VALUES (1, 1, NOW(), NOW())
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 基础数据初始化：桌面图标
-- ----------------------------------------------------------------------------

INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (9,  0, 'AI 对话',   'app:chat',      'MENU', '/app/chat',      'MessageSquare', 1,  1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #5E5CE6)', 900, 640, 700, 480, 0, 1),
    (10, 0, '知识库',    'app:knowledge', 'MENU', '/app/knowledge', 'BookOpen',      2,  1, NOW(), NOW(), 'linear-gradient(135deg, #30D158, #34C759)', 880, 600, 700, 460, 1, 1),
    (11, 0, '模型管理',  'app:model',     'MENU', '/app/model',     'Bot',           3,  1, NOW(), NOW(), 'linear-gradient(135deg, #BF5AF2, #9B59B6)', 800, 560, 640, 400, -1, 1),
    (12, 0, '用户管理',  'app:users',     'MENU', '/app/users',     'Users',         4,  1, NOW(), NOW(), 'linear-gradient(135deg, #FF9F0A, #FF6B00)', 900, 640, 700, 480, -1, 1),
    (13, 0, '技能管理',  'app:mcp',       'MENU', '/app/mcp',       'Wrench',        5,  1, NOW(), NOW(), 'linear-gradient(135deg, #64D2FF, #5AC8FA)', 920, 600, 720, 460, 2, 1),
    (14, 0, '记忆管理',  'app:memory',    'MENU', '/app/memory',    'Zap',           6,  1, NOW(), NOW(), 'linear-gradient(135deg, #FFD60A, #FF9F0A)', 800, 560, 640, 400, -1, 1),
    (15, 0, '个人设置',  'app:settings',  'MENU', '/app/settings',  'Settings',      7,  1, NOW(), NOW(), 'linear-gradient(135deg, #8E8E93, #636366)', 780, 560, 640, 400, 3, 1),
    (16, 0, '数据大屏',  'app:dashboard', 'MENU', '/app/dashboard', 'Monitor',       8,  1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #30D158)', 1280, 800, 1024, 600, 4, 1),
    (17, 0, '宠物管理',  'app:pets',      'MENU', '/app/pets',      'Cat',           9,  1, NOW(), NOW(), 'linear-gradient(135deg, #FF6B9D, #BF5AF2)', 900, 640, 700, 480, -1, 1),
    (18, 0, '部门管理',  'app:dept',      'MENU', '/app/dept',      'Building',     10,  1, NOW(), NOW(), 'linear-gradient(135deg, #64D2FF, #5AC8FA)', 800, 600, 640, 400, -1, 1),
    (19, 0, '图标管理',  'app:desktopicon','MENU', '/app/desktopicon','Grid',        11,  1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #5E5CE6)', 900, 640, 700, 480, -1, 1),
    (26, 0, '角色管理',  'app:role',      'MENU', '/app/role',      'Shield',       12,  1, NOW(), NOW(), 'linear-gradient(135deg, #BF5AF2, #9B59B6)', 800, 600, 640, 400, -1, 1),
    (27, 0, '权限管理',  'app:permission','MENU', '/app/permission', 'Key',          13,  1, NOW(), NOW(), 'linear-gradient(135deg, #FFD60A, #FF9F0A)', 800, 600, 640, 400, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 基础数据初始化：接口权限
-- ----------------------------------------------------------------------------

INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (20, 0, '桌面图标查询', 'desktop-icon:query',  'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (21, 0, '桌面图标列表', 'desktop-icon:list',   'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (22, 0, '桌面图标创建', 'desktop-icon:create', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (23, 0, '桌面图标更新', 'desktop-icon:update', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (24, 0, '桌面图标删除', 'desktop-icon:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 基础数据初始化：角色-桌面图标关联
-- ----------------------------------------------------------------------------

-- 超级管理员（SUPER_ADMIN）- 全部
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

-- 普通管理员（ADMIN）
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 4, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 18, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 部门负责人（DEPT_HEAD）
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 3, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 18, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 高层领导（LEADER）
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 2, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 15, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 16, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 普通员工（USER）
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 5, 9, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 10, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 14, NOW(), NOW()),
    (nextval('seq_role_permission'), 5, 15, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- =============================================================================
-- 第二部分：01-resource-scope.sql - 资源分级权限扩展
-- =============================================================================

-- ----------------------------------------------------------------------------
-- eh_model_config 新增 scope + owner_id（已在第一部分创建，此处确保索引存在）
-- ----------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_model_config_scope ON eh_model_config(scope);
CREATE INDEX IF NOT EXISTS idx_model_config_owner_id ON eh_model_config(owner_id);

-- ----------------------------------------------------------------------------
-- eh_skill_config 新增 scope + owner_id（已在第一部分创建，此处确保索引存在）
-- ----------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_skill_config_scope ON eh_skill_config(scope);
CREATE INDEX IF NOT EXISTS idx_skill_config_owner ON eh_skill_config(owner_id);

-- ----------------------------------------------------------------------------
-- eh_mcp_config 新增 scope + owner_id（已在第一部分创建，此处确保索引存在）
-- ----------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_mcp_config_scope ON eh_mcp_config(scope);
CREATE INDEX IF NOT EXISTS idx_mcp_config_owner ON eh_mcp_config(owner_id);


-- =============================================================================
-- 第三部分：02-model-provider.sql - 模型提供商维度表
-- =============================================================================

CREATE TABLE IF NOT EXISTS eh_model_provider (
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

CREATE INDEX IF NOT EXISTS idx_model_provider_enabled ON eh_model_provider(enabled);

-- ----------------------------------------------------------------------------
-- 模型提供商种子数据
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
    ('Cohere',     'https://api.cohere.ai/v1',                           15, 1, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;


-- =============================================================================
-- 第四部分：03-model-config-seed.sql - 模型配置测试数据
-- =============================================================================

-- ----------------------------------------------------------------------------
-- 系统级对话模型（SYSTEM LLM）
-- ----------------------------------------------------------------------------

INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    -- OpenAI 系列（系统级）
    (1,  'GPT-4o',         'OpenAI',    'sk-test-placeholder-openai-gpt4o',        'https://api.openai.com/v1',         1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (2,  'GPT-4o-mini',    'OpenAI',    'sk-test-placeholder-openai-gpt4o-mini',  'https://api.openai.com/v1',         1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (3,  'GPT-4-turbo',    'OpenAI',    'sk-test-placeholder-openai-gpt4-turbo', 'https://api.openai.com/v1',         1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),

    -- Anthropic 系列（系统级）
    (4,  'Claude 3.5 Sonnet', 'Anthropic', 'sk-test-placeholder-anthropic-sonnet', 'https://api.anthropic.com/v1',    1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (5,  'Claude 3 Opus',    'Anthropic', 'sk-test-placeholder-anthropic-opus',    'https://api.anthropic.com/v1',    1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),

    -- DeepSeek 系列（系统级）
    (6,  'DeepSeek Chat',   'DeepSeek',  'sk-test-placeholder-deepseek-chat',      'https://api.deepseek.com/v1',      1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (7,  'DeepSeek Coder',  'DeepSeek',  'sk-test-placeholder-deepseek-coder',     'https://api.deepseek.com/v1',      1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),

    -- 阿里云（系统级）
    (8,  '通义千问 Plus',   '阿里云',    'sk-test-placeholder-aliyun-qwen-plus',   'https://dashscope.aliyuncs.com/compatible-mode/v1', 1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (9,  '通义千问 Turbo',  '阿里云',    'sk-test-placeholder-aliyun-qwen-turbo', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),

    -- 硅基流动（系统级）
    (10, '硅基 Qwen2.5',    '硅基流动',  'sk-test-placeholder-siliconflow-qwen',   'https://api.siliconflow.cn/v1',    1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (11, '硅基 GLM-4',      '硅基流动',  'sk-test-placeholder-siliconflow-glm4',   'https://api.siliconflow.cn/v1',    1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),

    -- 智谱 GLM（系统级）
    (12, 'GLM-4-Plus',     '智谱 GLM',  'sk-test-placeholder-zhipu-glm4-plus',  'https://open.bigmodel.cn/api/paas/v4', 1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW()),

    -- Groq（系统级）
    (13, 'Groq Llama3.1',   'Groq',      'sk-test-placeholder-groq-llama',        'https://api.groq.com/openai/v1',  1, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 系统级向量模型（SYSTEM EMBEDDING）
-- ----------------------------------------------------------------------------

INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    (21, 'text-embedding-3-small', 'OpenAI',   'sk-test-placeholder-openai-embed',   'https://api.openai.com/v1',              1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (22, 'text-embedding-3-large', 'OpenAI',   'sk-test-placeholder-openai-embed-3l','https://api.openai.com/v1',              1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (23, 'text-embedding-ada-002',  'OpenAI',   'sk-test-placeholder-openai-ada',    'https://api.openai.com/v1',              1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (24, 'embedding-v1.5',         '智谱 GLM',  'sk-test-placeholder-zhipu-embed',   'https://open.bigmodel.cn/api/paas/v4',   1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW()),
    (25, 'bge-large-zh-v1.5',      '硅基流动',  'sk-test-placeholder-silicon-bge',   'https://api.siliconflow.cn/v1',          1, 'EMBEDDING', 'SYSTEM', NULL, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 个人模型（USER 级，仅 owner_id = 1 的用户可用）
-- ----------------------------------------------------------------------------

INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    -- admin 个人配置的 OpenAI Key
    (31, '我的 GPT-4o',        'OpenAI',   'sk-personal-openai-gpt4o-test',     'https://api.openai.com/v1',              1, 'LLM', 'USER', 1, 1, NOW(), NOW()),
    (32, '我的 Claude',        'Anthropic', 'sk-personal-anthropic-test',        'https://api.anthropic.com/v1',            1, 'LLM', 'USER', 1, 1, NOW(), NOW()),
    (33, '我的 DeepSeek',      'DeepSeek',  'sk-personal-deepseek-test',         'https://api.deepseek.com/v1',             1, 'LLM', 'USER', 1, 1, NOW(), NOW()),

    -- admin 个人的向量模型
    (34, '我的 Embedding',     'OpenAI',   'sk-personal-openai-embed-test',    'https://api.openai.com/v1',              1, 'EMBEDDING', 'USER', 1, 1, NOW(), NOW()),

    -- 本地 Ollama（个人部署）
    (35, 'Ollama Llama3',     'Ollama',    'sk-personal-ollama-llama3',         'http://localhost:11434/v1',              1, 'LLM', 'USER', 1, 1, NOW(), NOW()),
    (36, 'Ollama Qwen2.5',    'Ollama',    'sk-personal-ollama-qwen',           'http://localhost:11434/v1',              1, 'LLM', 'USER', 1, 1, NOW(), NOW()),

    -- LM Studio 本地
    (37, 'LM Studio GPT4',    'LM Studio', 'sk-personal-lmstudio-gpt4',         'http://localhost:1234/v1',                1, 'LLM', 'USER', 1, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 禁用一些模型（模拟已禁用的）
-- ----------------------------------------------------------------------------

INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    (40, '已禁用的 GPT-4',   'OpenAI',   'sk-disabled-openai-gpt4',            'https://api.openai.com/v1',              0, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;


-- =============================================================================
-- 第五部分：06-mcp-skill-upgrade.sql - MCP & Skill 详细设计升级 DDL
-- =============================================================================

-- ----------------------------------------------------------------------------
-- 1. eh_mcp_config 表扩展（新增字段支持 STDIO/UPLOADED 和 DEPT 部门级）
-- ----------------------------------------------------------------------------

-- 修改 server_url 字段为可空（STDIO 模式不需要 server_url）
ALTER TABLE eh_mcp_config ALTER COLUMN server_url DROP NOT NULL;

ALTER TABLE eh_mcp_config ADD COLUMN IF NOT EXISTS work_dir VARCHAR(500);
ALTER TABLE eh_mcp_config ADD COLUMN IF NOT EXISTS transport_type VARCHAR(20);
ALTER TABLE eh_mcp_config ADD COLUMN IF NOT EXISTS command VARCHAR(500);
ALTER TABLE eh_mcp_config ADD COLUMN IF NOT EXISTS args VARCHAR(1000);
ALTER TABLE eh_mcp_config ADD COLUMN IF NOT EXISTS env TEXT;
ALTER TABLE eh_mcp_config ADD COLUMN IF NOT EXISTS dept_id BIGINT;

COMMENT ON COLUMN eh_mcp_config.work_dir IS 'STDIO 模式工作目录路径';
COMMENT ON COLUMN eh_mcp_config.transport_type IS '传输类型：REMOTE-远程 SSE / UPLOADED-本地上传 STDIO';
COMMENT ON COLUMN eh_mcp_config.command IS 'STDIO 启动命令';
COMMENT ON COLUMN eh_mcp_config.args IS '命令行参数';
COMMENT ON COLUMN eh_mcp_config.env IS '环境变量（JSON 格式）';
COMMENT ON COLUMN eh_mcp_config.dept_id IS '所属部门 ID（scope=DEPT 时使用）';

CREATE INDEX IF NOT EXISTS idx_mcp_config_dept_id ON eh_mcp_config(dept_id);

-- ----------------------------------------------------------------------------
-- 2. eh_skill_config 表扩展（新增字段支持内容管理、标签、来源追踪）
-- ----------------------------------------------------------------------------

ALTER TABLE eh_skill_config ADD COLUMN IF NOT EXISTS content TEXT;
ALTER TABLE eh_skill_config ADD COLUMN IF NOT EXISTS package_path VARCHAR(500);
ALTER TABLE eh_skill_config ADD COLUMN IF NOT EXISTS source VARCHAR(20) DEFAULT 'MANUAL';
ALTER TABLE eh_skill_config ADD COLUMN IF NOT EXISTS source_url VARCHAR(500);
ALTER TABLE eh_skill_config ADD COLUMN IF NOT EXISTS tags JSONB;
ALTER TABLE eh_skill_config ADD COLUMN IF NOT EXISTS dept_id BIGINT;

COMMENT ON COLUMN eh_skill_config.content IS 'SKILL.md 内容（Markdown）';
COMMENT ON COLUMN eh_skill_config.package_path IS 'ZIP包路径（上传模式）';
COMMENT ON COLUMN eh_skill_config.source IS '来源：MANUAL-手动创建 HUB-Hub安装 BUILTIN-内置';
COMMENT ON COLUMN eh_skill_config.source_url IS 'Hub安装时的来源URL';
COMMENT ON COLUMN eh_skill_config.tags IS '标签数组，如 ["coder","document"]';
COMMENT ON COLUMN eh_skill_config.dept_id IS '部门ID，scope=DEPT 时必填';

CREATE INDEX IF NOT EXISTS idx_skill_config_dept_id ON eh_skill_config(dept_id);

-- ----------------------------------------------------------------------------
-- 3. eh_resource_grant 表扩展（dept_id, role_id 已在 CREATE TABLE 中定义）
-- ----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_resource_grant_role ON eh_resource_grant(role_id) WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 4. eh_mcp_tool MCP 工具表
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_mcp_tool (
    id              BIGSERIAL PRIMARY KEY,
    mcp_config_id   BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    input_schema    JSONB,
    risk_level      VARCHAR(10) DEFAULT 'MEDIUM',
    tool_scope      VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    dept_id         BIGINT,
    owner_id        BIGINT,
    enabled         INTEGER DEFAULT 1,
    deleted         INTEGER DEFAULT 0,
    create_by       BIGINT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE eh_mcp_tool IS 'MCP 服务工具表';
COMMENT ON COLUMN eh_mcp_tool.mcp_config_id IS '所属 MCP Server ID';
COMMENT ON COLUMN eh_mcp_tool.name IS '工具名称';
COMMENT ON COLUMN eh_mcp_tool.description IS '工具描述';
COMMENT ON COLUMN eh_mcp_tool.input_schema IS '工具输入参数 JSON Schema';
COMMENT ON COLUMN eh_mcp_tool.risk_level IS '风险等级：LOW-低 MEDIUM-中 HIGH-高';
COMMENT ON COLUMN eh_mcp_tool.tool_scope IS '工具可见范围：SYSTEM-系统级 DEPT-部门级 USER-个人级 GRANT-授权访问';
COMMENT ON COLUMN eh_mcp_tool.dept_id IS '部门ID，tool_scope=DEPT 时必填';
COMMENT ON COLUMN eh_mcp_tool.owner_id IS '所有者ID，tool_scope=USER 时必填';
COMMENT ON COLUMN eh_mcp_tool.enabled IS '工具开关：1-启用 0-禁用';

CREATE INDEX IF NOT EXISTS idx_mcp_tool_mcp_config_id ON eh_mcp_tool(mcp_config_id);
CREATE INDEX IF NOT EXISTS idx_mcp_tool_scope ON eh_mcp_tool(tool_scope);
CREATE INDEX IF NOT EXISTS idx_mcp_tool_dept_id ON eh_mcp_tool(dept_id);
CREATE INDEX IF NOT EXISTS idx_mcp_tool_owner_id ON eh_mcp_tool(owner_id);

-- 添加外键约束
ALTER TABLE eh_mcp_tool ADD CONSTRAINT fk_mcp_tool_mcp_config
    FOREIGN KEY (mcp_config_id) REFERENCES eh_mcp_config(id) ON DELETE CASCADE;

-- ----------------------------------------------------------------------------
-- 5. eh_mcp_tool_grant 工具级授权表
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_mcp_tool_grant (
    id            BIGSERIAL PRIMARY KEY,
    tool_id       BIGINT NOT NULL,
    grant_type    VARCHAR(20) NOT NULL,
    target_id     BIGINT NOT NULL,
    create_by     BIGINT,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_mcp_tool_grant IS 'MCP 工具级授权表';
COMMENT ON COLUMN eh_mcp_tool_grant.tool_id IS '工具ID';
COMMENT ON COLUMN eh_mcp_tool_grant.grant_type IS '授权类型：USER-用户 DEPT-部门 ROLE-角色';
COMMENT ON COLUMN eh_mcp_tool_grant.target_id IS '目标ID（user_id/dept_id/role_id）';

CREATE INDEX IF NOT EXISTS idx_tool_grant_tool_id ON eh_mcp_tool_grant(tool_id);
CREATE INDEX IF NOT EXISTS idx_tool_grant_target ON eh_mcp_tool_grant(grant_type, target_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tool_grant ON eh_mcp_tool_grant(tool_id, grant_type, target_id) WHERE deleted = 0;

-- 添加外键约束
ALTER TABLE eh_mcp_tool_grant ADD CONSTRAINT fk_mcp_tool_grant_tool
    FOREIGN KEY (tool_id) REFERENCES eh_mcp_tool(id) ON DELETE CASCADE;

-- ----------------------------------------------------------------------------
-- 6. eh_mcp_instance MCP 服务实例表
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_mcp_instance (
    id              BIGSERIAL PRIMARY KEY,
    mcp_config_id   BIGINT NOT NULL,
    instance_key    VARCHAR(50) NOT NULL UNIQUE,
    process_id      BIGINT,
    server_url      VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'STARTING',
    last_heartbeat  TIMESTAMP,
    start_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stop_time       TIMESTAMP,
    fail_count      INTEGER DEFAULT 0,
    error_msg       TEXT,
    transport_type  VARCHAR(20) NOT NULL DEFAULT 'STDIO',
    work_dir        VARCHAR(500),
    tool_count      INTEGER DEFAULT 0,
    deleted         INTEGER DEFAULT 0,
    create_by       BIGINT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE eh_mcp_instance IS 'MCP 服务实例表';
COMMENT ON COLUMN eh_mcp_instance.mcp_config_id IS '关联的 MCP Config ID';
COMMENT ON COLUMN eh_mcp_instance.instance_key IS '唯一实例标识 (UUID)';
COMMENT ON COLUMN eh_mcp_instance.process_id IS 'OS 进程 ID (STDIO 模式)';
COMMENT ON COLUMN eh_mcp_instance.server_url IS '服务器地址 (SSE 模式)';
COMMENT ON COLUMN eh_mcp_instance.status IS '实例状态：STARTING/RUNNING/STOPPING/STOPPED/ERROR';
COMMENT ON COLUMN eh_mcp_instance.last_heartbeat IS '上次心跳时间';
COMMENT ON COLUMN eh_mcp_instance.start_time IS '启动时间';
COMMENT ON COLUMN eh_mcp_instance.stop_time IS '停止时间';
COMMENT ON COLUMN eh_mcp_instance.fail_count IS '连续心跳失败次数';
COMMENT ON COLUMN eh_mcp_instance.error_msg IS '错误信息';
COMMENT ON COLUMN eh_mcp_instance.transport_type IS '传输类型';
COMMENT ON COLUMN eh_mcp_instance.work_dir IS '工作目录';
COMMENT ON COLUMN eh_mcp_instance.tool_count IS '工具数量';

CREATE INDEX IF NOT EXISTS idx_mcp_instance_config_id ON eh_mcp_instance(mcp_config_id);
CREATE INDEX IF NOT EXISTS idx_mcp_instance_key ON eh_mcp_instance(instance_key);
CREATE INDEX IF NOT EXISTS idx_mcp_instance_status ON eh_mcp_instance(status);
CREATE UNIQUE INDEX IF NOT EXISTS idx_mcp_instance_unique_config
    ON eh_mcp_instance(mcp_config_id) WHERE status = 'RUNNING' AND deleted = 0;

-- 添加外键约束
ALTER TABLE eh_mcp_instance ADD CONSTRAINT fk_mcp_instance_mcp_config
    FOREIGN KEY (mcp_config_id) REFERENCES eh_mcp_config(id) ON DELETE CASCADE;

-- 创建序列
CREATE SEQUENCE IF NOT EXISTS eh_mcp_instance_id_seq START 1;

-- ----------------------------------------------------------------------------
-- 7. 更新现有 MCP 数据：将 transport_type 设置为 REMOTE（兼容现有 server_url）
-- ----------------------------------------------------------------------------

UPDATE eh_mcp_config SET transport_type = 'REMOTE' WHERE transport_type IS NULL;
UPDATE eh_mcp_config SET protocol = 'SSE' WHERE protocol = 'stdio' OR protocol = 'STDIO';
UPDATE eh_mcp_config SET scope = 'SYSTEM' WHERE scope IS NULL;

-- ----------------------------------------------------------------------------
-- 7. 更新现有 Skill 数据：设置 source 为 MANUAL，tags 为空数组
-- ----------------------------------------------------------------------------

UPDATE eh_skill_config SET source = 'MANUAL' WHERE source IS NULL;
UPDATE eh_skill_config SET tags = '[]'::jsonb WHERE tags IS NULL;


-- =============================================================================
-- 第六部分：04-mcp-config-seed.sql - MCP Server 种子数据
-- =============================================================================

INSERT INTO eh_mcp_config (id, name, description, transport_type, server_url, protocol, config, enabled, scope, dept_id, owner_id, create_by, create_time, update_time, deleted) VALUES
(1, 'OA 系统', '企业办公自动化系统 MCP 服务，提供请假、报销、表单等工具', 'REMOTE', 'http://oa.internal:3001', 'SSE', '{"timeout": 30000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(2, 'CRM 系统', '客户关系管理系统 MCP 服务，提供客户查询、跟进记录等工具', 'REMOTE', 'http://crm.internal:3002', 'SSE', '{"timeout": 30000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(3, 'Git 仓库', 'Git 仓库 MCP 服务，提供代码提交、分支管理等工具', 'REMOTE', 'http://git.internal:3003', 'SSE', '{"timeout": 60000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(4, '数据库管理', '数据库管理 MCP 服务，提供表结构查询、数据导出等工具', 'REMOTE', 'http://db.internal:3004', 'SSE', '{"timeout": 30000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(5, '文件存储', '企业网盘 MCP 服务，提供文件上传、下载、分享等工具', 'REMOTE', 'http://storage.internal:3005', 'SSE', '{"timeout": 60000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(6, '邮件系统', '企业邮件 MCP 服务，提供邮件发送、查询等工具', 'REMOTE', 'http://mail.internal:3006', 'SSE', '{"timeout": 30000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(7, '日历服务', '企业日历 MCP 服务，提供日程创建、查询等工具', 'REMOTE', 'http://calendar.internal:3007', 'SSE', '{"timeout": 20000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(8, '项目管理', '项目管理 MCP 服务，提供任务创建、进度跟踪等工具', 'REMOTE', 'http://pm.internal:3008', 'SSE', '{"timeout": 30000}', 0, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0);

-- 创建序列（MCP config 使用 BIGINT，不是 BIGSERIAL）
CREATE SEQUENCE IF NOT EXISTS eh_mcp_config_id_seq START 9;
SELECT setval('eh_mcp_config_id_seq', 9);


-- =============================================================================
-- 第七部分：05-skill-config-seed.sql - Skill 种子数据
-- =============================================================================

INSERT INTO eh_skill_config (id, name, description, skill_type, content, source, tags, config, enabled, scope, dept_id, owner_id, create_by, create_time, update_time, deleted) VALUES
(1, '代码助手', '智能代码编写辅助工具，支持代码生成、代码审查、代码重构', 'CODER', '# SKILL.md

# Role: Code Assistant

You are an expert programmer specializing in code generation, review, and refactoring.

## Capabilities
- Write clean, efficient, and well-documented code
- Review code for bugs, security issues, and performance problems
- Refactor existing code to improve readability and maintainability
- Follow best practices and design patterns

## Constraints
- Always use type hints in Python
- Follow PEP 8 style guide
- Write comprehensive unit tests
- Document complex logic with comments', 'MANUAL', '["coder", "programming", "review"]', '{"temperature": 0.7, "maxTokens": 4000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(2, '文档撰写', '技术文档和报告撰写助手，支持 Markdown 格式输出', 'WRITER', '# SKILL.md

# Role: Technical Writer

You are an expert technical writer creating clear, concise documentation.

## Capabilities
- Write technical documentation (README, API docs, guides)
- Create reports with proper structure
- Format content in Markdown
- Explain complex concepts in simple terms

## Constraints
- Use clear heading hierarchy
- Include code examples where relevant
- Keep documentation up to date', 'MANUAL', '["writer", "documentation", "markdown"]', '{"temperature": 0.6, "maxTokens": 8000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(3, '数据分析师', '数据分析与可视化助手，支持 SQL 生成和图表建议', 'ANALYST', '# SKILL.md

# Role: Data Analyst

You are a data analyst expert helping with data analysis and visualization.

## Capabilities
- Generate SQL queries for data analysis
- Suggest appropriate charts and visualizations
- Analyze trends and patterns
- Create data summaries and insights

## Constraints
- Optimize queries for performance
- Consider data security and privacy
- Validate data quality', 'MANUAL', '["analyst", "data", "sql", "visualization"]', '{"temperature": 0.5, "maxTokens": 6000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(4, '翻译助手', '多语言翻译助手，支持中英日韩等常见语言互译', 'TRANSLATOR', '# SKILL.md

# Role: Translator

You are a professional translator helping with language translation.

## Capabilities
- Translate between Chinese, English, Japanese, Korean and other languages
- Maintain context and tone of original text
- Handle technical terminology accurately

## Constraints
- Preserve original meaning
- Consider cultural nuances
- Handle idioms appropriately', 'MANUAL', '["translator", "language"]', '{"temperature": 0.3, "maxTokens": 4000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(5, '测试工程师', '自动化测试用例生成和测试报告撰写助手', 'TESTER', '# SKILL.md

# Role: QA Engineer

You are a QA engineer specializing in test case generation and reporting.

## Capabilities
- Generate comprehensive test cases
- Write automated test scripts
- Create test reports and summaries
- Identify edge cases and corner conditions

## Constraints
- Follow testing best practices
- Aim for high code coverage
- Document test rationale', 'MANUAL', '["tester", "qa", "testing"]', '{"temperature": 0.6, "maxTokens": 5000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(6, '架构师', '系统架构设计和评审助手，支持微服务架构设计', 'ARCHITECT', '# SKILL.md

# Role: Software Architect

You are a software architect specializing in system design and architecture.

## Capabilities
- Design scalable system architectures
- Evaluate and improve existing architectures
- Recommend appropriate technologies
- Create architecture documentation

## Constraints
- Consider maintainability and extensibility
- Balance between simplicity and functionality
- Follow architectural principles', 'MANUAL', '["architect", "system design", "microservices"]', '{"temperature": 0.7, "maxTokens": 8000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(7, '运维助手', 'DevOps 运维脚本生成和故障诊断助手', 'OPS', '# SKILL.md

# Role: DevOps Engineer

You are a DevOps engineer helping with operations and infrastructure.

## Capabilities
- Write deployment and automation scripts
- Diagnose system issues and propose solutions
- Create monitoring and alerting configurations
- Optimize infrastructure costs

## Constraints
- Follow security best practices
- Ensure reliability and availability
- Document all changes', 'MANUAL', '["ops", "devops", "infrastructure"]', '{"temperature": 0.5, "maxTokens": 4000}', 0, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0),
(8, '产品经理', 'PRD 文档撰写和需求分析助手', 'PM', '# SKILL.md

# Role: Product Manager

You are a product manager helping with requirements analysis and documentation.

## Capabilities
- Write PRD documents
- Analyze user requirements
- Create user stories and acceptance criteria
- Prioritize features based on impact

## Constraints
- Focus on user needs
- Consider technical feasibility
- Maintain clear documentation', 'MANUAL', '["pm", "product", "requirements"]', '{"temperature": 0.7, "maxTokens": 6000}', 1, 'SYSTEM', NULL, NULL, 1, NOW(), NOW(), 0);

-- 创建序列（Skill config 使用 BIGINT，不是 BIGSERIAL）
CREATE SEQUENCE IF NOT EXISTS eh_skill_config_id_seq START 9;
SELECT setval('eh_skill_config_id_seq', 9);


-- =============================================================================
-- 第八部分：MCP 工具种子数据
-- =============================================================================

-- 确保序列存在
CREATE SEQUENCE IF NOT EXISTS eh_mcp_tool_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS eh_mcp_tool_grant_id_seq START 1;

INSERT INTO eh_mcp_tool (id, mcp_config_id, name, description, input_schema, risk_level, tool_scope, enabled, create_by, create_time, update_time, deleted) VALUES
-- OA 系统工具
(1, 1, 'query_leave', '查询员工请假记录', '{"employee_id": {"type": "string"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(2, 1, 'submit_leave', '提交请假申请', '{"employee_id": {"type": "string"}, "days": {"type": "number"}, "reason": {"type": "string"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(3, 1, 'approve_leave', '审批请假申请', '{"request_id": {"type": "string"}, "approved": {"type": "boolean"}}', 'HIGH', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(4, 1, 'query_salary', '查询工资记录', '{"employee_id": {"type": "string"}, "month": {"type": "string"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),

-- CRM 系统工具
(5, 2, 'query_customer', '查询客户信息', '{"customer_id": {"type": "string"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(6, 2, 'update_record', '更新客户记录', '{"customer_id": {"type": "string"}, "data": {"type": "object"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(7, 2, 'export_data', '导出客户数据', '{"format": {"type": "string"}, "filters": {"type": "object"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),

-- Git 仓库工具
(8, 3, 'list_repos', '列出仓库列表', '{}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(9, 3, 'read_file', '读取文件内容', '{"repo": {"type": "string"}, "path": {"type": "string"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(10, 3, 'commit_change', '提交代码变更', '{"repo": {"type": "string"}, "message": {"type": "string"}, "files": {"type": "array"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(11, 3, 'create_branch', '创建分支', '{"repo": {"type": "string"}, "branch": {"type": "string"}, "from_branch": {"type": "string"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),

-- 数据库管理工具
(12, 4, 'query_tables', '查询数据库表结构', '{"table_name": {"type": "string"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(13, 4, 'execute_query', '执行 SQL 查询', '{"sql": {"type": "string"}}', 'HIGH', 'SYSTEM', 0, 1, NOW(), NOW(), 0),
(14, 4, 'export_data', '导出数据', '{"table": {"type": "string"}, "format": {"type": "string"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),

-- 文件存储工具
(15, 5, 'upload_file', '上传文件', '{"filename": {"type": "string"}, "content": {"type": "string"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(16, 5, 'download_file', '下载文件', '{"file_id": {"type": "string"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(17, 5, 'share_file', '分享文件链接', '{"file_id": {"type": "string"}, "expire_hours": {"type": "number"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),

-- 邮件系统工具
(18, 6, 'send_email', '发送邮件', '{"to": {"type": "array"}, "subject": {"type": "string"}, "body": {"type": "string"}}', 'MEDIUM', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(19, 6, 'query_emails', '查询邮件', '{"folder": {"type": "string"}, "limit": {"type": "number"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),

-- 日历服务工具
(20, 7, 'create_event', '创建日程事件', '{"title": {"type": "string"}, "start": {"type": "string"}, "end": {"type": "string"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(21, 7, 'query_events', '查询日程', '{"start_date": {"type": "string"}, "end_date": {"type": "string"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),

-- 项目管理工具
(22, 8, 'create_task', '创建任务', '{"title": {"type": "string"}, "assignee": {"type": "string"}, "priority": {"type": "string"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0),
(23, 8, 'update_progress', '更新进度', '{"task_id": {"type": "string"}, "progress": {"type": "number"}}', 'LOW', 'SYSTEM', 1, 1, NOW(), NOW(), 0);

SELECT setval('eh_mcp_tool_id_seq', 24);


-- =============================================================================
-- 使用说明
-- =============================================================================
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
--    SYSTEM - 系统级资源，平台全局可见
--    DEPT   - 部门级资源，仅本部门及子部门用户可用
--    USER   - 个人级资源，仅 owner 本人可用
--
-- 5. MCP transport_type 说明：
--    - UPLOADED：上传 zip 包，本地 STDIO 模式部署
--    - REMOTE：填写远程 URL，SSE 模式直连
--
-- 6. MCP tool_scope 说明：
--    - SYSTEM：所有可访问该 MCP 的用户都可见
--    - DEPT：仅指定部门可见
--    - USER：仅指定用户可见
--    - GRANT：需单独授权（查 eh_mcp_tool_grant 表）
--
-- 7. eh_resource_grant 扩展说明：
--    - user_id：USER 级别授权必填
--    - dept_id：DEPT 级别授权必填
--    - role_id：ROLE 级别授权必填
--    - 三者至少填写一个
--
-- 8. MCP Instance 状态机：
--    STARTING -> RUNNING -> STOPPING -> STOPPED
--                   |
--                   v
--                ERROR (心跳失败)
--    每个 MCP Config 同时只能有一个 RUNNING 状态的实例
--
-- =============================================================================
ALTER TABLE eh_mcp_config ALTER COLUMN args TYPE VARCHAR(1000);
ALTER TABLE eh_mcp_config ALTER COLUMN env TYPE TEXT;

ALTER TABLE eh_mcp_tool ALTER COLUMN input_schema TYPE TEXT;
