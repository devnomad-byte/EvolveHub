package org.evolve.aiplatform.memory.application.service;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;

import java.util.List;

/**
 * 结构化记忆服务接口
 * 
 * 负责结构化记忆的写入、更新和检索，是事实、偏好等结构化记忆能力的统一扩展点。
 * 该接口聚焦业务语义，不暴露底层数据库访问细节。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
public interface MemoryStructuredService {

    /**
     * 保存或更新结构化记忆
     *
     * @param item 结构化记忆
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-12
     */
    MemoryStructuredItemDTO upsertMemoryStructuredItem(MemoryStructuredItemDTO item);

    /**
     * 查询结构化记忆
     *
     * @param userId 用户 ID
     * @param memoryType 记忆类型
     * @return 结构化记忆列表
     * @author TellyJiang
     * @since 2026-04-12
     */
    List<MemoryStructuredItemDTO> listMemoryStructuredItems(Long userId, String memoryType);
}
