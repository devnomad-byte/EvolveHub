package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.DesktopIconInfra;
import org.evolve.domain.rbac.infra.PermissionsInfra;
import org.evolve.domain.resource.model.DesktopIconEntity;
import org.evolve.domain.rbac.model.PermissionsEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.page.PageResponse;
import org.evolve.admin.response.IconResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DesktopIconListManager extends BaseManager<Void, PageResponse<IconResponse>> {

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
    protected PageResponse<IconResponse> process(Void req) {
        List<DesktopIconEntity> icons = iconInfra.listAll();

        List<IconResponse> responses = icons.stream()
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

        return new PageResponse<>(responses, responses.size(), 1, responses.size());
    }
}
