package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateEnvVarRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.EnvVarInfra;
import org.evolve.domain.resource.model.EnvVarEntity;
import org.springframework.stereotype.Service;

@Service
public class UpdateEnvVarManager extends BaseManager<UpdateEnvVarRequest, Void> {

    @Resource
    private EnvVarInfra envVarInfra;

    @Override
    protected void check(UpdateEnvVarRequest request) {
        EnvVarEntity existing = envVarInfra.getByKey(request.varKey());
        if (existing != null && !existing.getId().equals(request.id())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "变量名已存在: " + request.varKey());
        }
        EnvVarEntity byId = envVarInfra.getById(request.id());
        if (byId == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "变量不存在: " + request.id());
        }
    }

    @Override
    protected Void process(UpdateEnvVarRequest request) {
        EnvVarEntity entity = new EnvVarEntity();
        entity.setId(request.id());
        entity.setVarKey(request.varKey());
        entity.setVarValue(request.varValue());
        entity.setVarGroup(request.varGroup() != null ? request.varGroup() : "DEFAULT");
        entity.setDescription(request.description());
        entity.setIsSensitive(request.isSensitive() != null ? request.isSensitive() : 0);
        entity.setStatus(request.status() != null ? request.status() : 1);
        entity.setSort(request.sort() != null ? request.sort() : 0);
        envVarInfra.updateById(entity);
        return null;
    }
}
