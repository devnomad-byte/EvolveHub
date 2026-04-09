package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.PermissionsInfra;
import org.evolve.auth.user.infra.RolePermissionsInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除权限业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>权限必须存在</li>
 *     <li>存在子权限时不允许删除（需先删除子权限）</li>
 *     <li>有角色持有该权限时不允许删除（需先移除所有角色的该权限）</li>
 *     <li>删除权限时级联清理 t_role_permission 关联记录</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class DeletePermissionManager extends BaseManager<Long, Void> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Override
    protected void check(Long id) {
        // 权限必须存在
        if (permissionsInfra.getPermissionById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "权限不存在");
        }
        // 存在子权限时不允许删除
        if (permissionsInfra.existsChildPermission(id)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该权限下存在子权限，请先删除子权限");
        }
        // 有角色持有该权限时不允许删除
        if (rolePermissionsInfra.countByPermissionId(id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该权限仍被角色持有，请先移除所有角色的该权限");
        }
    }

    @Override
    protected Void process(Long id) {
        // 清理权限的角色关联（兜底清理）
        rolePermissionsInfra.removeByPermissionId(id);
        // 删除权限
        permissionsInfra.deletePermission(id);
        return null;
    }
}
