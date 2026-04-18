package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatMessageInfra extends ServiceImpl<ChatMessageInfra.ChatMessageMapper, ChatMessageEntity> {

    @Mapper
    interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {}

    public Page<ChatMessageEntity> listPageBySessionId(Long sessionId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(ChatMessageEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    public List<ChatMessageEntity> listRecentMessages(Long sessionId, int limit) {
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByDesc(ChatMessageEntity::getCreateTime)
                .last("LIMIT " + limit)
                .list()
                .reversed();
    }

    public long countBySessionId(Long sessionId) {
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .count();
    }

    public List<ChatMessageEntity> listBySessionId(Long sessionId) {
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(ChatMessageEntity::getCreateTime)
                .list();
    }

    public void deleteBySessionId(Long sessionId) {
        this.lambdaUpdate()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .set(ChatMessageEntity::getDeleted, 1)
                .update();
    }

    public List<ChatMessageEntity> listOverflowMessages(Long sessionId, int windowSize, int overflowLimit) {
        long totalCount = countBySessionId(sessionId);
        if (totalCount <= windowSize) {
            return List.of();
        }
        return this.lambdaQuery()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByDesc(ChatMessageEntity::getCreateTime)
                .last("LIMIT " + overflowLimit + " OFFSET " + windowSize)
                .list()
                .reversed();
    }
}
