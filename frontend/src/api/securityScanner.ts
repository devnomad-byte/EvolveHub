import { http } from '@/utils/request'

/** 安全扫描配置 */
export interface SecurityScannerConfig {
  id: number
  enabled: number
  mode: 'block' | 'warn' | 'off'
  timeout: number
  updateTime: string | null
}

/** 安全扫描白名单 */
export interface SecurityScannerWhitelist {
  id: number
  skillName: string
  contentHash: string
  addedBy: number
  createTime: string
}

/** 安全扫描阻断历史 */
export interface SecurityScannerBlockedHistory {
  id: number
  skillName: string
  action: 'BLOCKED' | 'WARNED'
  contentHash: string
  maxSeverity: string
  findings: string
  userId: number
  userNickname: string
  createTime: string
}

/** 安全扫描发现的问题 */
export interface SecurityScannerFinding {
  severity: string
  title: string
  description: string
  filePath: string
  lineNumber: number
  ruleId: string
}

export const securityScannerApi = {
  /** 获取配置 */
  getConfig: () => {
    return http.get<SecurityScannerConfig>('/admin/security-scanner/config')
  },

  /** 更新配置 */
  updateConfig: (data: {
    enabled: number
    mode: string
    timeout: number
  }) => {
    return http.put('/admin/security-scanner/config', data)
  },

  /** 获取白名单列表 */
  listWhitelist: () => {
    return http.get<SecurityScannerWhitelist[]>('/admin/security-scanner/whitelist')
  },

  /** 添加白名单 */
  addWhitelist: (data: {
    skillName: string
    contentHash: string
  }) => {
    return http.post<{ id: number }>('/admin/security-scanner/whitelist', data)
  },

  /** 删除白名单 */
  deleteWhitelist: (id: number) => {
    return http.delete(`/admin/security-scanner/whitelist/${id}`)
  },

  /** 获取阻断历史 */
  listHistory: (params: {
    pageNum?: number
    pageSize?: number
    action?: string
    userId?: number
  }) => {
    return http.get<{ records: SecurityScannerBlockedHistory[]; total: number }>('/admin/security-scanner/history', { params })
  },

  /** 清除所有历史 */
  clearHistory: () => {
    return http.delete('/admin/security-scanner/history')
  }
}
