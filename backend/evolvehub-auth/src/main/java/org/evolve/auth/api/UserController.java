package org.evolve.auth.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.request.PasswordChangeRequest;
import org.evolve.auth.request.ResetPasswordRequest;
import org.evolve.auth.request.UserCreateRequest;
import org.evolve.auth.request.UserUpdateRequest;
import org.evolve.auth.response.CurrentUserResponse;
import org.evolve.auth.response.UserResponse;
import org.evolve.auth.service.PasswordChangeManager;
import org.evolve.auth.service.ResetPasswordManager;
import org.evolve.auth.service.UserCreateManager;
import org.evolve.auth.service.UserDeleteManager;
import org.evolve.auth.service.UserListManager;
import org.evolve.auth.service.UserUpdateManager;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口
 *
 * @author zhao
 */
@RestController
public class UserController {

    @Resource
    private UserCreateManager userCreateManager;

    @Resource
    private UserUpdateManager userUpdateManager;

    @Resource
    private UserDeleteManager userDeleteManager;

    @Resource
    private UserListManager userListManager;

    @Resource
    private PasswordChangeManager passwordChangeManager;

    @Resource
    private ResetPasswordManager resetPasswordManager;

    /**
     * 创建用户（仅超级管理员）
     *
     * @param request 创建请求
     * @return 用户信息
     */
    @PostMapping("/api/admin/users")
    @SaCheckRole("SUPER_ADMIN")
    public Result<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        return Result.ok(userCreateManager.execute(request));
    }

    /**
     * 用户列表（超级管理员和普通管理员）
     *
     * @return 用户列表
     */
    @GetMapping("/api/admin/users")
    @SaCheckRole({"SUPER_ADMIN", "ADMIN"})
    public Result<List<UserResponse>> listUsers() {
        return Result.ok(userListManager.execute());
    }

    /**
     * 修改用户（超级管理员和普通管理员）
     *
     * @param request 修改请求
     * @return 用户信息
     */
    @PutMapping("/api/admin/users")
    @SaCheckRole({"SUPER_ADMIN", "ADMIN"})
    public Result<UserResponse> updateUser(@RequestBody @Valid UserUpdateRequest request) {
        return Result.ok(userUpdateManager.execute(request));
    }

    /**
     * 删除用户（仅超级管理员）
     *
     * @param userId 用户 ID
     * @return 空
     */
    @DeleteMapping("/api/admin/users/{userId}")
    @SaCheckRole("SUPER_ADMIN")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userDeleteManager.execute(userId);
        return Result.ok();
    }

    /**
     * 重置密码（仅超级管理员）
     *
     * @param request 重置密码请求
     * @return 空
     */
    @PutMapping("/api/admin/users/password/reset")
    @SaCheckRole("SUPER_ADMIN")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        resetPasswordManager.execute(request);
        return Result.ok();
    }

    /**
     * 获取当前用户信息（所有登录用户）
     *
     * @return 当前用户信息
     */
    @GetMapping("/api/auth/me")
    public Result<CurrentUserResponse> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();

        // 获取角色和权限列表
        List<String> roles = StpUtil.getRoleList();
        List<String> permissions = StpUtil.getPermissionList();

        CurrentUserResponse response = new CurrentUserResponse(
                userId,
                StpUtil.getLoginIdAsString(),
                null, // nickname 需要查询数据库
                null, // email 需要查询数据库
                null, // avatar 需要查询数据库
                null, // deptId 需要查询数据库
                roles,
                permissions
        );

        return Result.ok(response);
    }

    /**
     * 修改密码（所有登录用户）
     *
     * @param request 修改密码请求
     * @return 空
     */
    @PutMapping("/api/auth/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordChangeRequest request) {
        passwordChangeManager.execute(request);
        return Result.ok();
    }
}
