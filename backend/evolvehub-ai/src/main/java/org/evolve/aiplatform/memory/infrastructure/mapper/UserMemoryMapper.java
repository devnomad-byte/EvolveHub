package org.evolve.aiplatform.memory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.memory.domain.bean.entity.UserMemoryEntity;

/**
 * 用户结构化记忆 Mapper
 *
 * @author TellyJiang
 * @since 2026-04-18
 */
@Mapper
public interface UserMemoryMapper extends BaseMapper<UserMemoryEntity> {
}
