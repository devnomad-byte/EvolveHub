package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.request.CreateUserModelConfigRequest;
import org.evolve.aiplatform.response.CreateUserModelConfigResponse;
import org.evolve.common.ai.ModelConnectivityTester;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.ModelConfigInfra;
import org.evolve.common.model.ModelConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建用户级模型配置业务处理器
 * <p>
 * scope 固定为 USER，owner_id 为当前登录用户。
 * </p>
 *
 * @author zhao
 */
@Service
public class CreateUserModelConfigManager extends BaseManager<CreateUserModelConfigRequest, CreateUserModelConfigResponse> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ModelConnectivityTester modelConnectivityTester;

    @Override
    protected void check(CreateUserModelConfigRequest request) {
        // 验证模型连通性
        ModelConnectivityTester.TestResult testResult =
                modelConnectivityTester.test(request.provider(), request.baseUrl(), request.apiKey());
        if (!testResult.success()) {
            throw new BusinessException(ResultCode.MODEL_CONNECT_FAIL, testResult.message());
        }
    }

    @Override
    protected CreateUserModelConfigResponse process(CreateUserModelConfigRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ModelConfigEntity entity = new ModelConfigEntity();
        entity.setName(request.name());
        entity.setProvider(request.provider());
        entity.setApiKey(request.apiKey());
        entity.setBaseUrl(request.baseUrl());
        entity.setEnabled(request.enabled());
        entity.setModelType(request.modelType());
        entity.setScope("USER");
        entity.setOwnerId(currentUserId);
        modelConfigInfra.createModelConfig(entity);
        return new CreateUserModelConfigResponse(entity.getId());
    }
}
