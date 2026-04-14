package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolUseBlock;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.entity.ChatMessageEntity;
import org.evolve.aiplatform.bean.entity.ChatSessionEntity;
import org.evolve.aiplatform.bean.entity.ChatTokenUsageEntity;
import org.evolve.aiplatform.infra.ChatMessageInfra;
import org.evolve.aiplatform.infra.ChatSessionInfra;
import org.evolve.aiplatform.infra.ChatTokenUsageInfra;
import org.evolve.aiplatform.request.SendMessageRequest;
import org.evolve.aiplatform.service.agent.ChatAgentFactory;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 发送对话消息业务处理器
 * <p>
 * 核心流程：校验会话 → 保存用户消息 → 加载历史上下文 → 构建 Agent → 流式调用 → SSE 推送 → 保存助手消息 → 更新统计。
 * 不继承 BaseManager，因为返回 SseEmitter 流式对象。
 * </p>
 *
 * @author zhao
 */
@Service
public class SendMessageManager {

    private static final Logger log = LoggerFactory.getLogger(SendMessageManager.class);

    /**
     * 上下文消息最大加载条数
     */
    private static final int MAX_CONTEXT_MESSAGES = 20;

    /**
     * 默认系统提示词
     */
    private static final String DEFAULT_SYS_PROMPT = "你是一个有帮助的 AI 助手。";

    /**
     * 异步执行流式推送的线程池
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private ChatMessageInfra chatMessageInfra;

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ChatAgentFactory chatAgentFactory;

    @Resource
    private ChatMemoryService chatMemoryService;

    @Resource
    private ContextSummaryService contextSummaryService;

    @Resource
    private ChatContextCacheService chatContextCacheService;

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 发送消息并返回 SSE 流
     * <p>
     * 如果 request.sessionId 为空，自动创建新会话；否则校验已有会话归属。
     * SSE 流的第一条事件为 session_created（仅新建会话时），告知前端 sessionId。
     * </p>
     *
     * @param request 发送消息请求
     * @return SseEmitter 流式发射器
     */
    public SseEmitter send(SendMessageRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        boolean isNewSession = (request.sessionId() == null);

        ChatSessionEntity session;
        if (isNewSession) {
            // 自动创建新会话
            if (request.modelConfigId() == null) {
                throw new BusinessException("新建会话时 modelConfigId 不能为空");
            }
            ModelConfigEntity modelConfig = modelConfigInfra.getModelConfigById(request.modelConfigId());
            if (modelConfig == null || modelConfig.getEnabled() == 0) {
                throw new BusinessException("模型配置不可用");
            }
            session = new ChatSessionEntity();
            session.setUserId(currentUserId);
            session.setModelConfigId(request.modelConfigId());
            session.setSysPrompt(request.sysPrompt());
            session.setTotalPromptTokens(0);
            session.setTotalCompletionTokens(0);
            session.setTotalTokens(0);
            session.setMessageCount(0);
            chatSessionInfra.save(session);
        } else {
            // 校验已有会话归属
            session = chatSessionInfra.getByIdAndUserId(request.sessionId(), currentUserId);
            if (session == null) {
                throw new BusinessException("会话不存在或无权操作");
            }
        }

        // 校验模型配置
        ModelConfigEntity modelConfig = modelConfigInfra.getModelConfigById(session.getModelConfigId());
        if (modelConfig == null || modelConfig.getEnabled() == 0) {
            throw new BusinessException("模型配置不可用");
        }

        // 保存用户消息
        ChatMessageEntity userMsg = new ChatMessageEntity();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(request.content());
        chatMessageInfra.save(userMsg);

        // 创建 SSE，异步执行流式推送
        SseEmitter emitter = new SseEmitter(120_000L);

        // 新建会话时，立即发送 sessionId 给前端
        if (isNewSession) {
            try {
                emitter.send(SseEmitter.event()
                        .data(objectMapper.writeValueAsString(
                                Map.of("type", "session_created", "sessionId", session.getId()))));
            } catch (IOException e) {
                log.error("SSE 发送 session_created 事件失败", e);
            }
        }

        executor.execute(() -> doStreamChat(emitter, session, modelConfig, userMsg));
        return emitter;
    }

    /**
     * 异步执行流式对话（在虚拟线程中运行）
     * <p>
     * 核心流程：构建 Agent → agent.stream() → Event 映射 SSE → 后处理（保存消息/统计/记忆）。
     * Event 类型映射：
     * <ul>
     *   <li>REASONING → type=reasoning（推理过程/文本片段）</li>
     *   <li>TOOL_RESULT → type=tool_result（工具调用结果）</li>
     *   <li>AGENT_RESULT → type=chunk（最终文本回复）</li>
     * </ul>
     * </p>
     */
    private void doStreamChat(SseEmitter emitter, ChatSessionEntity session,
                               ModelConfigEntity modelConfig, ChatMessageEntity userMsg) {
        long startTime = System.currentTimeMillis();
        StringBuilder fullResponse = new StringBuilder();

        try {
            // 5. 加载历史消息并组装上下文
            List<String> memories = Collections.emptyList();
            try {
                memories = chatMemoryService.retrieve(session.getUserId(), userMsg.getContent());
            } catch (Exception e) {
                log.warn("长期记忆检索失败，继续对话", e);
            }
            List<Msg> messages = buildContextMessages(session, userMsg, memories);

            // 6. 构建系统提示词（已包含在 messages 的第一条 SYSTEM 消息中）
            String sysPrompt = (session.getSysPrompt() != null && !session.getSysPrompt().isBlank())
                    ? session.getSysPrompt() : DEFAULT_SYS_PROMPT;

            // 7. 构建 Agent（含 Model + Toolkit + sysPrompt）
            ReActAgent agent = chatAgentFactory.createAgent(
                    modelConfig, session.getUserId(), session.getId(), sysPrompt);
            StreamOptions streamOptions = chatAgentFactory.buildStreamOptions();

            // 8. 流式调用 Agent
            agent.stream(messages, streamOptions)
                    .doOnNext(event -> handleEvent(emitter, event, fullResponse))
                    .doOnComplete(() -> {
                        try {
                            long durationMs = System.currentTimeMillis() - startTime;
                            // 发送完成事件
                            emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(Map.of(
                                            "type", "done",
                                            "durationMs", durationMs))));
                            emitter.complete();

                            // 9. 保存助手消息
                            saveAssistantMessage(session, modelConfig, fullResponse.toString(),
                                    new int[]{0, 0, 0}, "stop", (int) durationMs);

                            // 10. 更新会话统计（token 由 Agent 内部管理，此处暂记 0）
                            chatSessionInfra.incrementTokenUsage(session.getId(), 0, 0, 0, 2);

                            // 11. 首轮自动生成标题
                            autoGenerateTitle(session);

                            // 12. 异步提取并保存长期记忆
                            chatMemoryService.extractAndSaveAsync(
                                    session.getUserId(), session.getId(), modelConfig,
                                    userMsg.getContent(), fullResponse.toString());

                            // 13. 异步压缩上下文摘要（短期记忆压缩）
                            contextSummaryService.compressIfNeededAsync(session, modelConfig);
                        } catch (IOException e) {
                            log.error("SSE 完成事件发送失败", e);
                        }
                    })
                    .doOnError(error -> {
                        log.error("Agent 调用异常", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(Map.of(
                                            "type", "error",
                                            "message", error.getMessage() != null
                                                    ? error.getMessage() : "Agent 调用失败"))));
                        } catch (IOException e) {
                            log.error("SSE 错误事件发送失败", e);
                        }
                        emitter.completeWithError(error);
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("流式对话处理异常", e);
            try {
                emitter.send(SseEmitter.event()
                        .data(objectMapper.writeValueAsString(Map.of(
                                "type", "error", "message", e.getMessage()))));
            } catch (IOException ex) {
                log.error("SSE 异常事件发送失败", ex);
            }
            emitter.completeWithError(e);
        }
    }

    /**
     * 处理 Agent 流式事件，映射为 SSE 推送
     *
     * @param emitter      SSE 发射器
     * @param event        Agent 事件
     * @param fullResponse 累积完整回复内容
     */
    private void handleEvent(SseEmitter emitter, Event event, StringBuilder fullResponse) {
        try {
            EventType type = event.getType();
            Msg msg = event.getMessage();

            switch (type) {
                case REASONING -> {
                    // 推理过程（思考链），提取文本推送
                    String text = extractTextFromMsg(msg);
                    if (text != null && !text.isEmpty()) {
                        emitter.send(SseEmitter.event()
                                .data(objectMapper.writeValueAsString(
                                        Map.of("type", "reasoning", "content", text))));
                    }
                    // 检查是否包含工具调用请求
                    List<ToolUseBlock> toolCalls = extractToolUseCalls(msg);
                    for (ToolUseBlock toolCall : toolCalls) {
                        emitter.send(SseEmitter.event()
                                .data(objectMapper.writeValueAsString(Map.of(
                                        "type", "tool_call",
                                        "toolCallId", toolCall.getId(),
                                        "toolName", toolCall.getName(),
                                        "input", toolCall.getInput() != null ? toolCall.getInput() : Map.of()))));
                    }
                }
                case TOOL_RESULT -> {
                    // 工具调用结果，推送给前端
                    List<ToolResultBlock> results = extractToolResults(msg);
                    for (ToolResultBlock result : results) {
                        String resultText = extractToolResultText(result);
                        emitter.send(SseEmitter.event()
                                .data(objectMapper.writeValueAsString(Map.of(
                                        "type", "tool_result",
                                        "toolCallId", result.getId() != null ? result.getId() : "",
                                        "toolName", result.getName() != null ? result.getName() : "",
                                        "result", resultText))));
                    }
                }
                case AGENT_RESULT -> {
                    // 最终回复，按 chunk 推送
                    String text = extractTextFromMsg(msg);
                    if (text != null && !text.isEmpty()) {
                        fullResponse.append(text);
                        emitter.send(SseEmitter.event()
                                .data(objectMapper.writeValueAsString(
                                        Map.of("type", "chunk", "content", text))));
                    }
                }
                case SUMMARY -> {
                    // Agent 总结事件（通常在最后），提取文本作为完整回复
                    String text = extractTextFromMsg(msg);
                    if (text != null && !text.isEmpty() && fullResponse.isEmpty()) {
                        fullResponse.append(text);
                    }
                }
                default -> log.debug("忽略 Agent 事件: type={}", type);
            }
        } catch (IOException e) {
            log.error("SSE 发送 Agent 事件失败", e);
        }
    }

    /**
     * 从 Msg 中提取文本内容
     */
    private String extractTextFromMsg(Msg msg) {
        if (msg == null || msg.getContent() == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : msg.getContent()) {
            if (block instanceof TextBlock textBlock) {
                sb.append(textBlock.getText());
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    /**
     * 从 Msg 中提取工具调用请求
     */
    private List<ToolUseBlock> extractToolUseCalls(Msg msg) {
        if (msg == null || msg.getContent() == null) {
            return List.of();
        }
        List<ToolUseBlock> calls = new ArrayList<>();
        for (ContentBlock block : msg.getContent()) {
            if (block instanceof ToolUseBlock toolUseBlock) {
                calls.add(toolUseBlock);
            }
        }
        return calls;
    }

    /**
     * 从 Msg 中提取工具调用结果
     */
    private List<ToolResultBlock> extractToolResults(Msg msg) {
        if (msg == null || msg.getContent() == null) {
            return List.of();
        }
        List<ToolResultBlock> results = new ArrayList<>();
        for (ContentBlock block : msg.getContent()) {
            if (block instanceof ToolResultBlock resultBlock) {
                results.add(resultBlock);
            }
        }
        return results;
    }

    /**
     * 从 ToolResultBlock 中提取文本结果
     */
    private String extractToolResultText(ToolResultBlock result) {
        if (result.getOutput() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : result.getOutput()) {
            if (block instanceof TextBlock textBlock) {
                sb.append(textBlock.getText());
            }
        }
        return sb.toString();
    }

    /**
     * 组装上下文消息列表（system + 摘要 + 历史 + 当前用户消息）
     */
    private List<Msg> buildContextMessages(ChatSessionEntity session, ChatMessageEntity userMsg,
                                              List<String> memories) {
        List<Msg> messages = new ArrayList<>();

        // 系统提示词
        String sysPrompt = (session.getSysPrompt() != null && !session.getSysPrompt().isBlank())
                ? session.getSysPrompt() : DEFAULT_SYS_PROMPT;

        // 注入长期记忆到系统提示词
        if (memories != null && !memories.isEmpty()) {
            StringBuilder memoryBlock = new StringBuilder();
            memoryBlock.append("\n\n以下是你对该用户的了解（长期记忆），请在回答时参考：\n");
            for (int i = 0; i < memories.size(); i++) {
                memoryBlock.append(i + 1).append(". ").append(memories.get(i)).append("\n");
            }
            sysPrompt = sysPrompt + memoryBlock;
        }

        // 注入用户画像（MEMORY.md）
        String userProfile = userProfileService.getProfile(session.getUserId());
        if (userProfile != null && !userProfile.isBlank()) {
            sysPrompt = sysPrompt + "\n\n以下是该用户的个人画像：\n" + userProfile;
        }

        messages.add(Msg.builder()
                .role(MsgRole.SYSTEM)
                .textContent(sysPrompt)
                .build());

        // 注入上下文摘要（短期记忆压缩，优先从 Redis 读取）
        String contextSummary = chatContextCacheService.getSummary(session.getUserId(), session.getId());
        if (contextSummary != null && !contextSummary.isBlank()) {
            messages.add(Msg.builder()
                    .role(MsgRole.SYSTEM)
                    .textContent("以下是之前对话的摘要，帮助你理解上下文：\n" + contextSummary)
                    .build());
        }

        // 历史消息（不包含刚保存的用户消息，因为它是最新一条）
        List<ChatMessageEntity> history = chatMessageInfra.listRecentMessages(
                session.getId(), MAX_CONTEXT_MESSAGES);
        for (ChatMessageEntity msg : history) {
            MsgRole role = switch (msg.getRole()) {
                case "user" -> MsgRole.USER;
                case "assistant" -> MsgRole.ASSISTANT;
                case "system" -> MsgRole.SYSTEM;
                case "tool" -> MsgRole.TOOL;
                default -> MsgRole.USER;
            };
            if (msg.getContent() != null) {
                messages.add(Msg.builder()
                        .role(role)
                        .textContent(msg.getContent())
                        .build());
            }
        }

        return messages;
    }

    /**
     * 保存助手消息到数据库
     */
    private void saveAssistantMessage(ChatSessionEntity session, ModelConfigEntity modelConfig,
                                       String content, int[] tokenStats,
                                       String finishReason, int durationMs) {
        ChatMessageEntity assistantMsg = new ChatMessageEntity();
        assistantMsg.setSessionId(session.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(content);
        assistantMsg.setModelName(modelConfig.getName());
        assistantMsg.setPromptTokens(tokenStats[0]);
        assistantMsg.setCompletionTokens(tokenStats[1]);
        assistantMsg.setTotalTokens(tokenStats[2]);
        assistantMsg.setFinishReason(finishReason);
        assistantMsg.setDurationMs(durationMs);
        chatMessageInfra.save(assistantMsg);
    }

    /**
     * 更新 Token 消费日报（upsert 逻辑）
     */
    private void updateTokenUsage(Long userId, Long modelConfigId,
                                   int promptTokens, int completionTokens, int totalTokens) {
        LocalDate today = LocalDate.now();
        ChatTokenUsageEntity usage = chatTokenUsageInfra.getByUserAndModelAndDate(
                userId, modelConfigId, today);
        if (usage != null) {
            chatTokenUsageInfra.incrementUsage(usage.getId(),
                    promptTokens, completionTokens, totalTokens);
        } else {
            ChatTokenUsageEntity newUsage = new ChatTokenUsageEntity();
            newUsage.setUserId(userId);
            newUsage.setModelConfigId(modelConfigId);
            newUsage.setUsageDate(today);
            newUsage.setRequestCount(1);
            newUsage.setPromptTokens(promptTokens);
            newUsage.setCompletionTokens(completionTokens);
            newUsage.setTotalTokens(totalTokens);
            chatTokenUsageInfra.save(newUsage);
        }
    }

    /**
     * 首轮对话自动生成会话标题（截取助手回复前 30 个字符）
     */
    private void autoGenerateTitle(ChatSessionEntity session) {
        if (session.getTitle() != null && !session.getTitle().isBlank()) {
            return;
        }
        long msgCount = chatMessageInfra.countBySessionId(session.getId());
        if (msgCount <= 2) {
            // 取最新的 assistant 消息内容作为标题
            List<ChatMessageEntity> recent = chatMessageInfra.listRecentMessages(session.getId(), 2);
            for (ChatMessageEntity msg : recent) {
                if ("assistant".equals(msg.getRole()) && msg.getContent() != null) {
                    String title = msg.getContent().length() > 30
                            ? msg.getContent().substring(0, 30) + "..."
                            : msg.getContent();
                    ChatSessionEntity update = new ChatSessionEntity();
                    update.setId(session.getId());
                    update.setTitle(title);
                    chatSessionInfra.updateById(update);
                    break;
                }
            }
        }
    }
}
