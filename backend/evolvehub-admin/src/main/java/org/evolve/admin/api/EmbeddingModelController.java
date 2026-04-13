package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.SetEmbeddingModelRequest;
import org.evolve.admin.service.GetEmbeddingModelManager;
import org.evolve.admin.service.SetEmbeddingModelManager;
import org.evolve.common.model.ModelConfigEntity;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 全局向量模型管理控制器
 * <p>
 * 系统全局只允许一个向量模型，由管理员统一配置。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/embedding-model")
public class EmbeddingModelController {

    @Resource
    private SetEmbeddingModelManager setEmbeddingModelManager;

    @Resource
    private GetEmbeddingModelManager getEmbeddingModelManager;

    /**
     * 设置全局向量模型（有则更新，无则创建）
     *
     * @param request 向量模型配置
     * @return 设置后的向量模型配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/set")
    public Result<ModelConfigEntity> set(@RequestBody @Valid SetEmbeddingModelRequest request) {
        return Result.ok(setEmbeddingModelManager.execute(request));
    }

    /**
     * 获取当前全局向量模型配置
     *
     * @return 向量模型配置，未配置时 data 为 null
     */
    @GetMapping("/get")
    public Result<ModelConfigEntity> get() {
        return Result.ok(getEmbeddingModelManager.execute());
    }
}
