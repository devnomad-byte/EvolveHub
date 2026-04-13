package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.RevokeResourceGrantRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.ResourceGrantInfra;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 撤销资源授权业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Service
public class RevokeResourceGrantManager extends BaseManager<RevokeResourceGrantRequest, Void> {

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Override
    protected void check(RevokeResourceGrantRequest request) {
        if (!resourceGrantInfra.exists(request.userId(), request.resourceType(), request.resourceId())) {
            throw new BusinessException(ResultCode.RESOURCE_GRANT_NOT_EXIST, "授权记录不存在");
        }
    }

    @Override
    protected Void process(RevokeResourceGrantRequest request) {
        resourceGrantInfra.revokeGrant(request.userId(), request.resourceType(), request.resourceId());
        return null;
    }
}
