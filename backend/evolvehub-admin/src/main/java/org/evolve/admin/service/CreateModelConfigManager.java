package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.resource.ai.ModelConnectivityTester;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.admin.request.CreateModelConfigRequest;
import org.evolve.admin.response.CreateModelConfigResponse;
import org.evolve.common.base.BaseManager;
import cn.dev33.satoken.stp.StpUtil;
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

    @Resource
    private ModelConnectivityTester modelConnectivityTester;

    @Override
    protected void check(CreateModelConfigRequest request) {
        // 向量模型走专用接口，禁止通过通用创建入口
        if ("embedding".equalsIgnoreCase(request.modelType())) {
            throw new BusinessException(ResultCode.EMBEDDING_MODEL_NOT_ALLOWED,
                    "向量模型请通过 /embedding-model/set 接口配置");
        }
        if (modelConfigInfra.getByName(request.name()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "模型名称已存在");
        }
        // 验证模型连通性：baseUrl 可达 + apiKey 有效
        ModelConnectivityTester.TestResult testResult =
                modelConnectivityTester.test(request.provider(), request.baseUrl(), request.apiKey());
        if (!testResult.success()) {
            throw new BusinessException(ResultCode.MODEL_CONNECT_FAIL, testResult.message());
        }
    }

    @Override
    protected CreateModelConfigResponse process(CreateModelConfigRequest request) {
        String scope = request.scope() != null ? request.scope() : "SYSTEM";

        ModelConfigEntity entity = new ModelConfigEntity();
        entity.setName(request.name());
        entity.setProvider(request.provider());
        entity.setApiKey(request.apiKey());
        entity.setBaseUrl(request.baseUrl());
        entity.setEnabled(request.enabled());
        entity.setModelType(request.modelType());
        entity.setScope(scope);
        entity.setDeptId("DEPT".equals(scope) ? request.deptId() : null);
        entity.setOwnerId("USER".equals(scope) ? StpUtil.getLoginIdAsLong() : null);
        modelConfigInfra.createModelConfig(entity);
        return new CreateModelConfigResponse(entity.getId());
    }
}
