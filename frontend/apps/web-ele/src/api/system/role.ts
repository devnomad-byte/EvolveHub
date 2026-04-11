import {requestClient} from '#/api/request';

export interface Role {
  id: number;
  roleName: string;
  roleCode: string;
  dataScope: 1 | 2 | 3 | 4 | 5;
  sort: number;
  status: 0 | 1;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CreateRoleRequest {
  roleName: string;
  roleCode: string;
  dataScope: 1 | 2 | 3 | 4 | 5;
  sort?: number;
  status?: 0 | 1;
  remark?: string;
}

export interface UpdateRoleRequest extends Partial<CreateRoleRequest> {
  id: number;
}

export interface AssignRolePermissionRequest {
  roleId: number;
  permissionId: number;
}

export interface RemoveRolePermissionRequest {
  roleId: number;
  permissionId: number;
}

export interface AssignRoleDataScopeRequest {
  roleId: number;
  deptIds: number[];
}

export interface PageResponse<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

// 数据权限范围选项
export const DATA_SCOPE_OPTIONS = [
  { label: '全部数据', value: 1 },
  { label: '本部门及子部门', value: 2 },
  { label: '仅本部门', value: 3 },
  { label: '仅本人', value: 4 },
  { label: '自定义部门', value: 5 },
];

/**
 * 获取角色分页列表
 */
export async function getRoleListApi(pageNum = 1, pageSize = 10) {
  return requestClient.get<PageResponse<Role>>('/role/list', {
    params: { pageNum, pageSize },
  });
}

/**
 * 获取角色详情
 */
export async function getRoleByIdApi(id: number) {
  return requestClient.get<Role>(`/role/${id}`);
}

/**
 * 创建角色
 */
export async function createRoleApi(data: CreateRoleRequest) {
  return requestClient.post<{ id: number }>('/role/create', data);
}

/**
 * 更新角色
 */
export async function updateRoleApi(data: UpdateRoleRequest) {
  return requestClient.put<{ id: number }>('/role/update', data);
}

/**
 * 删除角色
 */
export async function deleteRoleApi(id: number) {
  return requestClient.delete(`/role/${id}`);
}

/**
 * 分配权限
 */
export async function assignRolePermissionApi(data: AssignRolePermissionRequest) {
  return requestClient.post('/role/assign-permission', data);
}

/**
 * 移除权限
 */
export async function removeRolePermissionApi(data: RemoveRolePermissionRequest) {
  return requestClient.post('/role/remove-permission', data);
}

/**
 * 设置数据范围
 */
export async function assignRoleDataScopeApi(data: AssignRoleDataScopeRequest) {
  return requestClient.post('/role/assign-data-scope', data);
}
