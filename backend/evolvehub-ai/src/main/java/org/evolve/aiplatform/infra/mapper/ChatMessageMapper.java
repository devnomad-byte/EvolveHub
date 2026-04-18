package org.evolve.aiplatform.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ChatMessageEntity;

/**
 * 对话消息 Mapper
 *
 * @author TellyJiang
 * @since 2026-04-17
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {
}
