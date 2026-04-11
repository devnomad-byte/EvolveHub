package org.evolve.auth.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.request.PasswordChangeRequest;
import org.evolve.auth.request.ResetPasswordRequest;
import org.evolve.auth.request.UpdateAvatarRequest;
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
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.model.UsersEntity;
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

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private DeptInfra deptInfra;

    /**
     * 创建用户（仅超级管理员）
     *
     * @param request 创建请求
     * @return 用户信息
     */
    @PostMapping("/admin/users")
    @SaCheckRole("SUPER_ADMIN")
    public Result<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        return Result.ok(userCreateManager.execute(request));
    }

    /**
     * 用户列表（超级管理员和普通管理员）
     *
     * @return 用户列表
     */
    @GetMapping("/admin/users")
    @SaCheckRole(value = {"SUPER_ADMIN", "ADMIN"}, mode = SaMode.OR)
    public Result<List<UserResponse>> listUsers() {
        return Result.ok(userListManager.execute());
    }

    /**
     * 修改用户（超级管理员和普通管理员）
     *
     * @param request 修改请求
     * @return 用户信息
     */
    @PutMapping("/admin/users")
    @SaCheckRole(value = {"SUPER_ADMIN", "ADMIN"}, mode = SaMode.OR)
    public Result<UserResponse> updateUser(@RequestBody @Valid UserUpdateRequest request) {
        return Result.ok(userUpdateManager.execute(request));
    }

    /**
     * 删除用户（仅超级管理员）
     *
     * @param userId 用户 ID
     * @return 空
     */
    @DeleteMapping("/admin/users/{userId}")
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
    @PutMapping("/admin/users/password/reset")
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
    @GetMapping("/me")
    public Result<CurrentUserResponse> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = StpUtil.getRoleList();
        List<String> permissions = StpUtil.getPermissionList();

        // 查询用户详情
        UsersEntity user = usersInfra.getUserById(userId);
        String nickname = null;
        String email = null;
        String avatar = null;
        Long deptId = null;
        String deptName = null;

        if (user != null) {
            nickname = user.getNickname();
            email = user.getEmail();
            avatar = user.getAvatar();
            deptId = user.getDeptId();

            // 查询部门名称
            if (deptId != null) {
                DeptEntity dept = deptInfra.getDeptById(deptId);
                if (dept != null) {
                    deptName = dept.getDeptName();
                }
            }
        }

        CurrentUserResponse response = new CurrentUserResponse(
                userId,
                user != null ? user.getUsername() : StpUtil.getLoginIdAsString(),
                nickname,
                email,
                avatar,
                deptId,
                deptName,
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
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordChangeRequest request) {
        passwordChangeManager.execute(request);
        return Result.ok();
    }

    /**
     * 更新当前用户头像（所有登录用户）
     *
     * @param request 头像数据（Base64）
     * @return 空
     */
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody @Valid UpdateAvatarRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UsersEntity user = usersInfra.getUserById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setAvatar(request.avatar());
        usersInfra.updateUser(user);
        return Result.ok();
    }
}
