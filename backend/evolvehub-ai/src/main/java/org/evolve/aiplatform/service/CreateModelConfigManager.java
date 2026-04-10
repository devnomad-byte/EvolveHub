package org.evolve.aiplatform.service;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.infra.ModelConfigInfra;
import org.evolve.aiplatform.model.ModelConfigEntity;
import org.evolve.aiplatform.request.CreateModelConfigRequest;
import org.evolve.aiplatform.response.CreateModelConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建模型配置业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>模型名称全局唯一</li>
 *     <li>提供商不能为空</li>
 *     <li>API 密钥不能为空</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/10
 */
@Service
public class CreateModelConfigManager extends BaseManager<CreateModelConfigRequest, CreateModelConfigResponse> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(CreateModelConfigRequest request) {
        // 模型名称不能重复
        if (modelConfigInfra.getByName(request.name()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "模型名称已存在");
        }
    }

    @Override
    protected CreateModelConfigResponse process(CreateModelConfigRequest request) {
        ModelConfigEntity entity = new ModelConfigEntity();
        entity.setName(request.name());
        entity.setProvider(request.provider());
        entity.setApiKey(request.apiKey());
        entity.setBaseUrl(request.baseUrl());
        entity.setEnabled(request.enabled());
        entity.setModelType(request.modelType());
        modelConfigInfra.createModelConfig(entity);
        return new CreateModelConfigResponse(entity.getId());
    }
}
