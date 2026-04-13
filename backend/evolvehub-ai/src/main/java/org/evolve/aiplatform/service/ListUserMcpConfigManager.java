package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询当前用户的用户级 MCP 配置列表
 *
 * @author zhao
 */
@Service
public class ListUserMcpConfigManager extends BaseManager<PageRequest, PageResponse<McpConfigEntity>> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<McpConfigEntity> process(PageRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Page<McpConfigEntity> page = mcpConfigInfra.listPageByOwnerId(currentUserId, request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
