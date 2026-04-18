package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateEnvVarRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.EnvVarInfra;
import org.evolve.domain.resource.model.EnvVarEntity;
import org.springframework.stereotype.Service;

@Service
public class CreateEnvVarManager extends BaseManager<CreateEnvVarRequest, Long> {

    @Resource
    private EnvVarInfra envVarInfra;

    @Override
    protected void check(CreateEnvVarRequest request) {
        EnvVarEntity existing = envVarInfra.getByKey(request.varKey());
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "变量名已存在: " + request.varKey());
        }
    }

    @Override
    protected Long process(CreateEnvVarRequest request) {
        EnvVarEntity entity = new EnvVarEntity();
        entity.setVarKey(request.varKey());
        entity.setVarValue(request.varValue());
        entity.setVarGroup(request.varGroup() != null ? request.varGroup() : "DEFAULT");
        entity.setDescription(request.description());
        entity.setIsSensitive(request.isSensitive() != null ? request.isSensitive() : 0);
        entity.setStatus(request.status() != null ? request.status() : 1);
        entity.setSort(request.sort() != null ? request.sort() : 0);
        entity.setCreateBy(StpUtil.getLoginIdAsLong());
        envVarInfra.save(entity);
        return entity.getId();
    }
}
