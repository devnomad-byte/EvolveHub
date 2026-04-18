import { http } from '@/utils/request'

export interface MemoryProfileRecord {
  userId: number
  markdownContent: string
}

export interface MemoryManagedItemRecord {
  id: number
  vectorDocId?: string
  memoryKey: string
  memoryType: string
  sourceKind: string
  content: string
  importance?: number
  updatedAt?: string
}

export interface SaveMemoryProfileRequest {
  targetUserId?: number
  markdownContent: string
}

export const memoryApi = {
  getProfile(targetUserId?: number) {
    return http.get<MemoryProfileRecord>('/user/memory/profile', {
      params: targetUserId ? { targetUserId } : undefined
    })
  },

  saveProfile(payload: SaveMemoryProfileRequest) {
    return http.put<MemoryProfileRecord>('/user/memory/profile', payload)
  },

  listItems(targetUserId?: number) {
    return http.get<MemoryManagedItemRecord[]>('/user/memory/items', {
      params: targetUserId ? { targetUserId } : undefined
    })
  },

  deleteItem(id: number, targetUserId?: number) {
    return http.delete<void>(`/user/memory/items/${id}`, {
      params: targetUserId ? { targetUserId } : undefined
    })
  }
}
