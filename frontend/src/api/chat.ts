import { http } from '@/utils/request'

interface ErrorResponse {
  code?: number
  message?: string
}

/**
 * 对话会话记录
 */
export interface ChatSessionRecord {
  id: string
  title?: string
  modelConfigId?: string
  sysPrompt?: string
  messageCount?: number
  createTime?: string
  updateTime?: string
}

/**
 * 对话消息记录
 */
export interface ChatMessageRecord {
  id: string
  sessionId: string
  role: string
  content: string
  modelName?: string
  createTime?: string
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum?: number
  pageSize?: number
}

/**
 * 发送消息请求
 */
export interface SendChatMessageRequest {
  sessionId?: string
  modelConfigId?: string
  sysPrompt?: string
  content: string
}

/**
 * 更新会话请求
 */
export interface UpdateChatSessionRequest {
  id: string
  title?: string
  sysPrompt?: string
  modelConfigId?: string
}

/**
 * 对话流事件
 */
export interface ChatStreamEvent {
  type: 'session_created' | 'reasoning' | 'tool_call' | 'tool_result' | 'chunk' | 'done' | 'error' | 'memory_warning'
  sessionId?: string
  content?: string
  toolCallId?: string
  toolName?: string
  input?: Record<string, unknown>
  result?: string
  message?: string
  durationMs?: number
}

/**
 * 对话接口
 */
export const chatApi = {
  /**
   * 查询会话列表
   */
  listSessions(pageNum = 1, pageSize = 50) {
    return http.get<PageResult<ChatSessionRecord>>('/user/chat/sessions', {
      params: { pageNum, pageSize }
    })
  },

  /**
   * 查询消息历史
   */
  listMessages(sessionId: string, pageNum = 1, pageSize = 100) {
    return http.get<PageResult<ChatMessageRecord>>(`/user/chat/sessions/${sessionId}/messages`, {
      params: { pageNum, pageSize }
    })
  },

  /**
   * 更新会话信息
   */
  updateSession(payload: UpdateChatSessionRequest) {
    return http.put<void>('/user/chat/sessions', payload)
  },

  /**
   * 发送消息并消费 SSE 响应流
   */
  async sendMessage(
    payload: SendChatMessageRequest,
    handlers: {
      onEvent: (event: ChatStreamEvent) => void
      onError?: (error: Error) => void
    }
  ) {
    const token = localStorage.getItem('token') || ''
    const response = await fetch('/api/user/chat/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token
      },
      body: JSON.stringify(payload)
    })

    const contentType = response.headers.get('content-type') || ''
    if (!response.ok || !response.body || !contentType.includes('text/event-stream')) {
      const error = new Error(await resolveErrorMessage(response))
      handlers.onError?.(error)
      throw error
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          break
        }
        buffer += decoder.decode(value, { stream: true })
        const blocks = buffer.split('\n\n')
        buffer = blocks.pop() || ''
        for (const block of blocks) {
          const event = parseSseBlock(block)
          if (event) {
            handlers.onEvent(event)
          }
        }
      }
      if (buffer.trim()) {
        const event = parseSseBlock(buffer)
        if (event) {
          handlers.onEvent(event)
        }
      }
    } catch (error) {
      const streamError = error instanceof Error ? error : new Error('SSE 流解析失败')
      handlers.onError?.(streamError)
      throw streamError
    } finally {
      reader.releaseLock()
    }
  }
}

async function resolveErrorMessage(response: Response) {
  let message = response.statusText || '发送消息失败'
  try {
    const errorBody = await response.clone().json() as ErrorResponse
    if (errorBody?.message) {
      message = errorBody.message
    }
  } catch {
    // ignore parse error and keep fallback message
  }
  return message
}

/**
 * 解析 SSE 数据块
 */
function parseSseBlock(block: string): ChatStreamEvent | null {
  const dataLines = block
    .split('\n')
    .filter(line => line.startsWith('data:'))
    .map(line => line.slice(5).trim())
  if (!dataLines.length) {
    return null
  }
  const payload = dataLines.join('\n')
  if (!payload) {
    return null
  }
  return JSON.parse(payload) as ChatStreamEvent
}
