package org.evolve.aiplatform.infra;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.evolve.aiplatform.bean.entity.ChatSessionEntity;
import org.evolve.aiplatform.infra.mapper.ChatSessionMapper;
import org.springframework.stereotype.Repository;

/**
 * 对话会话数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Repository
public class ChatSessionInfra extends ServiceImpl<ChatSessionMapper, ChatSessionEntity> {

    /**
     * 按用户分页查询会话列表（按更新时间倒序）
     *
     * @param userId   用户 ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<ChatSessionEntity> listPageByUserId(Long userId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(ChatSessionEntity::getUserId, userId)
                .orderByDesc(ChatSessionEntity::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 根据 ID 和用户 ID 查询会话（校验归属）
     *
     * @param id     会话 ID
     * @param userId 用户 ID
     * @return 会话实体，不存在返回 null
     */
    public ChatSessionEntity getByIdAndUserId(Long id, Long userId) {
        return this.lambdaQuery()
                .eq(ChatSessionEntity::getId, id)
                .eq(ChatSessionEntity::getUserId, userId)
                .one();
    }

    /**
     * 累加会话的 token 消耗和消息条数
     *
     * @param sessionId        会话 ID
     * @param promptTokens     本次 prompt token 增量
     * @param completionTokens 本次 completion token 增量
     * @param totalTokens      本次总 token 增量
     * @param messageIncrement 消息条数增量
     */
    public void incrementTokenUsage(Long sessionId, int promptTokens, int completionTokens,
                                     int totalTokens, int messageIncrement) {
        this.lambdaUpdate()
                .eq(ChatSessionEntity::getId, sessionId)
                .setSql("total_prompt_tokens = total_prompt_tokens + " + promptTokens)
                .setSql("total_completion_tokens = total_completion_tokens + " + completionTokens)
                .setSql("total_tokens = total_tokens + " + totalTokens)
                .setSql("message_count = message_count + " + messageIncrement)
                .update();
    }
}
