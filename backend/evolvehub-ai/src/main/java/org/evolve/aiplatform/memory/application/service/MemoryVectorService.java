package org.evolve.aiplatform.memory.application.service;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryManagedItemVO;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 向量记忆服务接口
 * 
 * 负责向量化记忆的保存与召回，是长期语义记忆能力的统一扩展点。
 * 向量生成、归一化和向量存储策略由具体实现类负责组合。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
public interface MemoryVectorService {

    /**
     * 保存向量记忆
     *
     * @param item 结构化记忆
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-12
     */
    MemoryStructuredItemDTO saveMemoryVector(MemoryStructuredItemDTO item);

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
    MemoryStructuredItemDTO saveConversationMemory(Long userId, Long sessionId, String content, BigDecimal importance);

    /**
     * 保存对话摘要类长期记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param content 摘要内容
     * @param importance 重要性
     * @param roundStartNo 起始回合
     * @param roundEndNo 结束回合
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-16
     */
    MemoryStructuredItemDTO saveConversationSummary(Long userId, Long sessionId, String content, BigDecimal importance,
                                                    Integer roundStartNo, Integer roundEndNo);

    /**
     * 召回向量记忆
     *
     * @param userId 用户 ID
     * @param query 查询语句
     * @param topK 召回数量
     * @return 召回结果
     * @author TellyJiang
     * @since 2026-04-12
     */
    List<MemoryRecallResultVO> recallMemoryVectors(Long userId, String query, Integer topK);

    /**
     * 查询可管理记忆
     *
     * @param userId 用户 ID
     * @return 可管理记忆列表
     * @author TellyJiang
     * @since 2026-04-15
     */
    List<MemoryManagedItemVO> listManagedMemories(Long userId);

    /**
     * 删除可管理记忆
     *
     * @param userId 用户 ID
     * @param memoryId 记忆 ID
     * @author TellyJiang
     * @since 2026-04-15
     */
    void deleteManagedMemory(Long userId, Long memoryId);
}
