package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.PermissionsInfra;
import org.evolve.domain.rbac.infra.RolePermissionsInfra;
import org.evolve.domain.rbac.model.PermissionsEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除桌面图标业务处理器
 *
 * @author zhao
 */
@Service
public class DeleteDesktopIconManager extends BaseManager<Long, Void> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Override
    protected void check(Long id) {
        PermissionsEntity entity = permissionsInfra.getPermissionById(id);
        if (entity == null || entity.getIsDesktopIcon() == null || entity.getIsDesktopIcon() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "桌面图标不存在");
        }
    }

    @Override
    protected Void process(Long id) {
        // 级联删除角色权限关联
        rolePermissionsInfra.removeByPermissionId(id);
        // 删除图标
        permissionsInfra.deletePermission(id);
        return null;
    }
}
