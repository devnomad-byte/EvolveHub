package org.evolve.aiplatform.memory.framework.agentscope;

import io.agentscope.core.memory.LongTermMemory;
import io.agentscope.core.message.Msg;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * 基于 Milvus / PostgreSQL 元数据的长期记忆适配器
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
public class MilvusLongTermMemoryAdapter implements LongTermMemory {

    private final MemoryApi memoryApi;

    private final Long userId;

    private final String sessionId;

    /**
     * 构造长期记忆适配器
     *
     * @param memoryApi Memory 统一门面
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @author TellyJiang
     * @since 2026-04-16
     */
    public MilvusLongTermMemoryAdapter(MemoryApi memoryApi,
                                       Long userId,
                                       String sessionId) {
        this.memoryApi = memoryApi;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    @Override
    public Mono<Void> record(List<Msg> messages) {
        if (messages == null || messages.isEmpty()) {
            return Mono.empty();
        }
        for (Msg msg : messages) {
            String textContent = msg == null ? null : msg.getTextContent();
            if (textContent == null || textContent.isBlank()) {
                continue;
            }
            memoryApi.saveConversationMemory(
                    userId,
                    parseSessionId(),
                    textContent,
                    BigDecimal.valueOf(0.8D)
            );
        }
        return Mono.empty();
    }

    @Override
    public Mono<String> retrieve(Msg queryMessage) {
        List<MemoryStructuredItemDTO> recallResults = memoryApi.recallMemoryVectors(
                userId,
                queryMessage == null ? "" : queryMessage.getTextContent(),
                MemoryConstants.MEMORY_DEFAULT_TOP_K
        ).stream()
                .map(item -> new MemoryStructuredItemDTO(
                        userId,
                        item.getMemoryKey(),
                        item.getMemoryType(),
                        item.getContent(),
                        item.getImportance()
                ))
                .toList();
        String formatted = recallResults.stream()
                .map(MemoryStructuredItemDTO::getContent)
                .filter(content -> content != null && !content.isBlank())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("");
        return Mono.just(formatted);
    }

    private Long parseSessionId() {
        try {
            return Long.valueOf(sessionId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
