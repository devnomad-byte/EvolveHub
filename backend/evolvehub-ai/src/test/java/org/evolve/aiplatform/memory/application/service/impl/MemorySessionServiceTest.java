package org.evolve.aiplatform.memory.application.service.impl;

import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemorySessionMetaEntity;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryRecordRepository;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemorySessionMetaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Memory 会话服务测试
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
class MemorySessionServiceTest {

    @Test
    void shouldKeepSlidingWindowWhenAppendSessionMessages() {
        MemorySessionServiceImpl service = new MemorySessionServiceImpl();
        InMemorySessionWorkspaceStore workspaceStore = new InMemorySessionWorkspaceStore();
        MemorySessionBackupRepository backupRepository = Mockito.mock(MemorySessionBackupRepository.class);
        AgentMemorySessionMetaRepository sessionMetaRepository = Mockito.mock(AgentMemorySessionMetaRepository.class);
        AgentMemoryRecordRepository recordRepository = Mockito.mock(AgentMemoryRecordRepository.class);
        Mockito.when(backupRepository.load(1L, "session-a")).thenReturn(Optional.empty());
        Mockito.when(sessionMetaRepository.getByUserIdAndSessionId(1L, "session-a")).thenReturn(Optional.empty());
        Mockito.when(recordRepository.listActiveSummaryBySession(1L, "session-a", 0)).thenReturn(java.util.List.of());

        ReflectionTestUtils.setField(service, "memorySessionWorkspaceStore", workspaceStore);
        ReflectionTestUtils.setField(service, "memorySessionBackupRepository", backupRepository);
        ReflectionTestUtils.setField(service, "agentMemorySessionMetaRepository", sessionMetaRepository);
        ReflectionTestUtils.setField(service, "agentMemoryRecordRepository", recordRepository);

        MemorySessionDTO latest = null;
        for (int i = 1; i <= 12; i++) {
            latest = service.appendMemorySession(1L, "session-a", "msg-" + i, "gpt-4o");
        }

        Assertions.assertNotNull(latest);
        Assertions.assertEquals(10, latest.getRounds().size());
        Assertions.assertEquals("msg-3", latest.getRounds().get(0).getUserMessage());
        Assertions.assertEquals("msg-12", latest.getRounds().get(9).getUserMessage());
        Assertions.assertEquals("gpt-4o", latest.getModelName());
        Mockito.verify(backupRepository, Mockito.atLeastOnce()).save(Mockito.any());
    }

    @Test
    void shouldUseSessionMetaWhenLoadingSession() {
        MemorySessionServiceImpl service = new MemorySessionServiceImpl();
        InMemorySessionWorkspaceStore workspaceStore = new InMemorySessionWorkspaceStore();
        MemorySessionBackupRepository backupRepository = Mockito.mock(MemorySessionBackupRepository.class);
        AgentMemorySessionMetaRepository sessionMetaRepository = Mockito.mock(AgentMemorySessionMetaRepository.class);
        AgentMemoryRecordRepository recordRepository = Mockito.mock(AgentMemoryRecordRepository.class);
        Mockito.when(backupRepository.load(1L, "session-a")).thenReturn(Optional.empty());
        AgentMemorySessionMetaEntity sessionMeta = new AgentMemorySessionMetaEntity();
        sessionMeta.setModelName("gpt-4.1");
        sessionMeta.setCreateTime(LocalDateTime.of(2026, 4, 13, 9, 0));
        sessionMeta.setUpdateTime(LocalDateTime.of(2026, 4, 13, 9, 5));
        sessionMeta.setCurrentRoundNo(0);
        sessionMeta.setLastCompactedRoundNo(0);
        Mockito.when(sessionMetaRepository.getByUserIdAndSessionId(1L, "session-a"))
                .thenReturn(Optional.of(sessionMeta));
        Mockito.when(recordRepository.listActiveSummaryBySession(1L, "session-a", 0)).thenReturn(java.util.List.of());

        ReflectionTestUtils.setField(service, "memorySessionWorkspaceStore", workspaceStore);
        ReflectionTestUtils.setField(service, "memorySessionBackupRepository", backupRepository);
        ReflectionTestUtils.setField(service, "agentMemorySessionMetaRepository", sessionMetaRepository);
        ReflectionTestUtils.setField(service, "agentMemoryRecordRepository", recordRepository);

        MemorySessionDTO session = service.loadMemorySession(1L, "session-a");

        Assertions.assertEquals("gpt-4.1", session.getModelName());
        Assertions.assertEquals(LocalDateTime.of(2026, 4, 13, 9, 0), session.getCreatedAt());
        Assertions.assertEquals(LocalDateTime.of(2026, 4, 13, 9, 5), session.getUpdatedAt());
        Assertions.assertTrue(session.getRounds().isEmpty());
    }

    @Test
    void shouldMergePendingUserRoundWhenCommitConversationRound() {
        MemorySessionServiceImpl service = new MemorySessionServiceImpl();
        InMemorySessionWorkspaceStore workspaceStore = new InMemorySessionWorkspaceStore();
        MemorySessionBackupRepository backupRepository = Mockito.mock(MemorySessionBackupRepository.class);
        AgentMemorySessionMetaRepository sessionMetaRepository = Mockito.mock(AgentMemorySessionMetaRepository.class);
        AgentMemoryRecordRepository recordRepository = Mockito.mock(AgentMemoryRecordRepository.class);
        Mockito.when(backupRepository.load(1L, "session-a")).thenReturn(Optional.empty());
        Mockito.when(sessionMetaRepository.getByUserIdAndSessionId(1L, "session-a")).thenReturn(Optional.empty());
        Mockito.when(recordRepository.listActiveSummaryBySession(1L, "session-a", 0)).thenReturn(java.util.List.of());
        Mockito.when(recordRepository.listActiveSummaryBySession(1L, "session-a", 1)).thenReturn(java.util.List.of());

        ReflectionTestUtils.setField(service, "memorySessionWorkspaceStore", workspaceStore);
        ReflectionTestUtils.setField(service, "memorySessionBackupRepository", backupRepository);
        ReflectionTestUtils.setField(service, "agentMemorySessionMetaRepository", sessionMetaRepository);
        ReflectionTestUtils.setField(service, "agentMemoryRecordRepository", recordRepository);

        service.appendMemorySession(1L, "session-a", "今天服务器怎么样", "gpt-4o");
        MemorySessionDTO session = service.commitConversationRound(
                1L,
                "session-a",
                "今天服务器怎么样",
                "一切正常",
                "gpt-4o"
        );

        Assertions.assertEquals(1, session.getRounds().size());
        Assertions.assertEquals(1, session.getRounds().get(0).getRoundNo());
        Assertions.assertEquals("今天服务器怎么样", session.getRounds().get(0).getUserMessage());
        Assertions.assertEquals("一切正常", session.getRounds().get(0).getAssistantMessage());
    }
}
