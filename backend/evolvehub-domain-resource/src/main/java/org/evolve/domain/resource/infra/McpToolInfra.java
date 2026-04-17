package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.McpToolEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MCP 工具数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Repository
public class McpToolInfra extends ServiceImpl<McpToolInfra.McpToolMapper, McpToolEntity> {

    @Mapper
    public interface McpToolMapper extends BaseMapper<McpToolEntity> {}

    public McpToolEntity getById(Long id) {
        return super.getById(id);
    }

    /**
     * 按 MCP Server ID 查询所有工具
     */
    public List<McpToolEntity> listByMcpConfigId(Long mcpConfigId) {
        return this.lambdaQuery()
                .eq(McpToolEntity::getMcpConfigId, mcpConfigId)
                .eq(McpToolEntity::getDeleted, 0)
                .list();
    }

    /**
     * 按 MCP Server ID 分页查询工具
     */
    public Page<McpToolEntity> listPageByMcpConfigId(Long mcpConfigId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(McpToolEntity::getMcpConfigId, mcpConfigId)
                .eq(McpToolEntity::getDeleted, 0)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 查询用户可用的工具
     * <p>
     * 可见性规则：
     * 1. SYSTEM → 所有用户可见
     * 2. DEPT 且 deptId 在用户部门祖先链中 → 部门及子部门可见
     * 3. USER 且 ownerId = userId → 创建者可见
     * 4. GRANT 且 toolId 在 grantToolIds 中 → 通过 eh_mcp_tool_grant 授权可见
     * </p>
     *
     * @param mcpConfigId     MCP 服务 ID
     * @param ancestorDeptIds 用户部门及所有祖先部门 ID 列表（含自身）
     * @param grantToolIds    通过 eh_mcp_tool_grant 授权给该用户的工具 ID 列表
     */
    public List<McpToolEntity> listVisibleTools(Long mcpConfigId, List<Long> ancestorDeptIds, List<Long> grantToolIds) {
        return this.lambdaQuery()
                .eq(McpToolEntity::getMcpConfigId, mcpConfigId)
                .eq(McpToolEntity::getEnabled, 1)
                .eq(McpToolEntity::getDeleted, 0)
                .and(w -> {
                    w.eq(McpToolEntity::getToolScope, "SYSTEM");

                    if (ancestorDeptIds != null && !ancestorDeptIds.isEmpty()) {
                        w.or(ow -> ow
                                .eq(McpToolEntity::getToolScope, "DEPT")
                                .in(McpToolEntity::getDeptId, ancestorDeptIds)
                        );
                    }

                    if (grantToolIds != null && !grantToolIds.isEmpty()) {
                        w.or(ow -> ow.in(McpToolEntity::getId, grantToolIds));
                    }
                })
                .list();
    }

    public void saveTool(McpToolEntity entity) {
        this.save(entity);
    }

    public void updateTool(McpToolEntity entity) {
        this.updateById(entity);
    }

    public void deleteTool(Long id) {
        this.removeById(id);
    }

    /**
     * 通用分页查询（不分 scope）
     */
    public Page<McpToolEntity> listPage(int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(McpToolEntity::getDeleted, 0)
                .orderByDesc(McpToolEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }
}
