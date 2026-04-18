package org.evolve.aiplatform.memory.framework.agentscope.algorithm;

import io.agentscope.core.message.Msg;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;

import java.math.BigDecimal;

/**
 * 记忆重要度算法接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface MemoryImportanceAlgorithm {

    /**
     * 计算消息重要度
     *
     * @param message 消息对象
     * @return 重要度
     * @author TellyJiang
     * @since 2026-04-14
     */
    BigDecimal scoreMessage(Msg message);

    /**
     * 计算结构化记忆重要度
     *
     * @param item 结构化记忆
     * @return 重要度
     * @author TellyJiang
     * @since 2026-04-14
     */
    BigDecimal scoreStructuredItem(MemoryStructuredItemDTO item);

    /**
     * 计算画像字段重要度
     *
     * @param profile 用户画像
     * @param memoryKey 记忆键
     * @param content 内容
     * @return 重要度
     * @author TellyJiang
     * @since 2026-04-14
     */
    BigDecimal scoreProfileField(MemoryProfileDTO profile, String memoryKey, String content);
}
