package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.RoleDataScopeInfra;
import org.evolve.auth.user.infra.RolePermissionsInfra;
import org.evolve.auth.user.infra.RolesInfra;
import org.evolve.auth.user.infra.UserRolesInfra;
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
        // 角色必须存在
        if (rolesInfra.getRoleById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        // 角色下有用户持有时不允许删除
        if (userRolesInfra.countByRoleId(id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该角色下仍有用户持有，请先移除所有用户的该角色");
        }
    }

    @Override
    protected Void process(Long id) {
        // 清理角色-权限关联
        rolePermissionsInfra.removeByRoleId(id);
        // 清理角色-数据范围关联
        roleDataScopeInfra.removeByRoleId(id);
        // 删除角色
        rolesInfra.deleteRole(id);
        return null;
    }
}
