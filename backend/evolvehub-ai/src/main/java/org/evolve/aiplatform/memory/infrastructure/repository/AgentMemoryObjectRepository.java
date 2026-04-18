package org.evolve.aiplatform.memory.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryObjectEntity;
import org.evolve.aiplatform.memory.infrastructure.mapper.AgentMemoryObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AgentScope Memory 对象仓储
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Repository
public class AgentMemoryObjectRepository extends ServiceImpl<AgentMemoryObjectMapper, AgentMemoryObjectEntity> {

    /**
     * 根据主键查询对象
     *
     * @param id 主键
     * @return 对象实体
     * @author TellyJiang
     * @since 2026-04-12
     */
    public Optional<AgentMemoryObjectEntity> getById(Long id) {
        return Optional.ofNullable(this.getBaseMapper().selectById(id));
    }

    /**
     * 保存或更新对象
     *
     * @param entity 对象实体
     * @author TellyJiang
     * @since 2026-04-12
     */
    public void saveOrUpdateEntity(AgentMemoryObjectEntity entity) {
        AgentMemoryObjectEntity existing = this.lambdaQuery()
                .eq(AgentMemoryObjectEntity::getObjectKey, entity.getObjectKey())
                .one();
        if (existing == null) {
            this.save(entity);
            return;
        }
        entity.setId(existing.getId());
        this.updateById(entity);
    }
}
