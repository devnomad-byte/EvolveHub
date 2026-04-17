package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MCP 服务配置数据访问层
 *
 * @author zhao
 * @version v1.1
 * @date 2026/4/14
 */
@Repository
public class McpConfigInfra extends ServiceImpl<McpConfigInfra.McpConfigMapper, McpConfigEntity> {

    @Mapper
    public interface McpConfigMapper extends BaseMapper<McpConfigEntity> {}

    public McpConfigEntity getMcpConfigById(Long id) {
        return this.getById(id);
    }

    public McpConfigEntity getByName(String name) {
        return this.lambdaQuery().eq(McpConfigEntity::getName, name).one();
    }

    public void createMcpConfig(McpConfigEntity entity) {
        this.save(entity);
    }

    public void updateMcpConfig(McpConfigEntity entity) {
        this.updateById(entity);
    }

    public void deleteMcpConfig(Long id) {
        this.removeById(id);
    }

    /**
     * 按 scope 分页查询（SYSTEM / DEPT / USER）
     */
    public Page<McpConfigEntity> listPageByScope(String scope, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(McpConfigEntity::getScope, scope)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 按部门查询部门级 MCP（包含子部门）
     */
    public Page<McpConfigEntity> listPageByDeptId(Long deptId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(McpConfigEntity::getScope, "DEPT")
                .eq(McpConfigEntity::getDeptId, deptId)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 按 ownerId 分页查询（用户级资源）
     */
    public Page<McpConfigEntity> listPageByOwnerId(Long ownerId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(McpConfigEntity::getScope, "USER")
                .eq(McpConfigEntity::getOwnerId, ownerId)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 查询用户自建的已启用 MCP 服务
     *
     * @param ownerId 用户 ID
     * @return 已启用的 MCP 配置列表
     */
    public List<McpConfigEntity> listEnabledByOwnerId(Long ownerId) {
        return this.lambdaQuery()
                .eq(McpConfigEntity::getScope, "USER")
                .eq(McpConfigEntity::getOwnerId, ownerId)
                .eq(McpConfigEntity::getEnabled, 1)
                .list();
    }

    /**
     * 查询用户可用的 MCP
     * <p>
     * 可见性规则：
     * 1. scope=SYSTEM 且 enabled=1 → 所有用户可见
     * 2. scope=DEPT 且 deptId 在用户部门及其祖先链中 → 部门及子部门可见
     * 3. scope=USER 且 resourceId 在 grantIds 中 → 通过 eh_resource_grant 授权的用户可见
     * 4. scope=SYSTEM 且 resourceId 在 grantIds 中 → grant 表批量授权（按部门/角色）
     * </p>
     *
     * @param ancestorDeptIds 用户部门及其所有祖先部门的 ID 集合（含自身部门），用于 DEPT 匹配
     * @param grantResourceIds 通过 eh_resource_grant 授权给该用户的资源 ID 列表
     */
    public List<McpConfigEntity> listVisibleMcps(List<Long> ancestorDeptIds, List<Long> grantResourceIds) {
        return this.lambdaQuery()
                .eq(McpConfigEntity::getEnabled, 1)
                .and(w -> {
                    // 1. 所有 SYSTEM 级资源
                    w.eq(McpConfigEntity::getScope, "SYSTEM");

                    // 2. DEPT 级：资源 deptId 在用户的部门及祖先链中
                    if (ancestorDeptIds != null && !ancestorDeptIds.isEmpty()) {
                        w.or(ow -> ow
                                .eq(McpConfigEntity::getScope, "DEPT")
                                .in(McpConfigEntity::getDeptId, ancestorDeptIds)
                        );
                    }

                    // 3. grant 表授权的资源（USER 级 + 批量授权的 SYSTEM 级）
                    if (grantResourceIds != null && !grantResourceIds.isEmpty()) {
                        w.or(ow -> ow.in(McpConfigEntity::getId, grantResourceIds));
                    }
                })
                .list();
    }

    /**
     * 查询用户可用的系统级 MCP（通过授权 ID 列表）
     */
    public List<McpConfigEntity> listByIdsAndScope(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }
        return this.lambdaQuery()
                .eq(McpConfigEntity::getScope, "SYSTEM")
                .eq(McpConfigEntity::getEnabled, 1)
                .in(McpConfigEntity::getId, resourceIds)
                .list();
    }

    /**
     * 查询所有启用的系统级 MCP
     */
    public List<McpConfigEntity> listSystemEnabled() {
        return this.lambdaQuery()
                .eq(McpConfigEntity::getScope, "SYSTEM")
                .eq(McpConfigEntity::getEnabled, 1)
                .list();
    }

    /**
     * 通用分页查询（不分 scope）
     */
    public Page<McpConfigEntity> listPage(int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(McpConfigEntity::getDeleted, 0)
                .orderByDesc(McpConfigEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }
}
