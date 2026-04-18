package org.evolve.aiplatform.memory.application.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.common.base.CurrentUserHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Agent 记忆工具集
 * <p>
 * 通过 AgentScope @Tool 注解暴露给 ReActAgent，使 Agent 能主动检索和保存长期记忆。
 * 每次对话构建 Agent 时，按当前用户/会话实例化并注册到 Toolkit。
 * </p>
 *
 * @author zhao
 */
public record MemoryTools(MemoryApi memoryApi, Long userId, Long sessionId) {

    /**
     * @param memoryApi         Memory 统一门面
     * @param userId            当前用户 ID
     * @param sessionId         当前会话 ID
     */
    public MemoryTools {
    }

    /**
     * 检索长期记忆
     * <p>
     * Agent 在需要了解用户的偏好、习惯或历史信息时调用此工具。
     * 基于语义相似度从 Milvus 向量库中检索最相关的记忆条目。
     * </p>
     *
     * @param query 检索关键词或问题
     * @param topK  返回条数（默认 5）
     * @return 记忆文本列表，按相关性排序
     */
    @Tool(name = "recall_memory", description = "检索关于当前用户的长期记忆。当你需要了解用户的偏好、习惯、个人信息或过往交流记录时使用此工具。")
    public List<String> recallMemory(
            @ToolParam(name = "query", description = "检索关键词或语义查询文本") String query,
            @ToolParam(name = "top_k", description = "返回条数，默认 5", required = false) Integer topK) {
        int k = (topK != null && topK > 0) ? topK : 5;
        return withUserContext(() -> memoryApi.recallMemoryVectors(userId, query, k).stream()
                .map(result -> result.getContent())
                .toList());
    }

    /**
     * 保存长期记忆
     * <p>
     * Agent 在对话中识别到用户的重要偏好、习惯或个人信息时，主动调用此工具进行记忆。
     * 写入前会自动去重（相似度 > 0.95 则跳过）。
     * </p>
     *
     * @param content 要记忆的内容（一句简洁的陈述句）
     * @return 保存结果描述
     */
    @Tool(name = "save_memory", description = "保存一条关于当前用户的长期记忆。当用户透露了重要的偏好、习惯、个人信息或做出重要决策时使用此工具。内容应为一句简洁的陈述句。")
    public String saveMemory(
            @ToolParam(name = "content", description = "要记忆的内容，一句简洁的陈述句") String content) {
        try {
            withUserContext(() -> {
                memoryApi.saveConversationMemory(userId, sessionId, content, BigDecimal.valueOf(0.8D));
                return null;
            });
            return "记忆已保存：" + content;
        } catch (Exception e) {
            try {
                withUserContext(() -> memoryApi.upsertMemoryStructuredItem(new MemoryStructuredItemDTO(
                        userId,
                        buildMemoryKey(content),
                        MemoryConstants.MEMORY_TYPE_FACT,
                        content,
                        BigDecimal.valueOf(0.8D)
                )));
                return "记忆已保存（结构化兜底）：" + content;
            } catch (Exception fallbackException) {
                return "记忆保存失败：" + fallbackException.getMessage();
            }
        }
    }

    private String buildMemoryKey(String content) {
        return sessionId + "-tool-" + UUID.nameUUIDFromBytes(String.valueOf(content).getBytes());
    }

    private <T> T withUserContext(Supplier<T> action) {
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
}
