import { http } from '@/utils/request'

/** LLM重试配置 */
export interface LLMRetryConfig {
  llm_retry_enabled: boolean
  llm_max_retries: number
  llm_backoff_base: number
  llm_backoff_cap: number
  llm_max_concurrent: number
  llm_max_qpm: number
  llm_rate_limit_pause: number
  llm_rate_limit_jitter: number
  llm_acquire_timeout: number
}

/** 上下文压缩配置 */
export interface ContextCompactConfig {
  token_count_model: string
  token_count_use_mirror: boolean
  token_count_estimate_divisor: number
  context_compact_enabled: boolean
  memory_compact_ratio: number
  memory_reserve_ratio: number
  compact_with_thinking_block: boolean
}

/** 工具结果压缩配置 */
export interface ToolResultCompactConfig {
  enabled: boolean
  recent_n: number
  old_max_bytes: number
  recent_max_bytes: number
  retention_days: number
}

/** 记忆摘要配置 */
export interface MemorySummaryConfig {
  memory_summary_enabled: boolean
  memory_prompt_enabled: boolean
  dream_cron: string
  force_memory_search: boolean
  force_max_results: number
  force_min_score: number
  force_memory_search_timeout: number
  rebuild_memory_index_on_start: boolean
  recursive_file_watcher: boolean
}

/** Embedding配置 */
export interface EmbeddingConfig {
  backend: string
  api_key: string
  base_url: string
  model_name: string
  dimensions: number
  enable_cache: boolean
  use_dimensions: boolean
  max_cache_size: number
  max_input_length: number
  max_batch_size: number
}

/** 基础运行时配置 */
export interface RuntimeBasicConfig {
  max_iters: number
  auto_continue_on_text_only: boolean
  max_input_length: number
  history_max_length: number
}

/** Agent运行时配置 */
export interface AgentRuntimeConfig {
  id: number | null
  configKey: string
  configValue: string
  description: string
  updateTime: string | null
}

/** Agent运行时配置历史 */
export interface AgentRuntimeConfigHistory {
  id: number
  operatorId: number
  operatorName: string
  configKey: string
  oldValue: string | null
  newValue: string
  changeReason: string | null
  createTime: string
}

export const agentRuntimeConfigApi = {
  /** 获取指定配置 */
  get: (configKey: string) => {
    return http.get<AgentRuntimeConfig[]>(`/admin/runtime-config/${configKey}`)
  },

  /** 获取所有配置 */
  getAll: () => {
    return http.get<AgentRuntimeConfig[]>('/admin/runtime-config/all')
  },

  /** 更新配置 */
  update: (configKey: string, data: { configValue: string; changeReason?: string }) => {
    return http.put<any>(`/admin/runtime-config/${configKey}`, data)
  },

  /** 批量更新配置 */
  batchUpdate: (data: { configs: Record<string, string>; changeReason?: string }) => {
    return http.put('/admin/runtime-config/batch', data)
  },

  /** 获取默认配置值 */
  getDefaults: () => {
    return http.get<AgentRuntimeConfig[]>('/admin/runtime-config/defaults')
  },

  /** 获取配置变更历史 */
  getHistory: (params: {
    configKey?: string
    pageNum?: number
    pageSize?: number
  }) => {
    return http.get<{ records: AgentRuntimeConfigHistory[]; total: number }>('/admin/runtime-config/history', { params })
  }
}
