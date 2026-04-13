package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.McpConfigInfra;
import org.evolve.common.model.McpConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询系统级 MCP 配置列表业务处理器
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
        Page<McpConfigEntity> page = mcpConfigInfra.listPageByScope("SYSTEM", request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
