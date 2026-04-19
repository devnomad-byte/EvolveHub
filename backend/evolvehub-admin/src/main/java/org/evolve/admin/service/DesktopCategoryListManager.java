package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.DesktopCategoryInfra;
import org.evolve.domain.resource.infra.DesktopIconInfra;
import org.evolve.domain.resource.model.DesktopCategoryEntity;
import org.evolve.domain.resource.model.DesktopIconEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.admin.response.CategoryResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DesktopCategoryListManager extends BaseManager<Void, List<CategoryResponse>> {

    @Resource
    private DesktopCategoryInfra categoryInfra;

    @Resource
    private DesktopIconInfra iconInfra;

    @Override
    protected void check(Void req) {
        if (!StpUtil.hasPermission("desktop:view")) {
            throw new BusinessException("无权查看桌面配置");
        }
    }

    @Override
    protected List<CategoryResponse> process(Void req) {
        List<DesktopCategoryEntity> categories = categoryInfra.lambdaQuery()
                .eq(DesktopCategoryEntity::getDeleted, 0)
                .orderByAsc(DesktopCategoryEntity::getSort)
                .list();

        return categories.stream()
                .map(cat -> {
                    int iconCount = iconInfra.lambdaQuery()
                            .eq(DesktopIconEntity::getCategoryId, cat.getId())
                            .eq(DesktopIconEntity::getDeleted, 0)
                            .count()
                            .intValue();
                    return CategoryResponse.from(cat, iconCount);
                })
                .collect(Collectors.toList());
    }
}
