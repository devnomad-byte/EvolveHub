package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.EnvVarInfra;
import org.evolve.domain.resource.model.EnvVarEntity;
import org.springframework.stereotype.Service;

@Service
public class DeleteEnvVarManager extends BaseManager<Long, Void> {

    @Resource
    private EnvVarInfra envVarInfra;

    @Override
    protected void check(Long id) {
        EnvVarEntity existing = envVarInfra.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "变量不存在: " + id);
        }
    }

    @Override
    protected Void process(Long id) {
        envVarInfra.removeById(id);
        return null;
    }
}
