package org.evolve.aiplatform.service.agent;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.tool.Toolkit;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.config.ChatModelFactory;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Component;

/**
 * 对话 Agent 工厂
 * <p>
 * 每次对话请求时，根据模型配置和用户权限构建一个完整的 ReActAgent 实例。
 * Agent = ChatModel + Toolkit（记忆工具 + MCP + Skill）+ 系统提示词。
 * </p>
 *
 * @author zhao
 */
@Component
public class ChatAgentFactory {

    /**
     * Agent ReAct 循环最大迭代次数
     */
    private static final int DEFAULT_MAX_ITERS = 10;

    @Resource
    private ChatModelFactory chatModelFactory;

    @Resource
    private UserToolkitLoader userToolkitLoader;

    /**
     * 构建 ReActAgent 实例
     *
     * @param modelConfig 模型配置
     * @param userId      当前用户 ID
     * @param sessionId   当前会话 ID
     * @param sysPrompt   系统提示词
     * @return 组装完成的 ReActAgent
     */
    public ReActAgent createAgent(ModelConfigEntity modelConfig, Long userId, Long sessionId, String sysPrompt) {
        // 1. 构建 ChatModel
        ChatModelBase model = chatModelFactory.createModel(modelConfig);

        // 2. 加载用户可用的工具集（记忆 + MCP + Skill）
        Toolkit toolkit = userToolkitLoader.loadToolkit(userId, sessionId);

        // 3. 组装 ReActAgent
        return ReActAgent.builder()
                .name("chat-agent-" + sessionId)
                .sysPrompt(sysPrompt)
                .model(model)
                .toolkit(toolkit)
                .maxIters(DEFAULT_MAX_ITERS)
                .build();
    }

    /**
     * 构建 StreamOptions（用于 agent.stream() 调用）
     * <p>
     * 订阅所有事件类型：REASONING（推理）+ TOOL_RESULT（工具调用结果）+ AGENT_RESULT（最终结果）。
     * 增量模式（incremental=true），每个 Event 只包含新增的内容片段。
     * </p>
     *
     * @return StreamOptions 配置
     */
    public StreamOptions buildStreamOptions() {
        return StreamOptions.builder()
                .incremental(true)
                .includeReasoningChunk(true)
                .includeActingChunk(true)
                .build();
    }

    public ChatModelFactory getChatModelFactory() {
        return chatModelFactory;
    }
}
