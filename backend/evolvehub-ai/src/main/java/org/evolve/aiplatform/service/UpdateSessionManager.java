package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.evolve.domain.resource.infra.ChatSessionInfra;
import org.evolve.aiplatform.request.UpdateSessionRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.springframework.stereotype.Service;

/**
 * 更新对话会话业务处理器
 * <p>
 * 仅允许修改会话标题和系统提示词，校验会话归属当前用户。
 * </p>
 *
 * @author zhao
 */
@Service
public class UpdateSessionManager extends BaseManager<UpdateSessionRequest, Void> {

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private AvailableModelSupport availableModelSupport;

    @Override
    protected void check(UpdateSessionRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ChatSessionEntity session = chatSessionInfra.getByIdAndUserId(request.id(), currentUserId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权操作");
        }
        if (request.modelConfigId() != null) {
            availableModelSupport.requireAvailableChatModel(currentUserId, request.modelConfigId());
        }
    }

    @Override
    protected Void process(UpdateSessionRequest request) {
        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setId(request.id());
        if (request.title() != null) {
            entity.setTitle(request.title());
        }
        if (request.sysPrompt() != null) {
            entity.setSysPrompt(request.sysPrompt());
        }
        if (request.modelConfigId() != null) {
            entity.setModelConfigId(request.modelConfigId());
        }
        chatSessionInfra.updateById(entity);
        return null;
    }
}
