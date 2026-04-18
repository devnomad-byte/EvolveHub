package org.evolve.aiplatform.memory.framework.agentscope.algorithm;

import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;

import java.util.List;

/**
 * 记忆召回格式化算法接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface MemoryRecallFormattingAlgorithm {

    /**
     * 格式化召回结果
     *
     * @param results 召回结果
     * @return 格式化文本
     * @author TellyJiang
     * @since 2026-04-14
     */
    String format(List<MemoryRecallResultVO> results);
}
