package org.evolve.admin.api;

import jakarta.annotation.Resource;
import org.evolve.admin.service.AdminListSessionsManager;
import org.evolve.admin.service.AdminListMessagesManager;
import org.evolve.admin.request.ListMessagesRequest;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat-history")
public class AdminChatHistoryController {

    @Resource
    private AdminListSessionsManager adminListSessionsManager;

    @Resource
    private AdminListMessagesManager adminListMessagesManager;

    @GetMapping("/sessions")
    public Result<PageResponse<ChatSessionEntity>> listSessions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(adminListSessionsManager.execute(new PageRequest(pageNum, pageSize)));
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<PageResponse<ChatMessageEntity>> listMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(adminListMessagesManager.execute(new ListMessagesRequest(id, pageNum, pageSize)));
    }
}
