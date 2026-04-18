package org.evolve.aiplatform.memory.domain.constant;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Memory 模块常量
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
public final class MemoryConstants {

    /*
     * AgentScope Redis Session 键前缀
     */
    public static final String AGENTSCOPE_MEMORY_SESSION_PREFIX = "evolvehub:agentscope:memory";

    /*
     * 默认 Memory Bucket
     */
    public static final String MEMORY_DEFAULT_BUCKET = "evolvehub-memory";

    /*
     * 用户画像对象类型
     */
    public static final String MEMORY_OBJECT_TYPE_PROFILE = "profile_markdown";

    /*
     * 会话 Transcript 对象类型
     */
    public static final String MEMORY_OBJECT_TYPE_TRANSCRIPT = "session_transcript";

    /*
     * 会话快照对象类型
     */
    public static final String MEMORY_OBJECT_TYPE_SESSION_SNAPSHOT = "session_snapshot";

    /*
     * 来源类型：消息
     */
    public static final String MEMORY_SOURCE_KIND_MESSAGE = "message";

    /*
     * 来源类型：结构化记忆
     */
    public static final String MEMORY_SOURCE_KIND_STRUCTURED = "structured";

    /*
     * 来源类型：显式向量记忆
     */
    public static final String MEMORY_SOURCE_KIND_VECTOR = "vector";

    /*
     * 来源类型：归档
     */
    public static final String MEMORY_SOURCE_KIND_ARCHIVE = "archive";

    /*
     * 会话激活状态
     */
    public static final String MEMORY_SESSION_STATUS_ACTIVE = "ACTIVE";

    /*
     * 默认向量模型 ID
     */
    public static final Long MEMORY_DEFAULT_EMBEDDING_MODEL_ID = 1L;

    /*
     * 用户 MEMORY.md 文件路径模板
     */
    public static final String MEMORY_PROFILE_PATH_TEMPLATE = "/users/%d/MEMORY.md";

    /*
     * 会话记忆 Redis Key 模板
     */
    public static final String MEMORY_SESSION_KEY_TEMPLATE = "evolvehub:session:%d:%s";

    /*
     * 默认会话记忆过期时间
     */
    public static final Duration MEMORY_SESSION_TTL = Duration.ofSeconds(1800);

    /*
     * 默认召回数量
     */
    public static final int MEMORY_DEFAULT_TOP_K = 3;

    /*
     * 默认滑动窗口保留轮数
     */
    public static final int MEMORY_DEFAULT_MAX_TURNS = 10;

    /*
     * 活动摘要未再次使用的沉睡回合数
     */
    public static final int MEMORY_ACTIVE_SUMMARY_SLEEP_ROUNDS = 3;

    /*
     * 记忆归类：长期摘要
     */
    public static final String MEMORY_KIND_SUMMARY = "summary";

    /*
     * 记忆归类：长期事实
     */
    public static final String MEMORY_KIND_FACT = "fact";

    /*
     * 重要性最小值
     */
    public static final BigDecimal MEMORY_IMPORTANCE_MIN = BigDecimal.ZERO;

    /*
     * 重要性最大值
     */
    public static final BigDecimal MEMORY_IMPORTANCE_MAX = BigDecimal.ONE;

    /*
     * 偏好类记忆类型
     */
    public static final String MEMORY_TYPE_PREFERENCE = "preference";

    /*
     * 事实类记忆类型
     */
    public static final String MEMORY_TYPE_FACT = "fact";

    /*
     * 工具配置类记忆类型
     */
    public static final String MEMORY_TYPE_TOOL_CONFIG = "tool_config";

    /*
     * 技能类记忆类型
     */
    public static final String MEMORY_TYPE_SKILL = "skill";

    /*
     * 支持的记忆类型列表
     */
    public static final List<String> MEMORY_SUPPORTED_TYPES = List.of(
            MEMORY_TYPE_PREFERENCE,
            MEMORY_TYPE_FACT,
            MEMORY_TYPE_TOOL_CONFIG,
            MEMORY_TYPE_SKILL
    );

    private MemoryConstants() {
    }
}
