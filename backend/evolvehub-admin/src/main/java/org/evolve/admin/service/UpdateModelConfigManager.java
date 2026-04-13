package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.admin.request.UpdateModelConfigRequest;
import org.evolve.admin.response.UpdateModelConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新模型配置业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>模型配置必须存在</li>
 *     <li>如果修改名称，需保证新名称未被使用</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/10
 */
@Service
public class UpdateModelConfigManager extends BaseManager<UpdateModelConfigRequest, UpdateModelConfigResponse> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(UpdateModelConfigRequest request) {
        ModelConfigEntity existing = modelConfigInfra.getModelConfigById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "模型配置不存在");
        }
        if (request.name() != null && !request.name().isBlank()) {
            ModelConfigEntity byName = modelConfigInfra.getByName(request.name());
            if (byName != null && !byName.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "模型名称已存在");
            }
        }
    }

    @Override
    protected UpdateModelConfigResponse process(UpdateModelConfigRequest request) {
        ModelConfigEntity entity = new ModelConfigEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setProvider(request.provider());
        entity.setApiKey(request.apiKey());
        entity.setBaseUrl(request.baseUrl());
        entity.setEnabled(request.enabled());
        entity.setModelType(request.modelType());
        modelConfigInfra.updateModelConfig(entity);
        return new UpdateModelConfigResponse(request.id());
    }
}
