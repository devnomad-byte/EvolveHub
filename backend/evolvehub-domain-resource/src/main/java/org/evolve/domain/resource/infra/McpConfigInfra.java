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
 * @version v1.0
 * @date 2026/4/13
 */
@Repository
public class McpConfigInfra extends ServiceImpl<McpConfigInfra.McpConfigMapper, McpConfigEntity> {

    @Mapper
    interface McpConfigMapper extends BaseMapper<McpConfigEntity> {}

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
     * 按 scope 分页查询
     */
    public Page<McpConfigEntity> listPageByScope(String scope, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(McpConfigEntity::getScope, scope)
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
     * 查询用户可用的系统级 MCP 服务（通过授权 ID 列表）
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
}
