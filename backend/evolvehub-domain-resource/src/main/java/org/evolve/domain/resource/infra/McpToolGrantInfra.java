package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.McpToolGrantEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MCP 工具授权数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Repository
public class McpToolGrantInfra extends ServiceImpl<McpToolGrantInfra.McpToolGrantMapper, McpToolGrantEntity> {

    @Mapper
    public interface McpToolGrantMapper extends BaseMapper<McpToolGrantEntity> {}

    /**
     * 按工具ID查询所有授权
     */
    public List<McpToolGrantEntity> listByToolId(Long toolId) {
        return this.lambdaQuery()
                .eq(McpToolGrantEntity::getToolId, toolId)
                .eq(McpToolGrantEntity::getDeleted, 0)
                .list();
    }

    /**
     * 查询用户是否被授权访问某工具
     */
    public boolean hasGrant(Long toolId, Long userId, Long deptId) {
        return this.lambdaQuery()
                .eq(McpToolGrantEntity::getToolId, toolId)
                .eq(McpToolGrantEntity::getDeleted, 0)
                .and(w -> w
                        .eq(McpToolGrantEntity::getGrantType, "USER")
                        .eq(McpToolGrantEntity::getTargetId, userId)
                        .or()
                        .eq(McpToolGrantEntity::getGrantType, "DEPT")
                        .eq(McpToolGrantEntity::getTargetId, deptId)
                )
                .count() > 0;
    }

    /**
     * 查询用户被授权的所有工具ID（支持祖先部门链）
     *
     * @param userId          用户 ID
     * @param ancestorDeptIds 用户部门及其所有祖先部门 ID 列表（含自身）
     */
    public List<Long> listGrantedToolIds(Long userId, List<Long> ancestorDeptIds) {
        return this.lambdaQuery()
                .eq(McpToolGrantEntity::getDeleted, 0)
                .and(w -> {
                    w.eq(McpToolGrantEntity::getGrantType, "USER")
                            .eq(McpToolGrantEntity::getTargetId, userId);
                    if (ancestorDeptIds != null && !ancestorDeptIds.isEmpty()) {
                        w.or(ow -> ow
                                .eq(McpToolGrantEntity::getGrantType, "DEPT")
                                .in(McpToolGrantEntity::getTargetId, ancestorDeptIds)
                        );
                    }
                })
                .list()
                .stream()
                .map(McpToolGrantEntity::getToolId)
                .distinct()
                .toList();
    }

    public void saveGrant(McpToolGrantEntity entity) {
        this.save(entity);
    }

    public void deleteGrant(Long id) {
        this.removeById(id);
    }

    public void deleteByToolId(Long toolId) {
        this.lambdaUpdate()
                .eq(McpToolGrantEntity::getToolId, toolId)
                .remove();
    }
}
