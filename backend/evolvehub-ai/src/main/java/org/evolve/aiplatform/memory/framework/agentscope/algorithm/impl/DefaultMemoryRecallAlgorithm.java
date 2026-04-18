package org.evolve.aiplatform.memory.framework.agentscope.algorithm.impl;

import io.agentscope.core.message.Msg;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryRecallAlgorithm;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryRecordEntity;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 默认记忆召回算法
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultMemoryRecallAlgorithm implements MemoryRecallAlgorithm {

    @Override
    public List<MemoryRecallResultVO> recall(Msg queryMessage, List<AgentMemoryRecordEntity> records) {
        if (records == null) {
            return List.of();
        }
        return records.stream()
                .map(record -> new MemoryRecallResultVO(
                        record.getMemoryKey(),
                        record.getMemoryType(),
                        record.getExcerpt(),
                        BigDecimal.ONE,
                        record.getImportance(),
                        record.getMemoryKind(),
                        record.getRoundEndNo()))
                .toList();
    }
}
