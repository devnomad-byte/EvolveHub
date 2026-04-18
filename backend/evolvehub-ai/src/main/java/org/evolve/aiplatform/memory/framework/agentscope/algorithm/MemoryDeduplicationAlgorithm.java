package org.evolve.aiplatform.memory.framework.agentscope.algorithm;

import io.agentscope.core.message.Msg;

/**
 * 记忆去重算法接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface MemoryDeduplicationAlgorithm {

    /**
     * 生成稳定记忆键
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param message 消息对象
     * @return 稳定记忆键
     * @author TellyJiang
     * @since 2026-04-14
     */
    String buildMemoryKey(Long userId, String sessionId, Msg message);
}
