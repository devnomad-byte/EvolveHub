import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'

// 响应数据类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['satoken'] = token // Sa-Token 使用 satoken 作为 header
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // Blob 类型直接返回（用于文件下载）
    if (response.data instanceof Blob) {
      return response.data
    }

    const res = response.data as ApiResponse

    // 如果 code 不是 200，认为是错误
    if (res.code !== 200) {
      console.error('API Error:', res.message)

      // 401: Token 过期或未登录
      if (res.code === 401) {
        handleUnauthorized()
      }

      const error: any = new Error(res.message || '请求失败')
      error.code = res.code
      error.data = res.data
      return Promise.reject(error)
    }

    return res.data
  },
  (error: AxiosError<ApiResponse>) => {
    console.error('Request Error:', error.message)

    // 处理 401
    if (error.response?.status === 401) {
      handleUnauthorized()
    }

    const message = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

// 处理未授权
function handleUnauthorized() {
  // 清除 token
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')

  // 跳转到登录页
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

// 导出封装的请求方法
export default request

// 便捷方法
export const http = {
  get: <T = any>(url: string, config?: AxiosRequestConfig) => {
    return request.get<any, T>(url, config)
  },

  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => {
    return request.post<any, T>(url, data, config)
  },

  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => {
    return request.put<any, T>(url, data, config)
  },

  delete: <T = any>(url: string, config?: AxiosRequestConfig) => {
    return request.delete<any, T>(url, config)
  }
}
