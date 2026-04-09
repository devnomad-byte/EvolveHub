package org.evolve.auth.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.user.model.DeptEntity;
import org.evolve.auth.user.request.CreateDeptRequest;
import org.evolve.auth.user.request.UpdateDeptRequest;
import org.evolve.auth.user.response.CreateDeptResponse;
import org.evolve.auth.user.response.UpdateDeptResponse;
import org.evolve.auth.user.service.*;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 部门管理控制器
 * <p>
 * 提供部门的增删改查接口。部门支持树形结构，通过 parentId 构建父子关系。
 * 所有业务逻辑均委托给对应的 Manager 处理，Controller 仅负责接收请求和返回响应。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/dept")
public class DeptController {

    /** 创建部门业务处理器 */
    @Resource
    private CreateDeptManager createDeptManager;

    /** 更新部门业务处理器 */
    @Resource
    private UpdateDeptManager updateDeptManager;

    /** 查询单个部门业务处理器 */
    @Resource
    private GetDeptManager getDeptManager;

    /** 分页查询部门列表业务处理器 */
    @Resource
    private ListDeptManager listDeptManager;

    /** 删除部门业务处理器 */
    @Resource
    private DeleteDeptManager deleteDeptManager;

    /**
     * 创建部门
     * <p>包含父部门存在性校验、同级部门名称唯一性校验等业务逻辑。</p>
     *
     * @param request 创建部门请求体
     * @return 创建成功的部门ID
     */
    @PostMapping("/create")
    public Result<CreateDeptResponse> create(@RequestBody @Valid CreateDeptRequest request) {
        return Result.ok(createDeptManager.execute(request));
    }

    /**
     * 根据ID查询部门详情
     *
     * @param id 部门ID
     * @return 部门实体信息
     */
    @GetMapping("/{id}")
    public Result<DeptEntity> getById(@PathVariable Long id) {
        return Result.ok(getDeptManager.execute(id));
    }

    /**
     * 分页查询部门列表
     *
     * @param pageNum  页码，默认为1
     * @param pageSize 每页条数，默认为10
     * @return 分页部门列表
     */
    @GetMapping("/list")
    public Result<PageResponse<DeptEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listDeptManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新部门信息
     * <p>包含循环引用防护、父部门存在性校验、同级部门名称排他唯一性校验等业务逻辑。</p>
     *
     * @param request 更新部门请求体
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<UpdateDeptResponse> update(@RequestBody @Valid UpdateDeptRequest request) {
        return Result.ok(updateDeptManager.execute(request));
    }

    /**
     * 删除部门
     * <p>删除前会校验是否存在子部门、是否有用户属于该部门。</p>
     *
     * @param id 部门ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteDeptManager.execute(id);
        return Result.ok();
    }
}
