package org.evolve.aiplatform.memory;

import org.evolve.aiplatform.memory.infrastructure.support.MemoryImportanceUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Memory 重要性工具测试
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
class MemoryImportanceUtilTest {

    @Test
    void shouldClampImportanceIntoRange() {
        MemoryImportanceUtil util = new MemoryImportanceUtil();
        Assertions.assertEquals(BigDecimal.ZERO, util.normalize(BigDecimal.valueOf(-1)));
        Assertions.assertEquals(BigDecimal.ONE, util.normalize(BigDecimal.valueOf(2)));
    }

    @Test
    void shouldReturnDefaultValueWhenNull() {
        MemoryImportanceUtil util = new MemoryImportanceUtil();
        Assertions.assertEquals(BigDecimal.valueOf(0.5D), util.normalize(null));
    }
}
