package org.evolve.aiplatform.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.evolve.aiplatform.request.ListMessagesRequest;
import org.evolve.aiplatform.request.SendMessageRequest;
import org.evolve.aiplatform.request.TokenUsageQueryRequest;
import org.evolve.aiplatform.request.UpdateSessionRequest;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryManagedItemVO;
import org.evolve.aiplatform.service.DeleteSessionManager;
import org.evolve.aiplatform.service.ListMessagesManager;
import org.evolve.aiplatform.service.ListSessionsManager;
import org.evolve.aiplatform.service.SendMessageManager;
import org.evolve.aiplatform.service.TokenUsageManager;
import org.evolve.aiplatform.service.UpdateSessionManager;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户对话控制器
 * <p>
 * 提供对话会话的 CRUD 操作和消息历史查询。
 * 所有接口登录即可访问，业务层校验会话归属当前用户。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/user/chat")
public class UserChatController {

    @Resource
    private ListSessionsManager listSessionsManager;

    @Resource
    private UpdateSessionManager updateSessionManager;

    @Resource
    private DeleteSessionManager deleteSessionManager;

    @Resource
    private ListMessagesManager listMessagesManager;

    @Resource
    private SendMessageManager sendMessageManager;

    @Resource
    private TokenUsageManager tokenUsageManager;

    @Resource
    private MemoryApi memoryApi;

    /**
     * 分页查询当前用户的会话列表
     *
     * @param pageNum  页码，默认为 1
     * @param pageSize 每页条数，默认为 10
     * @return 分页会话列表
     */
    @GetMapping("/sessions")
    public Result<PageResponse<ChatSessionEntity>> listSessions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listSessionsManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新对话会话（标题 / 系统提示词）
     *
     * @param request 更新请求
     * @return 操作结果
     */
    @PutMapping("/sessions")
    public Result<Void> updateSession(@RequestBody @Valid UpdateSessionRequest request) {
        updateSessionManager.execute(request);
        return Result.ok();
    }

    /**
     * 删除对话会话（逻辑删除，级联删除消息）
     *
     * @param id 会话 ID
     * @return 操作结果
     */
    @DeleteMapping("/sessions/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        deleteSessionManager.execute(id);
        return Result.ok();
    }

    /**
     * 分页查询会话的消息历史
     *
     * @param id       会话 ID
     * @param pageNum  页码，默认为 1
     * @param pageSize 每页条数，默认为 20
     * @return 分页消息列表
     */
    @GetMapping("/sessions/{id}/messages")
    public Result<PageResponse<ChatMessageEntity>> listMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(listMessagesManager.execute(new ListMessagesRequest(id, pageNum, pageSize)));
    }

    /**
     * 发送消息（SSE 流式返回）
     * <p>
     * sessionId 为空时自动创建新会话（需传 modelConfigId），
     * SSE 流首条事件返回 sessionId。
     * 已有会话时传 sessionId 即可继续对话。
     * </p>
     *
     * @param request 发送消息请求
     * @return SSE 事件流
     */
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(@RequestBody @Valid SendMessageRequest request) {
        return sendMessageManager.send(request);
    }

    /**
     * 查询当前用户的 Token 消费统计
     *
     * @param startDate 起始日期（默认当月 1 号）
     * @param endDate   结束日期（默认今天）
     * @return 消费记录列表
     */
    @GetMapping("/token-usage")
    public Result<List<ChatTokenUsageEntity>> tokenUsage(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return Result.ok(tokenUsageManager.execute(new TokenUsageQueryRequest(startDate, endDate)));
    }

    /**
     * 查看当前用户的长期记忆列表
     *
     * @return 记忆文本列表
     */
    @GetMapping("/memories")
    public Result<List<String>> listMemories() {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        List<String> memories = memoryApi.listManagedMemories(userId).stream()
                .map(MemoryManagedItemVO::getContent)
                .toList();
        return Result.ok(memories);
    }

    /**
     * 删除一条长期记忆
     *
     * @param docId 文档 ID
     * @return 操作结果
     */
    @DeleteMapping("/memories/{docId}")
    public Result<Void> deleteMemory(@PathVariable String docId) {
        try {
            memoryApi.deleteManagedMemory(cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong(), Long.valueOf(docId));
        } catch (NumberFormatException e) {
            throw new org.evolve.common.web.exception.BusinessException("记忆标识无效");
        }
        return Result.ok();
    }
}
