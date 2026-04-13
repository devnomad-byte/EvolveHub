-- ============================================================================
-- EvolveHub v1.2 - 模型提供商维度表
-- ============================================================================
-- 功能：新建模型提供商维度表，存储 AI 厂商信息
-- 执行时机：在 00-init-all.sql 之后执行
-- 作者：EvolveHub Team
-- 日期：2026-04-13
-- ============================================================================

-- ----------------------------------------------------------------------------
-- eh_model_provider 模型提供商表
-- ----------------------------------------------------------------------------

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
-- 初始化种子数据：主流 AI 模型提供商
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

-- ============================================================================
-- 使用说明
-- ============================================================================
--
-- 1. 前端使用流程：
--    - 添加模型时，Provider 下拉框从 /model-provider/list 获取列表
--    - 选择 Provider 后，自动填充 default_base_url（可手动修改）
--
-- 2. Provider CRUD 接口：
--    - GET    /model-provider/list       获取全部启用的提供商
--    - GET    /model-provider/{id}       获取详情
--    - POST   /model-provider/create     创建（SUPER_ADMIN）
--    - PUT    /model-provider/update     更新（SUPER_ADMIN）
--    - DELETE /model-provider/{id}      删除（SUPER_ADMIN）
--
-- ============================================================================
