package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.ApiKeyInfra;
import org.evolve.common.model.ApiKeyEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询 API Key 列表业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Service
public class ListApiKeyManager extends BaseManager<PageRequest, PageResponse<ApiKeyEntity>> {

    @Resource
    private ApiKeyInfra apiKeyInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<ApiKeyEntity> process(PageRequest request) {
        Page<ApiKeyEntity> page = apiKeyInfra.page(
                new Page<>(request.pageNum(), request.pageSize()));
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
