import { http } from '@/utils/request'

/** 工具守卫规则 */
export interface ToolGuardRule {
  id: number
  ruleId: string
  name: string
  tools: string
  params: string
  category: string
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO'
  patterns: string
  excludePatterns: string | null
  description: string | null
  remediation: string | null
  isBuiltin: number
  enabled: number
  createTime: string
  updateTime: string
}

/** 工具守卫配置 */
export interface ToolGuardConfig {
  enabled: number
  guardedTools: string
  deniedTools: string
  updateTime: string | null
}

/** 工具守卫历史 */
export interface ToolGuardHistory {
  id: number
  sessionId: string
  userId: number
  userNickname: string
  toolName: string
  paramName: string
  matchedRuleId: string
  matchedValue: string
  severity: string
  action: 'BLOCKED' | 'WARNED'
  createTime: string
}

export const toolGuardApi = {
  /** 获取所有规则 */
  listRules: () => {
    return http.get<ToolGuardRule[]>('/admin/tool-guard/rules')
  },

  /** 创建规则 */
  createRule: (data: {
    ruleId: string
    name: string
    tools: string
    params: string
    severity: string
    patterns: string
    excludePatterns?: string
    category?: string
    description?: string
    remediation?: string
    enabled?: number
  }) => {
    return http.post<{ id: number }>('/admin/tool-guard/rules', data)
  },

  /** 更新规则 */
  updateRule: (data: {
    id: number
    name?: string
    tools?: string
    params?: string
    severity?: string
    patterns?: string
    excludePatterns?: string
    category?: string
    description?: string
    remediation?: string
    enabled?: number
  }) => {
    return http.put('/admin/tool-guard/rules', data)
  },

  /** 删除规则 */
  deleteRule: (id: number) => {
    return http.delete(`/admin/tool-guard/rules/${id}`)
  },

  /** 启用/禁用规则 */
  toggleRule: (id: number) => {
    return http.put(`/admin/tool-guard/rules/${id}/toggle`)
  },

  /** 获取配置 */
  getConfig: () => {
    return http.get<ToolGuardConfig>('/admin/tool-guard/config')
  },

  /** 更新配置 */
  updateConfig: (data: {
    enabled: number
    guardedTools?: string
    deniedTools?: string
  }) => {
    return http.put('/admin/tool-guard/config', data)
  },

  /** 获取历史记录 */
  listHistory: (params: {
    pageNum?: number
    pageSize?: number
    severity?: string
    userId?: number
  }) => {
    return http.get<{ records: ToolGuardHistory[]; total: number }>('/admin/tool-guard/history', { params })
  },

  /** 清除历史 */
  clearHistory: () => {
    return http.delete('/admin/tool-guard/history')
  }
}
