export interface LoginForm {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  roles: string[]
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface RouteMeta {
  title: string
  icon?: string
  requiresAuth?: boolean
}
