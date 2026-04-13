package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.rbac.infra.RoleDataScopeInfra;
import org.evolve.domain.rbac.infra.RolePermissionsInfra;
import org.evolve.domain.rbac.infra.RolesInfra;
import org.evolve.domain.rbac.infra.UserRolesInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除角色业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>角色必须存在</li>
 *     <li>角色下有用户持有时不允许删除（需先移除所有用户的该角色）</li>
 *     <li>删除角色时级联清理 t_role_permission 和 t_role_data_scope 关联记录</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class DeleteRoleManager extends BaseManager<Long, Void> {

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Resource
    private RoleDataScopeInfra roleDataScopeInfra;

    @Override
    protected void check(Long id) {
        if (rolesInfra.getRoleById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        if (userRolesInfra.countByRoleId(id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该角色下仍有用户持有，请先移除所有用户的该角色");
        }
    }

    @Override
    protected Void process(Long id) {
        rolePermissionsInfra.removeByRoleId(id);
        roleDataScopeInfra.removeByRoleId(id);
        rolesInfra.deleteRole(id);
        return null;
    }
}
