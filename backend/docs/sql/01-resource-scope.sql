-- ============================================================================
-- EvolveHub v1.1 - 资源分级权限 DDL
-- ============================================================================
-- 功能：为资源表增加 scope/owner_id 字段，新建 API Key 表和资源授权表
-- 执行时机：在 00-init-all.sql 之后执行
-- 作者：EvolveHub Team
-- 日期：2026-04-13
-- ============================================================================

-- ============================================================================
-- 第一部分：现有表结构变更
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 一、eh_model_config 新增 scope + owner_id
-- ----------------------------------------------------------------------------

ALTER TABLE eh_model_config ADD COLUMN IF NOT EXISTS scope VARCHAR(10) NOT NULL DEFAULT 'SYSTEM';
ALTER TABLE eh_model_config ADD COLUMN IF NOT EXISTS owner_id BIGINT;

COMMENT ON COLUMN eh_model_config.scope IS '资源范围：SYSTEM-系统级 USER-用户级';
COMMENT ON COLUMN eh_model_config.owner_id IS '资源所有者ID，scope=SYSTEM 时为 NULL';

CREATE INDEX IF NOT EXISTS idx_model_config_scope ON eh_model_config(scope);
CREATE INDEX IF NOT EXISTS idx_model_config_owner_id ON eh_model_config(owner_id);

-- ============================================================================
-- 第二部分：新建表
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 二、eh_api_key 用户密钥表
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

COMMENT ON TABLE eh_api_key IS '用户 API Key 表（一个用户一个 Key）';
COMMENT ON COLUMN eh_api_key.user_id IS '绑定的用户 ID（一对一）';
COMMENT ON COLUMN eh_api_key.api_key IS '密钥值，格式：sk-{32位随机hex}';
COMMENT ON COLUMN eh_api_key.status IS '状态：1-正常 0-禁用';
COMMENT ON COLUMN eh_api_key.expired_at IS '过期时间，NULL 表示永不过期';

CREATE UNIQUE INDEX IF NOT EXISTS uk_api_key ON eh_api_key(api_key) WHERE deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_api_key_user ON eh_api_key(user_id) WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 三、eh_resource_grant 资源授权表
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_resource_grant (
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    resource_id   BIGINT NOT NULL,
    create_by     BIGINT,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted       INTEGER DEFAULT 0
);

COMMENT ON TABLE eh_resource_grant IS '资源授权表（管理员将系统级资源授权给用户）';
COMMENT ON COLUMN eh_resource_grant.user_id IS '被授权用户 ID';
COMMENT ON COLUMN eh_resource_grant.resource_type IS '资源类型：MODEL-模型 SKILL-技能 MCP-MCP服务';
COMMENT ON COLUMN eh_resource_grant.resource_id IS '资源 ID（指向具体资源表主键）';

CREATE INDEX IF NOT EXISTS idx_resource_grant_user ON eh_resource_grant(user_id, resource_type);
CREATE INDEX IF NOT EXISTS idx_resource_grant_resource ON eh_resource_grant(resource_type, resource_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_resource_grant ON eh_resource_grant(user_id, resource_type, resource_id) WHERE deleted = 0;

-- ----------------------------------------------------------------------------
-- 四、eh_skill_config 技能配置表
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_skill_config (
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

COMMENT ON TABLE eh_skill_config IS '技能配置表';
COMMENT ON COLUMN eh_skill_config.name IS '技能名称';
COMMENT ON COLUMN eh_skill_config.skill_type IS '技能类型';
COMMENT ON COLUMN eh_skill_config.config IS '配置信息（JSON）';
COMMENT ON COLUMN eh_skill_config.scope IS '资源范围：SYSTEM-系统级 USER-用户级';
COMMENT ON COLUMN eh_skill_config.owner_id IS '资源所有者ID，scope=SYSTEM 时为 NULL';

CREATE INDEX IF NOT EXISTS idx_skill_config_scope ON eh_skill_config(scope);
CREATE INDEX IF NOT EXISTS idx_skill_config_owner ON eh_skill_config(owner_id);

-- ----------------------------------------------------------------------------
-- 五、eh_mcp_config MCP 服务配置表
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS eh_mcp_config (
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

COMMENT ON TABLE eh_mcp_config IS 'MCP 服务配置表';
COMMENT ON COLUMN eh_mcp_config.name IS '服务名称';
COMMENT ON COLUMN eh_mcp_config.server_url IS '服务地址';
COMMENT ON COLUMN eh_mcp_config.protocol IS '传输协议（stdio / sse）';
COMMENT ON COLUMN eh_mcp_config.config IS '配置信息（JSON）';
COMMENT ON COLUMN eh_mcp_config.scope IS '资源范围：SYSTEM-系统级 USER-用户级';
COMMENT ON COLUMN eh_mcp_config.owner_id IS '资源所有者ID，scope=SYSTEM 时为 NULL';

CREATE INDEX IF NOT EXISTS idx_mcp_config_scope ON eh_mcp_config(scope);
CREATE INDEX IF NOT EXISTS idx_mcp_config_owner ON eh_mcp_config(owner_id);

-- ============================================================================
-- 使用说明
-- ============================================================================
--
-- 1. scope 字段说明：
--    - SYSTEM：系统级资源，超级管理员创建，平台全局可见
--    - USER：用户级资源，仅 owner_id 本人可见可用
--
-- 2. API Key 使用流程：
--    - 管理员通过 POST /api/admin/api-key/generate 为用户生成 Key
--    - 用户调用系统资源时在请求头中携带 X-Api-Key
--    - 系统校验 Key → 定位用户 → 查授权表 → 放行
--
-- 3. 资源授权流程：
--    - 管理员通过 POST /api/admin/resource-grant/assign 授权
--    - 通过 POST /api/admin/resource-grant/revoke 撤销
--
-- ============================================================================
