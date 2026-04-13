package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelProviderInfra;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除模型提供商业务处理器
 *
 * @author zhao
 */
@Service
public class DeleteModelProviderManager extends BaseManager<Long, Void> {

    @Resource
    private ModelProviderInfra modelProviderInfra;

    @Override
    protected void check(Long id) {
        if (modelProviderInfra.getById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "提供商不存在");
        }
    }

    @Override
    protected Void process(Long id) {
        modelProviderInfra.delete(id);
        return null;
    }
}
