import {requestClient} from '#/api/request';

export interface Permission {
  id: number;
  parentId: number;
  permName: string;
  permCode: string;
  permType: 'MENU' | 'BUTTON' | 'API';
  path?: string;
  icon?: string;
  sort: number;
  status: 0 | 1;
  createTime?: string;
  updateTime?: string;
  children?: Permission[];
}

export interface CreatePermissionRequest {
  parentId?: number;
  permName: string;
  permCode?: string;
  permType: 'MENU' | 'BUTTON' | 'API';
  path?: string;
  icon?: string;
  sort?: number;
  status?: 0 | 1;
}

export interface UpdatePermissionRequest extends Partial<CreatePermissionRequest> {
  id: number;
}

export interface PageResponse<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

// 权限类型选项
export const PERM_TYPE_OPTIONS = [
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
  { label: 'API接口', value: 'API' },
];

/**
 * 获取权限列表（分页）
 */
export async function getPermissionListApi(pageNum = 1, pageSize = 100) {
  return requestClient.get<PageResponse<Permission>>('/permission/list', {
    params: { pageNum, pageSize },
  });
}

/**
 * 获取权限详情
 */
export async function getPermissionByIdApi(id: number) {
  return requestClient.get<Permission>(`/permission/${id}`);
}

/**
 * 创建权限
 */
export async function createPermissionApi(data: CreatePermissionRequest) {
  return requestClient.post<{ id: number }>('/permission/create', data);
}

/**
 * 更新权限
 */
export async function updatePermissionApi(data: UpdatePermissionRequest) {
  return requestClient.put<{ id: number }>('/permission/update', data);
}

/**
 * 删除权限
 */
export async function deletePermissionApi(id: number) {
  return requestClient.delete(`/permission/${id}`);
}
