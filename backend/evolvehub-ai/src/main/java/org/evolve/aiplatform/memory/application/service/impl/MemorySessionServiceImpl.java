package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.application.service.MemoryExtractionService;
import org.evolve.aiplatform.memory.application.service.MemorySessionService;
import org.evolve.aiplatform.memory.application.service.MemoryVectorService;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryActiveSummaryDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryConversationContextDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryRoundDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionWorkspaceDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryObjectEntity;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryRecordEntity;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemorySessionMetaEntity;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryObjectRepository;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryRecordRepository;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemorySessionMetaRepository;
import org.evolve.aiplatform.utils.S3Util;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话记忆服务实现
 *
 * 负责维护 JVM 工作区、Redis 备份、长期摘要激活态以及会话归档信息。
 * 新实现以“完整问答回合”为单位管理短期上下文，达到 10 回合后触发摘要沉淀。
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
@Service
class MemorySessionServiceImpl implements MemorySessionService {

    @Resource
    private MemorySessionWorkspaceStore memorySessionWorkspaceStore;

    @Resource
    private MemorySessionBackupRepository memorySessionBackupRepository;

    @Resource
    private AgentMemorySessionMetaRepository agentMemorySessionMetaRepository;

    @Resource
    private AgentMemoryRecordRepository agentMemoryRecordRepository;

    @Resource
    private AgentMemoryObjectRepository agentMemoryObjectRepository;

    @Resource(name = "memoryExtractionServiceImpl")
    private MemoryExtractionService memoryExtractionService;

    @Resource(name = "memoryVectorServiceImpl")
    private MemoryVectorService memoryVectorService;

    @Resource(name = "aiS3Util")
    private S3Util s3Util;

    @Override
    public MemorySessionDTO loadMemorySession(Long userId, String sessionId) {
        MemorySessionWorkspaceDTO workspace = loadWorkspace(userId, sessionId);
        return toSessionDto(workspace);
    }

    @Override
    public MemoryConversationContextDTO buildConversationContext(Long userId, String sessionId, String query,
                                                                List<MemoryRecallResultVO> recalledMemories) {
        MemorySessionWorkspaceDTO workspace = loadWorkspace(userId, sessionId);
        int currentRoundNo = defaultInteger(workspace.getCurrentRoundNo());

        List<MemoryActiveSummaryDTO> activeSummaries = retainActiveSummaries(workspace.getActiveSummaries(), currentRoundNo);
        List<MemoryActiveSummaryDTO> activatedSummaries = mergeActivatedSummaries(
                activeSummaries,
                recalledMemories,
                currentRoundNo
        );
        workspace.setActiveSummaries(activatedSummaries);
        workspace.setUpdatedAt(LocalDateTime.now());
        persistWorkspace(workspace);

        return new MemoryConversationContextDTO(
                null,
                activatedSummaries,
                safeRounds(workspace.getRounds()),
                recalledMemories == null ? List.of() : recalledMemories
        );
    }

    @Override
    public MemorySessionDTO appendMemorySession(Long userId, String sessionId, String message, String modelName) {
        MemorySessionWorkspaceDTO workspace = loadWorkspace(userId, sessionId);
        int nextRoundNo = defaultInteger(workspace.getCurrentRoundNo()) + 1;
        LocalDateTime now = LocalDateTime.now();
        List<MemoryRoundDTO> rounds = new ArrayList<>(safeRounds(workspace.getRounds()));
        rounds.add(new MemoryRoundDTO(nextRoundNo, normalizeText(message), "", modelName, now));
        if (rounds.size() > MemoryConstants.MEMORY_DEFAULT_MAX_TURNS) {
            rounds = new ArrayList<>(rounds.subList(rounds.size() - MemoryConstants.MEMORY_DEFAULT_MAX_TURNS, rounds.size()));
        }
        workspace.setModelName(modelName);
        workspace.setCurrentRoundNo(nextRoundNo);
        workspace.setRounds(rounds);
        workspace.setUpdatedAt(now);
        persistWorkspace(workspace);

        AgentMemorySessionMetaEntity sessionMeta = loadOrCreateMeta(userId, sessionId);
        sessionMeta.setModelName(modelName);
        sessionMeta.setMessageCount(rounds.size());
        sessionMeta.setCurrentRoundNo(nextRoundNo);
        sessionMeta.setLastMessageTime(now);
        saveSessionMeta(sessionMeta, workspace);
        return toSessionDto(workspace);
    }

    @Override
    public MemorySessionDTO commitConversationRound(Long userId, String sessionId, String userMessage,
                                                    String assistantMessage, String modelName) {
        MemorySessionWorkspaceDTO workspace = loadWorkspace(userId, sessionId);
        LocalDateTime now = LocalDateTime.now();

        List<MemoryRoundDTO> rounds = new ArrayList<>(safeRounds(workspace.getRounds()));
        int nextRoundNo = mergeOrAppendRound(rounds, userMessage, assistantMessage, modelName, now);
        if (rounds.size() > MemoryConstants.MEMORY_DEFAULT_MAX_TURNS) {
            rounds = new ArrayList<>(rounds.subList(rounds.size() - MemoryConstants.MEMORY_DEFAULT_MAX_TURNS, rounds.size()));
        }

        workspace.setModelName(modelName);
        workspace.setCurrentRoundNo(nextRoundNo);
        workspace.setRounds(rounds);
        workspace.setActiveSummaries(retainActiveSummaries(workspace.getActiveSummaries(), nextRoundNo));
        workspace.setUpdatedAt(now);

        AgentMemorySessionMetaEntity sessionMeta = loadOrCreateMeta(userId, sessionId);
        sessionMeta.setModelName(modelName);
        sessionMeta.setMessageCount(rounds.size() * 2);
        sessionMeta.setCurrentRoundNo(nextRoundNo);
        sessionMeta.setLastMessageTime(now);

        if (shouldCompact(sessionMeta, nextRoundNo)) {
            compactConversationWindow(workspace, sessionMeta, nextRoundNo);
        }

        persistWorkspace(workspace);
        saveSessionMeta(sessionMeta, workspace);
        return toSessionDto(workspace);
    }

    /**
     * 将完整问答回合合并到工作区；若上一条是仅包含用户消息的占位回合，则直接补齐助手回复。
     *
     * @param rounds 当前工作区回合列表
     * @param userMessage 用户消息
     * @param assistantMessage 助手回复
     * @param modelName 模型名称
     * @param occurredAt 发生时间
     * @return 当前回合号
     * @author TellyJiang
     * @since 2026-04-16
     */
    private int mergeOrAppendRound(List<MemoryRoundDTO> rounds, String userMessage, String assistantMessage,
                                   String modelName, LocalDateTime occurredAt) {
        String normalizedUserMessage = normalizeText(userMessage);
        String normalizedAssistantMessage = normalizeText(assistantMessage);
        if (!rounds.isEmpty()) {
            MemoryRoundDTO lastRound = rounds.get(rounds.size() - 1);
            if (lastRound != null
                    && Objects.equals(normalizeText(lastRound.getUserMessage()), normalizedUserMessage)
                    && normalizeText(lastRound.getAssistantMessage()).isBlank()) {
                lastRound.setAssistantMessage(normalizedAssistantMessage);
                lastRound.setModelName(modelName);
                lastRound.setOccurredAt(occurredAt);
                return defaultInteger(lastRound.getRoundNo());
            }
        }
        int nextRoundNo = rounds.stream()
                .map(MemoryRoundDTO::getRoundNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        rounds.add(new MemoryRoundDTO(
                nextRoundNo,
                normalizedUserMessage,
                normalizedAssistantMessage,
                modelName,
                occurredAt
        ));
        return nextRoundNo;
    }

    private MemorySessionWorkspaceDTO loadWorkspace(Long userId, String sessionId) {
        Optional<MemorySessionWorkspaceDTO> cachedWorkspace = memorySessionWorkspaceStore.get(userId, sessionId);
        if (cachedWorkspace.isPresent()) {
            return cachedWorkspace.get();
        }

        Optional<MemorySessionWorkspaceDTO> backedUpWorkspace = memorySessionBackupRepository.load(userId, sessionId);
        if (backedUpWorkspace.isPresent()) {
            MemorySessionWorkspaceDTO restored = backedUpWorkspace.get();
            memorySessionWorkspaceStore.save(restored);
            return restored;
        }

        AgentMemorySessionMetaEntity sessionMeta = loadOrCreateMeta(userId, sessionId);
        MemorySessionWorkspaceDTO workspace = new MemorySessionWorkspaceDTO(
                userId,
                sessionId,
                sessionMeta.getModelName(),
                defaultInteger(sessionMeta.getCurrentRoundNo()),
                defaultInteger(sessionMeta.getLastCompactedRoundNo()),
                loadActiveSummaries(userId, sessionId, defaultInteger(sessionMeta.getCurrentRoundNo())),
                new ArrayList<>(),
                resolveCreatedAt(sessionMeta),
                resolveUpdatedAt(sessionMeta)
        );
        memorySessionWorkspaceStore.save(workspace);
        memorySessionBackupRepository.save(workspace);
        return workspace;
    }

    private void compactConversationWindow(MemorySessionWorkspaceDTO workspace,
                                           AgentMemorySessionMetaEntity sessionMeta,
                                           Integer currentRoundNo) {
        int startRoundNo = defaultInteger(sessionMeta.getLastCompactedRoundNo()) + 1;
        int endRoundNo = startRoundNo + MemoryConstants.MEMORY_DEFAULT_MAX_TURNS - 1;
        List<MemoryRoundDTO> compactRounds = workspace.getRounds().stream()
                .filter(round -> round.getRoundNo() != null
                        && round.getRoundNo() >= startRoundNo
                        && round.getRoundNo() <= endRoundNo)
                .sorted(Comparator.comparing(MemoryRoundDTO::getRoundNo))
                .toList();
        if (compactRounds.size() < MemoryConstants.MEMORY_DEFAULT_MAX_TURNS) {
            return;
        }

        String transcriptContent = buildTranscript(compactRounds);
        String summaryContent = summarizeConversation(workspace.getUserId(), workspace.getSessionId(), transcriptContent);

        Long transcriptObjectId = archiveObject(
                workspace.getUserId(),
                workspace.getSessionId(),
                MemoryConstants.MEMORY_OBJECT_TYPE_TRANSCRIPT,
                buildTranscriptObjectKey(workspace.getUserId(), workspace.getSessionId(), startRoundNo, endRoundNo),
                transcriptContent,
                null
        );
        Long snapshotObjectId = archiveObject(
                workspace.getUserId(),
                workspace.getSessionId(),
                MemoryConstants.MEMORY_OBJECT_TYPE_SESSION_SNAPSHOT,
                buildSnapshotObjectKey(workspace.getUserId(), workspace.getSessionId(), endRoundNo),
                buildSnapshotContent(workspace),
                null
        );

        memoryVectorService.saveConversationSummary(
                workspace.getUserId(),
                parseSessionId(workspace.getSessionId()),
                summaryContent,
                BigDecimal.valueOf(0.800D),
                startRoundNo,
                endRoundNo
        );
        AgentMemoryRecordEntity recordEntity = agentMemoryRecordRepository.lambdaQuery()
                .eq(AgentMemoryRecordEntity::getUserId, workspace.getUserId())
                .eq(AgentMemoryRecordEntity::getSessionId, workspace.getSessionId())
                .eq(AgentMemoryRecordEntity::getMemoryKey, buildSummaryKey(workspace.getSessionId(), startRoundNo, endRoundNo))
                .one();
        if (recordEntity != null) {
            recordEntity.setObjectId(snapshotObjectId);
            recordEntity.setArchiveObjectId(transcriptObjectId);
            recordEntity.setLastActivatedRoundNo(currentRoundNo);
            recordEntity.setSleepAfterRoundNo(currentRoundNo + MemoryConstants.MEMORY_ACTIVE_SUMMARY_SLEEP_ROUNDS);
            agentMemoryRecordRepository.updateById(recordEntity);
        }

        workspace.setLastCompactedRoundNo(endRoundNo);
        workspace.setActiveSummaries(mergeActivatedSummaries(
                safeSummaries(workspace.getActiveSummaries()),
                List.of(new MemoryRecallResultVO(
                        recordEntity == null ? buildSummaryKey(workspace.getSessionId(), startRoundNo, endRoundNo) : recordEntity.getMemoryKey(),
                        recordEntity == null ? MemoryConstants.MEMORY_TYPE_FACT : recordEntity.getMemoryType(),
                        summaryContent,
                        BigDecimal.ONE,
                        BigDecimal.valueOf(0.800D),
                        MemoryConstants.MEMORY_KIND_SUMMARY,
                        endRoundNo
                )),
                currentRoundNo
        ));

        sessionMeta.setLastCompactedRoundNo(endRoundNo);
        sessionMeta.setTranscriptObjectId(transcriptObjectId);
        sessionMeta.setLastSnapshotObjectId(snapshotObjectId);
        sessionMeta.setLastSummaryObjectId(snapshotObjectId);
    }

    private String summarizeConversation(Long userId, String sessionId, String transcriptContent) {
        MemorySessionWorkspaceDTO workspace = loadWorkspace(userId, sessionId);
        if (workspace.getModelName() == null || workspace.getModelName().isBlank()) {
            return transcriptContent.length() <= 600 ? transcriptContent : transcriptContent.substring(0, 600);
        }
        MemoryExtractionResultDTO extracted = memoryExtractionService.extractMemoryFromConversation(
                new MemoryExtractionRequestDTO(userId, parseSessionId(sessionId), workspace.getModelName(), transcriptContent)
        );
        if (extracted.getExtractedItems() == null || extracted.getExtractedItems().isEmpty()) {
            return transcriptContent.length() <= 600 ? transcriptContent : transcriptContent.substring(0, 600);
        }
        return extracted.getExtractedItems().stream()
                .map(item -> item.getContent())
                .filter(Objects::nonNull)
                .filter(content -> !content.isBlank())
                .distinct()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private List<MemoryActiveSummaryDTO> mergeActivatedSummaries(List<MemoryActiveSummaryDTO> existingSummaries,
                                                                 List<MemoryRecallResultVO> recalledMemories,
                                                                 Integer currentRoundNo) {
        List<MemoryActiveSummaryDTO> merged = new ArrayList<>(retainActiveSummaries(existingSummaries, currentRoundNo));
        if (recalledMemories == null || recalledMemories.isEmpty()) {
            return merged;
        }

        for (MemoryRecallResultVO recalledMemory : recalledMemories) {
            if (recalledMemory == null || !MemoryConstants.MEMORY_KIND_SUMMARY.equals(recalledMemory.getMemoryKind())) {
                continue;
            }
            String summaryKey = recalledMemory.getMemoryKey();
            MemoryActiveSummaryDTO existing = merged.stream()
                    .filter(item -> Objects.equals(item.getSummaryKey(), summaryKey))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                existing.setContent(recalledMemory.getContent());
                existing.setLastActivatedRoundNo(currentRoundNo);
                existing.setSleepAfterRoundNo(currentRoundNo + MemoryConstants.MEMORY_ACTIVE_SUMMARY_SLEEP_ROUNDS);
                continue;
            }
            merged.add(new MemoryActiveSummaryDTO(
                    summaryKey,
                    recalledMemory.getContent(),
                    currentRoundNo,
                    currentRoundNo + MemoryConstants.MEMORY_ACTIVE_SUMMARY_SLEEP_ROUNDS
            ));
        }
        return merged;
    }

    private List<MemoryActiveSummaryDTO> retainActiveSummaries(List<MemoryActiveSummaryDTO> summaries, Integer currentRoundNo) {
        return safeSummaries(summaries).stream()
                .filter(item -> item.getSleepAfterRoundNo() == null || item.getSleepAfterRoundNo() >= currentRoundNo)
                .sorted(Comparator.comparing(MemoryActiveSummaryDTO::getLastActivatedRoundNo,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<MemoryActiveSummaryDTO> loadActiveSummaries(Long userId, String sessionId, Integer currentRoundNo) {
        return agentMemoryRecordRepository.listActiveSummaryBySession(userId, sessionId, currentRoundNo).stream()
                .map(entity -> new MemoryActiveSummaryDTO(
                        entity.getMemoryKey(),
                        entity.getExcerpt(),
                        entity.getLastActivatedRoundNo(),
                        entity.getSleepAfterRoundNo()
                ))
                .toList();
    }

    private void persistWorkspace(MemorySessionWorkspaceDTO workspace) {
        memorySessionWorkspaceStore.save(workspace);
        memorySessionBackupRepository.save(workspace);
    }

    private void saveSessionMeta(AgentMemorySessionMetaEntity sessionMeta, MemorySessionWorkspaceDTO workspace) {
        sessionMeta.setSessionKey(workspace.getUserId() + ":" + workspace.getSessionId());
        sessionMeta.setStatus(MemoryConstants.MEMORY_SESSION_STATUS_ACTIVE);
        sessionMeta.setExpireTime(LocalDateTime.now().plus(MemoryConstants.MEMORY_SESSION_TTL));
        agentMemorySessionMetaRepository.saveOrUpdateEntity(sessionMeta);
    }

    private AgentMemorySessionMetaEntity loadOrCreateMeta(Long userId, String sessionId) {
        return agentMemorySessionMetaRepository.getByUserIdAndSessionId(userId, sessionId)
                .orElseGet(() -> {
                    AgentMemorySessionMetaEntity entity = new AgentMemorySessionMetaEntity();
                    entity.setUserId(userId);
                    entity.setSessionId(sessionId);
                    entity.setSessionKey(userId + ":" + sessionId);
                    entity.setStatus(MemoryConstants.MEMORY_SESSION_STATUS_ACTIVE);
                    entity.setCurrentRoundNo(0);
                    entity.setLastCompactedRoundNo(0);
                    entity.setMessageCount(0);
                    entity.setLastMessageTime(LocalDateTime.now());
                    return entity;
                });
    }

    private MemorySessionDTO toSessionDto(MemorySessionWorkspaceDTO workspace) {
        return new MemorySessionDTO(
                workspace.getUserId(),
                workspace.getSessionId(),
                workspace.getModelName(),
                retainActiveSummaries(workspace.getActiveSummaries(), defaultInteger(workspace.getCurrentRoundNo())),
                safeRounds(workspace.getRounds()),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }

    private boolean shouldCompact(AgentMemorySessionMetaEntity sessionMeta, Integer currentRoundNo) {
        return currentRoundNo - defaultInteger(sessionMeta.getLastCompactedRoundNo()) >= MemoryConstants.MEMORY_DEFAULT_MAX_TURNS;
    }

    private Long archiveObject(Long userId, String sessionId, String objectType, String objectKey,
                               String content, Long sourceRecordId) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        s3Util.upload(objectKey, bytes, "application/json");

        AgentMemoryObjectEntity objectEntity = new AgentMemoryObjectEntity();
        objectEntity.setUserId(userId);
        objectEntity.setSessionId(sessionId);
        objectEntity.setObjectType(objectType);
        objectEntity.setBucket(MemoryConstants.MEMORY_DEFAULT_BUCKET);
        objectEntity.setObjectKey(objectKey);
        objectEntity.setContentType("application/json");
        objectEntity.setChecksum(Integer.toHexString(content.hashCode()));
        objectEntity.setSizeBytes((long) bytes.length);
        objectEntity.setVersionNo(1);
        objectEntity.setSourceRecordId(sourceRecordId);
        agentMemoryObjectRepository.saveOrUpdateEntity(objectEntity);
        return objectEntity.getId();
    }

    private String buildTranscript(List<MemoryRoundDTO> rounds) {
        return rounds.stream()
                .map(round -> "第" + round.getRoundNo() + "回合\n用户：" + normalizeText(round.getUserMessage())
                        + "\n助手：" + normalizeText(round.getAssistantMessage()))
                .collect(Collectors.joining("\n\n"));
    }

    private String buildSnapshotContent(MemorySessionWorkspaceDTO workspace) {
        String rounds = safeRounds(workspace.getRounds()).stream()
                .map(round -> round.getRoundNo() + ":" + normalizeText(round.getUserMessage()) + " -> " + normalizeText(round.getAssistantMessage()))
                .collect(Collectors.joining("\n"));
        String summaries = safeSummaries(workspace.getActiveSummaries()).stream()
                .map(summary -> summary.getSummaryKey() + ":" + normalizeText(summary.getContent()))
                .collect(Collectors.joining("\n"));
        return "{\n"
                + "\"userId\":" + workspace.getUserId() + ",\n"
                + "\"sessionId\":\"" + workspace.getSessionId() + "\",\n"
                + "\"currentRoundNo\":" + defaultInteger(workspace.getCurrentRoundNo()) + ",\n"
                + "\"lastCompactedRoundNo\":" + defaultInteger(workspace.getLastCompactedRoundNo()) + ",\n"
                + "\"rounds\":\"" + escapeJson(rounds) + "\",\n"
                + "\"activeSummaries\":\"" + escapeJson(summaries) + "\"\n"
                + "}";
    }

    private String buildTranscriptObjectKey(Long userId, String sessionId, Integer startRoundNo, Integer endRoundNo) {
        return "memory/archive/" + userId + "/" + sessionId + "/transcript-" + startRoundNo + "-" + endRoundNo + ".json";
    }

    private String buildSnapshotObjectKey(Long userId, String sessionId, Integer roundNo) {
        return "memory/archive/" + userId + "/" + sessionId + "/snapshot-" + roundNo + "-" + UUID.randomUUID() + ".json";
    }

    private String buildSummaryKey(String sessionId, Integer startRoundNo, Integer endRoundNo) {
        return sessionId + "-summary-" + startRoundNo + "-" + endRoundNo;
    }

    private LocalDateTime resolveCreatedAt(AgentMemorySessionMetaEntity sessionMeta) {
        return sessionMeta.getCreateTime() == null ? LocalDateTime.now() : sessionMeta.getCreateTime();
    }

    private LocalDateTime resolveUpdatedAt(AgentMemorySessionMetaEntity sessionMeta) {
        return sessionMeta.getUpdateTime() == null ? LocalDateTime.now() : sessionMeta.getUpdateTime();
    }

    private List<MemoryRoundDTO> safeRounds(List<MemoryRoundDTO> rounds) {
        return rounds == null ? List.of() : rounds;
    }

    private List<MemoryActiveSummaryDTO> safeSummaries(List<MemoryActiveSummaryDTO> summaries) {
        return summaries == null ? List.of() : summaries;
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value;
    }

    private Long parseSessionId(String sessionId) {
        try {
            return Long.valueOf(sessionId);
        } catch (Exception exception) {
            return null;
        }
    }

    private String escapeJson(String content) {
        return normalizeText(content)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
