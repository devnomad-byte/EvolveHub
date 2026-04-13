package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.request.UpdateUserModelConfigRequest;
import org.evolve.aiplatform.response.UpdateUserModelConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新用户级模型配置业务处理器
 * <p>
 * 校验当前用户必须是资源所有者，且 scope=USER。
 * </p>
 *
 * @author zhao
 */
@Service
public class UpdateUserModelConfigManager extends BaseManager<UpdateUserModelConfigRequest, UpdateUserModelConfigResponse> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(UpdateUserModelConfigRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ModelConfigEntity existing = modelConfigInfra.getModelConfigById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "模型配置不存在");
        }
        if (!"USER".equals(existing.getScope()) || !currentUserId.equals(existing.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该模型配置");
        }
    }

    @Override
    protected UpdateUserModelConfigResponse process(UpdateUserModelConfigRequest request) {
        ModelConfigEntity entity = new ModelConfigEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setProvider(request.provider());
        entity.setApiKey(request.apiKey());
        entity.setBaseUrl(request.baseUrl());
        entity.setEnabled(request.enabled());
        entity.setModelType(request.modelType());
        modelConfigInfra.updateModelConfig(entity);
        return new UpdateUserModelConfigResponse(request.id());
    }
}
