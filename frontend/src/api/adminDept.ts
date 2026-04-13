import { http } from '@/utils/request'

// 部门信息
export interface DeptInfo {
  id: number
  parentId: number
  deptName: string
  sort: number
  status: number
  createBy?: number
  createTime: string
  updateTime: string
  deleted?: number
}

// 创建部门请求
export interface CreateDeptRequest {
  parentId: number
  deptName: string
  sort?: number
}

// 更新部门请求
export interface UpdateDeptRequest {
  id: number
  parentId?: number
  deptName?: string
  sort?: number
  status?: number
}

// 部门管理 API（admin 模块）
export const adminDeptApi = {
  /**
   * 创建部门
   */
  create: (data: CreateDeptRequest) => {
    return http.post<{ id: number }>('/admin/dept/create', data)
  },

  /**
   * 查询单个部门
   */
  get: (id: number) => {
    return http.get<DeptInfo>(`/admin/dept/${id}`)
  },

  /**
   * 分页查询部门列表
   */
  list: (pageNum = 1, pageSize = 100) => {
    return http.get<{ records: DeptInfo[]; total: number }>(`/admin/dept/list`, {
      params: { pageNum, pageSize }
    })
  },

  /**
   * 更新部门
   */
  update: (data: UpdateDeptRequest) => {
    return http.put('/admin/dept/update', data)
  },

  /**
   * 删除部门
   */
  delete: (id: number) => {
    return http.delete(`/admin/dept/${id}`)
  }
}
