package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelProviderInfra;
import org.evolve.domain.resource.model.ModelProviderEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 根据 ID 查询提供商详情业务处理器
 *
 * @author zhao
 */
@Service
public class GetModelProviderManager extends BaseManager<Long, ModelProviderEntity> {

    @Resource
    private ModelProviderInfra modelProviderInfra;

    @Override
    protected void check(Long id) {
        if (modelProviderInfra.getById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "提供商不存在");
        }
    }

    @Override
    protected ModelProviderEntity process(Long id) {
        return modelProviderInfra.getById(id);
    }
}
