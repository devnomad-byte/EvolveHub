package org.evolve.aiplatform.service;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.ChatResponse;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.entity.ChatMessageEntity;
import org.evolve.aiplatform.bean.entity.ChatSessionEntity;
import org.evolve.aiplatform.config.ChatModelFactory;
import org.evolve.aiplatform.infra.ChatMessageInfra;
import org.evolve.aiplatform.infra.ChatSessionInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 短期记忆压缩服务（上下文摘要）
 * <p>
 * 当会话消息数超过滑动窗口（20 条）时，将窗口外的旧消息用 LLM 压缩为一段摘要，
 * 存入 eh_chat_session.context_summary 字段。
 * </p>
 * <p>
 * 摘要是增量更新的：每次压缩时输入 = 旧摘要 + 新溢出的消息 → 输出新摘要。
 * 模型上下文组装时：system prompt → context_summary → 最近 20 条消息原文。
 * </p>
 *
 * @author zhao
 */
@Service
public class ContextSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ContextSummaryService.class);

    /**
     * 滑动窗口大小（与 SendMessageManager.MAX_CONTEXT_MESSAGES 保持一致）
     */
    private static final int WINDOW_SIZE = 20;

    /**
     * 单次压缩最多处理的溢出消息数（避免 prompt 过长）
     */
    private static final int MAX_OVERFLOW_BATCH = 30;

    /**
     * 摘要生成的系统提示词
     */
    private static final String SUMMARY_PROMPT = """
            你是一个对话摘要助手。你的任务是将对话历史压缩为一段简洁的摘要。
            
            要求：
            1. 保留对话中涉及的关键信息：用户需求、做出的决定、重要事实、讨论结论
            2. 去掉无意义的寒暄、重复内容、过程性细节
            3. 摘要长度控制在 200~500 字以内
            4. 使用第三人称描述，如"用户提到了..."、"助手建议了..."
            5. 只返回摘要文本，不要加标题或格式标记
            """;

    /**
     * 异步执行压缩的线程池
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Resource
    private ChatMessageInfra chatMessageInfra;

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private ChatModelFactory chatModelFactory;

    @Resource
    private ChatContextCacheService chatContextCacheService;

    /**
     * 异步检查并压缩上下文摘要
     * <p>
     * 如果会话消息数 > WINDOW_SIZE，触发摘要生成/更新。
     * </p>
     *
     * @param session     会话实体
     * @param modelConfig 模型配置（用于调用 LLM 生成摘要）
     */
    public void compressIfNeededAsync(ChatSessionEntity session, ModelConfigEntity modelConfig) {
        executor.execute(() -> {
            try {
                compressIfNeeded(session, modelConfig);
            } catch (Exception e) {
                log.error("上下文摘要压缩失败: sessionId={}", session.getId(), e);
            }
        });
    }

    /**
     * 同步检查并压缩
     * <p>
     * 优先从 Redis detail 字段取旧消息，miss 则回查 DB。
     * 压缩完成后同时写入 Redis（summary + detail）和 SQL。
     * </p>
     */
    private void compressIfNeeded(ChatSessionEntity session, ModelConfigEntity modelConfig) {
        long totalCount = chatMessageInfra.countBySessionId(session.getId());
        if (totalCount <= WINDOW_SIZE) {
            return;
        }

        // 组装待压缩内容 = 旧摘要 + 溢出消息
        StringBuilder toCompress = new StringBuilder();

        // 旧摘要：优先 Redis，miss 回 session 字段
        String existingSummary = chatContextCacheService.getSummary(session.getUserId(), session.getId());
        if (existingSummary != null && !existingSummary.isBlank()) {
            toCompress.append("【已有摘要】\n").append(existingSummary).append("\n\n");
        }

        // 窗口外旧消息：优先从 Redis detail 取，miss 回查 DB
        List<ChatMessageEntity> overflowFromDb = null;
        List<Map<String, String>> cachedDetail = chatContextCacheService.getDetail(session.getUserId(), session.getId());

        toCompress.append("【需要压缩的新对话】\n");
        if (cachedDetail != null && !cachedDetail.isEmpty()) {
            // Redis 命中
            for (Map<String, String> msg : cachedDetail) {
                String roleLabel = mapRoleLabel(msg.get("role"));
                toCompress.append(roleLabel).append("：").append(msg.get("content")).append("\n");
            }
        } else {
            // Redis miss，回查 DB
            overflowFromDb = chatMessageInfra.listOverflowMessages(
                    session.getId(), WINDOW_SIZE, MAX_OVERFLOW_BATCH);
            if (overflowFromDb.isEmpty()) {
                return;
            }
            for (ChatMessageEntity msg : overflowFromDb) {
                String roleLabel = mapRoleLabel(msg.getRole());
                toCompress.append(roleLabel).append("：").append(msg.getContent()).append("\n");
            }
        }

        // 调用 LLM 生成摘要
        String newSummary = generateSummary(modelConfig, toCompress.toString());
        if (newSummary == null || newSummary.isBlank()) {
            log.warn("LLM 未返回有效摘要: sessionId={}", session.getId());
            return;
        }

        // 双写 Redis（summary + detail）+ SQL
        chatContextCacheService.cacheSummary(session.getUserId(), session.getId(), newSummary);
        if (overflowFromDb != null) {
            // 回查了 DB 的情况下，把旧消息也缓存到 Redis，供下次压缩用
            chatContextCacheService.cacheDetail(session.getUserId(), session.getId(), overflowFromDb);
        }
        ChatSessionEntity update = new ChatSessionEntity();
        update.setId(session.getId());
        update.setContextSummary(newSummary);
        chatSessionInfra.updateById(update);
        log.info("上下文摘要已更新: sessionId={}, summaryLength={}", session.getId(), newSummary.length());
    }

    /**
     * 角色标签映射
     */
    private String mapRoleLabel(String role) {
        if (role == null) return "未知";
        return switch (role) {
            case "user" -> "用户";
            case "assistant" -> "助手";
            case "system" -> "系统";
            case "tool" -> "工具";
            default -> role;
        };
    }

    /**
     * 调用 LLM 生成摘要
     */
    private String generateSummary(ModelConfigEntity modelConfig, String content) {
        try {
            ChatModelBase model = chatModelFactory.createModel(modelConfig);
            List<Msg> messages = List.of(
                    Msg.builder().role(MsgRole.SYSTEM).textContent(SUMMARY_PROMPT).build(),
                    Msg.builder().role(MsgRole.USER).textContent(content).build()
            );

            StringBuilder responseText = new StringBuilder();
            model.stream(messages, null, null)
                    .doOnNext(response -> {
                        String text = extractText(response);
                        if (text != null) {
                            responseText.append(text);
                        }
                    })
                    .blockLast();

            return responseText.toString().trim();
        } catch (Exception e) {
            log.error("LLM 摘要生成失败", e);
            return null;
        }
    }

    /**
     * 从 ChatResponse 中提取文本
     */
    private String extractText(ChatResponse response) {
        if (response.getContent() == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (var block : response.getContent()) {
            if (block instanceof TextBlock textBlock) {
                sb.append(textBlock.getText());
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }
}
