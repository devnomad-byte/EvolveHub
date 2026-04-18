import { http } from '@/utils/request'

// S3 Bucket 响应
export interface S3Bucket {
  name: string
  creationDate: string
  fileCount: number
  totalSize: number | string
}

// S3 文件响应
export interface S3File {
  name: string
  path: string
  type: 'file' | 'folder'
  size: number
  modifiedTime: string | null
}

// S3 Admin API
export const s3BrowserApi = {
  /**
   * 获取 Bucket 列表
   */
  getBuckets: () => {
    return http.get<S3Bucket[]>('/admin/s3/buckets')
  },

  /**
   * 获取文件列表
   */
  getFiles: (bucketName: string, path?: string, pageNum = 1, pageSize = 100) => {
    return http.get<S3File[]>('/admin/s3/files', {
      params: { bucketName, path: path || '', pageNum, pageSize }
    })
  },

  /**
   * 上传文件
   */
  uploadFile: (bucketName: string, path: string | undefined, file: File) => {
    const formData = new FormData()
    formData.append('bucketName', bucketName)
    if (path) {
      formData.append('path', path)
    }
    formData.append('file', file)
    return http.post('/admin/s3/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * 删除文件或文件夹
   */
  deleteFile: (bucketName: string, objectKey: string) => {
    return http.delete('/admin/s3/files', {
      params: { bucketName, objectKey }
    })
  },

  /**
   * 创建文件夹
   */
  createFolder: (bucketName: string, folderPath: string | undefined, folderName: string) => {
    return http.post('/admin/s3/folders', null, {
      params: { bucketName, folderPath: folderPath || '', folderName }
    })
  },

  /**
   * 重命名文件或文件夹
   */
  rename: (bucketName: string, sourceKey: string, targetName: string) => {
    return http.put('/admin/s3/rename', null, {
      params: { bucketName, sourceKey, targetName }
    })
  },

  /**
   * 下载文件
   */
  downloadFile: (bucketName: string, objectKey: string) => {
    return http.get<Blob>('/admin/s3/download', {
      params: { bucketName, objectKey },
      responseType: 'blob'
    })
  }
}
