import { http } from '@/utils/request'

// 用户聊天活动信息
export interface UserChatActivity {
  userId: number
  username: string
  nickname: string
  avatar: string
  deptId: number
  deptName: string
  lastActiveTime: string
  sessionCount: number
}

// 会话项（含用户信息和模型信息）
export interface SessionItem {
  id: number
  userId: number
  username: string
  nickname: string
  title: string
  modelConfigId: number
  modelName: string
  totalPromptTokens: number
  totalCompletionTokens: number
  totalTokens: number
  messageCount: number
  createTime: string
  updateTime: string
}

// 消息信息
export interface ChatMessageInfo {
  id: number
  sessionId: number
  role: string
  content: string
  toolCalls: string
  toolCallId: string
  modelName: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
  finishReason: string
  durationMs: number
  createTime: string
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

export const adminChatHistoryApi = {
  /**
   * 获取有聊天记录的用户列表
   */
  getUsers: (keyword?: string, deptId?: number, pageNum = 1, pageSize = 20) => {
    return http.get<PageResponse<UserChatActivity>>('/admin/chat-history/users', {
      params: { keyword, deptId, pageNum, pageSize }
    })
  },

  /**
   * 获取用户的会话列表
   */
  getSessions: (userId?: number, keyword?: string, startDate?: string, endDate?: string, pageNum = 1, pageSize = 15) => {
    return http.get<PageResponse<SessionItem>>('/admin/chat-history/sessions', {
      params: { userId, keyword, startDate, endDate, pageNum, pageSize }
    })
  },

  /**
   * 获取会话消息列表
   */
  getMessages: (sessionId: number, pageNum = 1, pageSize = 20) => {
    return http.get<PageResponse<ChatMessageInfo>>(`/admin/chat-history/sessions/${sessionId}/messages`, {
      params: { pageNum, pageSize }
    })
  },

  /**
   * 导出对话记录
   */
  exportChatHistory: (userId?: number, startDate?: string, endDate?: string, format = 'md') => {
    return http.post<{ filename: string, content: string }>(`/admin/chat-history/export?userId=${userId || ''}&startDate=${startDate || ''}&endDate=${endDate || ''}&format=${format}`, {})
  }
}
