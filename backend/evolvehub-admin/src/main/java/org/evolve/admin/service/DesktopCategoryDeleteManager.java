package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.DesktopCategoryInfra;
import org.evolve.domain.resource.model.DesktopCategoryEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;

@Slf4j
@Component
public class DesktopCategoryDeleteManager extends BaseManager<Long, Boolean> {

    @Resource
    private DesktopCategoryInfra categoryInfra;

    @Override
    protected void check(Long id) {
        if (!StpUtil.hasPermission("desktop:manage")) {
            throw new BusinessException("无权管理桌面配置");
        }
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        DesktopCategoryEntity existing = categoryInfra.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }
    }

    @Override
    protected Boolean process(Long id) {
        // 软删除：将 deleted 设置为 1
        categoryInfra.lambdaUpdate()
                .eq(DesktopCategoryEntity::getId, id)
                .eq(DesktopCategoryEntity::getDeleted, 0)
                .set(DesktopCategoryEntity::getDeleted, 1)
                .update();
        log.info("Deleted desktop category: id={}", id);
        return true;
    }
}
