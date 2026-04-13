package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateModelProviderRequest;
import org.evolve.admin.response.UpdateModelProviderResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelProviderInfra;
import org.evolve.domain.resource.model.ModelProviderEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新模型提供商业务处理器
 *
 * @author zhao
 */
@Service
public class UpdateModelProviderManager extends BaseManager<UpdateModelProviderRequest, UpdateModelProviderResponse> {

    @Resource
    private ModelProviderInfra modelProviderInfra;

    @Override
    protected void check(UpdateModelProviderRequest request) {
        ModelProviderEntity existing = modelProviderInfra.getById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "提供商不存在");
        }
        // 检查名称是否与其他记录冲突
        if (request.name() != null && !request.name().equals(existing.getName())) {
            ModelProviderEntity byName = modelProviderInfra.getByName(request.name());
            if (byName != null && !byName.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "提供商名称已存在");
            }
        }
    }

    @Override
    protected UpdateModelProviderResponse process(UpdateModelProviderRequest request) {
        ModelProviderEntity entity = new ModelProviderEntity();
        entity.setId(request.id());
        if (request.name() != null) entity.setName(request.name());
        if (request.logoUrl() != null) entity.setLogoUrl(request.logoUrl());
        if (request.defaultBaseUrl() != null) entity.setDefaultBaseUrl(request.defaultBaseUrl());
        if (request.sort() != null) entity.setSort(request.sort());
        if (request.enabled() != null) entity.setEnabled(request.enabled());

        modelProviderInfra.update(entity);
        return new UpdateModelProviderResponse(true);
    }
}
