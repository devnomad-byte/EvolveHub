package org.evolve.aiplatform.config;

import io.agentscope.core.rag.exception.VectorStoreException;
import io.agentscope.core.rag.store.MilvusStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库配置
 * <p>
 * 使用 AgentScope 内置的 MilvusStore 连接 Zilliz Cloud，用于长期记忆的向量存储与检索。
 * </p>
 *
 * @author zhao
 */
@Configuration
public class MilvusConfig {

    private static final Logger log = LoggerFactory.getLogger(MilvusConfig.class);

    @Value("${milvus.uri}")
    private String uri;

    @Value("${milvus.token}")
    private String token;

    @Value("${milvus.collection-name}")
    private String collectionName;

    @Value("${milvus.dimensions}")
    private int dimensions;

    @Bean
    public MilvusStore milvusStore() throws VectorStoreException {
        log.info("初始化 MilvusStore: uri={}, collection={}, dimensions={}", uri, collectionName, dimensions);
        return MilvusStore.builder()
                .uri(uri)
                .token(token)
                .collectionName(collectionName)
                .dimensions(dimensions)
                .build();
    }
}
