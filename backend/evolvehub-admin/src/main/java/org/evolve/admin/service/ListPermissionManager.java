package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.infra.PermissionsInfra;
import org.evolve.common.model.PermissionsEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 权限分页列表业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class ListPermissionManager extends BaseManager<PageRequest, PageResponse<PermissionsEntity>> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<PermissionsEntity> process(PageRequest request) {
        Page<PermissionsEntity> page = permissionsInfra.listPage(request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
