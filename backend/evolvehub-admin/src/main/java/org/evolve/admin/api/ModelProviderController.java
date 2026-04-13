package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateModelProviderRequest;
import org.evolve.admin.request.UpdateModelProviderRequest;
import org.evolve.admin.response.CreateModelProviderResponse;
import org.evolve.admin.response.UpdateModelProviderResponse;
import org.evolve.admin.service.*;
import org.evolve.domain.resource.model.ModelProviderEntity;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型提供商管理控制器
 * <p>
 * 提供模型提供商的增删改查接口，所有接口仅超级管理员可操作。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/model-provider")
public class ModelProviderController {

    @Resource
    private ListModelProviderManager listModelProviderManager;

    @Resource
    private GetModelProviderManager getModelProviderManager;

    @Resource
    private CreateModelProviderManager createModelProviderManager;

    @Resource
    private UpdateModelProviderManager updateModelProviderManager;

    @Resource
    private DeleteModelProviderManager deleteModelProviderManager;

    /**
     * 获取全部启用的提供商列表（所有用户可访问）
     */
    @GetMapping("/list")
    public Result<List<ModelProviderEntity>> list() {
        return Result.ok(listModelProviderManager.execute(null));
    }

    /**
     * 根据 ID 查询提供商详情
     */
    @GetMapping("/{id}")
    public Result<ModelProviderEntity> getById(@PathVariable Long id) {
        return Result.ok(getModelProviderManager.execute(id));
    }

    /**
     * 创建模型提供商（仅 SUPER_ADMIN）
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/create")
    public Result<CreateModelProviderResponse> create(@RequestBody @Valid CreateModelProviderRequest request) {
        return Result.ok(createModelProviderManager.execute(request));
    }

    /**
     * 更新模型提供商（仅 SUPER_ADMIN）
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/update")
    public Result<UpdateModelProviderResponse> update(@RequestBody @Valid UpdateModelProviderRequest request) {
        return Result.ok(updateModelProviderManager.execute(request));
    }

    /**
     * 删除模型提供商（仅 SUPER_ADMIN）
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteModelProviderManager.execute(id);
        return Result.ok();
    }
}
