package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.AgentRuntimeConfigHistoryEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AgentRuntimeConfigHistoryInfra extends ServiceImpl<AgentRuntimeConfigHistoryInfra.AgentRuntimeConfigHistoryMapper, AgentRuntimeConfigHistoryEntity> {

    @Mapper
    interface AgentRuntimeConfigHistoryMapper extends BaseMapper<AgentRuntimeConfigHistoryEntity> {}

    /**
     * 记录配置变更
     */
    public void recordChange(Long operatorId, String operatorName, String configKey, String oldValue, String newValue, String changeReason) {
        AgentRuntimeConfigHistoryEntity entity = new AgentRuntimeConfigHistoryEntity();
        entity.setOperatorId(operatorId);
        entity.setOperatorName(operatorName);
        entity.setConfigKey(configKey);
        entity.setOldValue(oldValue);
        entity.setNewValue(newValue);
        entity.setChangeReason(changeReason);
        entity.setCreateTime(LocalDateTime.now());
        save(entity);
    }

    /**
     * 分页查询历史记录
     */
    public Page<AgentRuntimeConfigHistoryEntity> pageByConfigKey(String configKey, int pageNum, int pageSize) {
        Page<AgentRuntimeConfigHistoryEntity> page = new Page<>(pageNum, pageSize);
        return lambdaQuery()
                .eq(configKey != null && !configKey.isEmpty(), AgentRuntimeConfigHistoryEntity::getConfigKey, configKey)
                .orderByDesc(AgentRuntimeConfigHistoryEntity::getCreateTime)
                .page(page);
    }

    /**
     * 查询所有历史记录
     */
    public List<AgentRuntimeConfigHistoryEntity> listAll() {
        return lambdaQuery()
                .orderByDesc(AgentRuntimeConfigHistoryEntity::getCreateTime)
                .list();
    }
}
