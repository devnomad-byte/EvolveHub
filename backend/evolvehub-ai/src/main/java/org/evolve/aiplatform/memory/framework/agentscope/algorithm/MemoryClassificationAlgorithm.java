package org.evolve.aiplatform.memory.framework.agentscope.algorithm;

import io.agentscope.core.message.Msg;

/**
 * 记忆分类算法接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface MemoryClassificationAlgorithm {

    /**
     * 解析消息对应的记忆类型
     *
     * @param message 消息对象
     * @return 记忆类型
     * @author TellyJiang
     * @since 2026-04-14
     */
    String classify(Msg message);
}
