import { http } from '@/utils/request'

// 对话会话信息
export interface ChatSessionInfo {
  id: number
  userId: number
  title: string
  modelConfigId: number
  sysPrompt: string
  totalPromptTokens: number
  totalCompletionTokens: number
  totalTokens: number
  messageCount: number
  contextSummary: string
  deptId: number
  createBy: number
  createTime: string
  updateTime: string
}

// 对话消息信息
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

// 管理员对话历史 API
export const adminChatHistoryApi = {
  /**
   * 获取对话会话列表（数据权限过滤）
   */
  getSessions: (pageNum: number = 1, pageSize: number = 10) => {
    return http.get<PageResponse<ChatSessionInfo>>('/admin/chat-history/sessions', {
      params: { pageNum, pageSize }
    })
  },

  /**
   * 获取会话消息列表
   */
  getMessages: (sessionId: number, pageNum: number = 1, pageSize: number = 20) => {
    return http.get<PageResponse<ChatMessageInfo>>(`/admin/chat-history/sessions/${sessionId}/messages`, {
      params: { pageNum, pageSize }
    })
  }
}
