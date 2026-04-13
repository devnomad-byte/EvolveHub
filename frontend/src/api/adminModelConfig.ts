import { http } from '@/utils/request'

export interface ModelConfigInfo {
  id: number
  name: string
  provider: string
  apiKey: string
  baseUrl?: string
  enabled: number
  modelType: string
  createBy?: number
  createTime: string
  updateTime: string
  deleted?: number
}

export interface CreateModelConfigRequest {
  name: string
  provider: string
  apiKey: string
  baseUrl?: string
  enabled: number
  modelType?: string
}

export interface UpdateModelConfigRequest {
  id: number
  name?: string
  provider?: string
  apiKey?: string
  baseUrl?: string
  enabled?: number
  modelType?: string
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
  }
}
