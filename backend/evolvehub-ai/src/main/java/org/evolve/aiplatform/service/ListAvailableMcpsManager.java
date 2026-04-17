package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.infra.ResourceGrantInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 查询当前用户可用的 MCP 服务列表
 * <p>
 * 可见性规则：
 * 1. scope=SYSTEM 且 enabled=1 → 所有用户可见
 * 2. scope=DEPT 且 deptId 在用户部门及祖先链中 → 部门级可见
 * 3. 通过 eh_resource_grant 授权给该用户的资源 → 授权可见
 * </p>
 *
 * @author zhao
 */
@Service
public class ListAvailableMcpsManager extends BaseManager<Void, List<McpConfigEntity>> {

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(Void request) {
    }

    @Override
    protected List<McpConfigEntity> process(Void request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UsersEntity user = usersInfra.getUserById(currentUserId);
        Long deptId = user != null ? user.getDeptId() : null;

        // 构建部门祖先链（含自身部门）
        List<Long> ancestorDeptIds = deptInfra.getAncestorDeptIds(deptId);

        // 查询授权给该用户（及其部门）的资源 ID
        List<Long> grantResourceIds = resourceGrantInfra.listVisibleGrantedResourceIds(
                currentUserId, deptId, "MCP");

        // 统一可见性查询
        return mcpConfigInfra.listVisibleMcps(ancestorDeptIds, grantResourceIds);
    }
}
