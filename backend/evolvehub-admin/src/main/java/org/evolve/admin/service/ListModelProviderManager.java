package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelProviderInfra;
import org.evolve.domain.resource.model.ModelProviderEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 获取全部启用的提供商列表业务处理器
 *
 * @author zhao
 */
@Service
public class ListModelProviderManager extends BaseManager<Void, List<ModelProviderEntity>> {

    @Resource
    private ModelProviderInfra modelProviderInfra;

    @Override
    protected void check(Void unused) {
    }

    @Override
    protected List<ModelProviderEntity> process(Void unused) {
        return modelProviderInfra.listAllEnabled();
    }
}
