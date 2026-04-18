package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryRecordEntity;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.infrastructure.repository.AgentMemoryRecordRepository;
import org.evolve.aiplatform.memory.application.service.MemoryStructuredService;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryImportanceUtil;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 结构化记忆服务实现
 * 
 * 负责结构化记忆的新增、更新与查询，统一处理记忆类型校验和重要度归一化。
 * 该类是结构化记忆领域的默认实现，底层通过 Repository 访问持久化数据。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Service
class MemoryStructuredServiceImpl implements MemoryStructuredService {

    @Resource
    private AgentMemoryRecordRepository agentMemoryRecordRepository;

    @Resource
    private MemoryImportanceUtil memoryImportanceUtil;

    /**
     * 保存或更新结构化记忆
     *
     * @param item 结构化记忆
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public MemoryStructuredItemDTO upsertMemoryStructuredItem(MemoryStructuredItemDTO item) {
        validateMemoryType(item.getMemoryType());
        AgentMemoryRecordEntity entity = new AgentMemoryRecordEntity();
        entity.setUserId(item.getUserId());
        entity.setSessionId(null);
        entity.setMemoryKey(item.getMemoryKey());
        entity.setMemoryType(item.getMemoryType());
        entity.setSourceKind(MemoryConstants.MEMORY_SOURCE_KIND_STRUCTURED);
        entity.setRole("SYSTEM");
        entity.setEmbeddingModelId(MemoryConstants.MEMORY_DEFAULT_EMBEDDING_MODEL_ID);
        entity.setExcerpt(item.getContent());
        entity.setImportance(memoryImportanceUtil.normalize(item.getImportance()));
        agentMemoryRecordRepository.saveOrUpdateEntity(entity);
        return new MemoryStructuredItemDTO(
                entity.getUserId(),
                entity.getMemoryKey(),
                entity.getMemoryType(),
                entity.getExcerpt(),
                entity.getImportance()
        );
    }

    /**
     * 查询结构化记忆
     *
     * @param userId 用户 ID
     * @param memoryType 记忆类型
     * @return 结构化记忆列表
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public List<MemoryStructuredItemDTO> listMemoryStructuredItems(Long userId, String memoryType) {
        if (memoryType != null) {
            validateMemoryType(memoryType);
        }
        return agentMemoryRecordRepository.listStructuredByUserIdAndType(userId, memoryType).stream()
                .map(entity -> new MemoryStructuredItemDTO(
                        entity.getUserId(),
                        entity.getMemoryKey(),
                        entity.getMemoryType(),
                        entity.getExcerpt(),
                        entity.getImportance()))
                .toList();
    }

    private void validateMemoryType(String memoryType) {
        if (!MemoryConstants.MEMORY_SUPPORTED_TYPES.contains(memoryType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的记忆类型: " + memoryType);
        }
    }
}
