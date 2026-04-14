package org.evolve.aiplatform.service.agent;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.evolve.aiplatform.service.ChatMemoryService;

import java.util.List;

/**
 * Agent 记忆工具集
 * <p>
 * 通过 AgentScope @Tool 注解暴露给 ReActAgent，使 Agent 能主动检索和保存长期记忆。
 * 每次对话构建 Agent 时，按当前用户/会话实例化并注册到 Toolkit。
 * </p>
 *
 * @author zhao
 */
public record MemoryTools(ChatMemoryService chatMemoryService, Long userId, Long sessionId) {

    /**
     * @param chatMemoryService 长期记忆服务
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
        return chatMemoryService.retrieve(userId, query, k);
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
            chatMemoryService.saveMemoryWithDedup(userId, sessionId, content);
            return "记忆已保存：" + content;
        } catch (Exception e) {
            return "记忆保存失败：" + e.getMessage();
        }
    }
}
