package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.rbac.infra.PermissionsInfra;
import org.evolve.domain.rbac.infra.RolePermissionsInfra;
import org.evolve.domain.rbac.infra.RolesInfra;
import org.evolve.domain.rbac.model.RolePermissionsEntity;
import org.evolve.admin.request.AssignRolePermissionRequest;
import org.evolve.admin.response.AssignRolePermissionResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 为角色分配权限业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>角色必须存在</li>
 *     <li>权限必须存在</li>
 *     <li>不允许重复分配同一权限</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class AssignRolePermissionManager extends BaseManager<AssignRolePermissionRequest, AssignRolePermissionResponse> {

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private PermissionsInfra permissionsInfra;

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Override
    protected void check(AssignRolePermissionRequest request) {
        if (rolesInfra.getRoleById(request.roleId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        if (permissionsInfra.getPermissionById(request.permissionId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "权限不存在");
        }
        if (rolePermissionsInfra.getByRoleIdAndPermissionId(request.roleId(), request.permissionId()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "该角色已持有此权限");
        }
    }

    @Override
    protected AssignRolePermissionResponse process(AssignRolePermissionRequest request) {
        RolePermissionsEntity entity = new RolePermissionsEntity();
        entity.setRoleId(request.roleId());
        entity.setPermissionId(request.permissionId());
        rolePermissionsInfra.assignPermission(entity);
        return new AssignRolePermissionResponse();
    }
}
