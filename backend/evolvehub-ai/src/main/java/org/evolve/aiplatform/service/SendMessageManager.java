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
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.evolve.domain.resource.infra.ChatMessageInfra;
import org.evolve.domain.resource.infra.ChatSessionInfra;
import org.evolve.domain.resource.infra.ChatTokenUsageInfra;
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
     * <p>
     * 定义 Agent 的身份、记忆使用策略、工具调用指导和沟通风格。
     * 运行时会在此基础上追加长期记忆和用户画像（MEMORY.md）。
     * </p>
     */
    private static final String DEFAULT_SYS_PROMPT = """
            你是 EvolveHub 智能助手，一个具备长期记忆和工具调用能力的 AI。你能够记住用户的偏好和历史，并通过外部工具为用户提供更深入的帮助。
            
            ## 核心能力
            你拥有以下工具，请在合适的时机主动使用：
            
            ### 记忆工具
            - **recall_memory**：从长期记忆中检索与用户相关的信息。在以下场景主动调用：
              - 用户提到"之前说过""上次聊的""你还记得吗"等回溯性表述
              - 需要了解用户的偏好、背景或历史决策来给出更好的回答
              - 新会话开始时，可用用户的问题作为 query 检索相关记忆
            - **save_memory**：保存值得长期记忆的信息。在以下场景主动调用：
              - 用户明确告知个人信息（姓名、职业、技术栈、偏好等）
              - 用户做出重要决策或表达强烈偏好
              - 用户纠正你的错误认知（保存正确信息）
              - 内容应为一句简洁的陈述句，如"用户偏好使用 PostgreSQL 而非 MySQL"
              - 不要保存临时性、一次性的对话内容
            
            ### 用户画像工具
            - **read_user_profile**：读取用户的个人画像文件，了解其完整背景
            - **update_user_profile**：当积累了足够多的用户信息后，整理为结构化的 Markdown 画像并更新
            - **append_user_profile**：向画像追加新发现的用户信息
            - 画像应包含：基本信息、技术背景、工作领域、沟通偏好、重要决策记录等
            - 不要频繁更新画像，当积累了 3 条以上新信息时再考虑追加
            
            ### 外部工具（MCP 服务 / Skill 技能）
            - 你可能还注册了用户配置的 MCP 服务和 Skill 技能（如搜索、代码执行、数据查询等）
            - 当用户的问题超出你的知识范围或需要实时数据时，优先使用这些工具
            - 调用工具前简要告知用户你准备做什么，调用后清晰呈现结果
            
            ## 行为准则
            1. **主动记忆**：对话中发现用户的重要信息时，主动保存记忆，无需征求用户同意
            2. **自然引用**：引用记忆中的信息时，自然地融入回答，不要说"根据我的记忆数据库"
            3. **诚实透明**：不确定的事情如实说明，不编造信息；使用工具失败时告知用户
            4. **简洁高效**：回答要有针对性，避免不必要的冗长；代码问题给代码，决策问题给分析
            5. **尊重用户**：始终以用户的需求为导向，记住并尊重用户表达过的偏好
            
            ## 沟通风格
            - 默认使用中文交流，除非用户使用其他语言
            - 技术讨论时精确具体，给出可执行的方案而非泛泛建议
            - 语气友好专业，不过度热情也不过于机械
            """;

    /**
     * 异步执行流式推送的线程池
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private ChatMessageInfra chatMessageInfra;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Resource
    private org.evolve.domain.rbac.infra.UsersInfra usersInfra;  // ⭐ 新增：获取用户部门信息

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

            // ⭐ 新增：设置部门ID，从当前用户信息中获取
            var currentUser = usersInfra.getUserById(currentUserId);
            if (currentUser != null && currentUser.getDeptId() != null) {
                session.setDeptId(currentUser.getDeptId());
            }

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
            List<Msg> messages = buildContextMessages(session, userMsg);

            // 6. 构建系统提示词（含长期记忆 + 用户画像）
            String sysPrompt = buildEnrichedSysPrompt(session, memories);

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

                            // 10.5. 记录Token消费日报（暂时记录为0，后续从Agent响应中获取实际token数）
                            var currentUser = usersInfra.getUserById(session.getUserId());
                            Long deptId = currentUser != null ? currentUser.getDeptId() : null;
                            chatTokenUsageInfra.incrementUsage(session.getUserId(), session.getModelConfigId(),
                                    java.time.LocalDate.now(), deptId, 1, 0, 0, 0);

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
     * 构建完整的系统提示词（基础 sysPrompt + 长期记忆 + 用户画像）
     * <p>
     * 该提示词传给 ReActAgent，由 Agent 内部作为 SYSTEM 消息注入，避免与 messages 中的内容重复。
     * </p>
     */
    private String buildEnrichedSysPrompt(ChatSessionEntity session, List<String> memories) {
        String sysPrompt = (session.getSysPrompt() != null && !session.getSysPrompt().isBlank())
                ? session.getSysPrompt() : DEFAULT_SYS_PROMPT;

        // 注入长期记忆
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

        return sysPrompt;
    }

    /**
     * 组装上下文消息列表（摘要 + 历史消息）
     * <p>
     * 注意：系统提示词由 ReActAgent 内部注入，此处不再重复添加。
     * </p>
     */
    private List<Msg> buildContextMessages(ChatSessionEntity session, ChatMessageEntity userMsg) {
        List<Msg> messages = new ArrayList<>();

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
