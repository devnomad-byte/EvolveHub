package org.evolve.aiplatform.memory.framework.agentscope.algorithm;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;

/**
 * 记忆提取算法接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface MemoryExtractionAlgorithm {

    /**
     * 从对话中提取记忆
     *
     * @param request 提取请求
     * @return 提取结果
     * @author TellyJiang
     * @since 2026-04-14
     */
    MemoryExtractionResultDTO extract(MemoryExtractionRequestDTO request);
}
