import { http } from '@/utils/request'

export interface EnvVarItem {
  id: number
  varKey: string
  varValue: string
  varGroup: string
  description: string
  isSensitive: number
  status: number
  sort: number
  createBy: number
  createTime: string
  updateTime: string
}

export const envVarApi = {
  list(pageNum = 1, pageSize = 20, group?: string): Promise<any> {
    return http.get('/admin/env-var/list', {
      params: { pageNum, pageSize, group }
    })
  },

  getById(id: number): Promise<EnvVarItem> {
    return http.get(`/admin/env-var/${id}`)
  },

  create(data: Partial<EnvVarItem>): Promise<number> {
    return http.post('/admin/env-var/create', data)
  },

  update(data: Partial<EnvVarItem>): Promise<void> {
    return http.put('/admin/env-var/update', data)
  },

  delete(id: number): Promise<void> {
    return http.delete(`/admin/env-var/${id}`)
  }
}
