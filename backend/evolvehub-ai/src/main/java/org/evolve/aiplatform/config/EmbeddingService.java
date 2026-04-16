package org.evolve.aiplatform.config;

import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.embedding.openai.OpenAITextEmbedding;
import io.agentscope.core.message.TextBlock;
import jakarta.annotation.Resource;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 向量嵌入服务
 * <p>
 * 读取系统级 EMBEDDING 模型配置（eh_model_config 中 scope=SYSTEM, model_type=embedding），
 * 通过 AgentScope OpenAITextEmbedding（兼容所有 OpenAI 协议的 Embedding 端点）将文本向量化。
 * </p>
 *
 * @author zhao
 */
@Component
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Resource
    private ModelConfigInfra modelConfigInfra;

    /**
     * 缓存的 EmbeddingModel 实例（懒加载）
     */
    private volatile EmbeddingModel embeddingModel;

    /**
     * 将文本向量化
     *
     * @param text 待向量化的文本
     * @return 向量数组
     */
    public Mono<double[]> embed(String text) {
        return getOrCreateModel().embed(TextBlock.builder().text(text).build());
    }

    /**
     * 同步方式获取向量（阻塞等待）
     *
     * @param text 待向量化的文本
     * @return 向量数组
     */
    public double[] embedSync(String text) {
        return embed(text).block();
    }

    /**
     * 获取向量维度
     */
    public int getDimensions() {
        return getOrCreateModel().getDimensions();
    }

    /**
     * 懒加载 + 双重检查锁构建 EmbeddingModel
     */
    private EmbeddingModel getOrCreateModel() {
        if (embeddingModel == null) {
            synchronized (this) {
                if (embeddingModel == null) {
                    ModelConfigEntity config = modelConfigInfra.getSystemEmbeddingModel();
                    if (config == null) {
                        throw new IllegalStateException("系统级 EMBEDDING 模型未配置，请在 eh_model_config 中添加 scope=SYSTEM, model_type=embedding 的记录");
                    }
                    log.info("初始化 EmbeddingModel: name={}, baseUrl={}", config.getName(), config.getBaseUrl());
                    OpenAITextEmbedding.Builder builder = OpenAITextEmbedding.builder()
                            .apiKey(config.getApiKey())
                            .modelName(config.getName())
                            .dimensions(1536);
                    if (config.getBaseUrl() != null && !config.getBaseUrl().isBlank()) {
                        builder.baseUrl(config.getBaseUrl());
                    }
                    this.embeddingModel = builder.build();
                }
            }
        }
        return embeddingModel;
    }
}
