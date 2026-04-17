package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class ChatSessionInfra extends ServiceImpl<ChatSessionInfra.ChatSessionMapper, ChatSessionEntity> {

    @Mapper
    interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {}

    public Page<ChatSessionEntity> listPageByUserId(Long userId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(ChatSessionEntity::getUserId, userId)
                .orderByDesc(ChatSessionEntity::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    public ChatSessionEntity getByIdAndUserId(Long id, Long userId) {
        return this.lambdaQuery()
                .eq(ChatSessionEntity::getId, id)
                .eq(ChatSessionEntity::getUserId, userId)
                .one();
    }

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

    public Page<ChatSessionEntity> listPageByDataScope(
            Integer dataScope, Long userDeptId, Set<Long> visibleDeptIds,
            Long currentUserId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(dataScope == 4, ChatSessionEntity::getCreateBy, currentUserId)
                .eq(dataScope == 3, ChatSessionEntity::getDeptId, userDeptId)
                .in(dataScope == 2, ChatSessionEntity::getDeptId,
                        visibleDeptIds != null ? visibleDeptIds : Set.of(userDeptId))
                .in(dataScope == 5 && visibleDeptIds != null, ChatSessionEntity::getDeptId, visibleDeptIds)
                .orderByDesc(ChatSessionEntity::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
    }
}
