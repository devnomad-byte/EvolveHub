package org.evolve.aiplatform.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.bean.entity.ChatTokenUsageEntity;

/**
 * Token 消费 Mapper
 *
 * @author TellyJiang
 * @since 2026-04-17
 */
@Mapper
public interface ChatTokenUsageMapper extends BaseMapper<ChatTokenUsageEntity> {
}
