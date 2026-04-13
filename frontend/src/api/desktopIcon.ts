import { http } from '@/utils/request'

// 桌面图标信息
export interface DesktopIconInfo {
  id: number
  permName: string
  permCode: string
  icon: string
  gradient: string
  path: string
  sort: number
  status: number
  defaultWidth: number
  defaultHeight: number
  minWidth: number
  minHeight: number
  dockOrder: number
}

// 创建桌面图标请求
export interface CreateDesktopIconRequest {
  permName: string
  permCode: string
  icon: string
  gradient: string
  path: string
  sort: number
  defaultWidth: number
  defaultHeight: number
  minWidth: number
  minHeight: number
  dockOrder: number
  status: number
}

// 更新桌面图标请求
export interface UpdateDesktopIconRequest {
  id: number
  permName?: string
  permCode?: string
  icon?: string
  gradient?: string
  path?: string
  sort?: number
  defaultWidth?: number
  defaultHeight?: number
  minWidth?: number
  minHeight?: number
  dockOrder?: number
  status?: number
}

// 桌面图标管理 API
export const desktopIconApi = {
  /**
   * 获取所有桌面图标
   */
  list: () => {
    return http.get<DesktopIconInfo[]>('/admin/desktop-icon/list')
  },

  /**
   * 获取单个桌面图标
   */
  get: (id: number) => {
    return http.get<DesktopIconInfo>(`/admin/desktop-icon/${id}`)
  },

  /**
   * 创建桌面图标
   */
  create: (data: CreateDesktopIconRequest) => {
    return http.post<DesktopIconInfo>('/admin/desktop-icon', data)
  },

  /**
   * 更新桌面图标
   */
  update: (data: UpdateDesktopIconRequest) => {
    return http.put<DesktopIconInfo>(`/admin/desktop-icon/${data.id}`, data)
  },

  /**
   * 删除桌面图标
   */
  delete: (id: number) => {
    return http.delete(`/admin/desktop-icon/${id}`)
  }
}
