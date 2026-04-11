import {requestClient} from '#/api/request';

export interface User {
  id: number;
  username: string;
  nickname: string;
  email: string;
  phone: string;
  avatar: string;
  deptId: number | null;
  status: 0 | 1;
  createTime?: string;
  updateTime?: string;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  nickname: string;
  email?: string;
  phone?: string;
  deptId?: number;
  status?: 0 | 1;
}

export interface UpdateUserRequest extends Partial<CreateUserRequest> {
  id: number;
}

export interface AssignUserRoleRequest {
  userId: number;
  roleId: number;
}

export interface PageResponse<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

/**
 * 获取用户分页列表
 */
export async function getUserListApi(pageNum = 1, pageSize = 10) {
  return requestClient.get<PageResponse<User>>('/user/list', {
    params: { pageNum, pageSize },
  });
}

/**
 * 获取用户详情
 */
export async function getUserByIdApi(id: number) {
  return requestClient.get<User>(`/user/${id}`);
}

/**
 * 创建用户
 */
export async function createUserApi(data: CreateUserRequest) {
  return requestClient.post<{ id: number }>('/user/create', data);
}

/**
 * 更新用户
 */
export async function updateUserApi(data: UpdateUserRequest) {
  return requestClient.put<{ id: number }>('/user/update', data);
}

/**
 * 删除用户
 */
export async function deleteUserApi(id: number) {
  return requestClient.delete(`/user/${id}`);
}

/**
 * 分配角色
 */
export async function assignUserRoleApi(data: AssignUserRoleRequest) {
  return requestClient.post('/user/assign-role', data);
}

/**
 * 移除角色
 */
export async function removeUserRoleApi(data: AssignUserRoleRequest) {
  return requestClient.post('/user/remove-role', data);
}
