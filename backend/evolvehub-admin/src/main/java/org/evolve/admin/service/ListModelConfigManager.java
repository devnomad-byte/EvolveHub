package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询模型配置列表业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/10
 */
@Service
public class ListModelConfigManager extends BaseManager<PageRequest, PageResponse<ModelConfigEntity>> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<ModelConfigEntity> process(PageRequest request) {
        Page<ModelConfigEntity> page = modelConfigInfra.listPageByScope("SYSTEM", request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
