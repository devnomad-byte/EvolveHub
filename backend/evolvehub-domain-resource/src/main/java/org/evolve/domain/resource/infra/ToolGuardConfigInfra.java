package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ToolGuardConfigEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 工具守卫配置数据访问层
 */
@Repository
public class ToolGuardConfigInfra extends ServiceImpl<ToolGuardConfigInfra.ToolGuardConfigMapper, ToolGuardConfigEntity> {

    @Mapper
    public interface ToolGuardConfigMapper extends BaseMapper<ToolGuardConfigEntity> {}

    public ToolGuardConfigEntity getConfig() {
        return this.lambdaQuery()
                .eq(ToolGuardConfigEntity::getId, 1L)
                .one();
    }

    public void updateEnabled(Integer enabled) {
        this.lambdaUpdate()
                .eq(ToolGuardConfigEntity::getId, 1L)
                .set(ToolGuardConfigEntity::getEnabled, enabled)
                .set(ToolGuardConfigEntity::getUpdateTime, LocalDateTime.now())
                .update();
    }

    public void updateConfig(String guardedTools, String deniedTools) {
        this.lambdaUpdate()
                .eq(ToolGuardConfigEntity::getId, 1L)
                .set(guardedTools != null, ToolGuardConfigEntity::getGuardedTools, guardedTools)
                .set(deniedTools != null, ToolGuardConfigEntity::getDeniedTools, deniedTools)
                .set(ToolGuardConfigEntity::getUpdateTime, LocalDateTime.now())
                .update();
    }
}
