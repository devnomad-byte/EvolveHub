package org.evolve.admin.api;

import jakarta.annotation.Resource;
import org.evolve.admin.response.SessionItemResponse;
import org.evolve.admin.service.ChatHistoryExportManager;
import org.evolve.admin.service.ChatHistoryMessageListManager;
import org.evolve.admin.service.ChatHistorySessionListManager;
import org.evolve.admin.service.ChatHistoryUserListManager;
import org.evolve.admin.request.ListMessagesRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat-history")
public class ChatHistoryController {

    @Resource
    private ChatHistoryUserListManager userListManager;

    @Resource
    private ChatHistorySessionListManager sessionListManager;

    @Resource
    private ChatHistoryMessageListManager messageListManager;

    @Resource
    private ChatHistoryExportManager exportManager;

    /**
     * 获取有聊天记录的用户列表
     */
    @GetMapping("/users")
    public Result<PageResponse<org.evolve.admin.response.UserChatActivityResponse>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(userListManager.execute(
                new ChatHistoryUserListManager.Request(keyword, deptId, pageNum, pageSize)));
    }

    /**
     * 获取用户的会话列表
     */
    @GetMapping("/sessions")
    public Result<PageResponse<SessionItemResponse>> listSessions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize) {
        return Result.ok(sessionListManager.execute(
                new ChatHistorySessionListManager.Request(userId, keyword, startDate, endDate, pageNum, pageSize)));
    }

    /**
     * 获取会话的消息列表
     */
    @GetMapping("/sessions/{id}/messages")
    public Result<PageResponse<ChatMessageEntity>> listMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(messageListManager.execute(new ListMessagesRequest(id, pageNum, pageSize)));
    }

    /**
     * 导出对话记录
     */
    @PostMapping("/export")
    public Result<ChatHistoryExportManager.ExportResult> exportChatHistory(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "md") String format) {
        var result = exportManager.execute(
                new ChatHistoryExportManager.Request(userId, startDate, endDate, format));
        return Result.ok(result);
    }
}
