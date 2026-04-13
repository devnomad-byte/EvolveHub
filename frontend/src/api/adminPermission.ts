import { http } from '@/utils/request'

// 权限信息
export interface PermissionInfo {
  id: number
  parentId: number
  permName: string
  permCode: string
  permType: string
  path?: string
  icon?: string
  sort: number
  status: number
  createBy?: number
  createTime: string
  updateTime: string
  deleted?: number
  gradient?: string
  defaultWidth?: number
  defaultHeight?: number
  minWidth?: number
  minHeight?: number
  dockOrder?: number
  isDesktopIcon?: number
}

// 创建权限请求
export interface CreatePermissionRequest {
  parentId: number
  permName: string
  permCode: string
  permType: string
  path?: string
  icon?: string
  sort?: number
}

// 更新权限请求
export interface UpdatePermissionRequest {
  id: number
  parentId?: number
  permName?: string
  permCode?: string
  permType?: string
  path?: string
  icon?: string
  sort?: number
  status?: number
}

// 权限管理 API（admin 模块）
export const adminPermissionApi = {
  /**
   * 创建权限
   */
  create: (data: CreatePermissionRequest) => {
    return http.post<{ id: number }>('/admin/permission/create', data)
  },

  /**
   * 分页查询权限列表
   */
  list: (pageNum = 1, pageSize = 1000) => {
    return http.get<{ records: PermissionInfo[]; total: number }>(`/admin/permission/list`, {
      params: { pageNum, pageSize }
    })
  },

  /**
   * 查询单个权限
   */
  get: (id: number) => {
    return http.get<PermissionInfo>(`/admin/permission/${id}`)
  },

  /**
   * 更新权限
   */
  update: (data: UpdatePermissionRequest) => {
    return http.put('/admin/permission/update', data)
  },

  /**
   * 删除权限
   */
  delete: (id: number) => {
    return http.delete(`/admin/permission/${id}`)
  }
}
