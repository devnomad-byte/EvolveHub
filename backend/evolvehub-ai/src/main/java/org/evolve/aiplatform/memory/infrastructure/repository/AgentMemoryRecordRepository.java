package org.evolve.aiplatform.memory.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryRecordEntity;
import org.evolve.aiplatform.memory.infrastructure.mapper.AgentMemoryRecordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AgentScope 长期记忆仓储
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Repository
public class AgentMemoryRecordRepository extends ServiceImpl<AgentMemoryRecordMapper, AgentMemoryRecordEntity> {

    /**
     * 保存或更新记忆记录
     *
     * @param entity 记忆实体
     * @author TellyJiang
     * @since 2026-04-12
     */
    public void saveOrUpdateEntity(AgentMemoryRecordEntity entity) {
        AgentMemoryRecordEntity existing = this.lambdaQuery()
                .eq(AgentMemoryRecordEntity::getUserId, entity.getUserId())
                .eq(AgentMemoryRecordEntity::getSourceKind, entity.getSourceKind())
                .eq(AgentMemoryRecordEntity::getMemoryKey, entity.getMemoryKey())
                .one();
        if (existing == null) {
            this.save(entity);
            return;
        }
        entity.setId(existing.getId());
        this.updateById(entity);
    }

    /**
     * 按用户查询结构化记忆
     *
     * @param userId 用户 ID
     * @param memoryType 记忆类型
     * @return 结构化记忆列表
     * @author TellyJiang
     * @since 2026-04-12
     */
    public List<AgentMemoryRecordEntity> listByUserIdAndType(Long userId, String memoryType) {
        return listStructuredByUserIdAndType(userId, memoryType);
    }

    /**
     * 按用户查询结构化记忆
     *
     * @param userId 用户 ID
     * @param memoryType 记忆类型
     * @return 结构化记忆列表
     * @author TellyJiang
     * @since 2026-04-15
     */
    public List<AgentMemoryRecordEntity> listStructuredByUserIdAndType(Long userId, String memoryType) {
        return this.lambdaQuery()
                .eq(AgentMemoryRecordEntity::getUserId, userId)
                .eq(AgentMemoryRecordEntity::getSourceKind, "structured")
                .eq(memoryType != null, AgentMemoryRecordEntity::getMemoryType, memoryType)
                .orderByDesc(AgentMemoryRecordEntity::getUpdateTime)
                .list();
    }

    /**
     * 查询可管理记忆
     *
     * @param userId 用户 ID
     * @return 可管理记忆
     * @author TellyJiang
     * @since 2026-04-15
     */
    public List<AgentMemoryRecordEntity> listManagedByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(AgentMemoryRecordEntity::getUserId, userId)
                .in(AgentMemoryRecordEntity::getSourceKind, "structured", "vector")
                .orderByDesc(AgentMemoryRecordEntity::getUpdateTime)
                .list();
    }

    /**
     * 按主键查询可管理记忆
     *
     * @param userId 用户 ID
     * @param memoryId 记忆主键
     * @return 记忆实体
     * @author TellyJiang
     * @since 2026-04-15
     */
    public AgentMemoryRecordEntity getManagedById(Long userId, Long memoryId) {
        return this.lambdaQuery()
                .eq(AgentMemoryRecordEntity::getId, memoryId)
                .eq(AgentMemoryRecordEntity::getUserId, userId)
                .in(AgentMemoryRecordEntity::getSourceKind, "structured", "vector")
                .one();
    }

    /**
     * 根据向量文档 ID 查询记忆
     *
     * @param userId 用户 ID
     * @param vectorDocId 向量文档 ID
     * @return 记忆实体
     * @author TellyJiang
     * @since 2026-04-15
     */
    public AgentMemoryRecordEntity getByVectorDocId(Long userId, String vectorDocId) {
        return this.lambdaQuery()
                .eq(AgentMemoryRecordEntity::getUserId, userId)
                .eq(AgentMemoryRecordEntity::getVectorDocId, vectorDocId)
                .one();
    }

    /**
     * 查询当前会话仍处于活动态的摘要记录
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param currentRoundNo 当前回合号
     * @return 活动摘要
     * @author TellyJiang
     * @since 2026-04-16
     */
    public List<AgentMemoryRecordEntity> listActiveSummaryBySession(Long userId, String sessionId, Integer currentRoundNo) {
        return this.lambdaQuery()
                .eq(AgentMemoryRecordEntity::getUserId, userId)
                .eq(AgentMemoryRecordEntity::getSessionId, sessionId)
                .eq(AgentMemoryRecordEntity::getMemoryKind, "summary")
                .ge(currentRoundNo != null, AgentMemoryRecordEntity::getSleepAfterRoundNo, currentRoundNo)
                .orderByDesc(AgentMemoryRecordEntity::getLastActivatedRoundNo)
                .list();
    }

    /**
     * 判断指定用户下的记忆键是否存在
     *
     * @param userId 用户 ID
     * @param memoryKey 记忆键
     * @return 是否存在
     * @author TellyJiang
     * @since 2026-04-14
     */
    public boolean existsByUserIdAndMemoryKey(Long userId, String memoryKey) {
        return this.lambdaQuery()
                .eq(AgentMemoryRecordEntity::getUserId, userId)
                .eq(AgentMemoryRecordEntity::getMemoryKey, memoryKey)
                .count() > 0;
    }
}
