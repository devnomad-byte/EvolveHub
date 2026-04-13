import { http } from '@/utils/request'

export interface ModelProviderInfo {
  id: number
  name: string
  logoUrl?: string
  defaultBaseUrl?: string
  sort: number
  enabled: number
  createBy?: number
  createTime: string
  updateTime: string
}

export interface CreateModelProviderRequest {
  name: string
  logoUrl?: string
  defaultBaseUrl?: string
  sort?: number
  enabled: number
}

export interface UpdateModelProviderRequest {
  id: number
  name?: string
  logoUrl?: string
  defaultBaseUrl?: string
  sort?: number
  enabled?: number
}

export const adminModelProviderApi = {
  list: () => {
    return http.get<ModelProviderInfo[]>('/admin/model-provider/list')
  },

  get: (id: number) => {
    return http.get<ModelProviderInfo>(`/admin/model-provider/${id}`)
  },

  create: (data: CreateModelProviderRequest) => {
    return http.post<{ id: number }>('/admin/model-provider/create', data)
  },

  update: (data: UpdateModelProviderRequest) => {
    return http.put('/admin/model-provider/update', data)
  },

  delete: (id: number) => {
    return http.delete(`/admin/model-provider/${id}`)
  }
}
