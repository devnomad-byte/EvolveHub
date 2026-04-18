package org.evolve.aiplatform.memory.application.service.impl;

import org.evolve.aiplatform.memory.application.service.MemoryStructuredService;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryProfileEntity;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryProfileProjectionAlgorithm;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryObjectRepository;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryProfileRepository;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryMarkdownRenderer;
import org.junit.jupiter.api.Assertions;
import org.evolve.aiplatform.utils.S3Util;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户画像记忆服务测试
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
class MemoryProfileServiceTest {

    @Test
    void shouldDelegateProfileProjectionToAlgorithm() {
        MemoryProfileServiceImpl service = new MemoryProfileServiceImpl();
        MemoryStructuredService structuredService = Mockito.mock(MemoryStructuredService.class);
        MemoryProfileProjectionAlgorithm algorithm = Mockito.mock(MemoryProfileProjectionAlgorithm.class);
        AgentMemoryProfileRepository profileRepository = Mockito.mock(AgentMemoryProfileRepository.class);
        AgentMemoryObjectRepository objectRepository = Mockito.mock(AgentMemoryObjectRepository.class);
        S3Util s3Util = Mockito.mock(S3Util.class);
        ReflectionTestUtils.setField(service, "memoryStructuredService", structuredService);
        ReflectionTestUtils.setField(service, "memoryProfileProjectionAlgorithm", algorithm);
        ReflectionTestUtils.setField(service, "agentMemoryProfileRepository", profileRepository);
        ReflectionTestUtils.setField(service, "agentMemoryObjectRepository", objectRepository);
        ReflectionTestUtils.setField(service, "memoryMarkdownRenderer", new MemoryMarkdownRenderer());
        ReflectionTestUtils.setField(service, "s3Util", s3Util);
        MemoryProfileDTO profile = new MemoryProfileDTO(1L, "Tom", "AI", "zh", "gpt", "tools", "");
        MemoryStructuredItemDTO item = new MemoryStructuredItemDTO(1L, "preferred_model", "preference", "gpt", BigDecimal.valueOf(0.8D));
        Mockito.when(algorithm.projectStructuredItems(profile)).thenReturn(List.of(item));

        service.updateMemoryProfile(profile);

        Mockito.verify(algorithm).projectStructuredItems(profile);
        Mockito.verify(structuredService).upsertMemoryStructuredItem(item);
        Mockito.verify(profileRepository).saveOrUpdateEntity(Mockito.any());
        Mockito.verify(objectRepository).saveOrUpdateEntity(Mockito.any());
        Mockito.verify(s3Util).upload(Mockito.anyString(), Mockito.any(), Mockito.eq("text/markdown"));
    }

    @Test
    void shouldSaveRawMarkdownThroughMemoryProfileService() {
        MemoryProfileServiceImpl service = new MemoryProfileServiceImpl();
        MemoryStructuredService structuredService = Mockito.mock(MemoryStructuredService.class);
        MemoryProfileProjectionAlgorithm algorithm = Mockito.mock(MemoryProfileProjectionAlgorithm.class);
        AgentMemoryProfileRepository profileRepository = Mockito.mock(AgentMemoryProfileRepository.class);
        AgentMemoryObjectRepository objectRepository = Mockito.mock(AgentMemoryObjectRepository.class);
        S3Util s3Util = Mockito.mock(S3Util.class);
        ReflectionTestUtils.setField(service, "memoryStructuredService", structuredService);
        ReflectionTestUtils.setField(service, "memoryProfileProjectionAlgorithm", algorithm);
        ReflectionTestUtils.setField(service, "agentMemoryProfileRepository", profileRepository);
        ReflectionTestUtils.setField(service, "agentMemoryObjectRepository", objectRepository);
        ReflectionTestUtils.setField(service, "memoryMarkdownRenderer", new MemoryMarkdownRenderer());
        ReflectionTestUtils.setField(service, "s3Util", s3Util);
        Mockito.when(profileRepository.getByUserId(1L)).thenReturn(Optional.of(new AgentMemoryProfileEntity()));

        String result = service.saveMemoryProfileMarkdown(1L, "# 用户画像\n- 偏好：Milvus");

        Mockito.verify(profileRepository).saveOrUpdateEntity(Mockito.any());
        Mockito.verify(objectRepository).saveOrUpdateEntity(Mockito.any());
        Mockito.verify(s3Util).upload(Mockito.anyString(), Mockito.any(), Mockito.eq("text/markdown"));
        Mockito.verifyNoInteractions(structuredService);
        Assertions.assertEquals("# 用户画像\n- 偏好：Milvus", result);
    }
}
