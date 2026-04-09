import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo, LoginForm } from '@/types'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const login = async (_form: LoginForm) => {
    // Mock login - always succeeds
    const mockUserInfo: UserInfo = {
      id: 1,
      username: _form.username || 'admin',
      nickname: '管理员',
      email: 'admin@example.com',
      roles: ['admin']
    }

    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 800))

    token.value = 'mock-token-' + Date.now()
    userInfo.value = mockUserInfo
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(mockUserInfo))
    router.push('/')
  }

  const fetchUserInfo = async () => {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      userInfo.value = JSON.parse(stored)
    }
  }

  const logout = async () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  return {
    token,
    userInfo,
    login,
    fetchUserInfo,
    logout
  }
})
