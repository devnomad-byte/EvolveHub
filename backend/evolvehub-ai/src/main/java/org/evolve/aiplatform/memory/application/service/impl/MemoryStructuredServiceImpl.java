package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.entity.UserMemoryEntity;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.infrastructure.repository.UserMemoryRepository;
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
    private UserMemoryRepository userMemoryRepository;

    @Resource
    private MemoryOperatorContext memoryOperatorContext;

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
        UserMemoryEntity entity = new UserMemoryEntity();
        entity.setUserId(item.getUserId());
        entity.setDeptId(memoryOperatorContext.getCurrentDeptId());
        entity.setMemoryKey(item.getMemoryKey());
        entity.setMemoryType(item.getMemoryType());
        entity.setContent(item.getContent());
        entity.setImportance(memoryImportanceUtil.normalize(item.getImportance()));
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
        return userMemoryRepository.listByUserIdAndType(userId, memoryType).stream()
                .map(entity -> new MemoryStructuredItemDTO(
                        entity.getUserId(),
                        entity.getMemoryKey(),
                        entity.getMemoryType(),
                        entity.getContent(),
                        entity.getImportance()))
                .toList();
    }

    private void validateMemoryType(String memoryType) {
        if (!List.of(
                MemoryConstants.MEMORY_TYPE_PREFERENCE,
                MemoryConstants.MEMORY_TYPE_FACT,
                MemoryConstants.MEMORY_TYPE_TOOL_CONFIG
        ).contains(memoryType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的记忆类型: " + memoryType);
        }
    }
}
