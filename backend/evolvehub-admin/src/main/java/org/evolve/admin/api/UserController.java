package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.AssignUserRoleRequest;
import org.evolve.admin.request.CreateUserRequest;
import org.evolve.admin.request.RemoveUserRoleRequest;
import org.evolve.admin.request.UpdateUserRequest;
import org.evolve.admin.response.AssignUserRoleResponse;
import org.evolve.admin.response.RemoveUserRoleResponse;
import org.evolve.admin.response.UpdateUserResponse;
import org.evolve.admin.response.UserResponse;
import org.evolve.admin.service.*;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * <p>
 * 提供用户的增删改查接口，以及用户-角色关联管理接口。
 * 所有业务逻辑均委托给对应的 Manager 处理，Controller 仅负责接收请求和返回响应。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/user")
public class UserController {

    /** 创建用户业务处理器 */
    @Resource
    private CreateUserManager createUserManager;

    /** 更新用户业务处理器 */
    @Resource
    private UpdateUserManager updateUserManager;

    /** 查询单个用户业务处理器 */
    @Resource
    private GetUserManager getUserManager;

    /** 分页查询用户列表业务处理器 */
    @Resource
    private ListUserManager listUserManager;

    /** 删除用户业务处理器 */
    @Resource
    private DeleteUserManager deleteUserManager;

    /** 为用户分配角色业务处理器 */
    @Resource
    private AssignUserRoleManager assignUserRoleManager;

    /** 移除用户角色业务处理器 */
    @Resource
    private RemoveUserRoleManager removeUserRoleManager;

    /**
     * 创建用户
     * <p>包含用户名/邮箱唯一性校验、部门存在性校验、密码加密等业务逻辑。</p>
     *
     * @param request 创建用户请求体
     * @return 创建成功的用户ID
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/create")
    public Result<UserResponse> create(@RequestBody @Valid CreateUserRequest request) {
        return Result.ok(createUserManager.execute(request));
    }

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户实体信息
     */
    @SaCheckRole(value = {"SUPER_ADMIN", "ADMIN"}, mode = SaMode.OR)
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        return Result.ok(getUserManager.execute(id));
    }

    /**
     * 查询用户列表
     *
     * @return 用户列表
     */
    @SaCheckRole(value = {"SUPER_ADMIN", "ADMIN"}, mode = SaMode.OR)
    @GetMapping("/list")
    public Result<List<UserResponse>> list() {
        return Result.ok(listUserManager.execute());
    }

    /**
     * 更新用户信息
     * <p>包含用户名/邮箱排他唯一性校验、部门存在性校验等业务逻辑。</p>
     *
     * @param request 更新用户请求体
     * @return 更新结果
     */
    @SaCheckRole(value = {"SUPER_ADMIN", "ADMIN"}, mode = SaMode.OR)
    @PutMapping("/update")
    public Result<UserResponse> update(@RequestBody @Valid UpdateUserRequest request) {
        return Result.ok(updateUserManager.execute(request));
    }

    /**
     * 删除用户
     * <p>删除前会级联清理用户-角色关联关系。</p>
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteUserManager.execute(id);
        return Result.ok();
    }

    /**
     * 为用户分配角色
     * <p>校验用户和角色是否存在，以及是否已存在相同的关联关系。</p>
     *
     * @param request 分配角色请求体（包含用户ID和角色ID）
     * @return 分配结果
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/assign-role")
    public Result<AssignUserRoleResponse> assignRole(@RequestBody @Valid AssignUserRoleRequest request) {
        return Result.ok(assignUserRoleManager.execute(request));
    }

    /**
     * 移除用户的角色
     * <p>校验关联关系是否存在后执行移除。</p>
     *
     * @param request 移除角色请求体（包含用户ID和角色ID）
     * @return 移除结果
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/remove-role")
    public Result<RemoveUserRoleResponse> removeRole(@RequestBody @Valid RemoveUserRoleRequest request) {
        return Result.ok(removeUserRoleManager.execute(request));
    }
}
