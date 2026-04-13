package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.DesktopIconResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.PermissionsInfra;
import org.evolve.domain.rbac.model.PermissionsEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询桌面图标列表业务处理器
 *
 * @author zhao
 */
@Service
public class ListDesktopIconManager extends BaseManager<Void, List<DesktopIconResponse>> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(Void request) {
        // 无需校验
    }

    @Override
    protected List<DesktopIconResponse> process(Void request) {
        List<PermissionsEntity> icons = permissionsInfra.listDesktopIcons();
        return icons.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
