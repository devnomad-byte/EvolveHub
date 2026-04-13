package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateModelProviderRequest;
import org.evolve.admin.response.CreateModelProviderResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelProviderInfra;
import org.evolve.domain.resource.model.ModelProviderEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建模型提供商业务处理器
 *
 * @author zhao
 */
@Service
public class CreateModelProviderManager extends BaseManager<CreateModelProviderRequest, CreateModelProviderResponse> {

    @Resource
    private ModelProviderInfra modelProviderInfra;

    @Override
    protected void check(CreateModelProviderRequest request) {
        // 检查名称是否重复
        if (modelProviderInfra.getByName(request.name()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "提供商名称已存在");
        }
    }

    @Override
    protected CreateModelProviderResponse process(CreateModelProviderRequest request) {
        ModelProviderEntity entity = new ModelProviderEntity();
        entity.setName(request.name());
        entity.setLogoUrl(request.logoUrl());
        entity.setDefaultBaseUrl(request.defaultBaseUrl());
        entity.setSort(request.sort() != null ? request.sort() : 0);
        entity.setEnabled(request.enabled());

        modelProviderInfra.create(entity);
        return new CreateModelProviderResponse(entity.getId());
    }
}
