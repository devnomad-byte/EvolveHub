import { http } from '@/utils/request'

export interface FileNode {
  name: string
  path: string
  type: 'file' | 'folder'
  size: number
  modifiedTime: string
  children?: FileNode[]
}

export const adminSkillFileApi = {
  listFiles(skillId: number): Promise<FileNode[]> {
    return http.get<FileNode[]>(`/admin/skill-config/${skillId}/files`)
  },

  uploadFile(skillId: number, path: string, file: File, skipScan = false): Promise<void> {
    const formData = new FormData()
    formData.append('path', path)
    formData.append('file', file)
    if (skipScan) formData.append('skipScan', 'true')
    return http.post(`/admin/skill-config/${skillId}/file`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  async downloadFile(skillId: number, path: string): Promise<Blob> {
    const response = await http.get<Blob>(`/admin/skill-config/${skillId}/file`, {
      params: { path },
      responseType: 'blob'
    })
    return response
  },

  deleteFile(skillId: number, path: string): Promise<void> {
    return http.delete(`/admin/skill-config/${skillId}/file`, { params: { path } })
  },

  createFolder(skillId: number, path: string): Promise<void> {
    return http.post(`/admin/skill-config/${skillId}/folder`, null, { params: { path } })
  }
}
