package org.evolve.aiplatform.memory.infrastructure.support;

import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;

import java.math.BigDecimal;

/**
 * 记忆重要性处理工具
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
public class MemoryImportanceUtil {

    /**
     * 归一化重要性分值
     *
     * @param importance 原始分值
     * @return 归一化结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    public BigDecimal normalize(BigDecimal importance) {
        if (importance == null) {
            return BigDecimal.valueOf(0.5D);
        }
        if (importance.compareTo(MemoryConstants.MEMORY_IMPORTANCE_MIN) < 0) {
            return MemoryConstants.MEMORY_IMPORTANCE_MIN;
        }
        if (importance.compareTo(MemoryConstants.MEMORY_IMPORTANCE_MAX) > 0) {
            return MemoryConstants.MEMORY_IMPORTANCE_MAX;
        }
        return importance;
    }
}
