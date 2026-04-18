import { http } from '@/utils/request'

// Token 用量记录
export interface TokenUsageRecord {
  id: number
  userId: number
  username: string
  nickname: string
  deptId: number
  deptName: string
  modelConfigId: number
  modelName: string
  usageDate: string
  requestCount: number
  promptTokens: number
  completionTokens: number
  totalTokens: number
}

// 用户Token用量聚合
export interface TokenUsageUser {
  userId: number
  username: string
  nickname: string
  avatar: string
  deptId: number
  deptName: string
  totalRequests: number
  totalPromptTokens: number
  totalCompletionTokens: number
  totalTokens: number
}

// 用量汇总
export interface TokenUsageSummary {
  totalRequests: number
  totalPromptTokens: number
  totalCompletionTokens: number
  totalTokens: number
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

// 管理员 Token 用量 API
export const adminTokenUsageApi = {
  /**
   * 获取有Token用量记录的用户列表
   */
  getUsers: (keyword?: string, deptId?: number, startDate?: string, endDate?: string, pageNum = 1, pageSize = 20) => {
    return http.get<PageResponse<TokenUsageUser>>('/admin/token-usage/users', {
      params: { keyword, deptId, startDate, endDate, pageNum, pageSize }
    })
  },

  /**
   * 获取Token用量记录列表
   */
  getRecords: (userId?: number, modelConfigId?: number, keyword?: string, startDate?: string, endDate?: string, pageNum = 1, pageSize = 20) => {
    return http.get<PageResponse<TokenUsageRecord>>('/admin/token-usage/records', {
      params: { userId, modelConfigId, keyword, startDate, endDate, pageNum, pageSize }
    })
  },

  /**
   * 获取Token用量汇总
   */
  getSummary: (userId?: number, startDate?: string, endDate?: string) => {
    return http.get<TokenUsageSummary>('/admin/token-usage/summary', {
      params: { userId, startDate, endDate }
    })
  },

  /**
   * 导出Token用量
   */
  exportTokenUsage: (userId?: number, startDate?: string, endDate?: string, format = 'md') => {
    return http.post<{ filename: string, content: string }>(`/admin/token-usage/export?userId=${userId || ''}&startDate=${startDate || ''}&endDate=${endDate || ''}&format=${format}`, {})
  }
}

// 用户端 Token 用量 API
export const tokenUsageApi = {
  /**
   * 获取我的用量记录
   */
  getUserRecords: (startDate?: string, endDate?: string) => {
    return http.get<TokenUsageRecord[]>('/user/token-usage/records', {
      params: { startDate, endDate }
    })
  },

  /**
   * 获取我的用量汇总
   */
  getUserSummary: (startDate?: string, endDate?: string) => {
    return http.get<TokenUsageSummary>('/user/token-usage/summary', {
      params: { startDate, endDate }
    })
  }
}
