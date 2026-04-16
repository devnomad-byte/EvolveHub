package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.entity.ChatSessionEntity;
import org.evolve.aiplatform.infra.ChatMessageInfra;
import org.evolve.aiplatform.infra.ChatSessionInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.springframework.stereotype.Service;

/**
 * 删除对话会话业务处理器
 * <p>
 * 逻辑删除会话及其关联的所有消息，校验会话归属当前用户。
 * </p>
 *
 * @author zhao
 */
@Service
public class DeleteSessionManager extends BaseManager<Long, Void> {

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private ChatMessageInfra chatMessageInfra;

    @Override
    protected void check(Long sessionId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ChatSessionEntity session = chatSessionInfra.getByIdAndUserId(sessionId, currentUserId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权操作");
        }
    }

    @Override
    protected Void process(Long sessionId) {
        chatMessageInfra.deleteBySessionId(sessionId);
        chatSessionInfra.removeById(sessionId);
        return null;
    }
}
