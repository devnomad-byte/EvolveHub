import { http } from '@/utils/request'

export interface ResourceGrantInfo {
  id: number
  userId: number
  deptId?: number | null
  roleId?: number | null
  resourceType: string
  resourceId: number
  createTime: string
}

export const resourceGrantApi = {
  /**
   * 授权资源给用户
   */
  assign: (userId: number, resourceType: string, resourceId: number) => {
    return http.post<{ id: number }>('/admin/resource-grant/assign', {
      userId,
      resourceType,
      resourceId
    })
  },

  /**
   * 撤销用户的资源授权
   */
  revoke: (userId: number, resourceType: string, resourceId: number) => {
    return http.post('/admin/resource-grant/revoke', {
      userId,
      resourceType,
      resourceId
    })
  },

  /**
   * 按资源查询所有授权记录
   */
  listByResource: (resourceType: string, resourceId: number) => {
    return http.get<ResourceGrantInfo[]>('/admin/resource-grant/list-by-resource', {
      params: { resourceType, resourceId }
    })
  }
}
