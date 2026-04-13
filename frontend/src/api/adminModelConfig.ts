import { http } from '@/utils/request'

/** 模型类型 */
export type ModelType = 'LLM' | 'EMBEDDING'
/** 资源范围：SYSTEM-系统级 USER-用户级 */
export type ModelScope = 'SYSTEM' | 'USER'

export interface ModelConfigInfo {
  id: number
  name: string
  provider: string
  apiKey: string
  baseUrl?: string
  enabled: number
  modelType: ModelType
  /** 资源范围：SYSTEM-系统级 USER-用户级 */
  scope?: ModelScope
  /** 资源所有者 ID，scope=SYSTEM 时为 NULL */
  ownerId?: number | null
  createBy?: number
  createTime: string
  updateTime: string
  deleted?: number
}

/** Admin 查看全部模型（带所有者信息） */
export interface ModelConfigWithOwner {
  id: number
  name: string
  provider: string
  /** 脱敏后的 API Key */
  apiKey: string
  baseUrl?: string
  enabled: number
  modelType: ModelType
  scope: ModelScope
  ownerId?: number | null
  ownerNickname?: string
  ownerUsername?: string
}

export interface CreateModelConfigRequest {
  name: string
  provider: string
  apiKey: string
  baseUrl?: string
  enabled: number
  modelType?: ModelType
  scope?: ModelScope
}

export interface UpdateModelConfigRequest {
  id: number
  name?: string
  provider?: string
  apiKey?: string
  baseUrl?: string
  enabled?: number
  modelType?: ModelType
}

export interface TestConnectionRequest {
  provider: string
  apiKey: string
  baseUrl?: string
  modelType?: ModelType
}

export interface TestConnectionResponse {
  success: boolean
  message: string
}

export const adminModelConfigApi = {
  create: (data: CreateModelConfigRequest) => {
    return http.post<{ id: number }>('/admin/model-config/create', data)
  },

  get: (id: number) => {
    return http.get<ModelConfigInfo>(`/admin/model-config/${id}`)
  },

  list: (pageNum = 1, pageSize = 100) => {
    return http.get<{ records: ModelConfigInfo[]; total: number }>(`/admin/model-config/list`, {
      params: { pageNum, pageSize }
    })
  },

  update: (data: UpdateModelConfigRequest) => {
    return http.put('/admin/model-config/update', data)
  },

  delete: (id: number) => {
    return http.delete(`/admin/model-config/${id}`)
  },

  /** 普通用户获取公共模型 + 个人模型 */
  myList: () => {
    return http.get<{ records: ModelConfigInfo[]; total: number }>(`/admin/model-config/my-list`)
  },

  /** Admin 获取全部模型（带所有者信息） */
  adminAll: (pageNum = 1, pageSize = 100) => {
    return http.get<{ records: ModelConfigWithOwner[]; total: number }>(`/admin/model-config/admin-all`, {
      params: { pageNum, pageSize }
    })
  },

  /** 测试模型连接 */
  testConnection: (data: TestConnectionRequest) => {
    return http.post<TestConnectionResponse>(`/admin/model-config/test-connection`, data)
  }
}
