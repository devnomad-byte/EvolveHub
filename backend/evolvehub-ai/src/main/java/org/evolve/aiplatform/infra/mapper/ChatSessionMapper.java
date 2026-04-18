package org.evolve.aiplatform.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ChatSessionEntity;

/**
 * 对话会话 Mapper
 *
 * @author TellyJiang
 * @since 2026-04-17
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {
}
