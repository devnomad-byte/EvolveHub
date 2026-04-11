import { http } from '@/utils/request'

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应
export interface LoginResponse {
  token: string
  id: number
  username: string
  nickname: string
  roles: string[]
  permissions: string[]
}

// 当前用户响应
export interface CurrentUserResponse {
  id: number
  username: string
  nickname: string
  email: string
  avatar: string
  deptId: number
  roles: string[]
  permissions: string[]
}

// 修改密码请求
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

// 认证相关 API
export const authApi = {
  /**
   * 用户登录
   */
  login: (data: LoginRequest) => {
    return http.post<LoginResponse>('/auth/login', data)
  },

  /**
   * 用户登出
   */
  logout: () => {
    return http.post('/auth/logout')
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser: () => {
    return http.get<CurrentUserResponse>('/auth/me')
  },

  /**
   * 修改密码
   */
  changePassword: (data: ChangePasswordRequest) => {
    return http.put('/auth/password', data)
  }
}
