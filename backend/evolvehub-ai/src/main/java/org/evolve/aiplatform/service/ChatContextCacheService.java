package org.evolve.aiplatform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.evolve.domain.resource.infra.ChatSessionInfra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 对话上下文 Redis 缓存服务（Hash 结构）
 * <p>
 * Redis key = evolvehub:session:{userId}:{sessionId}，Hash 内含两个 field：
 * <ul>
 *   <li><b>summary</b>：LLM 压缩后的摘要（轻量，每次对话必读，检索快）</li>
 *   <li><b>detail</b>：窗口外旧消息原始内容 JSON（压缩摘要时直接取，免回查 DB）</li>
 * </ul>
 * SQL（eh_chat_session.context_summary）作为持久化层。
 * </p>
 *
 * @author zhao
 */
@Service
public class ChatContextCacheService {

    private static final Logger log = LoggerFactory.getLogger(ChatContextCacheService.class);

    /**
     * Redis Hash key 前缀，完整 key = evolvehub:session:{userId}:{sessionId}
     */
    private static final String KEY_PREFIX = "evolvehub:session:";

    /**
     * Hash field：压缩摘要
     */
    private static final String FIELD_SUMMARY = "summary";

    /**
     * Hash field：窗口外旧消息原始内容（JSON 数组）
     */
    private static final String FIELD_DETAIL = "detail";

    /**
     * 缓存 TTL（2 小时无活动自动过期）
     */
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 构建 Redis Key
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return evolvehub:session:{userId}:{sessionId}
     */
    private String buildKey(Long userId, Long sessionId) {
        return KEY_PREFIX + userId + ":" + sessionId;
    }

    // ======================== summary（摘要） ========================

    /**
     * 写入摘要到 Redis（同时刷新 TTL）
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param summary   摘要文本
     */
    public void cacheSummary(Long userId, Long sessionId, String summary) {
        try {
            String key = buildKey(userId, sessionId);
            if (summary == null || summary.isBlank()) {
                stringRedisTemplate.opsForHash().delete(key, FIELD_SUMMARY);
                return;
            }
            stringRedisTemplate.opsForHash().put(key, FIELD_SUMMARY, summary);
            stringRedisTemplate.expire(key, CACHE_TTL);
        } catch (Exception e) {
            log.warn("摘要写入 Redis 失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 读取上下文摘要（优先 Redis，miss 回源 DB 并回填）
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 摘要文本，不存在则返回 null
     */
    public String getSummary(Long userId, Long sessionId) {
        String key = buildKey(userId, sessionId);
        try {
            Object cached = stringRedisTemplate.opsForHash().get(key, FIELD_SUMMARY);
            if (cached != null) {
                stringRedisTemplate.expire(key, CACHE_TTL);
                return cached.toString();
            }
        } catch (Exception e) {
            log.warn("从 Redis 读取摘要失败，回源 DB: sessionId={}", sessionId, e);
        }

        // 缓存 miss，回源 DB
        ChatSessionEntity session = chatSessionInfra.getById(sessionId);
        if (session != null && session.getContextSummary() != null && !session.getContextSummary().isBlank()) {
            cacheSummary(userId, sessionId, session.getContextSummary());
            return session.getContextSummary();
        }
        return null;
    }

    // ======================== detail（窗口外旧消息原文） ========================

    /**
     * 缓存窗口外旧消息到 Redis（压缩摘要后调用，供下次压缩时直接取）
     * <p>
     * 存储格式：JSON 数组，每个元素包含 role 和 content。
     * </p>
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param messages  窗口外的旧消息列表
     */
    public void cacheDetail(Long userId, Long sessionId, List<ChatMessageEntity> messages) {
        try {
            String key = buildKey(userId, sessionId);
            List<Map<String, String>> simplified = messages.stream()
                    .map(m -> Map.of("role", m.getRole(), "content",
                            m.getContent() != null ? m.getContent() : ""))
                    .toList();
            String json = objectMapper.writeValueAsString(simplified);
            stringRedisTemplate.opsForHash().put(key, FIELD_DETAIL, json);
            stringRedisTemplate.expire(key, CACHE_TTL);
        } catch (Exception e) {
            log.warn("旧消息缓存写入 Redis 失败: sessionId={}", sessionId, e);
        }
    }

    /**
     * 读取缓存中的窗口外旧消息
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 旧消息列表（Map 含 role/content），缓存不存在返回 null（调用方需回查 DB）
     */
    public List<Map<String, String>> getDetail(Long userId, Long sessionId) {
        String key = buildKey(userId, sessionId);
        try {
            Object cached = stringRedisTemplate.opsForHash().get(key, FIELD_DETAIL);
            if (cached != null) {
                stringRedisTemplate.expire(key, CACHE_TTL);
                return objectMapper.readValue(cached.toString(),
                        new TypeReference<List<Map<String, String>>>() {});
            }
        } catch (Exception e) {
            log.warn("从 Redis 读取旧消息缓存失败: sessionId={}", sessionId, e);
        }
        return null;
    }

    // ======================== 清理 ========================

    /**
     * 删除会话的全部缓存（会话删除时调用）
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     */
    public void evict(Long userId, Long sessionId) {
        try {
            stringRedisTemplate.delete(buildKey(userId, sessionId));
        } catch (Exception e) {
            log.warn("删除会话缓存失败: sessionId={}", sessionId, e);
        }
    }
}
