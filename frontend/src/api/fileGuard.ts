import { http } from '@/utils/request'

/** 文件守卫规则 */
export interface FileGuardRule {
  id: number
  ruleId: string
  name: string
  pathPattern: string
  pathType: 'FILE' | 'DIRECTORY' | 'WILDCARD'
  tools: string | null
  description: string | null
  remediation: string | null
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO'
  isBuiltin: number
  enabled: number
  createBy: number
  createTime: string
  updateTime: string
}

/** 文件守卫配置 */
export interface FileGuardConfig {
  enabled: number
  updateTime: string | null
}

export const fileGuardApi = {
  /** 获取所有规则 */
  listRules: () => {
    return http.get<FileGuardRule[]>('/admin/file-guard/rules')
  },

  /** 创建规则 */
  createRule: (data: {
    ruleId: string
    name: string
    pathPattern: string
    pathType: string
    tools?: string
    description?: string
    remediation?: string
    severity?: string
    enabled?: number
  }) => {
    return http.post<{ id: number }>('/admin/file-guard/rules', data)
  },

  /** 更新规则 */
  updateRule: (data: {
    id: number
    name?: string
    pathPattern?: string
    pathType?: string
    tools?: string
    description?: string
    remediation?: string
    severity?: string
    enabled?: number
  }) => {
    return http.put('/admin/file-guard/rules', data)
  },

  /** 删除规则 */
  deleteRule: (id: number) => {
    return http.delete(`/admin/file-guard/rules/${id}`)
  },

  /** 启用/禁用规则 */
  toggleRule: (id: number) => {
    return http.put(`/admin/file-guard/rules/${id}/toggle`)
  },

  /** 获取配置 */
  getConfig: () => {
    return http.get<FileGuardConfig>('/admin/file-guard/config')
  },

  /** 更新配置 */
  updateConfig: (data: { enabled: number }) => {
    return http.put('/admin/file-guard/config', data)
  }
}
