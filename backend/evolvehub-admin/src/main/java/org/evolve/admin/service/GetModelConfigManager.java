package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询单个模型配置业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/10
 */
@Service
public class GetModelConfigManager extends BaseManager<Long, ModelConfigEntity> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(Long id) {
        if (modelConfigInfra.getModelConfigById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "模型配置不存在");
        }
    }

    @Override
    protected ModelConfigEntity process(Long id) {
        return modelConfigInfra.getModelConfigById(id);
    }
}
