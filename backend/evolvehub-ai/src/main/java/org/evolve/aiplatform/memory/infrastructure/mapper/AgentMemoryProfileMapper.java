package org.evolve.aiplatform.memory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryProfileEntity;

/**
 * AgentScope 用户画像 Mapper
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Mapper
public interface AgentMemoryProfileMapper extends BaseMapper<AgentMemoryProfileEntity> {
}
