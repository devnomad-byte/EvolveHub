package org.evolve.aiplatform.memory.application.service;

/**
 * Memory 向量化接口
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
public interface MemoryEmbeddingService {

    /**
     * 对文本进行向量化
     *
     * @param content 文本内容
     * @return 向量结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    float[] embed(String content);

    /**
     * 获取向量维度
     *
     * @return 向量维度
     * @author TellyJiang
     * @since 2026-04-15
     */
    int dimensions();
}
