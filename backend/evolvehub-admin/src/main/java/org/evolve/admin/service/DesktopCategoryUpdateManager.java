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
import org.evolve.admin.request.UpdateCategoryRequest;
import org.evolve.admin.response.CategoryResponse;

@Slf4j
@Component
public class DesktopCategoryUpdateManager extends BaseManager<UpdateCategoryRequest, CategoryResponse> {

    @Resource
    private DesktopCategoryInfra categoryInfra;

    @Resource
    private DesktopIconInfra iconInfra;

    @Override
    protected void check(UpdateCategoryRequest req) {
        if (!StpUtil.hasPermission("desktop:manage")) {
            throw new BusinessException("无权管理桌面配置");
        }
        if (req.id() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        DesktopCategoryEntity existing = categoryInfra.getById(req.id());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }
    }

    @Override
    protected CategoryResponse process(UpdateCategoryRequest req) {
        DesktopCategoryEntity entity = categoryInfra.getById(req.id());

        if (req.name() != null) entity.setName(req.name());
        if (req.icon() != null) entity.setIcon(req.icon());
        if (req.color() != null) entity.setColor(req.color());
        if (req.sort() != null) entity.setSort(req.sort());
        if (req.status() != null) entity.setStatus(req.status());

        categoryInfra.updateById(entity);
        log.info("Updated desktop category: id={}", req.id());

        int iconCount = iconInfra.lambdaQuery()
                .eq(DesktopIconEntity::getCategoryId, entity.getId())
                .eq(DesktopIconEntity::getDeleted, 0)
                .count()
                .intValue();

        return CategoryResponse.from(entity, iconCount);
    }
}
