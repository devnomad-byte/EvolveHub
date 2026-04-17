package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.McpInstanceEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MCP 服务实例数据访问层
 *
 * @author devnomad-byte
 * @version v1.0
 * @date 2026/4/15
 */
@Repository
public class McpInstanceInfra extends ServiceImpl<McpInstanceInfra.McpInstanceMapper, McpInstanceEntity> {

    @Mapper
    public interface McpInstanceMapper extends BaseMapper<McpInstanceEntity> {}

    /**
     * 根据 ID 查询实例
     */
    public McpInstanceEntity getById(Long id) {
        return super.getById(id);
    }

    /**
     * 根据 instanceKey 查询实例
     */
    public McpInstanceEntity getByInstanceKey(String instanceKey) {
        return this.lambdaQuery()
                .eq(McpInstanceEntity::getInstanceKey, instanceKey)
                .one();
    }

    /**
     * 根据 MCP Config ID 查询运行中的实例
     */
    public McpInstanceEntity getRunningByConfigId(Long mcpConfigId) {
        return this.lambdaQuery()
                .eq(McpInstanceEntity::getMcpConfigId, mcpConfigId)
                .eq(McpInstanceEntity::getStatus, "RUNNING")
                .one();
    }

    /**
     * 查询所有运行中的实例
     */
    public List<McpInstanceEntity> listRunning() {
        return this.lambdaQuery()
                .eq(McpInstanceEntity::getStatus, "RUNNING")
                .list();
    }

    /**
     * 查询所有实例（分页）
     */
    public Page<McpInstanceEntity> listPage(int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(McpInstanceEntity::getDeleted, 0)
                .orderByDesc(McpInstanceEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 根据 MCP Config ID 查询所有实例
     */
    public List<McpInstanceEntity> listByConfigId(Long mcpConfigId) {
        return this.lambdaQuery()
                .eq(McpInstanceEntity::getMcpConfigId, mcpConfigId)
                .eq(McpInstanceEntity::getDeleted, 0)
                .orderByDesc(McpInstanceEntity::getCreateTime)
                .list();
    }

    /**
     * 创建实例
     */
    public void saveInstance(McpInstanceEntity entity) {
        this.save(entity);
    }

    /**
     * 更新实例
     */
    public void updateInstance(McpInstanceEntity entity) {
        this.updateById(entity);
    }

    /**
     * 删除实例（逻辑删除）
     */
    public void deleteInstance(Long id) {
        this.removeById(id);
    }

    /**
     * 更新心跳时间
     */
    public void updateHeartbeat(Long id) {
        this.lambdaUpdate()
                .eq(McpInstanceEntity::getId, id)
                .set(McpInstanceEntity::getLastHeartbeat, LocalDateTime.now())
                .set(McpInstanceEntity::getFailCount, 0)
                .update();
    }

    /**
     * 增加心跳失败次数
     */
    public void incrementFailCount(Long id, int failCount) {
        this.lambdaUpdate()
                .eq(McpInstanceEntity::getId, id)
                .set(McpInstanceEntity::getFailCount, failCount)
                .update();
    }

    /**
     * 更新实例状态
     */
    public void updateStatus(Long id, String status) {
        this.lambdaUpdate()
                .eq(McpInstanceEntity::getId, id)
                .set(McpInstanceEntity::getStatus, status)
                .update();
    }

    /**
     * 更新实例状态和错误信息
     */
    public void updateStatusAndError(Long id, String status, String errorMsg) {
        this.lambdaUpdate()
                .eq(McpInstanceEntity::getId, id)
                .set(McpInstanceEntity::getStatus, status)
                .set(McpInstanceEntity::getErrorMsg, errorMsg)
                .update();
    }
}
