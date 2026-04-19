package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.DesktopCategoryInfra;
import org.evolve.domain.resource.model.DesktopCategoryEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.admin.request.CreateCategoryRequest;
import org.evolve.admin.response.CategoryResponse;

@Slf4j
@Component
public class DesktopCategoryCreateManager extends BaseManager<CreateCategoryRequest, CategoryResponse> {

    @Resource
    private DesktopCategoryInfra categoryInfra;

    @Override
    protected void check(CreateCategoryRequest req) {
        if (!StpUtil.hasPermission("desktop:manage")) {
            throw new BusinessException("无权管理桌面配置");
        }
        if (req.name() == null || req.name().isBlank()) {
            throw new BusinessException("分类名称不能为空");
        }
    }

    @Override
    protected CategoryResponse process(CreateCategoryRequest req) {
        DesktopCategoryEntity entity = new DesktopCategoryEntity();
        entity.setName(req.name());
        entity.setIcon(req.icon() != null ? req.icon() : "📁");
        entity.setColor(req.color() != null ? req.color() : "#0A84FF");
        entity.setSort(req.sort() != null ? req.sort() : 0);
        entity.setStatus(1);
        entity.setCreateBy(StpUtil.getLoginIdAsLong());

        categoryInfra.save(entity);
        log.info("Created desktop category: id={}, name={}", entity.getId(), entity.getName());

        return CategoryResponse.from(entity, 0);
    }
}
