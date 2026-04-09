import request from '@/utils/request'
import type { LoginForm, LoginResponse, UserInfo } from '@/types'

export const login = (data: LoginForm) => {
  return request<LoginResponse>({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

export const getUserInfo = () => {
  return request<UserInfo>({
    url: '/auth/userinfo',
    method: 'GET'
  })
}

export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'POST'
  })
}
