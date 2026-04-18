package org.evolve.aiplatform.memory.application.service.impl;

import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryExtractionAlgorithm;
import org.evolve.aiplatform.memory.application.service.MemoryVectorService;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.application.service.MemoryStructuredService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 记忆提取服务测试
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
class MemoryExtractionServiceTest {

    @Test
    void shouldDelegateExtractionToAlgorithm() {
        MemoryExtractionServiceImpl service = new MemoryExtractionServiceImpl();
        MemoryStructuredService structuredService = Mockito.mock(MemoryStructuredService.class);
        MemoryVectorService vectorService = Mockito.mock(MemoryVectorService.class);
        MemoryExtractionAlgorithm algorithm = Mockito.mock(MemoryExtractionAlgorithm.class);
        ReflectionTestUtils.setField(service, "memoryStructuredService", structuredService);
        ReflectionTestUtils.setField(service, "memoryVectorService", vectorService);
        ReflectionTestUtils.setField(service, "memoryExtractionAlgorithm", algorithm);
        MemoryExtractionRequestDTO request = new MemoryExtractionRequestDTO(1L, 2L, "session-model", "hello");
        MemoryStructuredItemDTO item = new MemoryStructuredItemDTO(1L, "k1", "fact", "hello", BigDecimal.valueOf(0.5D));
        MemoryStructuredItemDTO persistedItem = new MemoryStructuredItemDTO(1L, "persisted-k1", "fact", "hello", BigDecimal.valueOf(0.5D));
        Mockito.when(algorithm.extract(request))
                .thenReturn(new MemoryExtractionResultDTO(List.of(item), "algo-summary"));
        Mockito.when(vectorService.saveConversationMemory(1L, 2L, "hello", BigDecimal.valueOf(0.5D)))
                .thenReturn(persistedItem);
        Mockito.when(structuredService.upsertMemoryStructuredItem(item)).thenReturn(item);

        MemoryExtractionResultDTO result = service.extractMemoryFromConversation(request);

        Mockito.verify(algorithm).extract(request);
        Mockito.verify(vectorService).saveConversationMemory(1L, 2L, "hello", BigDecimal.valueOf(0.5D));
        Mockito.verify(structuredService).upsertMemoryStructuredItem(item);
        Assertions.assertEquals("algo-summary", result.getSummary());
        Assertions.assertEquals(1, result.getExtractedItems().size());
        Assertions.assertEquals("persisted-k1", result.getExtractedItems().get(0).getMemoryKey());
    }
}
