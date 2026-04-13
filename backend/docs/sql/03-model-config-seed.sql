-- ============================================================================
-- EvolveHub v1.3 - 模型配置测试数据
-- ============================================================================
-- 功能：插入模型配置的模拟测试数据（无需真实 API 连接）
-- 说明：所有 api_key 为测试占位符，实际使用时替换为真实密钥
-- 作者：EvolveHub Team
-- 日期：2026-04-13
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 系统级对话模型（SYSTEM LLM）
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
-- 2. 系统级向量模型（SYSTEM EMBEDDING）
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
-- 3. 个人模型（USER 级，仅 owner_id = 1 的用户可用）
--    owner_id = 1 即 admin 用户
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
-- 4. 禁用一些模型（模拟已禁用的）
-- ----------------------------------------------------------------------------

INSERT INTO eh_model_config (id, name, provider, api_key, base_url, enabled, model_type, scope, owner_id, create_by, create_time, update_time)
VALUES
    (40, '已禁用的 GPT-4',   'OpenAI',   'sk-disabled-openai-gpt4',            'https://api.openai.com/v1',              0, 'LLM', 'SYSTEM', NULL, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 查询验证
-- ============================================================================

-- 系统级 LLM 总数
SELECT '系统级 LLM' AS type, COUNT(*) AS cnt FROM eh_model_config WHERE model_type = 'LLM' AND scope = 'SYSTEM' AND deleted = 0
UNION ALL
SELECT '系统级 Embedding' AS type, COUNT(*) AS cnt FROM eh_model_config WHERE model_type = 'EMBEDDING' AND scope = 'SYSTEM' AND deleted = 0
UNION ALL
SELECT '个人模型' AS type, COUNT(*) AS cnt FROM eh_model_config WHERE scope = 'USER' AND deleted = 0
UNION ALL
SELECT '已禁用模型' AS type, COUNT(*) AS cnt FROM eh_model_config WHERE enabled = 0 AND deleted = 0;
