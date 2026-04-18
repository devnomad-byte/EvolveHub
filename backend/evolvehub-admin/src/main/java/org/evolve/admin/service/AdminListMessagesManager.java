package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.admin.request.ListMessagesRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.resource.infra.ChatMessageInfra;
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminListMessagesManager extends BaseManager<ListMessagesRequest, PageResponse<ChatMessageEntity>> {

    @Resource
    private ChatMessageInfra chatMessageInfra;

    @Override
    protected void check(ListMessagesRequest request) {
        if (request.sessionId() == null) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
    }

    @Override
    protected PageResponse<ChatMessageEntity> process(ListMessagesRequest request) {
        Page<ChatMessageEntity> page = chatMessageInfra.listPageBySessionId(
                request.sessionId(), request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
