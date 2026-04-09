package org.evolve.auth.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.user.model.RolesEntity;
import org.evolve.auth.user.request.*;
import org.evolve.auth.user.response.*;
import org.evolve.auth.user.service.*;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理控制器
 * <p>
 * 提供角色的增删改查接口，以及角色-权限关联和角色-数据范围管理接口。
 * 所有业务逻辑均委托给对应的 Manager 处理，Controller 仅负责接收请求和返回响应。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    /** 创建角色业务处理器 */
    @Resource
    private CreateRoleManager createRoleManager;

    /** 更新角色业务处理器 */
    @Resource
    private UpdateRoleManager updateRoleManager;

    /** 查询单个角色业务处理器 */
    @Resource
    private GetRoleManager getRoleManager;

    /** 分页查询角色列表业务处理器 */
    @Resource
    private ListRoleManager listRoleManager;

    /** 删除角色业务处理器 */
    @Resource
    private DeleteRoleManager deleteRoleManager;

    /** 为角色分配权限业务处理器 */
    @Resource
    private AssignRolePermissionManager assignRolePermissionManager;

    /** 移除角色权限业务处理器 */
    @Resource
    private RemoveRolePermissionManager removeRolePermissionManager;

    /** 设置角色数据范围业务处理器 */
    @Resource
    private AssignRoleDataScopeManager assignRoleDataScopeManager;

    /**
     * 创建角色
     * <p>包含角色名/角色编码唯一性校验、dataScope范围校验等业务逻辑。</p>
     *
     * @param request 创建角色请求体
     * @return 创建成功的角色ID
     */
    @PostMapping("/create")
    public Result<CreateRoleResponse> create(@RequestBody @Valid CreateRoleRequest request) {
        return Result.ok(createRoleManager.execute(request));
    }

    /**
     * 根据ID查询角色详情
     *
     * @param id 角色ID
     * @return 角色实体信息
     */
    @GetMapping("/{id}")
    public Result<RolesEntity> getById(@PathVariable Long id) {
        return Result.ok(getRoleManager.execute(id));
    }

    /**
     * 分页查询角色列表
     *
     * @param pageNum  页码，默认为1
     * @param pageSize 每页条数，默认为10
     * @return 分页角色列表
     */
    @GetMapping("/list")
    public Result<PageResponse<RolesEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listRoleManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新角色信息
     * <p>包含角色名/角色编码排他唯一性校验，dataScope变更时自动清理自定义部门列表。</p>
     *
     * @param request 更新角色请求体
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<UpdateRoleResponse> update(@RequestBody @Valid UpdateRoleRequest request) {
        return Result.ok(updateRoleManager.execute(request));
    }

    /**
     * 删除角色
     * <p>删除前会校验角色下是否仍有用户持有，并级联清理角色-权限和角色-数据范围关联。</p>
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteRoleManager.execute(id);
        return Result.ok();
    }

    /**
     * 为角色分配权限
     * <p>校验角色和权限是否存在，以及是否已存在相同的关联关系。</p>
     *
     * @param request 分配权限请求体（包含角色ID和权限ID）
     * @return 分配结果
     */
    @PostMapping("/assign-permission")
    public Result<AssignRolePermissionResponse> assignPermission(@RequestBody @Valid AssignRolePermissionRequest request) {
        return Result.ok(assignRolePermissionManager.execute(request));
    }

    /**
     * 移除角色的权限
     * <p>校验关联关系是否存在后执行移除。</p>
     *
     * @param request 移除权限请求体（包含角色ID和权限ID）
     * @return 移除结果
     */
    @PostMapping("/remove-permission")
    public Result<RemoveRolePermissionResponse> removePermission(@RequestBody @Valid RemoveRolePermissionRequest request) {
        return Result.ok(removeRolePermissionManager.execute(request));
    }

    /**
     * 设置角色的数据范围
     * <p>仅当角色的dataScope为5（自定义部门）时才允许设置，会校验每个部门ID是否存在。</p>
     *
     * @param request 设置数据范围请求体（包含角色ID和部门ID列表）
     * @return 设置结果
     */
    @PostMapping("/assign-data-scope")
    public Result<AssignRoleDataScopeResponse> assignDataScope(@RequestBody @Valid AssignRoleDataScopeRequest request) {
        return Result.ok(assignRoleDataScopeManager.execute(request));
    }
}
