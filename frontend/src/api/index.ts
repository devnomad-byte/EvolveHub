// 导出所有 API
export { authApi } from './auth'
export { userApi } from './user'
export { deptApi } from './dept'
export { menuApi } from './menu'

// 导出类型
export type { LoginRequest, LoginResponse, CurrentUserResponse, ChangePasswordRequest } from './auth'
export type { UserInfo, RoleInfo, CreateUserRequest, UpdateUserRequest, ResetPasswordRequest } from './user'
export type { DeptInfo, CreateDeptRequest, UpdateDeptRequest } from './dept'
export type { MenuInfo } from './menu'
