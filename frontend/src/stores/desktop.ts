import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserRole, AppNotification } from '../types'

export interface LoginUser {
  id: number
  username: string
  displayName: string
  role: UserRole
  deptName: string
  avatar: string
}

export const useDesktopStore = defineStore('desktop', () => {
  const isLoggedIn = ref(false)
  const currentUser = ref<LoginUser | null>(null)
  const showNotifications = ref(false)
  const notifications = ref<AppNotification[]>([])
  const toasts = ref<{ id: string; message: string; type: 'success' | 'error' | 'info' }[]>([])

  // 从 localStorage 恢复登录状态
  function restoreSession() {
    const token = localStorage.getItem('token')
    const userInfo = localStorage.getItem('userInfo')

    if (token && userInfo) {
      try {
        const user = JSON.parse(userInfo)
        currentUser.value = user
        isLoggedIn.value = true
      } catch (e) {
        console.error('Failed to parse user info:', e)
        logout()
      }
    }
  }

  // 设置用户信息
  function setUserInfo(user: LoginUser) {
    currentUser.value = user
  }

  function login() {
    isLoggedIn.value = true
  }

  function logout() {
    isLoggedIn.value = false
    currentUser.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')

    // 调用后端登出接口
    import('../api').then(({ authApi }) => {
      authApi.logout().catch(console.error)
    })
  }

  function addToast(message: string, type: 'success' | 'error' | 'info' = 'info') {
    const id = Date.now().toString()
    toasts.value.push({ id, message, type })
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id)
    }, 3000)
  }

  function toggleNotifications() {
    showNotifications.value = !showNotifications.value
  }

  // 初始化时恢复会话
  restoreSession()

  return {
    isLoggedIn,
    currentUser,
    showNotifications,
    notifications,
    toasts,
    setUserInfo,
    login,
    logout,
    addToast,
    toggleNotifications
  }
})
