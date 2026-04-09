package org.evolve.auth.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.user.model.PermissionsEntity;
import org.evolve.auth.user.request.CreatePermissionRequest;
import org.evolve.auth.user.request.UpdatePermissionRequest;
import org.evolve.auth.user.response.CreatePermissionResponse;
import org.evolve.auth.user.response.UpdatePermissionResponse;
import org.evolve.auth.user.service.*;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理控制器
 * <p>
 * 提供权限的增删改查接口。权限支持树形结构，类型包括菜单(MENU)、按钮(BUTTON)、API接口(API)。
 * 所有业务逻辑均委托给对应的 Manager 处理，Controller 仅负责接收请求和返回响应。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    /** 创建权限业务处理器 */
    @Resource
    private CreatePermissionManager createPermissionManager;

    /** 更新权限业务处理器 */
    @Resource
    private UpdatePermissionManager updatePermissionManager;

    /** 查询单个权限业务处理器 */
    @Resource
    private GetPermissionManager getPermissionManager;

    /** 分页查询权限列表业务处理器 */
    @Resource
    private ListPermissionManager listPermissionManager;

    /** 删除权限业务处理器 */
    @Resource
    private DeletePermissionManager deletePermissionManager;

    /**
     * 创建权限
     * <p>包含权限编码唯一性校验、权限类型合法性校验、父权限存在性校验等业务逻辑。</p>
     *
     * @param request 创建权限请求体
     * @return 创建成功的权限ID
     */
    @PostMapping("/create")
    public Result<CreatePermissionResponse> create(@RequestBody @Valid CreatePermissionRequest request) {
        return Result.ok(createPermissionManager.execute(request));
    }

    /**
     * 根据ID查询权限详情
     *
     * @param id 权限ID
     * @return 权限实体信息
     */
    @GetMapping("/{id}")
    public Result<PermissionsEntity> getById(@PathVariable Long id) {
        return Result.ok(getPermissionManager.execute(id));
    }

    /**
     * 分页查询权限列表
     *
     * @param pageNum  页码，默认为1
     * @param pageSize 每页条数，默认为10
     * @return 分页权限列表
     */
    @GetMapping("/list")
    public Result<PageResponse<PermissionsEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listPermissionManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新权限信息
     * <p>包含权限编码排他唯一性校验、权限类型合法性校验、父权限存在性校验等业务逻辑。</p>
     *
     * @param request 更新权限请求体
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<UpdatePermissionResponse> update(@RequestBody @Valid UpdatePermissionRequest request) {
        return Result.ok(updatePermissionManager.execute(request));
    }

    /**
     * 删除权限
     * <p>删除前会校验是否存在子权限、是否有角色持有该权限，并级联清理关联数据。</p>
     *
     * @param id 权限ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deletePermissionManager.execute(id);
        return Result.ok();
    }
}
