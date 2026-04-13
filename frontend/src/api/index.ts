// 导出所有 API
export { authApi } from './auth'
export { userApi } from './user'
export { adminUserApi } from './adminUser'
export { deptApi } from './dept'
export { menuApi } from './menu'
export { desktopIconApi } from './desktopIcon'

// 导出类型
export type { LoginRequest, LoginResponse, CurrentUserResponse, ChangePasswordRequest } from './auth'
export type { UserInfo, RoleInfo, CreateUserRequest, UpdateUserRequest, ResetPasswordRequest } from './user'
export type { UserInfo as AdminUserInfo, RoleInfo, CreateUserRequest as AdminCreateUserRequest, UpdateUserRequest as AdminUpdateUserRequest, AssignUserRoleRequest, RemoveUserRoleRequest, ResetPasswordRequest as AdminResetPasswordRequest } from './adminUser'
export type { DeptInfo, CreateDeptRequest, UpdateDeptRequest } from './dept'
export type { MenuInfo } from './menu'
export type { DesktopIconInfo, CreateDesktopIconRequest, UpdateDesktopIconRequest } from './desktopIcon'
