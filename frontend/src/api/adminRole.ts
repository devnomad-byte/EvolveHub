import { http } from '@/utils/request'

// 角色信息
export interface RoleInfo {
  id: number
  roleName: string
  roleCode: string
  dataScope: number
  sort: number
  status: number
  remark?: string
  createBy?: number
  createTime: string
  updateTime: string
  deleted?: number
}

// 创建角色请求
export interface CreateRoleRequest {
  roleName: string
  roleCode: string
  dataScope: number
  sort?: number
  remark?: string
}

// 更新角色请求
export interface UpdateRoleRequest {
  id: number
  roleName?: string
  roleCode?: string
  dataScope?: number
  sort?: number
  status?: number
  remark?: string
}

// 分配角色权限请求
export interface AssignRolePermissionRequest {
  roleId: number
  permissionId: number
}

// 角色管理 API（admin 模块）
export const adminRoleApi = {
  /**
   * 创建角色
   */
  create: (data: CreateRoleRequest) => {
    return http.post<{ id: number }>('/admin/role/create', data)
  },

  /**
   * 查询单个角色
   */
  get: (id: number) => {
    return http.get<RoleInfo>(`/admin/role/${id}`)
  },

  /**
   * 分页查询角色列表
   */
  list: (pageNum = 1, pageSize = 100) => {
    return http.get<{ records: RoleInfo[]; total: number }>(`/admin/role/list`, {
      params: { pageNum, pageSize }
    })
  },

  /**
   * 更新角色
   */
  update: (data: UpdateRoleRequest) => {
    return http.put('/admin/role/update', data)
  },

  /**
   * 删除角色
   */
  delete: (id: number) => {
    return http.delete(`/admin/role/${id}`)
  },

  /**
   * 为角色分配权限
   */
  assignPermission: (data: AssignRolePermissionRequest) => {
    return http.post('/admin/role/assign-permission', data)
  },

  /**
   * 移除角色权限
   */
  removePermission: (data: AssignRolePermissionRequest) => {
    return http.post('/admin/role/remove-permission', data)
  },

  /**
   * 获取角色已有的权限ID列表
   */
  getPermissions: (roleId: number) => {
    return http.get<number[]>(`/admin/role/${roleId}/permissions`)
  }
}
