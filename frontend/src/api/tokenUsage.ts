import { http } from '@/utils/request'

// Token 用量记录
export interface TokenUsageRecord {
  id: number
  userId: number
  modelConfigId: number
  usageDate: string
  requestCount: number
  promptTokens: number
  completionTokens: number
  totalTokens: number
  deptId: number
  createBy: number
  createTime: string
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

// 用户端 Token 用量 API
export const tokenUsageApi = {
  /**
   * 获取我的用量记录
   */
  getUserRecords: (startDate?: string, endDate?: string) => {
    return http.get<TokenUsageRecord[]>('/admin/user/token-usage/records', {
      params: { startDate, endDate }
    })
  },

  /**
   * 获取我的用量汇总
   */
  getUserSummary: (startDate?: string, endDate?: string) => {
    return http.get<TokenUsageSummary>('/admin/user/token-usage/summary', {
      params: { startDate, endDate }
    })
  }
}

// 管理员 Token 用量 API
export const adminTokenUsageApi = {
  /**
   * 获取用量记录列表（数据权限过滤）
   */
  getRecords: (pageNum: number = 1, pageSize: number = 10) => {
    return http.get<PageResponse<TokenUsageRecord>>('/admin/token-usage/records', {
      params: { pageNum, pageSize }
    })
  }
}
