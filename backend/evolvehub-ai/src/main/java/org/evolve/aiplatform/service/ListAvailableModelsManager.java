package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.infra.ResourceGrantInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 查询当前用户可用的模型列表
 * <p>
 * 可见性规则同 ListAvailableMcpsManager：
 * SYSTEM 全部 + DEPT 祖先链 + GRANT 授权
 * </p>
 *
 * @author zhao
 */
@Service
public class ListAvailableModelsManager extends BaseManager<Void, List<ModelConfigEntity>> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(Void request) {
    }

    @Override
    protected List<ModelConfigEntity> process(Void request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UsersEntity user = usersInfra.getUserById(currentUserId);
        Long deptId = user != null ? user.getDeptId() : null;

        List<Long> ancestorDeptIds = deptInfra.getAncestorDeptIds(deptId);
        List<Long> grantResourceIds = resourceGrantInfra.listVisibleGrantedResourceIds(
                currentUserId, deptId, "MODEL");

        return modelConfigInfra.listVisibleModels(ancestorDeptIds, grantResourceIds);
    }
}
