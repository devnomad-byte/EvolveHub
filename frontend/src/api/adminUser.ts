import { http } from '@/utils/request'

// 用户信息
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  deptId: number
  deptName: string
  roles: RoleInfo[]
  status: number
  createTime: string
  updateTime: string
}

// 角色信息
export interface RoleInfo {
  id: number
  roleName: string
  roleCode: string
}

// 创建用户请求
export interface CreateUserRequest {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
  deptId: number
  roleId: number
  status?: number
}

// 更新用户请求
export interface UpdateUserRequest {
  id: number
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  deptId?: number
  roleId?: number
  status?: number
}

// 分配角色请求
export interface AssignUserRoleRequest {
  userId: number
  roleId: number
}

// 移除角色请求
export interface RemoveUserRoleRequest {
  userId: number
  roleId: number
}

// 重置密码请求
export interface ResetPasswordRequest {
  userId: number
  newPassword: string
}

// 用户管理 API（admin 模块）
export const adminUserApi = {
  /**
   * 创建用户
   */
  create: (data: CreateUserRequest) => {
    return http.post<UserInfo>('/admin/user/create', data)
  },

  /**
   * 用户列表
   */
  list: () => {
    return http.get<UserInfo[]>('/admin/user/list')
  },

  /**
   * 查询单个用户
   */
  get: (id: number) => {
    return http.get<UserInfo>(`/admin/user/${id}`)
  },

  /**
   * 更新用户
   */
  update: (data: UpdateUserRequest) => {
    return http.put<UserInfo>(`/admin/user/update`, data)
  },

  /**
   * 删除用户
   */
  delete: (id: number) => {
    return http.delete(`/admin/user/${id}`)
  },

  /**
   * 分配角色
   */
  assignRole: (data: AssignUserRoleRequest) => {
    return http.post('/admin/user/assign-role', data)
  },

  /**
   * 移除角色
   */
  removeRole: (data: RemoveUserRoleRequest) => {
    return http.post('/admin/user/remove-role', data)
  },

  /**
   * 重置密码
   */
  resetPassword: (data: ResetPasswordRequest) => {
    return http.put('/admin/user/password/reset', data)
  }
}
