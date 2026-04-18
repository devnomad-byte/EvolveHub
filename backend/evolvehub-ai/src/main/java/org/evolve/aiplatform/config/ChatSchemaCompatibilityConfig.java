package org.evolve.aiplatform.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 对话表结构兼容配置
 *
 * 用于兼容旧版数据库初始化脚本缺失的对话冗余字段，
 * 保证聊天主链在本地依赖环境下可以直接运行。
 *
 * @author TellyJiang
 * @since 2026-04-18
 */
@Configuration
public class ChatSchemaCompatibilityConfig {

    /**
     * 启动时补齐对话相关缺失列与索引
     *
     * @param jdbcTemplate JDBC 模板
     * @return 启动执行器
     * @author TellyJiang
     * @since 2026-04-18
     */
    @Bean
    public ApplicationRunner chatSchemaCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("ALTER TABLE eh_chat_session ADD COLUMN IF NOT EXISTS dept_id BIGINT");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_session_dept_id ON eh_chat_session(dept_id)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_session_create_by ON eh_chat_session(create_by)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_session_create_time ON eh_chat_session(create_time DESC)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_session_deleted ON eh_chat_session(deleted)");

            jdbcTemplate.execute("ALTER TABLE eh_chat_token_usage ADD COLUMN IF NOT EXISTS dept_id BIGINT");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_token_usage_user_date ON eh_chat_token_usage(user_id, usage_date DESC)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_token_usage_dept_date ON eh_chat_token_usage(dept_id, usage_date DESC)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_token_usage_create_by ON eh_chat_token_usage(create_by)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_token_usage_deleted ON eh_chat_token_usage(deleted)");
        };
    }
}
