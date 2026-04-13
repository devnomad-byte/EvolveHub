package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.DesktopIconResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.PermissionsInfra;
import org.evolve.domain.rbac.model.PermissionsEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询单个桌面图标业务处理器
 *
 * @author zhao
 */
@Service
public class GetDesktopIconManager extends BaseManager<Long, DesktopIconResponse> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(Long id) {
        PermissionsEntity entity = permissionsInfra.getPermissionById(id);
        if (entity == null || entity.getIsDesktopIcon() == null || entity.getIsDesktopIcon() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "桌面图标不存在");
        }
    }

    @Override
    protected DesktopIconResponse process(Long id) {
        PermissionsEntity entity = permissionsInfra.getPermissionById(id);
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
