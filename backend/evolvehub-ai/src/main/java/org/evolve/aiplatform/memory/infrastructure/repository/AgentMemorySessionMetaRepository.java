package org.evolve.aiplatform.memory.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemorySessionMetaEntity;
import org.evolve.aiplatform.memory.infrastructure.mapper.AgentMemorySessionMetaMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AgentScope 会话元数据仓储
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Repository
public class AgentMemorySessionMetaRepository extends ServiceImpl<AgentMemorySessionMetaMapper, AgentMemorySessionMetaEntity> {

    /**
     * 根据用户和会话查询元数据
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return 元数据实体
     * @author TellyJiang
     * @since 2026-04-12
     */
    public Optional<AgentMemorySessionMetaEntity> getByUserIdAndSessionId(Long userId, String sessionId) {
        return Optional.ofNullable(this.lambdaQuery()
                .eq(AgentMemorySessionMetaEntity::getUserId, userId)
                .eq(AgentMemorySessionMetaEntity::getSessionId, sessionId)
                .one());
    }

    /**
     * 保存或更新元数据
     *
     * @param entity 元数据实体
     * @author TellyJiang
     * @since 2026-04-12
     */
    public void saveOrUpdateEntity(AgentMemorySessionMetaEntity entity) {
        if (entity.getId() == null) {
            this.save(entity);
            return;
        }
        this.updateById(entity);
    }
}
