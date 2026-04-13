package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

/**
 * 获取全局向量模型配置业务处理器
 * <p>
 * 返回当前生效的系统级向量模型，未配置时返回 null。
 * </p>
 *
 * @author zhao
 */
@Service
public class GetEmbeddingModelManager extends BaseManager<Void, ModelConfigEntity> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(Void request) {
    }

    @Override
    protected ModelConfigEntity process(Void request) {
        return modelConfigInfra.getSystemEmbeddingModel();
    }
}
