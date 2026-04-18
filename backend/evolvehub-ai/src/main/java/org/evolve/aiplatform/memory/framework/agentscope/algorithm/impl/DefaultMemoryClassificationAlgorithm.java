package org.evolve.aiplatform.memory.framework.agentscope.algorithm.impl;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryClassificationAlgorithm;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.springframework.stereotype.Component;

/**
 * 默认记忆分类算法
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultMemoryClassificationAlgorithm implements MemoryClassificationAlgorithm {

    @Override
    public String classify(Msg message) {
        if (message == null || message.getRole() == null || message.getRole() == MsgRole.USER) {
            return MemoryConstants.MEMORY_TYPE_FACT;
        }
        return MemoryConstants.MEMORY_TYPE_SKILL;
    }
}
