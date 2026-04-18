package org.evolve.aiplatform.memory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemorySessionMetaEntity;

/**
 * AgentScope 会话元数据 Mapper
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Mapper
public interface AgentMemorySessionMetaMapper extends BaseMapper<AgentMemorySessionMetaEntity> {
}
