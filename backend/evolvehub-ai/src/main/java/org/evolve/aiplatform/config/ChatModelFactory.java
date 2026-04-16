package org.evolve.aiplatform.config;

import io.agentscope.core.model.AnthropicChatModel;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.OpenAIChatModel;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 聊天模型工厂
 * <p>
 * 根据模型配置（provider / baseUrl / apiKey / modelName）动态构建 AgentScope ChatModel 实例。
 * 每次调用都会创建新实例，保证线程安全（AgentScope Model 有状态，不可共享）。
 * </p>
 * <p>
 * 支持的 provider 类型：
 * <ul>
 *   <li>Anthropic → AnthropicChatModel</li>
 *   <li>其他（OpenAI / DeepSeek / 阿里云 / 硅基流动 / Groq / Ollama 等 OpenAI 兼容） → OpenAIChatModel</li>
 * </ul>
 * </p>
 *
 * @author zhao
 */
@Component
public class ChatModelFactory {

    /**
     * 使用 Anthropic 原生协议的 provider 集合（不区分大小写匹配）
     */
    private static final Set<String> ANTHROPIC_PROVIDERS = Set.of("anthropic");

    /**
     * 根据模型配置构建 ChatModel 实例
     *
     * @param config 模型配置实体
     * @return ChatModelBase 实例（OpenAIChatModel 或 AnthropicChatModel）
     */
    public ChatModelBase createModel(ModelConfigEntity config) {
        String provider = config.getProvider();
        if (provider != null && ANTHROPIC_PROVIDERS.contains(provider.toLowerCase())) {
            return buildAnthropicModel(config);
        }
        return buildOpenAIModel(config);
    }

    /**
     * 构建 OpenAI 兼容模型（适用于绝大多数 provider）
     */
    private OpenAIChatModel buildOpenAIModel(ModelConfigEntity config) {
        OpenAIChatModel.Builder builder = OpenAIChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getName())
                .stream(true);
        if (config.getBaseUrl() != null && !config.getBaseUrl().isBlank()) {
            builder.baseUrl(config.getBaseUrl());
        }
        return builder.build();
    }

    /**
     * 构建 Anthropic 原生协议模型
     */
    private AnthropicChatModel buildAnthropicModel(ModelConfigEntity config) {
        AnthropicChatModel.Builder builder = AnthropicChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getName())
                .stream(true);
        if (config.getBaseUrl() != null && !config.getBaseUrl().isBlank()) {
            builder.baseUrl(config.getBaseUrl());
        }
        return builder.build();
    }
}
