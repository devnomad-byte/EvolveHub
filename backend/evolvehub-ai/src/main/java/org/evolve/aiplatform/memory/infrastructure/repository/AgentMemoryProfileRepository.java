package org.evolve.aiplatform.memory.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryProfileEntity;
import org.evolve.aiplatform.memory.infrastructure.mapper.AgentMemoryProfileMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AgentScope 用户画像仓储
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Repository
public class AgentMemoryProfileRepository extends ServiceImpl<AgentMemoryProfileMapper, AgentMemoryProfileEntity> {

    /**
     * 根据用户 ID 查询画像
     *
     * @param userId 用户 ID
     * @return 画像对象
     * @author TellyJiang
     * @since 2026-04-12
     */
    public Optional<AgentMemoryProfileEntity> getByUserId(Long userId) {
        return Optional.ofNullable(this.lambdaQuery()
                .eq(AgentMemoryProfileEntity::getUserId, userId)
                .one());
    }

    /**
     * 保存或更新画像
     *
     * @param entity 画像实体
     * @author TellyJiang
     * @since 2026-04-12
     */
    public void saveOrUpdateEntity(AgentMemoryProfileEntity entity) {
        if (entity.getId() == null) {
            this.save(entity);
            return;
        }
        this.updateById(entity);
    }
}
