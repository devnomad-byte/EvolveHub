package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.model.ChatResponse;
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
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryConversationContextDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryRoundDTO;
import org.evolve.aiplatform.request.SendMessageRequest;
import org.evolve.aiplatform.service.agent.ChatAgentFactory;
import org.evolve.common.base.CurrentUserHolder;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
     * 长期记忆召回默认条数
     */
    private static final int DEFAULT_MEMORY_TOP_K = 5;
    private static final long MEMORY_CONTEXT_TIMEOUT_SECONDS = 5L;
    private static final int DIRECT_CHAT_CONNECT_TIMEOUT_MS = 5_000;
    private static final int DIRECT_CHAT_READ_TIMEOUT_MS = 60_000;

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
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Resource
    private AvailableModelSupport availableModelSupport;

    @Resource
    private ChatAgentFactory chatAgentFactory;

    @Resource
    private MemoryApi memoryApi;

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
        ModelConfigEntity modelConfig;
        if (isNewSession) {
            // 自动创建新会话
            if (request.modelConfigId() == null) {
                throw new BusinessException("新建会话时 modelConfigId 不能为空");
            }
            modelConfig = availableModelSupport.requireAvailableChatModel(currentUserId, request.modelConfigId());
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
            if (session.getModelConfigId() == null) {
                if (request.modelConfigId() == null) {
                    throw new BusinessException("当前会话未绑定模型，请先选择模型");
                }
                modelConfig = availableModelSupport.requireAvailableChatModel(currentUserId, request.modelConfigId());
                ChatSessionEntity bindingSession = new ChatSessionEntity();
                bindingSession.setId(session.getId());
                bindingSession.setModelConfigId(modelConfig.getId());
                chatSessionInfra.updateById(bindingSession);
                session.setModelConfigId(modelConfig.getId());
            } else {
                modelConfig = availableModelSupport.requireAvailableChatModel(currentUserId, session.getModelConfigId());
            }
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
            MemoryConversationContextDTO memoryContext = buildConversationContextWithTimeout(emitter, session, userMsg);
            List<Msg> messages = buildContextMessages(session, userMsg, memoryContext);

            // 6. 构建系统提示词（含长期记忆 + 用户画像）
            String sysPrompt = buildEnrichedSysPrompt(session, memoryContext);

            if (shouldUseDirectModelStream(modelConfig)) {
                doDirectProviderChat(
                        emitter,
                        session,
                        modelConfig,
                        userMsg,
                        messages,
                        buildDirectSysPrompt(memoryContext),
                        fullResponse,
                        startTime
                );
                return;
            }

            // 7. 构建 Agent（含 Model + Toolkit + sysPrompt）
            ReActAgent agent = chatAgentFactory.createAgent(
                    modelConfig, session.getUserId(), session.getId(), sysPrompt);
            StreamOptions streamOptions = chatAgentFactory.buildStreamOptions();

            // 8. 流式调用 Agent
            agent.stream(messages, streamOptions)
                    .doOnNext(event -> handleEvent(emitter, event, fullResponse))
                    .doOnComplete(() -> {
                        long durationMs = System.currentTimeMillis() - startTime;
                        try {
                            // 9. 保存助手消息
                            saveAssistantMessage(session, modelConfig, fullResponse.toString(),
                                    new int[]{0, 0, 0}, "stop", (int) durationMs);

                            // 10. 更新会话统计（token 由 Agent 内部管理，此处暂记 0）
                            chatSessionInfra.incrementTokenUsage(session.getId(), 0, 0, 0, 2);

                            // 11. 首轮自动生成标题
                            autoGenerateTitle(session);

                            // 12. 提交完整对话回合，并异步提取长期记忆
                            withUserContext(session.getUserId(), () -> memoryApi.commitConversationRound(
                                    session.getUserId(),
                                    String.valueOf(session.getId()),
                                    userMsg.getContent(),
                                    fullResponse.toString(),
                                    modelConfig.getName()
                            ));
                            withUserContext(session.getUserId(), () -> {
                                memoryApi.extractMemoryFromConversationAsync(new MemoryExtractionRequestDTO(
                                        session.getUserId(),
                                        session.getId(),
                                        modelConfig.getName(),
                                        buildRoundTranscript(userMsg.getContent(), fullResponse.toString())
                                ));
                                return null;
                            });
                        } catch (Exception e) {
                            log.warn("对话回合记忆落库失败: sessionId={}", session.getId(), e);
                            sendMemoryWarning(emitter, "本轮记忆写入失败，对话结果已返回");
                        }
                        try {
                            emitter.send(SseEmitter.event()
                                    .data(objectMapper.writeValueAsString(Map.of(
                                            "type", "done",
                                            "durationMs", durationMs))));
                            emitter.complete();
                        } catch (IOException e) {
                            log.error("SSE 完成事件发送失败", e);
                            emitter.completeWithError(e);
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

    private void doDirectProviderChat(SseEmitter emitter,
                                      ChatSessionEntity session,
                                      ModelConfigEntity modelConfig,
                                      ChatMessageEntity userMsg,
                                      List<Msg> messages,
                                      String sysPrompt,
                                      StringBuilder fullResponse,
                                      long startTime) {
        try {
            String content = streamDirectCompletion(modelConfig, messages, sysPrompt, chunk -> {
                if (chunk == null || chunk.isEmpty()) {
                    return;
                }
                fullResponse.append(chunk);
                try {
                    emitter.send(SseEmitter.event()
                            .data(objectMapper.writeValueAsString(Map.of("type", "chunk", "content", chunk))));
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            });
            if (content != null && fullResponse.isEmpty()) {
                fullResponse.append(content);
            }
            finalizeConversation(emitter, session, modelConfig, userMsg, fullResponse, startTime);
        } catch (Exception exception) {
            log.error("直连模型对话异常", exception);
            handleStreamingError(emitter, exception);
        }
    }

    private String streamDirectCompletion(ModelConfigEntity modelConfig,
                                          List<Msg> messages,
                                          String sysPrompt,
                                          Consumer<String> onChunk) throws Exception {
        List<Map<String, String>> payloadMessages = new ArrayList<>();
        payloadMessages.add(Map.of("role", "system", "content", sysPrompt));
        for (Msg msg : messages) {
            if (msg == null || msg.getRole() == null) {
                continue;
            }
            String content = extractTextFromMsg(msg);
            if (content == null || content.isBlank()) {
                continue;
            }
            payloadMessages.add(Map.of(
                    "role", toOpenAiRole(msg.getRole()),
                    "content", content
            ));
        }

        String url = normalizeChatCompletionUrl(modelConfig.getBaseUrl());
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", modelConfig.getName(),
                "messages", payloadMessages,
                "stream", true
        ));
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(DIRECT_CHAT_CONNECT_TIMEOUT_MS))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(DIRECT_CHAT_READ_TIMEOUT_MS))
                .header("Authorization", "Bearer " + modelConfig.getApiKey())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() >= 400) {
            String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new IllegalStateException("模型接口调用失败: " + errorBody);
        }
        StringBuilder fullResponse = new StringBuilder();
        StringBuilder fallbackBody = new StringBuilder();
        try (InputStream inputStream = response.body();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (!line.startsWith("data:")) {
                    fallbackBody.append(line);
                    continue;
                }
                String payload = line.substring(5).trim();
                if ("[DONE]".equals(payload)) {
                    break;
                }
                String chunk = extractDirectStreamChunk(payload);
                if (chunk == null) {
                    fallbackBody.append(payload);
                    continue;
                }
                fullResponse.append(chunk);
                onChunk.accept(chunk);
            }
        }
        if (!fullResponse.isEmpty()) {
            return fullResponse.toString();
        }
        if (!fallbackBody.isEmpty()) {
            return extractDirectCompletionText(fallbackBody.toString());
        }
        return "";
    }

    private String extractDirectStreamChunk(String payload) throws IOException {
        Map<?, ?> body = objectMapper.readValue(payload, Map.class);
        Object choicesObject = body.get("choices");
        if (!(choicesObject instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            return null;
        }
        Object deltaObject = choiceMap.get("delta");
        if (deltaObject instanceof Map<?, ?> deltaMap) {
            Object contentObject = deltaMap.get("content");
            if (contentObject != null) {
                return String.valueOf(contentObject);
            }
        }
        Object messageObject = choiceMap.get("message");
        if (messageObject instanceof Map<?, ?> messageMap) {
            Object contentObject = messageMap.get("content");
            if (contentObject != null) {
                return String.valueOf(contentObject);
            }
        }
        return null;
    }

    private void finalizeConversation(SseEmitter emitter,
                                      ChatSessionEntity session,
                                      ModelConfigEntity modelConfig,
                                      ChatMessageEntity userMsg,
                                      StringBuilder fullResponse,
                                      long startTime) {
        long durationMs = System.currentTimeMillis() - startTime;
        try {
            saveAssistantMessage(session, modelConfig, fullResponse.toString(),
                    new int[]{0, 0, 0}, "stop", (int) durationMs);
            chatSessionInfra.incrementTokenUsage(session.getId(), 0, 0, 0, 2);
            autoGenerateTitle(session);

            withUserContext(session.getUserId(), () -> memoryApi.commitConversationRound(
                    session.getUserId(),
                    String.valueOf(session.getId()),
                    userMsg.getContent(),
                    fullResponse.toString(),
                    modelConfig.getName()
            ));
            withUserContext(session.getUserId(), () -> {
                memoryApi.extractMemoryFromConversationAsync(new MemoryExtractionRequestDTO(
                        session.getUserId(),
                        session.getId(),
                        modelConfig.getName(),
                        buildRoundTranscript(userMsg.getContent(), fullResponse.toString())
                ));
                return null;
            });
        } catch (Exception e) {
            log.warn("直接模型对话回合记忆落库失败: sessionId={}", session.getId(), e);
            sendMemoryWarning(emitter, "本轮记忆写入失败，对话结果已返回");
        }
        try {
            emitter.send(SseEmitter.event()
                    .data(objectMapper.writeValueAsString(Map.of(
                            "type", "done",
                            "durationMs", durationMs))));
            emitter.complete();
        } catch (IOException e) {
            log.error("SSE 发送直接模型完成事件失败", e);
            emitter.completeWithError(e);
        }
    }

    private void handleStreamingError(SseEmitter emitter, Throwable error) {
        log.error("流式对话异常", error);
        try {
            emitter.send(SseEmitter.event()
                    .data(objectMapper.writeValueAsString(Map.of(
                            "type", "error",
                            "message", error.getMessage() != null ? error.getMessage() : "Agent 调用失败"))));
        } catch (IOException e) {
            log.error("SSE 错误事件发送失败", e);
        }
        emitter.completeWithError(error);
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
    private String buildEnrichedSysPrompt(ChatSessionEntity session, MemoryConversationContextDTO memoryContext) {
        String sysPrompt = (session.getSysPrompt() != null && !session.getSysPrompt().isBlank())
                ? session.getSysPrompt() : DEFAULT_SYS_PROMPT;

        // 注入长期记忆
        List<String> memories = memoryContext == null || memoryContext.getRecalledMemories() == null
                ? Collections.emptyList()
                : memoryContext.getRecalledMemories().stream()
                .map(result -> result.getContent())
                .filter(content -> content != null && !content.isBlank())
                .toList();
        if (!memories.isEmpty()) {
            StringBuilder memoryBlock = new StringBuilder();
            memoryBlock.append("\n\n以下是你对该用户的了解（长期记忆），请在回答时参考：\n");
            for (int i = 0; i < memories.size(); i++) {
                memoryBlock.append(i + 1).append(". ").append(memories.get(i)).append("\n");
            }
            sysPrompt = sysPrompt + memoryBlock;
        }

        // 注入用户画像（MEMORY.md）
        String userProfile = memoryContext == null ? null : memoryContext.getProfileMarkdown();
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
    private List<Msg> buildContextMessages(ChatSessionEntity session,
                                           ChatMessageEntity userMsg,
                                           MemoryConversationContextDTO memoryContext) {
        if (memoryContext == null) {
            return buildFallbackContextMessages(session);
        }

        List<Msg> messages = new ArrayList<>();

        String summaryContent = memoryContext.getActiveSummaries() == null
                ? ""
                : memoryContext.getActiveSummaries().stream()
                .map(summary -> summary.getContent())
                .filter(content -> content != null && !content.isBlank())
                .reduce((left, right) -> left + "\n- " + right)
                .map(content -> "- " + content)
                .orElse("");
        if (!summaryContent.isBlank()) {
            messages.add(Msg.builder()
                    .role(MsgRole.SYSTEM)
                    .textContent("以下是当前会话仍在生效的历史摘要，请结合它继续对话：\n" + summaryContent)
                    .build());
        }

        if (memoryContext.getRecentRounds() != null) {
            for (MemoryRoundDTO round : memoryContext.getRecentRounds()) {
                if (round.getUserMessage() != null && !round.getUserMessage().isBlank()) {
                    messages.add(Msg.builder()
                            .role(MsgRole.USER)
                            .textContent(round.getUserMessage())
                            .build());
                }
                if (round.getAssistantMessage() != null && !round.getAssistantMessage().isBlank()) {
                    messages.add(Msg.builder()
                            .role(MsgRole.ASSISTANT)
                            .textContent(round.getAssistantMessage())
                            .build());
                }
            }
        }
        if (userMsg.getContent() != null && !userMsg.getContent().isBlank()) {
            messages.add(Msg.builder()
                    .role(MsgRole.USER)
                    .textContent(userMsg.getContent())
                    .build());
        }

        return messages;
    }

    /**
     * 构建数据库回退上下文
     */
    private List<Msg> buildFallbackContextMessages(ChatSessionEntity session) {
        List<Msg> messages = new ArrayList<>();
        List<ChatMessageEntity> history = chatMessageInfra.listRecentMessages(session.getId(), MAX_CONTEXT_MESSAGES);
        for (ChatMessageEntity msg : history) {
            MsgRole role = switch (msg.getRole()) {
                case "user" -> MsgRole.USER;
                case "assistant" -> MsgRole.ASSISTANT;
                case "system" -> MsgRole.SYSTEM;
                case "tool" -> MsgRole.TOOL;
                default -> MsgRole.USER;
            };
            if (msg.getContent() != null && !msg.getContent().isBlank()) {
                messages.add(Msg.builder()
                        .role(role)
                        .textContent(msg.getContent())
                        .build());
            }
        }
        return messages;
    }

    private String buildRoundTranscript(String userMessage, String assistantMessage) {
        return "用户说：" + (userMessage == null ? "" : userMessage)
                + "\n\n助手回复：" + (assistantMessage == null ? "" : assistantMessage);
    }

    private void sendMemoryWarning(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .data(objectMapper.writeValueAsString(Map.of(
                            "type", "memory_warning",
                            "message", message
                    ))));
        } catch (IOException ioException) {
            log.warn("SSE 发送记忆告警失败", ioException);
        }
    }

    private MemoryConversationContextDTO buildConversationContextWithTimeout(SseEmitter emitter,
                                                                             ChatSessionEntity session,
                                                                             ChatMessageEntity userMsg) {
        try {
            return CompletableFuture
                    .supplyAsync(() -> withUserContext(session.getUserId(), () -> memoryApi.buildConversationContext(
                            session.getUserId(),
                            String.valueOf(session.getId()),
                            userMsg.getContent(),
                            DEFAULT_MEMORY_TOP_K
                    )))
                    .orTimeout(MEMORY_CONTEXT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .join();
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            log.warn("记忆上下文构建失败，回退到基础消息上下文: sessionId={}", session.getId(), cause);
            sendMemoryWarning(emitter, "记忆上下文加载失败，本轮按基础上下文继续");
            return null;
        } catch (Exception exception) {
            log.warn("记忆上下文构建超时，回退到基础消息上下文: sessionId={}", session.getId(), exception);
            sendMemoryWarning(emitter, "记忆上下文加载超时，本轮按基础上下文继续");
            return null;
        }
    }

    private boolean shouldUseDirectModelStream(ModelConfigEntity modelConfig) {
        String provider = modelConfig.getProvider();
        String baseUrl = modelConfig.getBaseUrl();
        String modelName = modelConfig.getName();
        return (provider != null && provider.contains("阿里"))
                || (baseUrl != null && baseUrl.contains("dashscope.aliyuncs.com"))
                || (modelName != null && modelName.toLowerCase().startsWith("qwen"));
    }

    private String normalizeChatCompletionUrl(String baseUrl) {
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        return normalized + "/chat/completions";
    }

    private String buildDirectSysPrompt(MemoryConversationContextDTO memoryContext) {
        StringBuilder builder = new StringBuilder("""
                你是 EvolveHub 智能助手。
                默认使用中文，回答简洁直接。
                如果用户要求只返回一句或固定文本，严格照做，不要补充解释。
                """);
        if (memoryContext != null && memoryContext.getProfileMarkdown() != null && !memoryContext.getProfileMarkdown().isBlank()) {
            builder.append("\n\n用户画像：\n").append(memoryContext.getProfileMarkdown());
        }
        if (memoryContext != null && memoryContext.getRecalledMemories() != null && !memoryContext.getRecalledMemories().isEmpty()) {
            builder.append("\n\n长期记忆：\n");
            memoryContext.getRecalledMemories().forEach(item -> {
                if (item != null && item.getContent() != null && !item.getContent().isBlank()) {
                    builder.append("- ").append(item.getContent()).append('\n');
                }
            });
        }
        return builder.toString();
    }

    private String extractDirectCompletionText(String responseBody) throws IOException {
        Map<?, ?> payload = objectMapper.readValue(responseBody, Map.class);
        Object choicesObject = payload.get("choices");
        if (!(choicesObject instanceof List<?> choices) || choices.isEmpty()) {
            return "";
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            return "";
        }
        Object messageObject = choiceMap.get("message");
        if (!(messageObject instanceof Map<?, ?> messageMap)) {
            return "";
        }
        Object contentObject = messageMap.get("content");
        return contentObject == null ? "" : String.valueOf(contentObject);
    }

    private String toOpenAiRole(MsgRole role) {
        return switch (role) {
            case SYSTEM -> "system";
            case ASSISTANT -> "assistant";
            case TOOL -> "tool";
            default -> "user";
        };
    }

    private String extractTextFromResponse(ChatResponse response) {
        if (response == null || response.getContent() == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (ContentBlock block : response.getContent()) {
            if (block instanceof TextBlock textBlock) {
                builder.append(textBlock.getText());
            }
        }
        return builder.isEmpty() ? null : builder.toString();
    }

    private <T> T withUserContext(Long userId, Supplier<T> action) {
        Long previousUserId = CurrentUserHolder.getUserId();
        try {
            CurrentUserHolder.set(userId);
            return action.get();
        } finally {
            if (previousUserId == null) {
                CurrentUserHolder.clear();
            } else {
                CurrentUserHolder.set(previousUserId);
            }
        }
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
