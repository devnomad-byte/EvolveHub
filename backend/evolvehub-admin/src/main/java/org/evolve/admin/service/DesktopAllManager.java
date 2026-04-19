package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.DesktopCategoryInfra;
import org.evolve.domain.resource.infra.DesktopIconInfra;
import org.evolve.domain.rbac.infra.PermissionsInfra;
import org.evolve.domain.resource.model.DesktopCategoryEntity;
import org.evolve.domain.resource.model.DesktopIconEntity;
import org.evolve.domain.rbac.model.PermissionsEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.admin.response.CategoryResponse;
import org.evolve.admin.response.DesktopAllResponse;
import org.evolve.admin.response.IconResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DesktopAllManager extends BaseManager<Void, DesktopAllResponse> {

    @Resource
    private DesktopCategoryInfra categoryInfra;

    @Resource
    private DesktopIconInfra iconInfra;

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(Void req) {
        if (!StpUtil.hasPermission("desktop:view")) {
            throw new BusinessException("无权查看桌面配置");
        }
    }

    @Override
    protected DesktopAllResponse process(Void req) {
        // 获取所有分类
        List<DesktopCategoryEntity> categories = categoryInfra.lambdaQuery()
                .eq(DesktopCategoryEntity::getDeleted, 0)
                .orderByAsc(DesktopCategoryEntity::getSort)
                .list();

        List<CategoryResponse> categoryResponses = categories.stream()
                .map(cat -> {
                    int iconCount = iconInfra.lambdaQuery()
                            .eq(DesktopIconEntity::getCategoryId, cat.getId())
                            .eq(DesktopIconEntity::getDeleted, 0)
                            .count()
                            .intValue();
                    return CategoryResponse.from(cat, iconCount);
                })
                .collect(Collectors.toList());

        // 获取所有图标
        List<DesktopIconEntity> icons = iconInfra.listAll();
        List<IconResponse> iconResponses = icons.stream()
                .map(icon -> {
                    PermissionsEntity perm = permissionsInfra.getById(icon.getPermId());
                    if (perm != null) {
                        return IconResponse.from(
                                icon,
                                perm.getPermCode(),
                                perm.getPermName(),
                                perm.getIcon(),
                                perm.getGradient()
                        );
                    }
                    return null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        // 获取桌面图标
        List<DesktopIconEntity> desktopIcons = iconInfra.listDesktop();
        List<IconResponse> desktopIconResponses = desktopIcons.stream()
                .map(icon -> {
                    PermissionsEntity perm = permissionsInfra.getById(icon.getPermId());
                    if (perm != null) {
                        return IconResponse.from(
                                icon,
                                perm.getPermCode(),
                                perm.getPermName(),
                                perm.getIcon(),
                                perm.getGradient()
                        );
                    }
                    return null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        return new DesktopAllResponse(categoryResponses, iconResponses, desktopIconResponses);
    }
}
