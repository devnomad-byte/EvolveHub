package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.config.EmbeddingService;
import org.evolve.aiplatform.memory.application.service.MemoryEmbeddingService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Memory 向量化服务实现
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
@Primary
@Service
public class MemoryEmbeddingServiceImpl implements MemoryEmbeddingService {

    @Resource
    private EmbeddingService embeddingService;

    /**
     * 对文本进行向量化
     *
     * @param content 文本内容
     * @return 向量结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public float[] embed(String content) {
        double[] values = embeddingService.embedSync(content == null ? "" : content);
        float[] result = new float[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (float) values[index];
        }
        return result;
    }

    /**
     * 获取向量维度
     *
     * @return 向量维度
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public int dimensions() {
        return embeddingService.getDimensions();
    }
}
