package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.RolePermissionsInfra;
import org.evolve.auth.user.request.RemoveRolePermissionRequest;
import org.evolve.auth.user.response.RemoveRolePermissionResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 移除角色权限业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>角色-权限关联必须存在（角色确实持有该权限）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class RemoveRolePermissionManager extends BaseManager<RemoveRolePermissionRequest, RemoveRolePermissionResponse> {

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Override
    protected void check(RemoveRolePermissionRequest request) {
        // 关联记录必须存在
        if (rolePermissionsInfra.getByRoleIdAndPermissionId(request.roleId(), request.permissionId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "该角色未持有此权限");
        }
    }

    @Override
    protected RemoveRolePermissionResponse process(RemoveRolePermissionRequest request) {
        rolePermissionsInfra.removeByRoleIdAndPermissionId(request.roleId(), request.permissionId());
        return new RemoveRolePermissionResponse();
    }
}
