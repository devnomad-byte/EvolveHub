package org.evolve.aiplatform.memory.application.service;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;

/**
 * 记忆提取服务接口
 * 
 * 负责从对话内容中提取候选记忆，并将其转换为结构化结果。
 * 该接口用于隔离提取策略，便于后续替换为更复杂的规则或模型实现。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
public interface MemoryExtractionService {

    /**
     * 从对话中提取记忆
     *
     * @param request 提取请求
     * @return 提取结果
     * @author TellyJiang
     * @since 2026-04-12
     */
    MemoryExtractionResultDTO extractMemoryFromConversation(MemoryExtractionRequestDTO request);

    /**
     * 异步从对话中提取记忆
     *
     * @param request 提取请求
     * @author TellyJiang
     * @since 2026-04-15
     */
    void extractMemoryFromConversationAsync(MemoryExtractionRequestDTO request);
}
