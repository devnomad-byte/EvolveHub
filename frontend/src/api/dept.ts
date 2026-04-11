import { http } from '@/utils/request'

// 部门信息
export interface DeptInfo {
  id: number
  deptName: string
  parentId: number
  sort: number
  status: number
  createTime: string
  updateTime: string
  children?: DeptInfo[]
}

// 创建部门请求
export interface CreateDeptRequest {
  deptName: string
  parentId?: number
  sort?: number
  status?: number
}

// 更新部门请求
export interface UpdateDeptRequest {
  id: number
  deptName: string
  parentId?: number
  sort?: number
  status?: number
}

// 部门管理 API
export const deptApi = {
  /**
   * 创建部门（超级管理员和普通管理员）
   */
  create: (data: CreateDeptRequest) => {
    return http.post<DeptInfo>('/admin/dept', data)
  },

  /**
   * 部门树（超级管理员和普通管理员）
   */
  tree: () => {
    return http.get<DeptInfo[]>('/admin/dept/tree')
  },

  /**
   * 修改部门（超级管理员和普通管理员）
   */
  update: (data: UpdateDeptRequest) => {
    return http.put<DeptInfo>('/admin/dept', data)
  },

  /**
   * 删除部门（仅超级管理员）
   */
  delete: (id: number) => {
    return http.delete(`/admin/dept/${id}`)
  }
}
