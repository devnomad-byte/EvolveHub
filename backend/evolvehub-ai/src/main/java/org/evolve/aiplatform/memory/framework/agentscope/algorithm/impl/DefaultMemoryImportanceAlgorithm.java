package org.evolve.aiplatform.memory.framework.agentscope.algorithm.impl;

import io.agentscope.core.message.Msg;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryImportanceAlgorithm;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryImportanceUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 默认记忆重要度算法
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultMemoryImportanceAlgorithm implements MemoryImportanceAlgorithm {

    private final MemoryImportanceUtil memoryImportanceUtil;

    public DefaultMemoryImportanceAlgorithm(MemoryImportanceUtil memoryImportanceUtil) {
        this.memoryImportanceUtil = memoryImportanceUtil;
    }

    @Override
    public BigDecimal scoreMessage(Msg message) {
        return memoryImportanceUtil.normalize(BigDecimal.valueOf(0.6D));
    }

    @Override
    public BigDecimal scoreStructuredItem(MemoryStructuredItemDTO item) {
        return memoryImportanceUtil.normalize(item == null ? null : item.getImportance());
    }

    @Override
    public BigDecimal scoreProfileField(MemoryProfileDTO profile, String memoryKey, String content) {
        return memoryImportanceUtil.normalize(BigDecimal.valueOf(0.8D));
    }
}
