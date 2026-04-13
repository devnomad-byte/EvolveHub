package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.SetEmbeddingModelRequest;
import org.evolve.common.ai.ModelConnectivityTester;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.ModelConfigInfra;
import org.evolve.common.model.ModelConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 设置全局向量模型业务处理器
 * <p>
 * 系统全局只允许一个向量模型（scope=SYSTEM, modelType=embedding）。
 * 已存在则更新，不存在则创建。
 * </p>
 *
 * @author zhao
 */
@Service
public class SetEmbeddingModelManager extends BaseManager<SetEmbeddingModelRequest, ModelConfigEntity> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ModelConnectivityTester modelConnectivityTester;

    @Override
    protected void check(SetEmbeddingModelRequest request) {
        // 验证向量模型连通性
        ModelConnectivityTester.TestResult testResult =
                modelConnectivityTester.test(request.provider(), request.baseUrl(), request.apiKey());
        if (!testResult.success()) {
            throw new BusinessException(ResultCode.MODEL_CONNECT_FAIL, testResult.message());
        }
    }

    @Override
    protected ModelConfigEntity process(SetEmbeddingModelRequest request) {
        ModelConfigEntity existing = modelConfigInfra.getSystemEmbeddingModel();
        if (existing != null) {
            // 已存在，更新
            existing.setName(request.name());
            existing.setProvider(request.provider());
            existing.setApiKey(request.apiKey());
            existing.setBaseUrl(request.baseUrl());
            existing.setEnabled(1);
            modelConfigInfra.updateModelConfig(existing);
            return existing;
        } else {
            // 不存在，创建
            ModelConfigEntity entity = new ModelConfigEntity();
            entity.setName(request.name());
            entity.setProvider(request.provider());
            entity.setApiKey(request.apiKey());
            entity.setBaseUrl(request.baseUrl());
            entity.setEnabled(1);
            entity.setModelType("embedding");
            entity.setScope("SYSTEM");
            entity.setOwnerId(null);
            modelConfigInfra.createModelConfig(entity);
            return entity;
        }
    }
}
