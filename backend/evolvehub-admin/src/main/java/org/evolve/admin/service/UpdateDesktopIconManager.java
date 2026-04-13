package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateDesktopIconRequest;
import org.evolve.admin.response.DesktopIconResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.PermissionsInfra;
import org.evolve.domain.rbac.model.PermissionsEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新桌面图标业务处理器
 *
 * @author zhao
 */
@Service
public class UpdateDesktopIconManager extends BaseManager<UpdateDesktopIconRequest, DesktopIconResponse> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(UpdateDesktopIconRequest request) {
        PermissionsEntity entity = permissionsInfra.getPermissionById(request.id());
        if (entity == null || entity.getIsDesktopIcon() == null || entity.getIsDesktopIcon() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "桌面图标不存在");
        }
    }

    @Override
    protected DesktopIconResponse process(UpdateDesktopIconRequest request) {
        PermissionsEntity entity = permissionsInfra.getPermissionById(request.id());

        if (request.permName() != null) {
            entity.setPermName(request.permName());
        }
        if (request.icon() != null) {
            entity.setIcon(request.icon());
        }
        if (request.gradient() != null) {
            entity.setGradient(request.gradient());
        }
        if (request.path() != null) {
            entity.setPath(request.path());
        }
        if (request.sort() != null) {
            entity.setSort(request.sort());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.defaultWidth() != null) {
            entity.setDefaultWidth(request.defaultWidth());
        }
        if (request.defaultHeight() != null) {
            entity.setDefaultHeight(request.defaultHeight());
        }
        if (request.minWidth() != null) {
            entity.setMinWidth(request.minWidth());
        }
        if (request.minHeight() != null) {
            entity.setMinHeight(request.minHeight());
        }
        if (request.dockOrder() != null) {
            entity.setDockOrder(request.dockOrder());
        }

        permissionsInfra.updatePermission(entity);
        return toResponse(entity);
    }

    private DesktopIconResponse toResponse(PermissionsEntity entity) {
        return new DesktopIconResponse(
                entity.getId(),
                entity.getPermName(),
                entity.getPermCode(),
                entity.getIcon(),
                entity.getGradient(),
                entity.getPath(),
                entity.getSort(),
                entity.getStatus(),
                entity.getDefaultWidth(),
                entity.getDefaultHeight(),
                entity.getMinWidth(),
                entity.getMinHeight(),
                entity.getDockOrder()
        );
    }
}
