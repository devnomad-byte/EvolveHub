package org.evolve.aiplatform.memory.application.service;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryVectorHitDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Memory 向量存储接口
 *
 * @author TellyJiang
 * @since 2026-04-15
 */
public interface MemoryVectorStore {

    /**
     * 查询相似向量记忆
     *
     * @param userId 用户 ID
     * @param query 查询文本
     * @param topK 数量
     * @return 召回结果
     * @author TellyJiang
     * @since 2026-04-15
     */
    List<MemoryVectorHitDTO> search(Long userId, Long deptId, String query, Integer topK);

    /**
     * 保存向量记忆
     *
     * @param userId 用户 ID
     * @param deptId 部门 ID
     * @param sessionId 会话 ID
     * @param content 内容
     * @param importance 重要性
     * @param memoryKind 记忆归类
     * @param roundStartNo 摘要起始回合
     * @param roundEndNo 摘要结束回合
     * @return 向量文档 ID
     * @author TellyJiang
     * @since 2026-04-15
     */
    String save(Long userId, Long deptId, Long sessionId, String memoryKey, String memoryType, String content,
                BigDecimal importance, String memoryKind, Integer roundStartNo, Integer roundEndNo);

    /**
     * 判断是否存在高相似记忆
     *
     * @param userId 用户 ID
     * @param content 内容
     * @param threshold 阈值
     * @return 是否存在
     * @author TellyJiang
     * @since 2026-04-15
     */
    boolean existsSimilar(Long userId, String content, double threshold);

    /**
     * 删除向量记忆
     *
     * @param vectorDocId 向量文档 ID
     * @author TellyJiang
     * @since 2026-04-15
     */
    void delete(String vectorDocId);
}
