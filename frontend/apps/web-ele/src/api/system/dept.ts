import {requestClient} from '#/api/request';

export interface Dept {
  id: number;
  parentId: number;
  deptName: string;
  sort: number;
  status: 0 | 1;
  createTime?: string;
  updateTime?: string;
  children?: Dept[];
}

export interface CreateDeptRequest {
  parentId?: number;
  deptName: string;
  sort?: number;
  status?: 0 | 1;
}

export interface UpdateDeptRequest extends Partial<CreateDeptRequest> {
  id: number;
}

export interface PageResponse<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

/**
 * 获取部门列表（分页）
 */
export async function getDeptListApi(pageNum = 1, pageSize = 100) {
  return requestClient.get<PageResponse<Dept>>('/dept/list', {
    params: { pageNum, pageSize },
  });
}

/**
 * 获取部门详情
 */
export async function getDeptByIdApi(id: number) {
  return requestClient.get<Dept>(`/dept/${id}`);
}

/**
 * 创建部门
 */
export async function createDeptApi(data: CreateDeptRequest) {
  return requestClient.post<{ id: number }>('/dept/create', data);
}

/**
 * 更新部门
 */
export async function updateDeptApi(data: UpdateDeptRequest) {
  return requestClient.put<{ id: number }>('/dept/update', data);
}

/**
 * 删除部门
 */
export async function deleteDeptApi(id: number) {
  return requestClient.delete(`/dept/${id}`);
}
