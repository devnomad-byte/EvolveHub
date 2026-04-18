package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateEnvVarRequest;
import org.evolve.admin.request.UpdateEnvVarRequest;
import org.evolve.admin.service.CreateEnvVarManager;
import org.evolve.admin.service.DeleteEnvVarManager;
import org.evolve.admin.service.ListEnvVarManager;
import org.evolve.admin.service.UpdateEnvVarManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.evolve.domain.resource.infra.EnvVarInfra;
import org.evolve.domain.resource.model.EnvVarEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 环境变量管理
 * @author devnomad-byte
 */
@RestController
@RequestMapping("/env-var")
public class EnvVarController {

    @Resource
    private ListEnvVarManager listEnvVarManager;

    @Resource
    private CreateEnvVarManager createEnvVarManager;

    @Resource
    private UpdateEnvVarManager updateEnvVarManager;

    @Resource
    private DeleteEnvVarManager deleteEnvVarManager;

    @Resource
    private EnvVarInfra envVarInfra;

    /**
     * 分页列表
     */
    @SaCheckPermission("env:list")
    @GetMapping("/list")
    public Result<PageResponse> list(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "20") int pageSize,
                                      @RequestParam(required = false) String group) {
        return Result.ok(listEnvVarManager.execute(new ListEnvVarManager.Request(group, pageNum, pageSize)));
    }

    /**
     * 详情
     */
    @SaCheckPermission("env:list")
    @GetMapping("/{id}")
    public Result<EnvVarEntity> getById(@PathVariable Long id) {
        return Result.ok(envVarInfra.getById(id));
    }

    /**
     * 获取所有环境变量（供 MCP/Skill 运行时使用）
     */
    @GetMapping("/all")
    public Result<List<EnvVarEntity>> getAllEnabled() {
        return Result.ok(envVarInfra.lambdaQuery()
                .eq(EnvVarEntity::getDeleted, 0)
                .eq(EnvVarEntity::getStatus, 1)
                .list());
    }

    /**
     * 创建
     */
    @SaCheckPermission("env:create")
    @PostMapping("/create")
    public Result<Long> create(@RequestBody @Valid CreateEnvVarRequest request) {
        return Result.ok(createEnvVarManager.execute(request));
    }

    /**
     * 更新
     */
    @SaCheckPermission("env:update")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid UpdateEnvVarRequest request) {
        updateEnvVarManager.execute(request);
        return Result.ok();
    }

    /**
     * 删除
     */
    @SaCheckPermission("env:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteEnvVarManager.execute(id);
        return Result.ok();
    }
}
