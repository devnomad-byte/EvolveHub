package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.rbac.infra.RolesInfra;
import org.evolve.domain.rbac.infra.UserRolesInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.UserRolesEntity;
import org.evolve.admin.request.AssignUserRoleRequest;
import org.evolve.admin.response.AssignUserRoleResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 为用户分配角色业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>用户必须存在</li>
 *     <li>角色必须存在</li>
 *     <li>不允许重复分配同一角色</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class AssignUserRoleManager extends BaseManager<AssignUserRoleRequest, AssignUserRoleResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(AssignUserRoleRequest request) {
        if (usersInfra.getUserById(request.userId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
        if (rolesInfra.getRoleById(request.roleId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        if (userRolesInfra.getByUserIdAndRoleId(request.userId(), request.roleId()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "该用户已持有此角色");
        }
    }

    @Override
    protected AssignUserRoleResponse process(AssignUserRoleRequest request) {
        UserRolesEntity entity = new UserRolesEntity();
        entity.setUserId(request.userId());
        entity.setRoleId(request.roleId());
        userRolesInfra.assignRole(entity);
        return new AssignUserRoleResponse();
    }
}
