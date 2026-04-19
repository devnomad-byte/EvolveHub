import { http } from '@/utils/request'

/** 桌面分类 */
export interface CategoryResponse {
  id: number
  name: string
  icon: string
  color: string
  sort: number
  status: number
  iconCount: number
  createBy: number
  createTime: string
  updateTime: string
}

/** 桌面图标配置 */
export interface IconResponse {
  id: number
  permId: number
  categoryId: number
  isDesktop: number
  sort: number
  permCode: string
  menuName: string
  icon: string
  gradient: string
  createBy: number
  createTime: string
  updateTime: string
}

/** 完整桌面配置 */
export interface DesktopAllResponse {
  categories: CategoryResponse[]
  icons: IconResponse[]
  desktopIcons: IconResponse[]
}

/** 创建分类请求 */
export interface CreateCategoryRequest {
  name: string
  icon?: string
  color?: string
  sort?: number
  status?: number
}

/** 更新分类请求 */
export interface UpdateCategoryRequest {
  id: number
  name?: string
  icon?: string
  color?: string
  sort?: number
  status?: number
}

/** 更新图标请求 */
export interface UpdateIconRequest {
  permId: number
  categoryId?: number
  isDesktop?: number
  sort?: number
}

export const desktopApi = {
  /** 获取分类列表 */
  listCategories: () => {
    return http.get<CategoryResponse[]>('/admin/desktop/categories')
  },

  /** 创建分类 */
  createCategory: (data: CreateCategoryRequest) => {
    return http.post<CategoryResponse>('/admin/desktop/categories', data)
  },

  /** 更新分类 */
  updateCategory: (data: UpdateCategoryRequest) => {
    return http.put<CategoryResponse>(`/admin/desktop/categories/${data.id}`, data)
  },

  /** 删除分类 */
  deleteCategory: (id: number) => {
    return http.delete<boolean>(`/admin/desktop/categories/${id}`)
  },

  /** 获取图标配置列表 */
  listIcons: () => {
    return http.get<IconResponse[]>('/admin/desktop/icons')
  },

  /** 更新图标配置 */
  updateIcon: (data: UpdateIconRequest) => {
    return http.put<IconResponse>(`/admin/desktop/icons/${data.permId}`, data)
  },

  /** 获取完整桌面配置 */
  getAll: () => {
    return http.get<DesktopAllResponse>('/admin/desktop/all')
  }
}
