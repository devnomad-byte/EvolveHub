package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.DesktopIconInfra;
import org.evolve.domain.resource.model.DesktopIconEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.admin.request.UpdateIconRequest;
import org.evolve.admin.response.IconResponse;

@Slf4j
@Component
public class DesktopIconUpdateManager extends BaseManager<UpdateIconRequest, IconResponse> {

    @Resource
    private DesktopIconInfra iconInfra;

    @Override
    protected void check(UpdateIconRequest req) {
        if (!StpUtil.hasPermission("desktop:manage")) {
            throw new BusinessException("无权管理桌面配置");
        }
        if (req.permId() == null) {
            throw new BusinessException("图标权限ID不能为空");
        }
    }

    @Override
    protected IconResponse process(UpdateIconRequest req) {
        DesktopIconEntity entity = iconInfra.getByPermId(req.permId());
        if (entity == null) {
            throw new BusinessException("图标配置不存在");
        }

        if (req.categoryId() != null) entity.setCategoryId(req.categoryId());
        if (req.isDesktop() != null) entity.setIsDesktop(req.isDesktop());
        if (req.sort() != null) entity.setSort(req.sort());

        iconInfra.updateById(entity);
        log.info("Updated desktop icon: permId={}, categoryId={}, isDesktop={}",
                req.permId(), req.categoryId(), req.isDesktop());

        return IconResponse.from(entity, null, null, null, null);
    }
}
