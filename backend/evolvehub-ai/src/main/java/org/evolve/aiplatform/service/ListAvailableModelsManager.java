package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.infra.ResourceGrantInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询当前用户可用的模型列表（自己的 USER 模型 + 被授权的 SYSTEM 模型）
 *
 * @author zhao
 */
@Service
public class ListAvailableModelsManager extends BaseManager<Void, List<ModelConfigEntity>> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Override
    protected void check(Void request) {
    }

    @Override
    protected List<ModelConfigEntity> process(Void request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 自己创建的用户级模型
        List<ModelConfigEntity> userModels = modelConfigInfra.listPageByOwnerId(currentUserId, 1, 1000).getRecords();
        // 被授权的系统级模型
        List<Long> grantedIds = resourceGrantInfra.listGrantedResourceIds(currentUserId, "MODEL");
        List<ModelConfigEntity> systemModels = modelConfigInfra.listByIdsAndScope(grantedIds);
        // 合并
        List<ModelConfigEntity> result = new ArrayList<>(userModels.size() + systemModels.size());
        result.addAll(userModels);
        result.addAll(systemModels);
        return result;
    }
}
