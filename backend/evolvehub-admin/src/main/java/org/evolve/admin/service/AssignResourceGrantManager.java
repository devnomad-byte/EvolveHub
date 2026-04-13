package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.AssignResourceGrantRequest;
import org.evolve.admin.response.AssignResourceGrantResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ResourceGrantInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.resource.model.ResourceGrantEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 资源授权业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>目标用户必须存在</li>
 *     <li>不能重复授权</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Service
public class AssignResourceGrantManager extends BaseManager<AssignResourceGrantRequest, AssignResourceGrantResponse> {

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(AssignResourceGrantRequest request) {
        if (usersInfra.getById(request.userId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
        if (resourceGrantInfra.exists(request.userId(), request.resourceType(), request.resourceId())) {
            throw new BusinessException(ResultCode.RESOURCE_GRANT_ALREADY_EXIST, "该授权记录已存在");
        }
    }

    @Override
    protected AssignResourceGrantResponse process(AssignResourceGrantRequest request) {
        ResourceGrantEntity entity = new ResourceGrantEntity();
        entity.setUserId(request.userId());
        entity.setResourceType(request.resourceType());
        entity.setResourceId(request.resourceId());
        resourceGrantInfra.createGrant(entity);
        return new AssignResourceGrantResponse(entity.getId());
    }
}
