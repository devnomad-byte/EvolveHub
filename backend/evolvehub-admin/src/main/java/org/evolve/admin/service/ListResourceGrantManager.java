package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.ResourceGrantInfra;
import org.evolve.common.model.ResourceGrantEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询资源授权列表业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Service
public class ListResourceGrantManager extends BaseManager<PageRequest, PageResponse<ResourceGrantEntity>> {

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<ResourceGrantEntity> process(PageRequest request) {
        Page<ResourceGrantEntity> page = resourceGrantInfra.listPage(request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
