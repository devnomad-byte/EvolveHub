package org.evolve.aiplatform.memory.application.service.impl;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryRecordEntity;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryRecordRepository;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryImportanceUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 结构化记忆服务测试
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
class MemoryStructuredServiceTest {

    @Test
    void shouldNotExposeEmbeddingAccessorsOnRecordEntity() {
        Method[] methods = AgentMemoryRecordEntity.class.getMethods();

        Assertions.assertTrue(Arrays.stream(methods)
                .noneMatch(method -> "getEmbedding".equals(method.getName())));
        Assertions.assertTrue(Arrays.stream(methods)
                .noneMatch(method -> "setEmbedding".equals(method.getName())));
    }

    @Test
    void shouldUseCustomRepositoryUpsertForStructuredMemory() {
        MemoryStructuredServiceImpl service = new MemoryStructuredServiceImpl();
        AgentMemoryRecordRepository repository = Mockito.mock(AgentMemoryRecordRepository.class);
        ReflectionTestUtils.setField(service, "agentMemoryRecordRepository", repository);
        ReflectionTestUtils.setField(service, "memoryImportanceUtil", new MemoryImportanceUtil());

        MemoryStructuredItemDTO result = service.upsertMemoryStructuredItem(
                new MemoryStructuredItemDTO(1L, "pref-language", "preference", "zh-CN", BigDecimal.valueOf(0.66D))
        );

        ArgumentCaptor<AgentMemoryRecordEntity> captor = ArgumentCaptor.forClass(AgentMemoryRecordEntity.class);
        Mockito.verify(repository).saveOrUpdateEntity(captor.capture());
        Assertions.assertEquals("pref-language", captor.getValue().getMemoryKey());
        Assertions.assertEquals("zh-CN", captor.getValue().getExcerpt());
        Assertions.assertEquals("structured", captor.getValue().getSourceKind());
        Assertions.assertEquals(BigDecimal.valueOf(0.660), result.getImportance());
    }
}
