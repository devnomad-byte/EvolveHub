package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateDesktopIconRequest;
import org.evolve.admin.response.DesktopIconResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.PermissionsInfra;
import org.evolve.domain.rbac.model.PermissionsEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建桌面图标业务处理器
 *
 * @author zhao
 */
@Service
public class CreateDesktopIconManager extends BaseManager<CreateDesktopIconRequest, DesktopIconResponse> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(CreateDesktopIconRequest request) {
        // 校验 permCode 唯一性
        if (permissionsInfra.getByPermCode(request.permCode()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "图标编码已存在");
        }
    }

    @Override
    protected DesktopIconResponse process(CreateDesktopIconRequest request) {
        PermissionsEntity entity = new PermissionsEntity();
        entity.setParentId(0L); // 桌面图标是顶层
        entity.setPermName(request.permName());
        entity.setPermCode(request.permCode());
        entity.setPermType("MENU");
        entity.setIcon(request.icon());
        entity.setGradient(request.gradient());
        entity.setPath(request.path());
        entity.setSort(request.sort() != null ? request.sort() : 0);
        entity.setStatus(request.status() != null ? request.status() : 1);
        entity.setDefaultWidth(request.defaultWidth() != null ? request.defaultWidth() : 800);
        entity.setDefaultHeight(request.defaultHeight() != null ? request.defaultHeight() : 600);
        entity.setMinWidth(request.minWidth() != null ? request.minWidth() : 640);
        entity.setMinHeight(request.minHeight() != null ? request.minHeight() : 400);
        entity.setDockOrder(request.dockOrder() != null ? request.dockOrder() : -1);
        entity.setIsDesktopIcon(1); // 标记为桌面图标

        permissionsInfra.createPermission(entity);
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
