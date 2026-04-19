package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.AgentRuntimeConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AgentRuntimeConfigInfra extends ServiceImpl<AgentRuntimeConfigInfra.AgentRuntimeConfigMapper, AgentRuntimeConfigEntity> {

    @Mapper
    interface AgentRuntimeConfigMapper extends BaseMapper<AgentRuntimeConfigEntity> {}

    /**
     * 根据配置键获取配置
     */
    public AgentRuntimeConfigEntity getByKey(String configKey) {
        return lambdaQuery()
                .eq(AgentRuntimeConfigEntity::getConfigKey, configKey)
                .eq(AgentRuntimeConfigEntity::getDeleted, 0)
                .one();
    }

    /**
     * 获取所有未删除的配置
     */
    public List<AgentRuntimeConfigEntity> listAll() {
        return lambdaQuery()
                .eq(AgentRuntimeConfigEntity::getDeleted, 0)
                .orderByAsc(AgentRuntimeConfigEntity::getId)
                .list();
    }

    /**
     * 更新配置值
     */
    public void updateConfigValue(String configKey, String configValue) {
        lambdaUpdate()
                .eq(AgentRuntimeConfigEntity::getConfigKey, configKey)
                .set(AgentRuntimeConfigEntity::getConfigValue, configValue)
                .update();
    }

    /**
     * 插入新配置
     */
    public void insertConfig(String configKey, String configValue, String description) {
        AgentRuntimeConfigEntity entity = new AgentRuntimeConfigEntity();
        entity.setConfigKey(configKey);
        entity.setConfigValue(configValue);
        entity.setDescription(description);
        save(entity);
    }
}
