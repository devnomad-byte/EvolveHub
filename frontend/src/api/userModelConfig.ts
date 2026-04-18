import { http } from '@/utils/request'
import type {
  ModelConfigInfo,
  CreateModelConfigRequest,
  UpdateModelConfigRequest
} from './adminModelConfig'

export const userModelConfigApi = {
  list: (pageNum = 1, pageSize = 100) => {
    return http.get<{ records: ModelConfigInfo[]; total: number }>('/user/model-config/list', {
      params: { pageNum, pageSize }
    })
  },

  create: (data: CreateModelConfigRequest) => {
    return http.post<{ id: number }>('/user/model-config/create', data)
  },

  update: (data: UpdateModelConfigRequest) => {
    return http.put('/user/model-config/update', data)
  },

  delete: (id: number) => {
    return http.delete(`/user/model-config/${id}`)
  }
}
