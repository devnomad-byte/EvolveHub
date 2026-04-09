package org.evolve.auth.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.RolesInfra;
import org.evolve.auth.user.model.RolesEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 角色分页列表业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class ListRoleManager extends BaseManager<PageRequest, PageResponse<RolesEntity>> {

    @Resource
    private RolesInfra rolesInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<RolesEntity> process(PageRequest request) {
        Page<RolesEntity> page = rolesInfra.listPage(request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
