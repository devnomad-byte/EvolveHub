package org.evolve.aiplatform.memory.infrastructure.config;

import io.agentscope.core.session.Session;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeRedisSession;
import org.evolve.aiplatform.memory.framework.agentscope.MemoryRuntimeJsonCodec;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryImportanceUtil;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryMarkdownRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Memory 模块配置
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
@Configuration
public class MemoryConfig {

    /**
     * 注册 Markdown 渲染器
     *
     * @return 渲染器
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Bean
    public MemoryMarkdownRenderer memoryMarkdownRenderer() {
        return new MemoryMarkdownRenderer();
    }

    /**
     * 注册重要性处理工具
     *
     * @return 工具对象
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Bean
    public MemoryImportanceUtil memoryImportanceUtil() {
        return new MemoryImportanceUtil();
    }

    /**
     * 注册 AgentScope Session
     *
     * @param stringRedisTemplate Redis 模板
     * @param memoryRuntimeJsonCodec JSON 编解码器
     * @return AgentScope Session
     * @author TellyJiang
     * @since 2026-04-12
     */
    @Bean
    public Session agentScopeSession(StringRedisTemplate stringRedisTemplate,
                                     MemoryRuntimeJsonCodec memoryRuntimeJsonCodec) {
        return new AgentScopeRedisSession(
                stringRedisTemplate,
                memoryRuntimeJsonCodec,
                MemoryConstants.AGENTSCOPE_MEMORY_SESSION_PREFIX
        );
    }

}
