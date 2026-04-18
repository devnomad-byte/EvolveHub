package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.application.service.MemoryVectorService;
import org.evolve.aiplatform.memory.application.service.MemoryVectorStore;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryVectorHitDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.UserMemoryEntity;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryManagedItemVO;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryImportanceAlgorithm;
import org.evolve.aiplatform.memory.infrastructure.repository.UserMemoryRepository;
import org.evolve.common.web.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 向量记忆服务实现
 * 
 * 负责向量记忆的保存与召回，统一编排向量化、重要度归一化和向量存储访问。
 * 该类是长期语义记忆能力的默认实现。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Service
class MemoryVectorServiceImpl implements MemoryVectorService {

    @Resource
    private UserMemoryRepository userMemoryRepository;

    @Resource
    private MemoryImportanceAlgorithm memoryImportanceAlgorithm;

    @Resource
    private MemoryVectorStore memoryVectorStore;

    @Resource
    private MemoryOperatorContext memoryOperatorContext;

    /**
     * 保存向量记忆
     *
     * @param item 结构化记忆
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public MemoryStructuredItemDTO saveMemoryVector(MemoryStructuredItemDTO item) {
        BigDecimal importance = memoryImportanceAlgorithm.scoreStructuredItem(item);
        String vectorDocId = memoryVectorStore.save(
                item.getUserId(),
                memoryOperatorContext.getCurrentDeptId(),
                null,
                item.getMemoryKey(),
                mapVectorMemoryType(item.getMemoryType()),
                item.getContent(),
                importance,
                mapVectorMemoryType(item.getMemoryType()),
                null,
                null
        );
        UserMemoryEntity entity = new UserMemoryEntity();
        entity.setUserId(item.getUserId());
        entity.setDeptId(memoryOperatorContext.getCurrentDeptId());
        entity.setMemoryKey(item.getMemoryKey());
        entity.setMemoryType(item.getMemoryType());
        entity.setVectorDocId(vectorDocId);
        entity.setContent(item.getContent());
        entity.setImportance(importance);
        userMemoryRepository.saveOrUpdateByUserAndKey(entity);
        return new MemoryStructuredItemDTO(
                entity.getUserId(),
                entity.getMemoryKey(),
                entity.getMemoryType(),
                entity.getContent(),
                entity.getImportance()
        );
    }

    /**
     * 保存对话长期记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param content 内容
     * @param importance 重要性
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public MemoryStructuredItemDTO saveConversationMemory(Long userId, Long sessionId, String content, BigDecimal importance) {
        if (memoryVectorStore.existsSimilar(userId, content, 0.95D)) {
            return new MemoryStructuredItemDTO(
                    userId,
                    buildMemoryKey(sessionId, content),
                    MemoryConstants.MEMORY_TYPE_FACT,
                    content,
                    normalizeImportance(importance)
            );
        }
        String memoryKey = buildMemoryKey(sessionId, content);
        String vectorDocId = memoryVectorStore.save(
                userId,
                memoryOperatorContext.getCurrentDeptId(),
                sessionId,
                memoryKey,
                MemoryConstants.MEMORY_TYPE_FACT,
                content,
                normalizeImportance(importance),
                MemoryConstants.MEMORY_KIND_FACT,
                null,
                null
        );
        UserMemoryEntity entity = new UserMemoryEntity();
        entity.setUserId(userId);
        entity.setDeptId(memoryOperatorContext.getCurrentDeptId());
        entity.setSessionId(sessionId == null ? null : String.valueOf(sessionId));
        entity.setMemoryKey(memoryKey);
        entity.setMemoryType(MemoryConstants.MEMORY_TYPE_FACT);
        entity.setVectorDocId(vectorDocId);
        entity.setContent(content);
        entity.setImportance(normalizeImportance(importance));
        userMemoryRepository.saveOrUpdateByUserAndKey(entity);
        return new MemoryStructuredItemDTO(
                entity.getUserId(),
                entity.getMemoryKey(),
                entity.getMemoryType(),
                entity.getContent(),
                entity.getImportance()
        );
    }

    @Override
    public MemoryStructuredItemDTO saveConversationSummary(Long userId, Long sessionId, String content,
                                                           BigDecimal importance, Integer roundStartNo, Integer roundEndNo) {
        String vectorDocId = memoryVectorStore.save(
                userId,
                memoryOperatorContext.getCurrentDeptId(),
                sessionId,
                buildSummaryKey(sessionId, roundStartNo, roundEndNo),
                MemoryConstants.MEMORY_TYPE_FACT,
                content,
                normalizeImportance(importance),
                MemoryConstants.MEMORY_KIND_SUMMARY,
                roundStartNo,
                roundEndNo
        );
        UserMemoryEntity entity = new UserMemoryEntity();
        entity.setUserId(userId);
        entity.setDeptId(memoryOperatorContext.getCurrentDeptId());
        entity.setSessionId(sessionId == null ? null : String.valueOf(sessionId));
        entity.setMemoryKey(buildSummaryKey(sessionId, roundStartNo, roundEndNo));
        entity.setMemoryType(MemoryConstants.MEMORY_TYPE_FACT);
        entity.setVectorDocId(vectorDocId);
        entity.setContent(content);
        entity.setImportance(normalizeImportance(importance));
        userMemoryRepository.saveOrUpdateByUserAndKey(entity);
        return new MemoryStructuredItemDTO(
                entity.getUserId(),
                entity.getMemoryKey(),
                entity.getMemoryType(),
                entity.getContent(),
                entity.getImportance()
        );
    }

    /**
     * 召回向量记忆
     *
     * @param userId 用户 ID
     * @param query 查询语句
     * @param topK 召回数量
     * @return 召回结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public List<MemoryRecallResultVO> recallMemoryVectors(Long userId, String query, Integer topK) {
        List<MemoryVectorHitDTO> vectorResults = memoryVectorStore.search(userId, memoryOperatorContext.getCurrentDeptId(), query, topK);
        if (vectorResults == null || vectorResults.isEmpty()) {
            return fallbackToStructuredMemories(userId, topK);
        }
        return vectorResults.stream()
                .map(result -> mergeWithManagedMetadata(userId, result))
                .toList();
    }

    /**
     * 查询可管理记忆
     *
     * @param userId 用户 ID
     * @return 可管理记忆列表
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public List<MemoryManagedItemVO> listManagedMemories(Long userId) {
        return userMemoryRepository.listByUserIdAndType(userId, null).stream()
                .map(entity -> new MemoryManagedItemVO(
                        entity.getId(),
                        entity.getVectorDocId(),
                        entity.getMemoryKey(),
                        entity.getMemoryType(),
                        MemoryConstants.MEMORY_SOURCE_KIND_VECTOR,
                        entity.getContent(),
                        entity.getImportance(),
                        entity.getUpdateTime()
                ))
                .toList();
    }

    /**
     * 删除可管理记忆
     *
     * @param userId 用户 ID
     * @param memoryId 记忆 ID
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public void deleteManagedMemory(Long userId, Long memoryId) {
        UserMemoryEntity entity = userMemoryRepository.getById(memoryId);
        if (entity == null) {
            throw new BusinessException("记忆不存在或无权操作");
        }
        if (!userId.equals(entity.getUserId())) {
            throw new BusinessException("记忆不存在或无权操作");
        }
        if (entity.getVectorDocId() != null && !entity.getVectorDocId().isBlank()) {
            memoryVectorStore.delete(entity.getVectorDocId());
        }
        userMemoryRepository.removeById(entity.getId());
    }

    private MemoryRecallResultVO mergeWithManagedMetadata(Long userId, MemoryVectorHitDTO vectorResult) {
        UserMemoryEntity metadata = userMemoryRepository.getByVectorDocId(userId, vectorResult.getVectorDocId());
        if (metadata == null) {
            return new MemoryRecallResultVO(
                    vectorResult.getVectorDocId(),
                    null,
                    vectorResult.getContent(),
                    vectorResult.getScore(),
                    vectorResult.getImportance(),
                    vectorResult.getMemoryKind(),
                    vectorResult.getRoundEndNo()
            );
        }
        return new MemoryRecallResultVO(
                metadata.getMemoryKey(),
                metadata.getMemoryType(),
                vectorResult.getContent(),
                vectorResult.getScore(),
                metadata.getImportance() == null ? vectorResult.getImportance() : metadata.getImportance(),
                mapVectorMemoryType(metadata.getMemoryType()),
                metadata.getRoundEndNo() == null ? vectorResult.getRoundEndNo() : metadata.getRoundEndNo()
        );
    }

    private BigDecimal normalizeImportance(BigDecimal importance) {
        BigDecimal value = importance == null ? BigDecimal.valueOf(0.8D) : importance;
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private String buildMemoryKey(Long sessionId, String content) {
        String sessionPart = sessionId == null ? "global" : String.valueOf(sessionId);
        return sessionPart + "-" + UUID.nameUUIDFromBytes(content.getBytes()).toString();
    }

    private String buildSummaryKey(Long sessionId, Integer roundStartNo, Integer roundEndNo) {
        String sessionPart = sessionId == null ? "global" : String.valueOf(sessionId);
        return sessionPart + "-summary-" + roundStartNo + "-" + roundEndNo;
    }

    private String mapVectorMemoryType(String memoryType) {
        if (MemoryConstants.MEMORY_TYPE_TOOL_CONFIG.equals(memoryType)) {
            return MemoryConstants.MEMORY_TYPE_SKILL;
        }
        if (memoryType == null || memoryType.isBlank()) {
            return MemoryConstants.MEMORY_TYPE_FACT;
        }
        return memoryType;
    }

    private List<MemoryRecallResultVO> fallbackToStructuredMemories(Long userId, Integer topK) {
        int limit = (topK == null || topK <= 0) ? 5 : topK;
        return userMemoryRepository.listByUserIdAndType(userId, null).stream()
                .sorted(Comparator.comparing(UserMemoryEntity::getImportance,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(UserMemoryEntity::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(entity -> new MemoryRecallResultVO(
                        entity.getMemoryKey(),
                        entity.getMemoryType(),
                        entity.getContent(),
                        BigDecimal.ZERO,
                        entity.getImportance(),
                        mapVectorMemoryType(entity.getMemoryType()),
                        entity.getRoundEndNo()
                ))
                .toList();
    }
}
