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
  deptId?: number
  roleId?: number
  status?: number
}

// 重置密码请求
export interface ResetPasswordRequest {
  userId: number
  newPassword: string
}

// 用户管理 API
export const userApi = {
  /**
   * 创建用户（仅超级管理员）
   */
  create: (data: CreateUserRequest) => {
    return http.post<UserInfo>('/auth/admin/users', data)
  },

  /**
   * 用户列表（超级管理员和普通管理员）
   */
  list: () => {
    return http.get<UserInfo[]>('/auth/admin/users')
  },

  /**
   * 修改用户（超级管理员和普通管理员）
   */
  update: (data: UpdateUserRequest) => {
    return http.put<UserInfo>('/auth/admin/users', data)
  },

  /**
   * 删除用户（仅超级管理员）
   */
  delete: (userId: number) => {
    return http.delete(`/auth/admin/users/${userId}`)
  },

  /**
   * 重置密码（仅超级管理员）
   */
  resetPassword: (data: ResetPasswordRequest) => {
    return http.put('/auth/admin/users/password/reset', data)
  }
}
