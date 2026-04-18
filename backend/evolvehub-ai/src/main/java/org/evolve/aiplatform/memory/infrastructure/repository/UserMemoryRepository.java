package org.evolve.aiplatform.memory.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.evolve.aiplatform.memory.domain.bean.entity.UserMemoryEntity;
import org.evolve.aiplatform.memory.infrastructure.mapper.UserMemoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户结构化记忆仓储
 *
 * @author TellyJiang
 * @since 2026-04-18
 */
@Repository
public class UserMemoryRepository extends ServiceImpl<UserMemoryMapper, UserMemoryEntity> {

    /**
     * 按用户和记忆键保存或更新
     *
     * @param entity 用户记忆
     * @author TellyJiang
     * @since 2026-04-18
     */
    public void saveOrUpdateByUserAndKey(UserMemoryEntity entity) {
        UserMemoryEntity existing = this.getOne(
                Wrappers.<UserMemoryEntity>lambdaQuery()
                        .eq(UserMemoryEntity::getUserId, entity.getUserId())
                        .eq(UserMemoryEntity::getMemoryKey, entity.getMemoryKey()),
                false
        );
        if (existing == null) {
            this.save(entity);
            return;
        }
        entity.setId(existing.getId());
        this.updateById(entity);
    }

    /**
     * 按用户和类型查询结构化记忆
     *
     * @param userId 用户 ID
     * @param memoryType 记忆类型
     * @return 结构化记忆
     * @author TellyJiang
     * @since 2026-04-18
     */
    public List<UserMemoryEntity> listByUserIdAndType(Long userId, String memoryType) {
        return this.lambdaQuery()
                .eq(UserMemoryEntity::getUserId, userId)
                .eq(memoryType != null, UserMemoryEntity::getMemoryType, memoryType)
                .orderByDesc(UserMemoryEntity::getUpdateTime)
                .list();
    }

    /**
     * 按向量文档 ID 查询
     *
     * @param userId 用户 ID
     * @param vectorDocId 向量文档 ID
     * @return 用户记忆
     * @author TellyJiang
     * @since 2026-04-18
     */
    public UserMemoryEntity getByVectorDocId(Long userId, String vectorDocId) {
        return this.lambdaQuery()
                .eq(UserMemoryEntity::getUserId, userId)
                .eq(UserMemoryEntity::getVectorDocId, vectorDocId)
                .one();
    }
}
