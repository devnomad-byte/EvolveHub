import { http } from '@/utils/request'

// 菜单信息
export interface MenuInfo {
  id: number
  parentId: number
  menuName: string
  permCode: string
  menuType: string
  path: string
  icon: string
  sort: number
  status: number
  children?: MenuInfo[]
}

// 菜单管理 API
export const menuApi = {
  /**
   * 获取当前用户的菜单树
   */
  getMenus: () => {
    return http.get<MenuInfo[]>('/auth/admin/system/menus')
  },

  /**
   * 获取用户界面菜单
   */
  getAppMenus: () => {
    return http.get<MenuInfo[]>('/auth/app/menus')
  }
}
