package org.evolve.aiplatform.infra;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.evolve.aiplatform.bean.entity.ChatMessageEntity;
import org.evolve.aiplatform.infra.mapper.ChatMessageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对话消息数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Repository
public class ChatMessageInfra extends ServiceImpl<ChatMessageMapper, ChatMessageEntity> {

    /**
     * 按会话分页查询消息列表（按创建时间正序）
     *
     * @param sessionId 会话 ID
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 分页结果
     */
    public Page<ChatMessageEntity> listPageBySessionId(Long sessionId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(ChatMessageEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 加载会话最近的 N 条消息（用于组装上下文发送给模型）
     *
     * @param sessionId 会话 ID
     * @param limit     最大条数
     * @return 消息列表，按创建时间正序
     */
    public List<ChatMessageEntity> listRecentMessages(Long sessionId, int limit) {
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByDesc(ChatMessageEntity::getCreateTime)
                .last("LIMIT " + limit)
                .list()
                .reversed();
    }

    /**
     * 统计会话的消息条数
     *
     * @param sessionId 会话 ID
     * @return 消息条数
     */
    public long countBySessionId(Long sessionId) {
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .count();
    }

    /**
     * 按会话 ID 逻辑删除所有消息
     *
     * @param sessionId 会话 ID
     */
    public void deleteBySessionId(Long sessionId) {
        this.lambdaUpdate()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .set(ChatMessageEntity::getDeleted, 1)
                .update();
    }

    /**
     * 加载会话中窗口外的旧消息（排除最新的 N 条后，取剩余消息）
     * <p>
     * 用于短期记忆压缩：将窗口外的旧消息交给 LLM 生成摘要。
     * </p>
     *
     * @param sessionId     会话 ID
     * @param windowSize    窗口大小（保留的最新消息数）
     * @param overflowLimit 最多取多少条溢出消息（避免一次加载过多）
     * @return 窗口外的旧消息列表，按创建时间正序
     */
    public List<ChatMessageEntity> listOverflowMessages(Long sessionId, int windowSize, int overflowLimit) {
        // 查询总数
        long totalCount = countBySessionId(sessionId);
        if (totalCount <= windowSize) {
            return List.of();
        }
        // 跳过最新的 windowSize 条，取剩余的
        // 读取所有消息按时间倒序，跳过前 windowSize 条，取下一批
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByDesc(ChatMessageEntity::getCreateTime)
                .last("LIMIT " + overflowLimit + " OFFSET " + windowSize)
                .list()
                .reversed();
    }
}
