package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.entity.ChatMessageEntity;
import org.evolve.aiplatform.bean.entity.ChatSessionEntity;
import org.evolve.aiplatform.infra.ChatMessageInfra;
import org.evolve.aiplatform.infra.ChatSessionInfra;
import org.evolve.aiplatform.request.ListMessagesRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询会话消息列表
 * <p>
 * 校验会话归属当前用户后，按创建时间正序分页返回消息。
 * </p>
 *
 * @author zhao
 */
@Service
public class ListMessagesManager extends BaseManager<ListMessagesRequest, PageResponse<ChatMessageEntity>> {

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private ChatMessageInfra chatMessageInfra;

    @Override
    protected void check(ListMessagesRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ChatSessionEntity session = chatSessionInfra.getByIdAndUserId(request.sessionId(), currentUserId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权操作");
        }
    }

    @Override
    protected PageResponse<ChatMessageEntity> process(ListMessagesRequest request) {
        Page<ChatMessageEntity> page = chatMessageInfra.listPageBySessionId(
                request.sessionId(), request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
