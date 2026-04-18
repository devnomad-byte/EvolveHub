package org.evolve.aiplatform.memory.framework.agentscope.algorithm.impl;

import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryRecallFormattingAlgorithm;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认记忆召回格式化算法
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultMemoryRecallFormattingAlgorithm implements MemoryRecallFormattingAlgorithm {

    @Override
    public String format(List<MemoryRecallResultVO> results) {
        if (results == null || results.isEmpty()) {
            return "";
        }
        return results.stream()
                .map(MemoryRecallResultVO::getContent)
                .filter(content -> content != null && !content.isBlank())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }
}
