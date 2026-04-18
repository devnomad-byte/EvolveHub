package org.evolve.aiplatform.memory.infrastructure.config;

import io.agentscope.core.session.Session;
import org.evolve.aiplatform.memory.framework.agentscope.AgentScopeRedisSession;
import org.evolve.aiplatform.memory.framework.agentscope.MemoryRuntimeJsonCodec;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryImportanceUtil;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryMarkdownRenderer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

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

    /**
     * 初始化 memory 结构化记忆表。
     *
     * @param jdbcTemplate JDBC 模板
     * @return 启动执行器
     * @author TellyJiang
     * @since 2026-04-18
     */
    @Bean
    public ApplicationRunner memorySchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS user_memory (
                        id BIGINT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        dept_id BIGINT,
                        memory_key VARCHAR(128) NOT NULL,
                        memory_type VARCHAR(32) NOT NULL,
                        content TEXT NOT NULL,
                        importance NUMERIC(5, 3) DEFAULT 0.000,
                        vector_doc_id VARCHAR(128),
                        session_id VARCHAR(64),
                        create_by BIGINT DEFAULT 0,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted INTEGER NOT NULL DEFAULT 0
                    )
                    """);
            jdbcTemplate.execute("ALTER TABLE user_memory ADD COLUMN IF NOT EXISTS dept_id BIGINT");
            jdbcTemplate.execute("ALTER TABLE user_memory ADD COLUMN IF NOT EXISTS memory_type VARCHAR(32)");
            jdbcTemplate.execute("ALTER TABLE user_memory ADD COLUMN IF NOT EXISTS vector_doc_id VARCHAR(128)");
            jdbcTemplate.execute("ALTER TABLE user_memory ADD COLUMN IF NOT EXISTS session_id VARCHAR(64)");
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_user_memory_user_key ON user_memory (user_id, memory_key)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_memory_user_type_update ON user_memory (user_id, memory_type, update_time DESC)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_memory_user_vector_doc ON user_memory (user_id, vector_doc_id)");
        };
    }

}
