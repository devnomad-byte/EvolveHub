/**
 * 文件上传工具函数
 * 参考 QwenPaw 项目的实现，使用原生 fetch API + XMLHttpRequest 支持进度监控
 */

import { http } from '@/utils/request'

// 获取 API 基础 URL
function getApiUrl(): string {
  const base = import.meta.env.VITE_API_BASE_URL || ''
  return base ? `${base}/api` : '/api'
}

/**
 * 上传 MCP Server ZIP 包（带进度监控）
 * @param file ZIP 文件
 * @param options 上传选项
 * @param onProgress 进度回调 (progress: 0-100)
 * @returns 上传结果，包含文件存储路径
 */
export async function uploadMcpZip(
  file: File,
  options?: {
    overwrite?: boolean
    target_name?: string
  },
  onProgress?: (progress: number) => void
): Promise<{
  success: boolean
  packagePath: string  // MinIO/S3 存储路径（驼峰命名）
  fileSize: number
  fileName: string
  message?: string
}> {
  return new Promise((resolve, reject) => {
    const formData = new FormData()
    formData.append('file', file)

    // 构建查询参数
    const params = new URLSearchParams()
    if (options?.overwrite !== undefined) {
      params.set('overwrite', String(options.overwrite))
    }
    if (options?.target_name) {
      params.set('target_name', options.target_name)
    }

    const queryString = params.toString()
    const url = `${getApiUrl()}/admin/mcp-config/upload${queryString ? `?${queryString}` : ''}`

    // 使用 XMLHttpRequest 支持进度监控
    const xhr = new XMLHttpRequest()

    // 监听上传进度
    xhr.upload.addEventListener('progress', (e) => {
      if (e.lengthComputable) {
        const progress = Math.round((e.loaded / e.total) * 100)
        console.log(`📊 上传进度: ${progress}%`)
        if (onProgress) {
          onProgress(progress)
        }
      }
    })

    // 监听上传完成
    xhr.addEventListener('load', () => {
      console.log('✅ 上传请求完成，状态:', xhr.status)

      if (xhr.status >= 200 && xhr.status < 300) {
        try {
          const result = JSON.parse(xhr.responseText)
          console.log('📦 上传响应数据:', result)

          if (result.code !== 200) {
            console.error('❌ 业务错误:', result.message)
            reject(new Error(result.message || '上传失败'))
            return
          }

          console.log('✅ 最终返回的数据:', result.data)
          resolve(result.data)
        } catch (e) {
          console.error('❌ JSON 解析失败:', e)
          reject(new Error('响应解析失败'))
        }
      } else {
        console.error('❌ HTTP 错误:', xhr.status, xhr.responseText)
        reject(new Error(`上传失败: HTTP ${xhr.status}`))
      }
    })

    // 监听错误
    xhr.addEventListener('error', () => {
      console.error('❌ 网络错误')
      reject(new Error('网络错误'))
    })

    // 发送请求
    console.log('🚀 开始上传:', url)
    xhr.open('POST', url)
    xhr.setRequestHeader('Authorization', localStorage.getItem('token') || '')
    xhr.send(formData)
  })
}

/**
 * 上传技能 ZIP 包（预留接口）
 */
export async function uploadSkillZip(
  file: File,
  options?: {
    enable?: boolean
    overwrite?: boolean
    target_name?: string
  }
): Promise<{
  success: boolean
  workspace_path?: string
  imported?: string[]
  count?: number
  message?: string
}> {
  const formData = new FormData()
  formData.append('file', file)

  const params = new URLSearchParams()
  if (options?.enable !== undefined) {
    params.set('enable', String(options.enable))
  }
  if (options?.overwrite !== undefined) {
    params.set('overwrite', String(options.overwrite))
  }
  if (options?.target_name) {
    params.set('target_name', options.target_name)
  }

  const queryString = params.toString()
  const url = `/admin/skill-config/upload${queryString ? `?${queryString}` : ''}`

  return http.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
