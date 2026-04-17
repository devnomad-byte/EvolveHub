package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询 MCP 配置列表业务处理器
 *
 * @author zhao
 */
@Service
public class ListMcpConfigManager extends BaseManager<PageRequest, PageResponse<McpConfigEntity>> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<McpConfigEntity> process(PageRequest request) {
        // 默认查询所有启用的 MCP（不分 scope）
        Page<McpConfigEntity> page = mcpConfigInfra.listPage(request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
