package org.evolve.aiplatform.memory.application.service;

import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryConversationContextDTO;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;

import java.util.List;

/**
 * 会话记忆服务接口
 * 
 * 负责会话记忆的加载与追加，是短期对话记忆能力的统一扩展点。
 * 具体的会话窗口裁剪和存储策略由实现类负责。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
public interface MemorySessionService {

    /**
     * 读取会话记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return 会话记忆
     * @author TellyJiang
     * @since 2026-04-12
     */
    MemorySessionDTO loadMemorySession(Long userId, String sessionId);

    /**
     * 构建会话上下文
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param query 当前查询
     * @return 会话上下文
     * @author TellyJiang
     * @since 2026-04-16
     */
    MemoryConversationContextDTO buildConversationContext(Long userId, String sessionId, String query,
                                                          List<MemoryRecallResultVO> recalledMemories);

    /**
     * 追加会话记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param message 消息内容
     * @param modelName 模型名称
     * @return 更新后的会话记忆
     * @author TellyJiang
     * @since 2026-04-12
     */
    MemorySessionDTO appendMemorySession(Long userId, String sessionId, String message, String modelName);

    /**
     * 提交完整问答回合
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param userMessage 用户消息
     * @param assistantMessage 助手回复
     * @param modelName 模型名称
     * @return 最新会话状态
     * @author TellyJiang
     * @since 2026-04-16
     */
    MemorySessionDTO commitConversationRound(Long userId, String sessionId, String userMessage, String assistantMessage, String modelName);
}
