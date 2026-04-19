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
-- ============================================================================
-- EvolveHub v1.0 - 用户历史对话管理表结构（最终修正版）
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 一、对话会话表 (eh_chat_session)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_chat_session (
    id                      BIGINT PRIMARY KEY,
    user_id                 BIGINT       NOT NULL,
    title                   VARCHAR(200),
    model_config_id         BIGINT,
    sys_prompt              TEXT,
    total_prompt_tokens     INTEGER DEFAULT 0,
    total_completion_tokens INTEGER DEFAULT 0,
    total_tokens            INTEGER DEFAULT 0,
    message_count           INTEGER DEFAULT 0,
    context_summary         TEXT,
    dept_id                 BIGINT,
    create_by               BIGINT,
    create_time             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted                 INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_chat_session IS 'AI对话会话表';
COMMENT ON COLUMN eh_chat_session.dept_id IS '部门ID（冗余字段，便于数据权限过滤）';
COMMENT ON COLUMN eh_chat_session.create_by IS '创建者ID（=user_id，用于data_scope=4本人数据过滤）';

-- 索引
CREATE INDEX IF NOT EXISTS idx_session_user_id ON eh_chat_session(user_id);
CREATE INDEX IF NOT EXISTS idx_session_dept_id ON eh_chat_session(dept_id);
CREATE INDEX IF NOT EXISTS idx_session_create_by ON eh_chat_session(create_by);
CREATE INDEX IF NOT EXISTS idx_session_create_time ON eh_chat_session(create_time DESC);
CREATE INDEX IF NOT EXISTS idx_session_update_time ON eh_chat_session(update_time DESC);
CREATE INDEX IF NOT EXISTS idx_session_deleted ON eh_chat_session(deleted);

-- 外键
ALTER TABLE eh_chat_session ADD CONSTRAINT fk_session_user
    FOREIGN KEY (user_id) REFERENCES eh_user(id) ON DELETE CASCADE;

ALTER TABLE eh_chat_session ADD CONSTRAINT fk_session_dept
    FOREIGN KEY (dept_id) REFERENCES eh_dept(id) ON DELETE SET NULL;

-- ----------------------------------------------------------------------------
-- 二、对话消息表 (eh_chat_message)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_chat_message (
    id                BIGINT PRIMARY KEY,
    session_id        BIGINT       NOT NULL,
    role              VARCHAR(20)  NOT NULL,
    content           TEXT,
    tool_calls        TEXT,
    tool_call_id      VARCHAR(100),
    model_name        VARCHAR(100),
    prompt_tokens     INTEGER DEFAULT 0,
    completion_tokens INTEGER DEFAULT 0,
    total_tokens      INTEGER DEFAULT 0,
    finish_reason     VARCHAR(20),
    duration_ms       INTEGER,
    create_by         BIGINT,
    create_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted           INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_chat_message IS 'AI对话消息表';

-- 索引
CREATE INDEX IF NOT EXISTS idx_message_session_id ON eh_chat_message(session_id);
CREATE INDEX IF NOT EXISTS idx_message_role ON eh_chat_message(role);
CREATE INDEX IF NOT EXISTS idx_message_create_time ON eh_chat_message(create_time);
CREATE INDEX IF NOT EXISTS idx_message_deleted ON eh_chat_message(deleted);

-- 全文搜索索引
CREATE INDEX IF NOT EXISTS idx_message_content_gin ON eh_chat_message
    USING gin(to_tsvector('simple', content));

-- 外键
ALTER TABLE eh_chat_message ADD CONSTRAINT fk_message_session
    FOREIGN KEY (session_id) REFERENCES eh_chat_session(id) ON DELETE CASCADE;

-- ----------------------------------------------------------------------------
-- 三、Memory 长期记忆主表 (eh_agent_memory_record)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_agent_memory_record (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64),
    dept_id BIGINT,
    message_id VARCHAR(64),
    memory_key VARCHAR(128) NOT NULL,
    memory_type VARCHAR(32),
    source_kind VARCHAR(32) NOT NULL,
    memory_kind VARCHAR(32),
    role VARCHAR(32),
    model_config_id BIGINT,
    embedding_model_id BIGINT,
    vector_doc_id VARCHAR(128),
    object_id BIGINT,
    round_start_no INTEGER,
    round_end_no INTEGER,
    archive_object_id BIGINT,
    excerpt TEXT,
    importance NUMERIC(5, 3) DEFAULT 0.000,
    last_access_time TIMESTAMP,
    last_activated_round_no INTEGER,
    sleep_after_round_no INTEGER,
    create_by BIGINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_eh_agent_memory_record_user_source_key
    ON eh_agent_memory_record (user_id, source_kind, memory_key) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_record_user_source_update
    ON eh_agent_memory_record (user_id, source_kind, update_time DESC) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_record_user_session_kind
    ON eh_agent_memory_record (user_id, session_id, memory_kind) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_record_user_vector_doc
    ON eh_agent_memory_record (user_id, vector_doc_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_record_user_memory_key
    ON eh_agent_memory_record (user_id, memory_key) WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 四、Memory 对象索引表 (eh_agent_memory_object)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_agent_memory_object (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64),
    object_type VARCHAR(64) NOT NULL,
    bucket VARCHAR(128) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    content_type VARCHAR(128),
    checksum VARCHAR(128),
    size_bytes BIGINT,
    version_no INTEGER DEFAULT 1,
    source_record_id BIGINT,
    create_by BIGINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_eh_agent_memory_object_object_key
    ON eh_agent_memory_object (object_key) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_object_user_session
    ON eh_agent_memory_object (user_id, session_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_object_source_record
    ON eh_agent_memory_object (source_record_id) WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 五、Memory 用户画像索引表 (eh_agent_memory_profile)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_agent_memory_profile (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_object_id BIGINT,
    profile_summary TEXT,
    name VARCHAR(128),
    department VARCHAR(128),
    preferred_language VARCHAR(64),
    preferred_model VARCHAR(128),
    tool_preference VARCHAR(256),
    last_extracted_time TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_eh_agent_memory_profile_user_id
    ON eh_agent_memory_profile (user_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_profile_profile_object
    ON eh_agent_memory_profile (profile_object_id) WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 六、Memory 会话元数据表 (eh_agent_memory_session_meta)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_agent_memory_session_meta (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    session_key VARCHAR(128) NOT NULL,
    model_name VARCHAR(128),
    message_count INTEGER NOT NULL DEFAULT 0,
    last_summary_object_id BIGINT,
    transcript_object_id BIGINT,
    current_round_no INTEGER NOT NULL DEFAULT 0,
    last_compacted_round_no INTEGER NOT NULL DEFAULT 0,
    last_snapshot_object_id BIGINT,
    status VARCHAR(32),
    expire_time TIMESTAMP,
    last_message_time TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_eh_agent_memory_session_meta_user_session
    ON eh_agent_memory_session_meta (user_id, session_id) WHERE deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_eh_agent_memory_session_meta_session_key
    ON eh_agent_memory_session_meta (session_key) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_session_meta_user_status
    ON eh_agent_memory_session_meta (user_id, status) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_eh_agent_memory_session_meta_expire_time
    ON eh_agent_memory_session_meta (expire_time) WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 七、用户结构化记忆表 (user_memory)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS user_memory (
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    dept_id       BIGINT,
    memory_key    VARCHAR(128) NOT NULL,
    memory_type   VARCHAR(32)  NOT NULL,
    content       TEXT         NOT NULL,
    importance    NUMERIC(5, 3) DEFAULT 0.000,
    vector_doc_id VARCHAR(128),
    session_id    VARCHAR(64),
    create_by     BIGINT,
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    deleted       INTEGER      DEFAULT 0
);

COMMENT ON TABLE user_memory IS '用户结构化记忆表';
COMMENT ON COLUMN user_memory.user_id IS '用户ID';
COMMENT ON COLUMN user_memory.dept_id IS '部门ID';
COMMENT ON COLUMN user_memory.memory_key IS '记忆键，同一用户下唯一';
COMMENT ON COLUMN user_memory.memory_type IS '记忆类型：preference/fact/tool_config';
COMMENT ON COLUMN user_memory.content IS '结构化记忆内容';
COMMENT ON COLUMN user_memory.importance IS '重要性评分，范围0~1';
COMMENT ON COLUMN user_memory.vector_doc_id IS 'Milvus向量文档ID';
COMMENT ON COLUMN user_memory.session_id IS '来源会话ID';

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_memory_user_key
    ON user_memory(user_id, memory_key) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_user_memory_user_type_update
    ON user_memory(user_id, memory_type, update_time DESC) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_user_memory_user_vector_doc
    ON user_memory(user_id, vector_doc_id) WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 八、权限数据插入
-- ----------------------------------------------------------------------------

INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (28, 0, '对话历史管理', 'app:chat-history', 'MENU', '/app/chat-history', 'MessageSquare', 15, 1, NOW(), NOW(), 'linear-gradient(135deg, #0A84FF, #5E5CE6)', 1200, 700, 900, 500, -1, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (29, 0, '对话历史查看', 'chat-history:list', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (30, 0, '对话历史搜索', 'chat-history:search', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (31, 0, '对话历史导出', 'chat-history:export', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 超级管理员（SUPER_ADMIN）- 全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 28, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 29, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 30, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 31, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 部门负责人（DEPT_HEAD）- 查看和搜索权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 3, 28, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 29, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 30, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 高层领导（LEADER）- 查看和搜索权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 2, 28, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 29, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 30, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 九、测试数据
-- ----------------------------------------------------------------------------

INSERT INTO eh_chat_session (id, user_id, title, model_config_id, sys_prompt, total_prompt_tokens, total_completion_tokens, total_tokens, message_count, dept_id, create_by, create_time, update_time, deleted)
VALUES
    (1, 1, '如何使用Spring Boot集成Redis？', 1, '你是一个专业的Java开发助手，擅长Spring Boot相关技术。', 35, 1200, 1235, 8, 1, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 0),
    (2, 1, 'Vue3组件开发最佳实践', 2, '你是一个前端开发专家，精通Vue3生态系统。', 25, 800, 825, 5, 1, 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 0),
    (3, 1, 'PostgreSQL性能优化建议', 1, '你是一个数据库优化专家，精通PostgreSQL。', 40, 2300, 2340, 12, 1, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 0)
ON CONFLICT DO NOTHING;

INSERT INTO eh_chat_message (id, session_id, role, content, model_name, prompt_tokens, completion_tokens, total_tokens, finish_reason, duration_ms, create_time, deleted)
VALUES
    (1, 1, 'user', '如何使用Spring Boot集成Redis？需要配置文件和代码示例。', 'GLM-4-Plus', 35, 0, 35, NULL, NULL, NOW() - INTERVAL '1 day', 0),
    (2, 1, 'assistant', '# Spring Boot 集成 Redis

## 1. 添加依赖

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 2. 配置Redis连接

在 `application.yml` 中配置：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 3000ms
    jedis:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

## 3. 使用RedisTemplate

```java
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
```

这样就可以在Spring Boot中使用Redis了！', 'GLM-4-Plus', 450, 784, 1234, 'stop', 1500, NOW() - INTERVAL '1 day' + INTERVAL '1 second', 0),
    (3, 1, 'user', '能给我一个完整示例项目吗？包含启动类和配置类', 'GLM-4-Plus', 28, 0, 28, NULL, NULL, NOW() - INTERVAL '1 day' + INTERVAL '2 seconds', 0),
    (4, 1, 'assistant', '当然！这里是一个完整的Spring Boot Redis示例项目结构：

```
src/main/java/com/example/redis/
├── RedisApplication.java          # 启动类
├── config/
│   └── RedisConfig.java           # Redis配置
├── service/
│   └── RedisService.java          # Redis服务
└── controller/
    └── RedisController.java       # 控制器
```

详细的代码实现可以在GitHub上找到完整项目示例。', 'GLM-4-Plus', 380, 560, 940, 'stop', 1200, NOW() - INTERVAL '1 day' + INTERVAL '3 seconds', 0)
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------------------------
-- 十、用户对话统计视图
-- ----------------------------------------------------------------------------

CREATE OR REPLACE VIEW v_user_chat_stats AS
SELECT
    u.id AS user_id,
    u.username,
    u.nickname,
    u.dept_id,
    d.dept_name,
    COUNT(s.id) AS session_count,
    COALESCE(SUM(s.total_tokens), 0) AS total_tokens,
    COALESCE(SUM(s.message_count), 0) AS total_messages,
    MAX(s.update_time) AS last_active_time
FROM eh_user u
LEFT JOIN eh_chat_session s ON u.id = s.user_id AND s.deleted = 0
LEFT JOIN eh_dept d ON u.dept_id = d.id
WHERE u.deleted = 0
GROUP BY u.id, u.username, u.nickname, u.dept_id, d.dept_name;

COMMENT ON VIEW v_user_chat_stats IS '用户对话统计视图，用于管理员对话历史功能的用户列表展示';

-- ============================================================================
-- 十一、Token消费统计表 (eh_chat_token_usage)
-- ============================================================================

CREATE TABLE IF NOT EXISTS eh_chat_token_usage (
    id                BIGINT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    model_config_id   BIGINT       NOT NULL,
    usage_date        DATE         NOT NULL,
    request_count     INTEGER      DEFAULT 0,
    prompt_tokens     INTEGER      DEFAULT 0,
    completion_tokens INTEGER      DEFAULT 0,
    total_tokens      INTEGER      DEFAULT 0,
    dept_id           BIGINT,
    create_by         BIGINT,
    create_time       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    deleted           INTEGER      DEFAULT 0
);

COMMENT ON TABLE eh_chat_token_usage IS 'Token消费日报表';
COMMENT ON COLUMN eh_chat_token_usage.user_id IS '用户ID';
COMMENT ON COLUMN eh_chat_token_usage.model_config_id IS '模型配置ID';
COMMENT ON COLUMN eh_chat_token_usage.usage_date IS '统计日期';
COMMENT ON COLUMN eh_chat_token_usage.request_count IS '当日请求次数';
COMMENT ON COLUMN eh_chat_token_usage.prompt_tokens IS '当日prompt token数';
COMMENT ON COLUMN eh_chat_token_usage.completion_tokens IS '当日completion token数';
COMMENT ON COLUMN eh_chat_token_usage.total_tokens IS '当日总token数';
COMMENT ON COLUMN eh_chat_token_usage.dept_id IS '部门ID（冗余字段，便于数据权限过滤）';

-- 唯一索引：同一用户同一模型同一日期只能有一条记录
CREATE UNIQUE INDEX IF NOT EXISTS uk_token_usage ON eh_chat_token_usage(user_id, model_config_id, usage_date) WHERE deleted = 0;

-- 查询索引
CREATE INDEX IF NOT EXISTS idx_token_usage_user_date ON eh_chat_token_usage(user_id, usage_date DESC);
CREATE INDEX IF NOT EXISTS idx_token_usage_dept_date ON eh_chat_token_usage(dept_id, usage_date DESC);
CREATE INDEX IF NOT EXISTS idx_token_usage_create_by ON eh_chat_token_usage(create_by);
CREATE INDEX IF NOT EXISTS idx_token_usage_deleted ON eh_chat_token_usage(deleted);

-- 外键
ALTER TABLE eh_chat_token_usage ADD CONSTRAINT fk_token_usage_user
    FOREIGN KEY (user_id) REFERENCES eh_user(id) ON DELETE CASCADE;

ALTER TABLE eh_chat_token_usage ADD CONSTRAINT fk_token_usage_model
    FOREIGN KEY (model_config_id) REFERENCES eh_model_config(id) ON DELETE CASCADE;

ALTER TABLE eh_chat_token_usage ADD CONSTRAINT fk_token_usage_dept
    FOREIGN KEY (dept_id) REFERENCES eh_dept(id) ON DELETE SET NULL;

-- ============================================================================
-- 八、用量统计菜单权限
-- ============================================================================

-- 用量统计 MENU
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (32, 0, '用量统计', 'app:token-usage', 'MENU', '/app/token-usage', 'BarChart3', 16, 1, NOW(), NOW(), 'linear-gradient(135deg, #FF9F0A, #FF6B00)', 900, 600, 650, 400, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- 用量统计按钮权限
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (33, 0, '用量统计查看', 'token-usage:records', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (34, 0, '用量统计管理', 'token-usage:admin', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 超级管理员（SUPER_ADMIN）- 用量统计全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 32, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 33, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 34, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 普通管理员（ADMIN）- 对话历史 + 用量统计权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 4, 28, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 29, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 30, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 32, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 33, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 34, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 高层领导（LEADER）- 用量统计查看
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 2, 32, NOW(), NOW()),
    (nextval('seq_role_permission'), 2, 33, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 部门负责人（DEPT_HEAD）- 用量统计查看
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 3, 32, NOW(), NOW()),
    (nextval('seq_role_permission'), 3, 33, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================================
-- S3 文件服务器管理
-- ============================================================================

-- S3 文件管理菜单
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (35, 0, 'S3文件管理', 'app:s3-browser', 'MENU', '/app/s3-browser', 'HardDrive', 17, 1, NOW(), NOW(), 'linear-gradient(135deg, #64D2FF, #5AC8FA)', 1100, 700, 800, 500, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- S3 按钮权限
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (36, 0, 'S3查看', 's3:list', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (37, 0, 'S3上传', 's3:upload', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (38, 0, 'S3删除', 's3:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 超级管理员（SUPER_ADMIN）- S3 全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 35, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 36, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 37, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 38, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================================
-- 环境变量管理 (eh_env_var)
-- ============================================================================

CREATE TABLE IF NOT EXISTS eh_env_var (
    id            BIGINT PRIMARY KEY,
    var_key       VARCHAR(100) NOT NULL UNIQUE,
    var_value     TEXT,
    var_group     VARCHAR(50)  DEFAULT 'DEFAULT',
    description   VARCHAR(255),
    is_sensitive  INTEGER DEFAULT 0,
    status        INTEGER DEFAULT 1,
    sort          INTEGER DEFAULT 0,
    create_by     BIGINT,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_env_var IS '全局环境变量表';
COMMENT ON COLUMN eh_env_var.var_key IS '变量名（唯一键）';
COMMENT ON COLUMN eh_env_var.var_value IS '变量值';
COMMENT ON COLUMN eh_env_var.var_group IS '分组（OPENAI/MODELSCOPE/S3/CUSTOM/DEFAULT）';
COMMENT ON COLUMN eh_env_var.description IS '描述说明';
COMMENT ON COLUMN eh_env_var.is_sensitive IS '是否敏感（1=敏感值，前端默认隐藏）';
COMMENT ON COLUMN eh_env_var.status IS '状态（1=启用 0=禁用）';
COMMENT ON COLUMN eh_env_var.sort IS '排序';

CREATE INDEX IF NOT EXISTS idx_env_var_key ON eh_env_var(var_key);
CREATE INDEX IF NOT EXISTS idx_env_var_group ON eh_env_var(var_group);
CREATE INDEX IF NOT EXISTS idx_env_var_status ON eh_env_var(status);

-- 环境变量菜单
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (39, 0, '环境变量', 'app:env-var', 'MENU', '/app/env-var', 'Settings', 18, 1, NOW(), NOW(), 'linear-gradient(135deg, #30D158, #0A84FF)', 900, 600, 700, 450, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- 环境变量按钮权限
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (40, 0, '查看变量', 'env:list', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (41, 0, '新增变量', 'env:create', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (42, 0, '修改变量', 'env:update', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (43, 0, '删除变量', 'env:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 超级管理员（SUPER_ADMIN）- 环境变量全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 39, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 40, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 41, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 42, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 43, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- =============================================================================
-- Tool Guard 表
-- =============================================================================

-- ----------------------------------------------------------------------------
-- 一、工具守卫规则表 (eh_tool_guard_rule)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_tool_guard_rule (
    id              BIGINT PRIMARY KEY,
    rule_id         VARCHAR(64)  NOT NULL UNIQUE,
    name            VARCHAR(128) NOT NULL,
    tools           VARCHAR(512) NOT NULL,
    params          VARCHAR(512) NOT NULL,
    category        VARCHAR(32)  NOT NULL,
    severity        VARCHAR(16)  NOT NULL,
    patterns        TEXT         NOT NULL,
    exclude_patterns TEXT,
    description     VARCHAR(256),
    remediation     VARCHAR(256),
    is_builtin      INTEGER DEFAULT 0,
    enabled         INTEGER DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_tool_guard_rule IS '工具守卫规则表';
COMMENT ON COLUMN eh_tool_guard_rule.rule_id IS '规则唯一标识，如 TOOL_CMD_DANGEROUS_RM';
COMMENT ON COLUMN eh_tool_guard_rule.name IS '规则中文名称';
COMMENT ON COLUMN eh_tool_guard_rule.tools IS '适用的工具列表，JSON数组';
COMMENT ON COLUMN eh_tool_guard_rule.params IS '需要检查的参数名，JSON数组';
COMMENT ON COLUMN eh_tool_guard_rule.category IS '威胁类别';
COMMENT ON COLUMN eh_tool_guard_rule.severity IS 'CRITICAL/HIGH/MEDIUM/LOW/INFO';
COMMENT ON COLUMN eh_tool_guard_rule.patterns IS '正则表达式列表，JSON数组';
COMMENT ON COLUMN eh_tool_guard_rule.exclude_patterns IS '排除正则，JSON数组';
COMMENT ON COLUMN eh_tool_guard_rule.is_builtin IS '是否内置规则（1=内置，0=自定义）';
COMMENT ON COLUMN eh_tool_guard_rule.enabled IS '是否启用（1=启用，0=禁用）';

CREATE INDEX IF NOT EXISTS idx_tool_guard_rule_id ON eh_tool_guard_rule(rule_id);
CREATE INDEX IF NOT EXISTS idx_tool_guard_rule_enabled ON eh_tool_guard_rule(enabled);
CREATE INDEX IF NOT EXISTS idx_tool_guard_rule_tools ON eh_tool_guard_rule(tools);

-- ----------------------------------------------------------------------------
-- 二、工具守卫阻断历史表 (eh_tool_guard_history)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_tool_guard_history (
    id              BIGINT PRIMARY KEY,
    session_id      VARCHAR(64),
    user_id         BIGINT,
    user_nickname   VARCHAR(50),
    tool_name       VARCHAR(64)  NOT NULL,
    param_name      VARCHAR(64),
    matched_rule_id VARCHAR(64),
    matched_value   TEXT,
    severity        VARCHAR(16),
    action          VARCHAR(16)  NOT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  eh_tool_guard_history IS '工具守卫阻断历史表';
COMMENT ON COLUMN eh_tool_guard_history.session_id IS '对话会话ID';
COMMENT ON COLUMN eh_tool_guard_history.user_id IS '触发用户ID';
COMMENT ON COLUMN eh_tool_guard_history.tool_name IS '工具名称';
COMMENT ON COLUMN eh_tool_guard_history.matched_rule_id IS '匹配的规则ID';
COMMENT ON COLUMN eh_tool_guard_history.matched_value IS '匹配到的危险内容（脱敏）';
COMMENT ON COLUMN eh_tool_guard_history.action IS 'BLOCKED/WARNED';

CREATE INDEX IF NOT EXISTS idx_tool_guard_history_session ON eh_tool_guard_history(session_id);
CREATE INDEX IF NOT EXISTS idx_tool_guard_history_user ON eh_tool_guard_history(user_id);
CREATE INDEX IF NOT EXISTS idx_tool_guard_history_time ON eh_tool_guard_history(create_time);
CREATE INDEX IF NOT EXISTS idx_tool_guard_history_severity ON eh_tool_guard_history(severity);

-- 为 tool_guard_history 增加 guard_type 字段（TOOL=工具规则检查，FILE=文件规则检查）
ALTER TABLE eh_tool_guard_history ADD COLUMN IF NOT EXISTS guard_type VARCHAR(16) DEFAULT 'TOOL';
COMMENT ON COLUMN eh_tool_guard_history.guard_type IS '守卫类型：TOOL=工具规则，FILE=文件规则';

-- ----------------------------------------------------------------------------
-- 三、工具守卫全局配置表 (eh_tool_guard_config)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_tool_guard_config (
    id              BIGINT PRIMARY KEY DEFAULT 1,
    enabled         INTEGER DEFAULT 1,
    guarded_tools   TEXT,
    denied_tools    TEXT,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  eh_tool_guard_config IS '工具守卫全局配置表';
COMMENT ON COLUMN eh_tool_guard_config.enabled IS '整体开关（1=启用，0=禁用）';
COMMENT ON COLUMN eh_tool_guard_config.guarded_tools IS '受保护的工具列表，JSON数组，null表示全部';
COMMENT ON COLUMN eh_tool_guard_config.denied_tools IS '直接拒绝的工具列表，JSON数组';

-- 初始化默认配置
INSERT INTO eh_tool_guard_config (id, enabled, guarded_tools, denied_tools, update_time)
VALUES (1, 1, '["execute_shell_command"]', '[]', NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 四、预置内置规则
-- ----------------------------------------------------------------------------

-- TOOL_CMD_DANGEROUS_RM - 危险删除命令
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1001, 'TOOL_CMD_DANGEROUS_RM', '危险删除命令', '["execute_shell_command"]', '["command"]', 'command_injection', 'HIGH', '["\\\\brm\\\\b", "\\\\bdel\\\\b", "\\\\bRemove-Item\\\\b"]', '["^\\\\s*#"]', 'Shell command contains rm/del which may cause data loss', 'Confirm with the user before removing files or directories', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_DANGEROUS_MV - 危险移动命令
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1002, 'TOOL_CMD_DANGEROUS_MV', '危险移动命令', '["execute_shell_command"]', '["command"]', 'command_injection', 'HIGH', '["\\\\bmv\\\\b"]', NULL, 'Shell command contains mv which may move or overwrite files unexpectedly', 'Confirm with the user before moving or renaming files', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_FS_DESTRUCTION - 文件系统破坏
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1003, 'TOOL_CMD_FS_DESTRUCTION', '文件系统破坏', '["execute_shell_command"]', '["command"]', 'command_injection', 'CRITICAL', '["\\\\bmkfs(\\\\.[a-zA-Z0-9_]+)?\\\\b", "\\\\bmke2fs\\\\b", "\\\\bdd\\\\s+.*of=\\\\/dev\\\\/", ">\\\\s*\\\\/dev\\\\/(sd[a-z][0-9]*|vd[a-z][0-9]*|nvme\\\\d+n\\\\d+(p\\\\d+)?)"]', NULL, 'Detects low-level disk formatting or wiping commands', 'Block operation. Agents should not format or overwrite raw block devices.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_DOS_FORK_BOMB - 拒绝服务攻击
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1004, 'TOOL_CMD_DOS_FORK_BOMB', '拒绝服务攻击', '["execute_shell_command"]', '["command"]', 'resource_abuse', 'CRITICAL', '[":\\\\s*\\\\(\\\\s*\\\\)\\\\s*\\\\{\\\\s*:\\\\s*\\\\|\\\\s*:\\\\s*&\\\\s*\\\\}\\\\s*;\\\\s*:", "\\\\bkill\\\\s+-9\\\\s+(-1\\\\b|1\\\\b)"]', NULL, 'Detects classic Bash fork bombs and mass process termination', 'Block immediately. These commands will crash the host system.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_PIPE_TO_SHELL - 管道注入执行
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1005, 'TOOL_CMD_PIPE_TO_SHELL', '管道注入执行', '["execute_shell_command"]', '["command"]', 'code_execution', 'CRITICAL', '["\\\\b(curl|wget)\\\\b\\\\s+.*\\\\|.*\\\\b(bash|sh|zsh|ash|dash)\\\\b"]', NULL, 'Detects curl | bash patterns used to download and immediately execute remote payloads', 'Confirm with user. Agents should inspect scripts before executing them.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_REVERSE_SHELL - 反向Shell
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1006, 'TOOL_CMD_REVERSE_SHELL', '反向Shell', '["execute_shell_command"]', '["command"]', 'network_abuse', 'CRITICAL', '["\\\\/dev\\\\/(tcp|udp)\\\\/", "\\\\bnc\\\\s+.*-e\\\\s*\\\\S+", "\\\\bncat\\\\s+.*-e\\\\s*\\\\S+", "\\\\bsocat\\\\s+.*EXEC:"]', NULL, 'Detects attempts to establish reverse shells or unauthorized network tunnels', 'Block operation. Agents do not need to bind interactive shells to network sockets.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_SYSTEM_TAMPERING - 系统篡改
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1007, 'TOOL_CMD_SYSTEM_TAMPERING', '系统篡改', '["execute_shell_command"]', '["command"]', 'sensitive_file_access', 'HIGH', '["\\\\bcrontab\\\\b", "\\\\bauthorized_keys\\\\b", "\\\\/etc\\\\/sudoers", "\\\\/etc\\\\/crontab"]', NULL, 'Detects access to cron jobs, SSH keys, or sudo permissions', 'Confirm with user. Treat any access to credential and scheduling files as sensitive.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_UNSAFE_PERMISSIONS - 危险权限修改
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1008, 'TOOL_CMD_UNSAFE_PERMISSIONS', '危险权限修改', '["execute_shell_command"]', '["command"]', 'privilege_escalation', 'HIGH', '["\\\\bchmod\\\\s+-[a-zA-Z]*R[a-zA-Z]*\\\\s+(777|a\\\\+rwx)\\\\s+\\\\/", "\\\\bchattr\\\\s+\\\\+i"]', NULL, 'Detects global permission downgrades (chmod 777) or setting immutable flags', 'Prompt for confirmation. Suggest least-privilege permission models.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_OBFUSCATED_EXEC - 混淆命令执行
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1009, 'TOOL_CMD_OBFUSCATED_EXEC', '混淆命令执行', '["execute_shell_command"]', '["command"]', 'code_execution', 'HIGH', '["\\\\bbase64\\\\s+(-d|--decode)\\\\s*\\\\|\\\\s*\\\\b(bash|sh|zsh)\\\\b"]', NULL, 'Detects execution of base64 encoded strings passed directly to a shell interpreter', 'Block execution. Agents should use plain text commands.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_SYSTEM_REBOOT - 系统重启/关机
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1010, 'TOOL_CMD_SYSTEM_REBOOT', '系统重启/关机', '["execute_shell_command"]', '["command"]', 'resource_abuse', 'CRITICAL', '["\\\\b(reboot|shutdown|halt|poweroff)\\\\b", "\\\\binit\\\\s+(0|6)\\\\b", "\\\\btelinit\\\\s+(0|6)\\\\b", "\\\\bShutdown-Computer\\\\b", "\\\\bRestart-Computer\\\\b"]', NULL, 'Detects system reboot or shutdown commands that will terminate the host system', 'Block operation. Agents should not restart or shutdown the system.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_SERVICE_RESTART - 服务管理命令
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1011, 'TOOL_CMD_SERVICE_RESTART', '服务管理命令', '["execute_shell_command"]', '["command"]', 'resource_abuse', 'HIGH', '["\\\\bsystemctl\\\\s+(restart|stop|start|reload|kill)\\\\b", "\\\\bservice\\\\s+\\\\S+\\\\s+(restart|stop|start|reload)\\\\b", "\\\\b(sc|net)\\\\s+(start|stop|restart)\\\\b", "\\\\blaunchctl\\\\s+(load|unload|stop|start|kickstart|kill)\\\\b", "\\\\brc-service\\\\s+(restart|stop|start)\\\\b"]', NULL, 'Detects service management commands that can disrupt system services', 'Confirm with user. Restarting services may cause downtime or data loss.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_PROCESS_KILL - 进程终止命令
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1012, 'TOOL_CMD_PROCESS_KILL', '进程终止命令', '["execute_shell_command"]', '["command"]', 'resource_abuse', 'HIGH', '["\\\\b(pkill|killall)\\\\b", "\\\\bkill\\\\s+(-(9|KILL|15|TERM|1|HUP|2|INT)\\\\s+)?[^-\\\\s]", "\\\\btaskkill\\\\s+\\\\/F\\\\b", "\\\\bStop-Process\\\\b.*-Force\\\\b"]', '["kill\\\\s+\\\\\\$\\\\\\$"]', 'Detects process termination commands that may kill critical processes', 'Confirm with user. Killing processes may cause data loss or system instability.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_PRIVILEGE_ESCALATION - 权限提升
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1013, 'TOOL_CMD_PRIVILEGE_ESCALATION', '权限提升', '["execute_shell_command"]', '["command"]', 'privilege_escalation', 'CRITICAL', '["\\\\bsudo\\\\s+", "\\\\bsu\\\\b", "\\\\bdoas\\\\s+", "\\\\bpkexec\\\\b", "\\\\brunas\\\\s+\\\\/user:"]', '["^\\\\s*#"]', 'Detects privilege escalation attempts using sudo, su, doas, pkexec, or runas', 'Block operation. Agents should not execute commands with elevated privileges.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_IFS_INJECTION - IFS变量注入
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1014, 'TOOL_CMD_IFS_INJECTION', 'IFS变量注入', '["execute_shell_command"]', '["command"]', 'code_execution', 'HIGH', '["\\\\$IFS(?![A-Za-z0-9_])", "\\\\$\\\\{[^}]*IFS"]', '["^\\\\s*#"]', 'Command uses $IFS variable which could bypass security validation', 'Reject commands containing IFS manipulation.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_CONTROL_CHARS - 控制字符注入
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1015, 'TOOL_CMD_CONTROL_CHARS', '控制字符注入', '["execute_shell_command"]', '["command"]', 'code_execution', 'CRITICAL', '["[\\\\x00-\\\\x08\\\\x0b\\\\x0c\\\\x0e-\\\\x1f\\\\x7f]"]', NULL, 'Command contains non-printable control characters that could bypass security checks', 'Block commands containing control characters.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_UNICODE_WHITESPACE - Unicode空白注入
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1016, 'TOOL_CMD_UNICODE_WHITESPACE', 'Unicode空白注入', '["execute_shell_command"]', '["command"]', 'code_execution', 'HIGH', '["[\\\\u00a0\\\\u1680\\\\u2000-\\\\u200a\\\\u2028\\\\u2029\\\\u202f\\\\u205f\\\\u3000\\\\ufeff]"]', NULL, 'Command contains Unicode whitespace characters that could cause parsing inconsistencies', 'Block commands containing non-ASCII whitespace.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_PROC_ENVIRON - 进程环境读取
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1017, 'TOOL_CMD_PROC_ENVIRON', '进程环境读取', '["execute_shell_command"]', '["command"]', 'sensitive_file_access', 'HIGH', '["\\\\/proc\\\\/(?:self|\\\\d+)\\\\/environ(?:\\\\b|$)"]', '["^\\\\s*#"]', 'Command accesses /proc/*/environ which could expose sensitive environment variables', 'Block access to process environment files.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_JQ_SYSTEM - jq命令注入
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1018, 'TOOL_CMD_JQ_SYSTEM', 'jq命令注入', '["execute_shell_command"]', '["command"]', 'code_execution', 'HIGH', '["\\\\bjq\\\\b.*\\\\bsystem\\\\s*\\\\("]', '["^\\\\s*#"]', 'jq command contains system() function which can execute arbitrary shell commands', 'Block jq commands using system().', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_JQ_FILE_FLAGS - jq危险文件标志
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1019, 'TOOL_CMD_JQ_FILE_FLAGS', 'jq危险文件标志', '["execute_shell_command"]', '["command"]', 'code_execution', 'HIGH', '["\\\\bjq\\\\b.*(?:\\\\s-f\\\\b|\\\\s--from-file\\\\b|\\\\s--rawfile\\\\b|\\\\s--slurpfile\\\\b|\\\\s-L\\\\b|\\\\s--library-path\\\\b)"]', '["^\\\\s*#"]', 'jq command uses flags that could read arbitrary files or execute external code', 'Confirm with user. These jq flags can access files outside the intended scope.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- TOOL_CMD_ZSH_DANGEROUS - Zsh危险命令
INSERT INTO eh_tool_guard_rule (id, rule_id, name, tools, params, category, severity, patterns, exclude_patterns, description, remediation, is_builtin, enabled, create_time, update_time)
VALUES (1020, 'TOOL_CMD_ZSH_DANGEROUS', 'Zsh危险命令', '["execute_shell_command"]', '["command"]', 'code_execution', 'HIGH', '["\\\\bzmodload\\\\b", "\\\\bemulate\\\\b(?:\\\\s+-\\\\S+)*\\\\s+-c\\\\b", "\\\\b(sysopen|sysread|syswrite|sysseek)\\\\b", "\\\\b(zpty|ztcp|zsocket)\\\\b", "\\\\bzf_(rm|mv|ln|chmod|chown|mkdir|rmdir|chgrp)\\\\b", "\\\\bfc\\\\b.*\\\\s-\\\\S*e"]', '["^\\\\s*#"]', 'Command uses Zsh-specific builtins that can bypass security checks', 'Block Zsh module/builtin commands.', 1, 1, NOW(), NOW())
ON CONFLICT (rule_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 五、菜单和权限
-- ----------------------------------------------------------------------------

-- Tool Guard 菜单
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (44, 0, 'Tool Guard', 'app:tool-guard', 'MENU', '/app/tool-guard', 'Shield', 19, 1, NOW(), NOW(), 'linear-gradient(135deg, #FF453A, #BF5AF2)', 1000, 700, 800, 500, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- Tool Guard 按钮权限
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (45, 0, '查看规则', 'tool-guard:list', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (46, 0, '新增规则', 'tool-guard:create', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (47, 0, '修改规则', 'tool-guard:update', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (48, 0, '删除规则', 'tool-guard:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (49, 0, '查看历史', 'tool-guard:history', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (50, 0, '清除历史', 'tool-guard:history:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (51, 0, '管理配置', 'tool-guard:manage', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 超级管理员（SUPER_ADMIN）- Tool Guard 全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 44, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 45, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 46, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 47, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 48, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 49, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 50, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 51, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================================
-- 四、File Guard — 敏感文件保护
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 敏感文件规则表 (eh_file_guard_rule)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_file_guard_rule (
    id              BIGINT PRIMARY KEY,
    rule_id         VARCHAR(64)  NOT NULL UNIQUE,
    name            VARCHAR(128) NOT NULL,
    path_pattern    VARCHAR(512) NOT NULL,
    path_type       VARCHAR(16)  NOT NULL,
    tools           TEXT,
    description     VARCHAR(512),
    remediation     VARCHAR(512),
    severity        VARCHAR(16)  NOT NULL DEFAULT 'HIGH',
    is_builtin      INTEGER      NOT NULL DEFAULT 0,
    enabled         INTEGER      NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         INTEGER      NOT NULL DEFAULT 0
);

COMMENT ON TABLE  eh_file_guard_rule IS '敏感文件保护规则表';
COMMENT ON COLUMN eh_file_guard_rule.rule_id IS '规则唯一标识';
COMMENT ON COLUMN eh_file_guard_rule.path_pattern IS '路径模式（支持目录路径、通配符）';
COMMENT ON COLUMN eh_file_guard_rule.path_type IS '路径类型：FILE=精确文件，DIRECTORY=目录，WILDCARD=通配符';
COMMENT ON COLUMN eh_file_guard_rule.tools IS '适用的工具列表，JSON数组，null表示全部工具';

CREATE INDEX IF NOT EXISTS idx_file_guard_rule_id ON eh_file_guard_rule(rule_id);
CREATE INDEX IF NOT EXISTS idx_file_guard_rule_enabled ON eh_file_guard_rule(enabled);

-- ----------------------------------------------------------------------------
-- 2. 敏感文件全局配置表 (eh_file_guard_config)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_file_guard_config (
    id              BIGINT PRIMARY KEY DEFAULT 1,
    enabled         INTEGER      NOT NULL DEFAULT 1,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  eh_file_guard_config IS '敏感文件保护全局配置表';
COMMENT ON COLUMN eh_file_guard_config.enabled IS '整体开关（1=启用，0=禁用）';

-- 初始化默认配置
INSERT INTO eh_file_guard_config (id, enabled, update_time)
VALUES (1, 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 3. 内置敏感文件规则（12条，借鉴 QwenPaw）
-- ----------------------------------------------------------------------------
INSERT INTO eh_file_guard_rule (id, rule_id, name, path_pattern, path_type, tools, description, remediation, severity, is_builtin, enabled)
VALUES
    (100, 'FG_SSH_KEYS', 'SSH 密钥目录', '~/.ssh/', 'DIRECTORY', NULL, 'SSH 密钥目录，包含私钥文件', '使用项目内的临时文件目录，不要操作 SSH 密钥', 'CRITICAL', 1, 1),
    (101, 'FG_PEM_KEY', 'PEM 私钥文件', '*.pem', 'WILDCARD', NULL, 'PEM 格式私钥文件', '使用项目内的证书文件，不要操作私钥', 'CRITICAL', 1, 1),
    (102, 'FG_KEY_FILE', '密钥文件', '*.key', 'WILDCARD', NULL, '密钥文件', '使用项目内的密钥文件，不要操作私钥', 'CRITICAL', 1, 1),
    (103, 'FG_ETC_PASSWD', '系统用户账户', '/etc/passwd', 'FILE', NULL, 'Linux 系统用户账户文件', '不要读取系统账户文件', 'HIGH', 1, 1),
    (104, 'FG_ETC_SHADOW', '系统密码哈希', '/etc/shadow', 'FILE', NULL, 'Linux 系统用户密码哈希文件', '不要读取系统密码文件', 'CRITICAL', 1, 1),
    (105, 'FG_SUDOERS', 'Sudo 配置', '/etc/sudoers', 'FILE', NULL, 'sudo 权限配置文件', '不要修改 sudo 配置', 'CRITICAL', 1, 1),
    (106, 'FG_WIN_SYS', 'Windows 系统配置', 'C:\Windows\System32\config\', 'DIRECTORY', NULL, 'Windows 注册表配置目录', '不要修改 Windows 系统配置', 'CRITICAL', 1, 1),
    (107, 'FG_WIN_USERS', 'Windows 用户目录', 'C:\Users\', 'DIRECTORY', NULL, 'Windows 用户主目录', '不要操作其他用户的文件', 'CRITICAL', 1, 1),
    (108, 'FG_ENV_FILE', '环境变量文件', '.env', 'FILE', NULL, '环境变量文件，可能包含敏感凭证', '不要读取 .env 文件，使用配置管理工具', 'HIGH', 1, 1),
    (109, 'FG_SECRETS_DIR', '密钥目录', 'secrets/', 'DIRECTORY', NULL, '密钥和敏感信息目录', '不要访问 secrets 目录，使用授权的密钥管理服务', 'CRITICAL', 1, 1),
    (110, 'FG_KUBECONFIG', 'Kubernetes 配置', '*kubeconfig*', 'WILDCARD', NULL, 'Kubernetes 集群配置文件', '不要访问 kubeconfig，使用 K8S SDK 或已配置的集群', 'HIGH', 1, 1),
    (111, 'FG_AWS_CREDS', 'AWS 凭证文件', '*aws*credential*', 'WILDCARD', NULL, 'AWS 访问凭证文件', '不要访问 AWS 凭证，使用 IAM 角色或环境变量', 'CRITICAL', 1, 1)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 4. File Guard 菜单和权限
-- ----------------------------------------------------------------------------
-- 桌面图标菜单
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES (52, 0, 'File Guard', 'app:file-guard', 'MENU', '/app/file-guard', 'FileSearch', 20, 1, NOW(), NOW(), 'linear-gradient(135deg, #FF453A, #FF6B6B)', 950, 650, 700, 450, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- File Guard 按钮权限
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (53, 0, '查看规则', 'file-guard:list', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (54, 0, '新增规则', 'file-guard:create', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (55, 0, '修改规则', 'file-guard:update', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (56, 0, '删除规则', 'file-guard:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (57, 0, '管理配置', 'file-guard:manage', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 超级管理员（SUPER_ADMIN）- File Guard 全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 52, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 53, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 54, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 55, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 56, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 57, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================================
-- 4.3 Security Scanner - 安全扫描白名单 + 被阻止历史
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 安全扫描白名单表 (eh_security_scanner_whitelist)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_security_scanner_whitelist (
    id          BIGINT PRIMARY KEY,
    skill_name  VARCHAR(255) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    added_by    BIGINT,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_security_scanner_whitelist IS '安全扫描白名单表';
COMMENT ON COLUMN eh_security_scanner_whitelist.skill_name  IS '技能名称';
COMMENT ON COLUMN eh_security_scanner_whitelist.content_hash IS '内容哈希（SHA-256）';
COMMENT ON COLUMN eh_security_scanner_whitelist.added_by    IS '添加人';

CREATE INDEX IF NOT EXISTS idx_whitelist_skill ON eh_security_scanner_whitelist(skill_name);
CREATE INDEX IF NOT EXISTS idx_whitelist_hash  ON eh_security_scanner_whitelist(content_hash);

-- ----------------------------------------------------------------------------
-- 2. 被阻止/告警的技能历史表 (eh_security_scanner_blocked_history)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_security_scanner_blocked_history (
    id           BIGINT PRIMARY KEY,
    skill_name   VARCHAR(255) NOT NULL,
    action       VARCHAR(20) NOT NULL,
    content_hash VARCHAR(128),
    max_severity VARCHAR(20),
    findings     TEXT,
    user_id      BIGINT,
    user_nickname VARCHAR(100),
    create_by    BIGINT,
    create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted      INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_security_scanner_blocked_history IS '安全扫描阻断历史表';
COMMENT ON COLUMN eh_security_scanner_blocked_history.skill_name   IS '技能名称';
COMMENT ON COLUMN eh_security_scanner_blocked_history.action       IS '操作类型：BLOCKED/WARNED';
COMMENT ON COLUMN eh_security_scanner_blocked_history.content_hash IS '文件内容哈希';
COMMENT ON COLUMN eh_security_scanner_blocked_history.max_severity IS '最高严重级别';
COMMENT ON COLUMN eh_security_scanner_blocked_history.findings    IS '发现的问题详情，JSON数组';

CREATE INDEX IF NOT EXISTS idx_blocked_history_skill   ON eh_security_scanner_blocked_history(skill_name);
CREATE INDEX IF NOT EXISTS idx_blocked_history_action  ON eh_security_scanner_blocked_history(action);
CREATE INDEX IF NOT EXISTS idx_blocked_history_time    ON eh_security_scanner_blocked_history(create_time);
CREATE INDEX IF NOT EXISTS idx_blocked_history_user    ON eh_security_scanner_blocked_history(user_id);

-- ----------------------------------------------------------------------------
-- 3. 安全扫描全局配置表 (eh_security_scanner_config)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_security_scanner_config (
    id          BIGINT PRIMARY KEY,
    enabled     INTEGER DEFAULT 1,
    mode        VARCHAR(20) DEFAULT 'block',
    timeout     INTEGER DEFAULT 30,
    create_by   BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER DEFAULT 0
);

COMMENT ON TABLE  eh_security_scanner_config    IS '安全扫描全局配置表';
COMMENT ON COLUMN eh_security_scanner_config.enabled IS '整体开关（1=启用，0=禁用）';
COMMENT ON COLUMN eh_security_scanner_config.mode   IS '扫描模式：block=阻断，warn=警告，off=关闭';
COMMENT ON COLUMN eh_security_scanner_config.timeout  IS '扫描超时时间（秒）';

-- 初始化默认配置
INSERT INTO eh_security_scanner_config (id, enabled, mode, timeout, create_time, update_time)
VALUES (1, 1, 'block', 30, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- Security Scanner 菜单和权限
-- ----------------------------------------------------------------------------
-- 桌面图标菜单
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES (58, 0, 'Security Scanner', 'app:security-scanner', 'MENU', '/app/security-scanner', 'Scan', 21, 1, NOW(), NOW(), 'linear-gradient(135deg, #BF5AF2, #FF453A)', 1000, 700, 800, 500, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- Security Scanner 按钮权限
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (59, 0, '查看配置', 'security-scanner:config', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (60, 0, '管理配置', 'security-scanner:manage', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (61, 0, '查看白名单', 'security-scanner:whitelist', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (62, 0, '添加白名单', 'security-scanner:whitelist:add', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (63, 0, '删除白名单', 'security-scanner:whitelist:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (64, 0, '查看历史', 'security-scanner:history', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (65, 0, '删除历史', 'security-scanner:history:delete', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 超级管理员（SUPER_ADMIN）- Security Scanner 全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 58, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 59, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 60, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 61, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 62, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 63, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 64, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 65, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================================
-- 4.4 Cron 定时任务
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 定时任务定义表 (eh_cron_job)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_cron_job (
    id                    BIGINT PRIMARY KEY,
    name                 VARCHAR(128) NOT NULL,
    description          VARCHAR(512),
    enabled              INTEGER DEFAULT 1,
    cron_expression      VARCHAR(64) NOT NULL,
    timezone             VARCHAR(64) DEFAULT 'Asia/Shanghai',
    task_type            VARCHAR(16) DEFAULT 'agent',
    prompt_template      TEXT,
    target_user_id       BIGINT,
    target_session_id     BIGINT,
    timeout_seconds      INTEGER DEFAULT 300,
    max_retries          INTEGER DEFAULT 3,
    misfire_grace_seconds INTEGER DEFAULT 60,
    max_concurrency      INTEGER DEFAULT 1,
    last_run_time        TIMESTAMP,
    next_run_time        TIMESTAMP,
    last_run_status      VARCHAR(32),
    last_run_error       TEXT,
    dept_id              BIGINT,
    create_by            BIGINT,
    create_time          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted              INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_cron_job IS '定时任务定义表';
COMMENT ON COLUMN eh_cron_job.name IS '任务名称';
COMMENT ON COLUMN eh_cron_job.description IS '任务描述';
COMMENT ON COLUMN eh_cron_job.enabled IS '是否启用（1=启用，0=禁用）';
COMMENT ON COLUMN eh_cron_job.cron_expression IS 'cron 表达式，支持5/4/3字段，周字段支持0-7或mon-sun';
COMMENT ON COLUMN eh_cron_job.timezone IS '时区';
COMMENT ON COLUMN eh_cron_job.task_type IS '任务类型：agent=AI对话，text=发送文本';
COMMENT ON COLUMN eh_cron_job.prompt_template IS '执行时的prompt模板，支持{{current_date}}等变量';
COMMENT ON COLUMN eh_cron_job.target_user_id IS '目标用户ID，执行时加载该用户上下文';
COMMENT ON COLUMN eh_cron_job.target_session_id IS '目标会话ID，不填则创建新会话';
COMMENT ON COLUMN eh_cron_job.timeout_seconds IS '执行超时时间（秒）';
COMMENT ON COLUMN eh_cron_job.max_retries IS '最大重试次数';
COMMENT ON COLUMN eh_cron_job.misfire_grace_seconds IS 'misfire宽限秒数';
COMMENT ON COLUMN eh_cron_job.max_concurrency IS '同一任务最大并发执行数';
COMMENT ON COLUMN eh_cron_job.last_run_time IS '上次执行时间';
COMMENT ON COLUMN eh_cron_job.next_run_time IS '下次执行时间';
COMMENT ON COLUMN eh_cron_job.last_run_status IS '上次执行状态：SUCCESS/ERROR/TIMEOUT/CANCELLED';
COMMENT ON COLUMN eh_cron_job.last_run_error IS '上次执行错误信息';
COMMENT ON COLUMN eh_cron_job.dept_id IS '所属部门（数据权限）';
COMMENT ON COLUMN eh_cron_job.create_by IS '创建者';

CREATE INDEX IF NOT EXISTS idx_cron_enabled ON eh_cron_job(enabled);
CREATE INDEX IF NOT EXISTS idx_cron_target_user ON eh_cron_job(target_user_id);
CREATE INDEX IF NOT EXISTS idx_cron_create_by ON eh_cron_job(create_by);
CREATE INDEX IF NOT EXISTS idx_cron_dept ON eh_cron_job(dept_id);
CREATE INDEX IF NOT EXISTS idx_cron_deleted ON eh_cron_job(deleted);

-- ----------------------------------------------------------------------------
-- 2. 定时任务执行历史表 (eh_cron_job_history)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_cron_job_history (
    id                  BIGINT PRIMARY KEY,
    job_id              BIGINT NOT NULL,
    start_time          TIMESTAMP NOT NULL,
    end_time            TIMESTAMP,
    status              VARCHAR(32) NOT NULL,
    trigger_type        VARCHAR(32) NOT NULL,
    session_id          VARCHAR(64),
    prompt_content      TEXT,
    response_content    TEXT,
    error_message       TEXT,
    prompt_tokens       INTEGER,
    completion_tokens   INTEGER,
    total_tokens        INTEGER,
    deleted             SMALLINT DEFAULT 0,
    create_by           BIGINT,
    create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE eh_cron_job_history IS '定时任务执行历史表';
COMMENT ON COLUMN eh_cron_job_history.job_id IS '关联任务ID';
COMMENT ON COLUMN eh_cron_job_history.start_time IS '执行开始时间';
COMMENT ON COLUMN eh_cron_job_history.end_time IS '执行结束时间';
COMMENT ON COLUMN eh_cron_job_history.status IS '执行状态：RUNNING/SUCCESS/ERROR/TIMEOUT/CANCELLED';
COMMENT ON COLUMN eh_cron_job_history.trigger_type IS '触发类型：SCHEDULED/MANUAL';
COMMENT ON COLUMN eh_cron_job_history.session_id IS '关联的会话ID';
COMMENT ON COLUMN eh_cron_job_history.prompt_content IS '实际发送的prompt';
COMMENT ON COLUMN eh_cron_job_history.response_content IS 'AI回复内容（截断）';
COMMENT ON COLUMN eh_cron_job_history.error_message IS '错误信息';
COMMENT ON COLUMN eh_cron_job_history.prompt_tokens IS 'prompt token消耗';
COMMENT ON COLUMN eh_cron_job_history.completion_tokens IS 'completion token消耗';
COMMENT ON COLUMN eh_cron_job_history.total_tokens IS 'total token消耗';

CREATE INDEX IF NOT EXISTS idx_cron_history_job ON eh_cron_job_history(job_id);
CREATE INDEX IF NOT EXISTS idx_cron_history_trigger ON eh_cron_job_history(trigger_type);
CREATE INDEX IF NOT EXISTS idx_cron_history_status ON eh_cron_job_history(status);
CREATE INDEX IF NOT EXISTS idx_cron_history_time ON eh_cron_job_history(create_time);

-- ----------------------------------------------------------------------------
-- Cron 定时任务菜单和权限
-- ----------------------------------------------------------------------------
-- 桌面图标菜单 (ID=66)
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES (66, 0, '定时任务', 'app:cron', 'MENU', '/app/cron', 'Clock', 22, 1, NOW(), NOW(), 'linear-gradient(135deg, #30D158, #34C759)', 1100, 720, 800, 500, -1, 1)
ON CONFLICT (id) DO NOTHING;

-- 按钮权限 (ID=67, 68, 69)
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (67, 0, '查看任务', 'cron:list', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (68, 0, '管理任务', 'cron:manage', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (69, 0, '执行任务', 'cron:execute', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 角色权限分配
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 66, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 67, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 68, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 69, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 66, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 67, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 68, NOW(), NOW()),
    (nextval('seq_role_permission'), 4, 69, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================================
-- 桌面管理器 (Desktop Manager)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 桌面分类表 (eh_desktop_category)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_desktop_category (
    id                  BIGINT PRIMARY KEY,
    name                VARCHAR(64) NOT NULL,
    icon                VARCHAR(32) DEFAULT '📁',
    color               VARCHAR(8) DEFAULT '#0A84FF',
    sort                INT DEFAULT 0,
    status              SMALLINT DEFAULT 1,
    create_by           BIGINT,
    create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT DEFAULT 0
);

COMMENT ON TABLE eh_desktop_category IS '桌面分类表';
COMMENT ON COLUMN eh_desktop_category.name IS '分类名称';
COMMENT ON COLUMN eh_desktop_category.icon IS '分类图标';
COMMENT ON COLUMN eh_desktop_category.color IS '主题色';
COMMENT ON COLUMN eh_desktop_category.sort IS '排序';
COMMENT ON COLUMN eh_desktop_category.status IS '状态：1启用 0禁用';

CREATE INDEX IF NOT EXISTS idx_category_sort ON eh_desktop_category(sort);
CREATE INDEX IF NOT EXISTS idx_category_status ON eh_desktop_category(status);
CREATE INDEX IF NOT EXISTS idx_category_deleted ON eh_desktop_category(deleted);

-- ----------------------------------------------------------------------------
-- 2. 桌面图标配置表 (eh_desktop_icon)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_desktop_icon (
    id                  BIGINT PRIMARY KEY,
    perm_id             BIGINT NOT NULL UNIQUE,
    category_id         BIGINT,
    is_desktop          SMALLINT DEFAULT 0,
    sort                INT DEFAULT 0,
    create_by           BIGINT,
    create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT DEFAULT 0,
    CONSTRAINT fk_icon_category FOREIGN KEY (category_id)
        REFERENCES eh_desktop_category(id) ON DELETE SET NULL
);

COMMENT ON TABLE eh_desktop_icon IS '桌面图标配置表';
COMMENT ON COLUMN eh_desktop_icon.perm_id IS '关联权限表ID';
COMMENT ON COLUMN eh_desktop_icon.category_id IS '所属分类ID';
COMMENT ON COLUMN eh_desktop_icon.is_desktop IS '是否显示在桌面：1是 0否';
COMMENT ON COLUMN eh_desktop_icon.sort IS '在分类内的排序';

CREATE INDEX IF NOT EXISTS idx_icon_category ON eh_desktop_icon(category_id);
CREATE INDEX IF NOT EXISTS idx_icon_desktop ON eh_desktop_icon(is_desktop);
CREATE INDEX IF NOT EXISTS idx_icon_perm ON eh_desktop_icon(perm_id);
CREATE INDEX IF NOT EXISTS idx_icon_deleted ON eh_desktop_icon(deleted);

-- ----------------------------------------------------------------------------
-- 3. 桌面管理器菜单权限 (ID=72) + 按钮权限 (ID=70, 71)
-- ----------------------------------------------------------------------------
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (72, 0, '整理桌面', 'app:desktop-manager', 'MENU', '/app/desktop-manager', 'LayoutGrid', 25, 1, NOW(), NOW(), 'linear-gradient(135deg, #00f5ff, #ff00ff)', 1100, 720, 800, 500, -1, 1),
    (70, 0, '查看桌面配置', 'desktop:view', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (71, 0, '管理桌面配置', 'desktop:manage', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- 超级管理员（SUPER_ADMIN）- 全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 70, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 71, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 普通管理员（ADMIN）- 仅查看权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 4, 70, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 4. 默认分类数据
-- ----------------------------------------------------------------------------
INSERT INTO eh_desktop_category (id, name, icon, color, sort, status, create_by, create_time, update_time, deleted)
VALUES
    (1, 'AI 助手', '🤖', '#0A84FF', 1, 1, 1, NOW(), NOW(), 0),
    (2, '系统管理', '⚙️', '#636366', 2, 1, 1, NOW(), NOW(), 0),
    (3, '安全中心', '🔒', '#FF453A', 3, 1, 1, NOW(), NOW(), 0),
    (4, '数据分析', '📊', '#FF9F0A', 4, 1, 1, NOW(), NOW(), 0),
    (5, '个人', '👤', '#BF5AF2', 5, 1, 1, NOW(), NOW(), 0),
    (6, '未分类', '📁', '#8E8E93', 99, 1, 1, NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 5. 默认图标分类配置
-- ----------------------------------------------------------------------------
-- 格式: (id, perm_id, category_id, is_desktop, sort)
--
-- AI 助手 (category_id=1): AI对话(9), 知识库(10), 模型管理(11), 记忆管理(14)
-- 系统管理 (category_id=2): 用户管理(12), 技能管理(13), 部门管理(18), 图标管理(19), 角色管理(26), 权限管理(27), S3文件管理(35), 环境变量(39), 定时任务(66), Agent运行时配置(73)
-- 安全中心 (category_id=3): Tool Guard(44), File Guard(52), Security Scanner(58)
-- 数据分析 (category_id=4): 数据大屏(16), 用量统计(32)
-- 个人 (category_id=5): 宠物管理(17), 个人设置(15)

INSERT INTO eh_desktop_icon (id, perm_id, category_id, is_desktop, sort, create_by, create_time, update_time, deleted)
VALUES
    -- AI 助手 - 默认桌面图标 (is_desktop=1)
    (1,  9,  1, 1, 1,  1, NOW(), NOW(), 0),   -- AI 对话
    (2,  10, 1, 1, 2,  1, NOW(), NOW(), 0),   -- 知识库
    (3,  11, 1, 1, 3,  1, NOW(), NOW(), 0),   -- 模型管理
    (4,  14, 1, 1, 4,  1, NOW(), NOW(), 0),   -- 记忆管理

    -- 系统管理
    (5,  12, 2, 0, 1,  1, NOW(), NOW(), 0),   -- 用户管理
    (6,  13, 2, 0, 2,  1, NOW(), NOW(), 0),   -- 技能管理
    (7,  18, 2, 0, 3,  1, NOW(), NOW(), 0),   -- 部门管理
    (8,  19, 2, 0, 4,  1, NOW(), NOW(), 0),   -- 图标管理
    (9,  26, 2, 0, 5,  1, NOW(), NOW(), 0),   -- 角色管理
    (10, 27, 2, 0, 6,  1, NOW(), NOW(), 0),   -- 权限管理
    (11, 35, 2, 0, 7,  1, NOW(), NOW(), 0),   -- S3文件管理
    (12, 39, 2, 0, 8,  1, NOW(), NOW(), 0),   -- 环境变量
    (13, 66, 2, 1, 5,  1, NOW(), NOW(), 0),   -- 定时任务（也设为桌面图标）

    -- 安全中心
    (14, 44, 3, 0, 1,  1, NOW(), NOW(), 0),   -- Tool Guard
    (15, 52, 3, 0, 2,  1, NOW(), NOW(), 0),   -- File Guard
    (16, 58, 3, 0, 3,  1, NOW(), NOW(), 0),   -- Security Scanner

    -- 数据分析
    (17, 16, 4, 0, 1,  1, NOW(), NOW(), 0),   -- 数据大屏
    (18, 32, 4, 0, 2,  1, NOW(), NOW(), 0),   -- 用量统计

    -- 个人
    (19, 17, 5, 0, 1,  1, NOW(), NOW(), 0),   -- 宠物管理
    (20, 15, 5, 0, 2,  1, NOW(), NOW(), 0)    -- 个人设置
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 执行完成！现在实体类和数据库表已完全对齐
-- ============================================================================

-- =============================================================================
-- 第九部分：Agent 运行时配置 (agent-runtime-config)
-- =============================================================================

-- ----------------------------------------------------------------------------
-- 1. 全局运行时配置表
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_agent_runtime_config (
    id                  BIGINT PRIMARY KEY,
    config_key          VARCHAR(64) NOT NULL UNIQUE,
    config_value        TEXT,
    description         VARCHAR(255),
    update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT DEFAULT 0,
    create_by           BIGINT,
    create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE eh_agent_runtime_config IS 'Agent运行时配置表';
COMMENT ON COLUMN eh_agent_runtime_config.config_key IS '配置键';
COMMENT ON COLUMN eh_agent_runtime_config.config_value IS '配置值(JSON)';

-- ----------------------------------------------------------------------------
-- 2. 运行时配置变更历史表
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eh_agent_runtime_config_history (
    id                  BIGINT PRIMARY KEY,
    operator_id         BIGINT,
    operator_name       VARCHAR(64),
    config_key          VARCHAR(64) NOT NULL,
    old_value           TEXT,
    new_value           TEXT,
    change_reason       VARCHAR(255),
    create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT DEFAULT 0,
    create_by           BIGINT,
    update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE eh_agent_runtime_config_history IS '运行时配置变更历史表';
COMMENT ON COLUMN eh_agent_runtime_config_history.operator_id IS '操作人ID';
COMMENT ON COLUMN eh_agent_runtime_config_history.operator_name IS '操作人姓名';
COMMENT ON COLUMN eh_agent_runtime_config_history.config_key IS '配置键';
COMMENT ON COLUMN eh_agent_runtime_config_history.old_value IS '旧值';
COMMENT ON COLUMN eh_agent_runtime_config_history.new_value IS '新值';
COMMENT ON COLUMN eh_agent_runtime_config_history.change_reason IS '变更原因';
COMMENT ON COLUMN eh_agent_runtime_config_history.deleted IS '删除标记';

CREATE INDEX IF NOT EXISTS idx_config_history_key ON eh_agent_runtime_config_history(config_key);
CREATE INDEX IF NOT EXISTS idx_config_history_time ON eh_agent_runtime_config_history(create_time);

-- ----------------------------------------------------------------------------
-- 3. 权限数据 (ID=73, 74, 75)
-- ----------------------------------------------------------------------------
INSERT INTO eh_permission (id, parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, create_time, update_time, gradient, default_width, default_height, min_width, min_height, dock_order, is_desktop_icon)
VALUES
    (73, 0, 'Agent运行时配置', 'agent-config', 'MENU', '/app/agent-runtime-config', 'Settings2', 26, 1, NOW(), NOW(), 'linear-gradient(135deg, #30D158, #00C7BE)', 900, 650, 700, 500, -1, 1),
    (74, 73, '查看配置', 'agent-config:list', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0),
    (75, 73, '管理配置', 'agent-config:update', 'BUTTON', NULL, NULL, 0, 1, NOW(), NOW(), NULL, NULL, NULL, NULL, NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- SUPER_ADMIN - 全部权限
INSERT INTO eh_role_permission (id, role_id, permission_id, create_time, update_time)
VALUES
    (nextval('seq_role_permission'), 1, 73, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 74, NOW(), NOW()),
    (nextval('seq_role_permission'), 1, 75, NOW(), NOW())
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 4. 默认配置值初始化
-- ----------------------------------------------------------------------------
INSERT INTO eh_agent_runtime_config (id, config_key, config_value, description, update_time, deleted)
VALUES
    (1, 'llm_retry', '{"llm_retry_enabled":true,"llm_max_retries":3,"llm_backoff_base":1.0,"llm_backoff_cap":10.0,"llm_max_concurrent":10,"llm_max_qpm":600,"llm_rate_limit_pause":5.0,"llm_rate_limit_jitter":1.0,"llm_acquire_timeout":300.0}', 'LLM重试与限流配置', NOW(), 0),
    (2, 'context_compact', '{"token_count_model":"default","token_count_use_mirror":false,"token_count_estimate_divisor":4.0,"context_compact_enabled":true,"memory_compact_ratio":0.75,"memory_reserve_ratio":0.1,"compact_with_thinking_block":true}', '上下文压缩配置', NOW(), 0),
    (3, 'tool_result_compact', '{"enabled":true,"recent_n":2,"old_max_bytes":3000,"recent_max_bytes":50000,"retention_days":5}', '工具结果压缩配置', NOW(), 0),
    (4, 'memory_summary', '{"memory_summary_enabled":true,"memory_prompt_enabled":true,"dream_cron":"0 23 * * *","force_memory_search":false,"force_max_results":1,"force_min_score":0.3,"force_memory_search_timeout":10.0,"rebuild_memory_index_on_start":false,"recursive_file_watcher":false}', '记忆摘要配置', NOW(), 0),
    (5, 'embedding', '{"backend":"openai","api_key":"","base_url":"","model_name":"","dimensions":1024,"enable_cache":true,"use_dimensions":false,"max_cache_size":3000,"max_input_length":8192,"max_batch_size":10}', 'Embedding模型配置', NOW(), 0),
    (6, 'runtime_basic', '{"max_iters":100,"auto_continue_on_text_only":false,"max_input_length":131072,"history_max_length":10000}', '基础运行时配置', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 5. 初始历史记录写入
-- ----------------------------------------------------------------------------
INSERT INTO eh_agent_runtime_config_history (id, operator_id, operator_name, config_key, old_value, new_value, change_reason, create_time, deleted, create_by, update_time)
VALUES
    (1, 1, '系统', 'llm_retry', NULL, '{"llm_retry_enabled":true,"llm_max_retries":3,"llm_backoff_base":1.0,"llm_backoff_cap":10.0,"llm_max_concurrent":10,"llm_max_qpm":600,"llm_rate_limit_pause":5.0,"llm_rate_limit_jitter":1.0,"llm_acquire_timeout":300.0}', '系统初始化', NOW(), 0, 1, NOW()),
    (2, 1, '系统', 'context_compact', NULL, '{"token_count_model":"default","token_count_use_mirror":false,"token_count_estimate_divisor":4.0,"context_compact_enabled":true,"memory_compact_ratio":0.75,"memory_reserve_ratio":0.1,"compact_with_thinking_block":true}', '系统初始化', NOW(), 0, 1, NOW()),
    (3, 1, '系统', 'tool_result_compact', NULL, '{"enabled":true,"recent_n":2,"old_max_bytes":3000,"recent_max_bytes":50000,"retention_days":5}', '系统初始化', NOW(), 0, 1, NOW()),
    (4, 1, '系统', 'memory_summary', NULL, '{"memory_summary_enabled":true,"memory_prompt_enabled":true,"dream_cron":"0 23 * * *","force_memory_search":false,"force_max_results":1,"force_min_score":0.3,"force_memory_search_timeout":10.0,"rebuild_memory_index_on_start":false,"recursive_file_watcher":false}', '系统初始化', NOW(), 0, 1, NOW()),
    (5, 1, '系统', 'embedding', NULL, '{"backend":"openai","api_key":"","base_url":"","model_name":"","dimensions":1024,"enable_cache":true,"use_dimensions":false,"max_cache_size":3000,"max_input_length":8192,"max_batch_size":10}', '系统初始化', NOW(), 0, 1, NOW()),
    (6, 1, '系统', 'runtime_basic', NULL, '{"max_iters":100,"auto_continue_on_text_only":false,"max_input_length":131072,"history_max_length":10000}', '系统初始化', NOW(), 0, 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 6. 添加桌面图标 - Agent运行时配置 (ID=73)
-- ----------------------------------------------------------------------------
INSERT INTO eh_desktop_icon (id, perm_id, category_id, is_desktop, sort, create_by, create_time, update_time, deleted)
VALUES (21, 73, 2, 0, 9, 1, NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 执行完成
-- ============================================================================
