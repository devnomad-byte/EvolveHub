package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.application.service.MemoryExtractionService;
import org.evolve.aiplatform.memory.application.service.MemoryStructuredService;
import org.evolve.aiplatform.memory.application.service.MemoryVectorService;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryExtractionAlgorithm;
import org.evolve.common.base.CurrentUserHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.UUID;

/**
 * 记忆提取服务实现
 * 
 * 负责从对话内容中提取候选记忆并转换为结构化记忆项。
 * 当前实现采用轻量规则策略，后续可无缝替换为模型驱动或混合提取实现。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Service
class MemoryExtractionServiceImpl implements MemoryExtractionService {

    /**
     * 异步提取线程池
     */
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Resource(name = "memoryStructuredServiceImpl")
    private MemoryStructuredService memoryStructuredService;

    @Resource(name = "memoryVectorServiceImpl")
    private MemoryVectorService memoryVectorService;

    @Resource
    private MemoryExtractionAlgorithm memoryExtractionAlgorithm;

    /**
     * 从对话中提取记忆
     *
     * @param request 提取请求
     * @return 提取结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public MemoryExtractionResultDTO extractMemoryFromConversation(MemoryExtractionRequestDTO request) {
        MemoryExtractionResultDTO extracted = memoryExtractionAlgorithm.extract(request);
        List<MemoryStructuredItemDTO> persistedItems = extracted.getExtractedItems().stream()
                .map(item -> persistExtractedItem(request, item))
                .toList();
        return new MemoryExtractionResultDTO(persistedItems, extracted.getSummary());
    }

    /**
     * 异步从对话中提取记忆
     *
     * @param request 提取请求
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public void extractMemoryFromConversationAsync(MemoryExtractionRequestDTO request) {
        Long inheritedUserId = CurrentUserHolder.getUserId();
        executorService.execute(() -> {
            Long previousUserId = CurrentUserHolder.getUserId();
            try {
                if (request.getUserId() != null) {
                    CurrentUserHolder.set(request.getUserId());
                } else if (inheritedUserId != null) {
                    CurrentUserHolder.set(inheritedUserId);
                }
                extractMemoryFromConversation(request);
            } finally {
                if (previousUserId == null) {
                    CurrentUserHolder.clear();
                } else {
                    CurrentUserHolder.set(previousUserId);
                }
            }
        });
    }

    private MemoryStructuredItemDTO persistExtractedItem(MemoryExtractionRequestDTO request, MemoryStructuredItemDTO item) {
        MemoryStructuredItemDTO normalizedItem = new MemoryStructuredItemDTO(
                request.getUserId(),
                item.getMemoryKey() == null || item.getMemoryKey().isBlank()
                        ? buildMemoryKey(request.getSessionId(), item.getContent())
                        : item.getMemoryKey(),
                item.getMemoryType() == null || item.getMemoryType().isBlank()
                        ? MemoryConstants.MEMORY_TYPE_FACT
                        : item.getMemoryType(),
                item.getContent(),
                item.getImportance()
        );
        try {
            MemoryStructuredItemDTO vectorItem = memoryVectorService.saveConversationMemory(
                    request.getUserId(),
                    request.getSessionId(),
                    normalizedItem.getContent(),
                    normalizedItem.getImportance()
            );
            memoryStructuredService.upsertMemoryStructuredItem(normalizedItem);
            return vectorItem;
        } catch (Exception exception) {
            return memoryStructuredService.upsertMemoryStructuredItem(normalizedItem);
        }
    }

    private String buildMemoryKey(Long sessionId, String content) {
        String sessionKey = sessionId == null ? "global" : String.valueOf(sessionId);
        return sessionKey + "-extract-" + UUID.nameUUIDFromBytes(String.valueOf(content).getBytes());
    }
}
