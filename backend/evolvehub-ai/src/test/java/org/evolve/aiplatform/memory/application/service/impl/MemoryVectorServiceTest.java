package org.evolve.aiplatform.memory.application.service.impl;

import org.evolve.aiplatform.memory.application.service.MemoryVectorStore;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryVectorHitDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryRecordEntity;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryImportanceAlgorithm;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryRecordRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 向量记忆服务测试
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
class MemoryVectorServiceTest {

    @Test
    void shouldPersistVectorMemoryMetadataViaMilvus() {
        MemoryVectorServiceImpl service = new MemoryVectorServiceImpl();
        AgentMemoryRecordRepository repository = Mockito.mock(AgentMemoryRecordRepository.class);
        MemoryVectorStore vectorStore = Mockito.mock(MemoryVectorStore.class);
        MemoryImportanceAlgorithm importanceAlgorithm = Mockito.mock(MemoryImportanceAlgorithm.class);
        MemoryOperatorContext memoryOperatorContext = Mockito.mock(MemoryOperatorContext.class);
        ReflectionTestUtils.setField(service, "agentMemoryRecordRepository", repository);
        ReflectionTestUtils.setField(service, "memoryVectorStore", vectorStore);
        ReflectionTestUtils.setField(service, "memoryImportanceAlgorithm", importanceAlgorithm);
        ReflectionTestUtils.setField(service, "memoryOperatorContext", memoryOperatorContext);
        Mockito.when(memoryOperatorContext.getCurrentDeptId()).thenReturn(10L);
        Mockito.when(vectorStore.save(1L, 10L, null, "hello vector", BigDecimal.valueOf(0.8D), "fact", null, null)).thenReturn("doc-1");
        Mockito.when(importanceAlgorithm.scoreStructuredItem(Mockito.any(MemoryStructuredItemDTO.class)))
                .thenReturn(BigDecimal.valueOf(0.8D));

        MemoryStructuredItemDTO result = service.saveMemoryVector(
                new MemoryStructuredItemDTO(1L, "k1", "fact", "hello vector", BigDecimal.valueOf(0.8D))
        );

        ArgumentCaptor<AgentMemoryRecordEntity> captor = ArgumentCaptor.forClass(AgentMemoryRecordEntity.class);
        Mockito.verify(repository).saveOrUpdateEntity(captor.capture());
        Assertions.assertEquals("vector", captor.getValue().getSourceKind());
        Assertions.assertEquals("doc-1", captor.getValue().getVectorDocId());
        Assertions.assertEquals(10L, captor.getValue().getDeptId());
        Assertions.assertEquals("hello vector", result.getContent());
        Assertions.assertEquals(BigDecimal.valueOf(0.800), result.getImportance());
    }

    @Test
    void shouldRecallVectorMemoryByMilvusAndMergeMetadata() {
        MemoryVectorServiceImpl service = new MemoryVectorServiceImpl();
        AgentMemoryRecordRepository repository = Mockito.mock(AgentMemoryRecordRepository.class);
        MemoryVectorStore vectorStore = Mockito.mock(MemoryVectorStore.class);
        ReflectionTestUtils.setField(service, "agentMemoryRecordRepository", repository);
        ReflectionTestUtils.setField(service, "memoryVectorStore", vectorStore);
        AgentMemoryRecordEntity entity = new AgentMemoryRecordEntity();
        entity.setVectorDocId("doc-1");
        entity.setMemoryKey("m1");
        entity.setMemoryType("fact");
        entity.setImportance(BigDecimal.valueOf(0.9D));
        entity.setMemoryKind("fact");
        Mockito.when(vectorStore.search(1L, "travel plan", 3)).thenReturn(List.of(
                new MemoryVectorHitDTO("doc-1", "travel plan", BigDecimal.ONE, BigDecimal.valueOf(0.8D), "fact", null)
        ));
        Mockito.when(repository.getByVectorDocId(1L, "doc-1")).thenReturn(entity);

        List<MemoryRecallResultVO> result = service.recallMemoryVectors(1L, "travel plan", 3);

        Mockito.verify(vectorStore).search(1L, "travel plan", 3);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("m1", result.get(0).getMemoryKey());
        Assertions.assertEquals("travel plan", result.get(0).getContent());
        Assertions.assertEquals(BigDecimal.ONE, result.get(0).getScore());
        Assertions.assertEquals(BigDecimal.valueOf(0.9D), result.get(0).getImportance());
        Assertions.assertEquals("fact", result.get(0).getMemoryKind());
    }

    @Test
    void shouldFallbackToVectorDocIdWhenMetadataMissing() {
        MemoryVectorServiceImpl service = new MemoryVectorServiceImpl();
        AgentMemoryRecordRepository repository = Mockito.mock(AgentMemoryRecordRepository.class);
        MemoryVectorStore vectorStore = Mockito.mock(MemoryVectorStore.class);
        ReflectionTestUtils.setField(service, "agentMemoryRecordRepository", repository);
        ReflectionTestUtils.setField(service, "memoryVectorStore", vectorStore);
        Mockito.when(vectorStore.search(1L, "travel plan", 3)).thenReturn(List.of(
                new MemoryVectorHitDTO("doc-9", "travel plan", BigDecimal.ONE, BigDecimal.valueOf(0.8D), "fact", null)
        ));

        List<MemoryRecallResultVO> result = service.recallMemoryVectors(1L, "travel plan", 3);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("doc-9", result.get(0).getMemoryKey());
        Assertions.assertNull(result.get(0).getMemoryType());
    }

    @Test
    void shouldPersistConversationMemoryToMilvusAndMetadata() {
        MemoryVectorServiceImpl service = new MemoryVectorServiceImpl();
        AgentMemoryRecordRepository repository = Mockito.mock(AgentMemoryRecordRepository.class);
        MemoryVectorStore vectorStore = Mockito.mock(MemoryVectorStore.class);
        MemoryOperatorContext memoryOperatorContext = Mockito.mock(MemoryOperatorContext.class);
        ReflectionTestUtils.setField(service, "agentMemoryRecordRepository", repository);
        ReflectionTestUtils.setField(service, "memoryVectorStore", vectorStore);
        ReflectionTestUtils.setField(service, "memoryOperatorContext", memoryOperatorContext);
        Mockito.when(memoryOperatorContext.getCurrentDeptId()).thenReturn(20L);

        Mockito.when(vectorStore.existsSimilar(1L, "remember this", 0.95D)).thenReturn(false);
        Mockito.when(vectorStore.save(Mockito.eq(1L), Mockito.eq(20L), Mockito.eq(2L), Mockito.eq("remember this"),
                        Mockito.any(BigDecimal.class), Mockito.eq("fact"), Mockito.isNull(), Mockito.isNull()))
                .thenReturn("doc-2");

        MemoryStructuredItemDTO result = service.saveConversationMemory(1L, 2L, "remember this", BigDecimal.valueOf(0.7D));

        ArgumentCaptor<AgentMemoryRecordEntity> captor = ArgumentCaptor.forClass(AgentMemoryRecordEntity.class);
        Mockito.verify(repository).saveOrUpdateEntity(captor.capture());
        Assertions.assertEquals(MemoryConstants.MEMORY_SOURCE_KIND_VECTOR, captor.getValue().getSourceKind());
        Assertions.assertEquals("doc-2", captor.getValue().getVectorDocId());
        Assertions.assertEquals("remember this", result.getContent());
        Assertions.assertEquals(new BigDecimal("0.700"), result.getImportance());
    }
}
