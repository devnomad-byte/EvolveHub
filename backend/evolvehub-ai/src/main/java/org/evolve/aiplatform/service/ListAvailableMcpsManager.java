package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.McpConfigInfra;
import org.evolve.common.infra.ResourceGrantInfra;
import org.evolve.common.model.McpConfigEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询当前用户可用的 MCP 服务列表（自己的 USER MCP + 被授权的 SYSTEM MCP）
 *
 * @author zhao
 */
@Service
public class ListAvailableMcpsManager extends BaseManager<Void, List<McpConfigEntity>> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Override
    protected void check(Void request) {
    }

    @Override
    protected List<McpConfigEntity> process(Void request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<McpConfigEntity> userMcps = mcpConfigInfra.listPageByOwnerId(currentUserId, 1, 1000).getRecords();
        List<Long> grantedIds = resourceGrantInfra.listGrantedResourceIds(currentUserId, "MCP");
        List<McpConfigEntity> systemMcps = mcpConfigInfra.listByIdsAndScope(grantedIds);
        List<McpConfigEntity> result = new ArrayList<>(userMcps.size() + systemMcps.size());
        result.addAll(userMcps);
        result.addAll(systemMcps);
        return result;
    }
}
