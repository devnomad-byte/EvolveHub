package org.evolve.auth.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.request.DeptCreateRequest;
import org.evolve.auth.request.DeptUpdateRequest;
import org.evolve.auth.response.DeptResponse;
import org.evolve.auth.service.DeptCreateManager;
import org.evolve.auth.service.DeptDeleteManager;
import org.evolve.auth.service.DeptTreeManager;
import org.evolve.auth.service.DeptUpdateManager;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理接口
 *
 * @author zhao
 */
@RestController
public class DeptController {

    @Resource
    private DeptCreateManager deptCreateManager;

    @Resource
    private DeptUpdateManager deptUpdateManager;

    @Resource
    private DeptDeleteManager deptDeleteManager;

    @Resource
    private DeptTreeManager deptTreeManager;

    /**
     * 创建部门（超级管理员和普通管理员）
     *
     * @param request 创建请求
     * @return 部门信息
     */
    @PostMapping("/admin/dept")
    @SaCheckRole(value = {"SUPER_ADMIN", "ADMIN"}, mode = SaMode.OR)
    public Result<DeptResponse> createDept(@RequestBody @Valid DeptCreateRequest request) {
        return Result.ok(deptCreateManager.execute(request));
    }

    /**
     * 查询部门树（超级管理员和普通管理员）
     *
     * @return 部门树
     */
    @GetMapping("/admin/dept/tree")
    @SaCheckRole(value = {"SUPER_ADMIN", "ADMIN"}, mode = SaMode.OR)
    public Result<List<DeptResponse>> getDeptTree() {
        return Result.ok(deptTreeManager.execute());
    }

    /**
     * 修改部门（超级管理员和普通管理员）
     *
     * @param request 修改请求
     * @return 部门信息
     */
    @PutMapping("/admin/dept")
    @SaCheckRole(value = {"SUPER_ADMIN", "ADMIN"}, mode = SaMode.OR)
    public Result<DeptResponse> updateDept(@RequestBody @Valid DeptUpdateRequest request) {
        return Result.ok(deptUpdateManager.execute(request));
    }

    /**
     * 删除部门（仅超级管理员）
     *
     * @param id 部门 ID
     * @return 空
     */
    @DeleteMapping("/admin/dept/{id}")
    @SaCheckRole("SUPER_ADMIN")
    public Result<Void> deleteDept(@PathVariable Long id) {
        deptDeleteManager.execute(id);
        return Result.ok();
    }
}
