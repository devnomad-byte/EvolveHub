package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.rbac.infra.UserRolesInfra;
import org.evolve.admin.request.RemoveUserRoleRequest;
import org.evolve.admin.response.RemoveUserRoleResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 移除用户角色业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>用户-角色关联必须存在（用户确实持有该角色）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class RemoveUserRoleManager extends BaseManager<RemoveUserRoleRequest, RemoveUserRoleResponse> {

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(RemoveUserRoleRequest request) {
        if (userRolesInfra.getByUserIdAndRoleId(request.userId(), request.roleId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "该用户未持有此角色");
        }
    }

    @Override
    protected RemoveUserRoleResponse process(RemoveUserRoleRequest request) {
        userRolesInfra.removeByUserIdAndRoleId(request.userId(), request.roleId());
        return new RemoveUserRoleResponse();
    }
}
