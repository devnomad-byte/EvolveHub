package org.evolve.aiplatform.memory.application.service;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryConversationContextDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryManagedItemVO;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;

import java.math.BigDecimal;

import java.util.List;

/**
 * Memory 模块对外公开接口
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
public interface MemoryApi {

    /**
     * 初始化用户 Memory 画像
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    String initializeMemoryProfile(Long userId);

    /**
     * 获取用户 Memory 画像
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    String getMemoryProfile(Long userId);

    /**
     * 更新用户 Memory 画像
     *
     * @param profile 画像对象
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    String updateMemoryProfile(MemoryProfileDTO profile);

    /**
     * 直接保存用户 Memory 画像 Markdown
     *
     * @param userId 用户 ID
     * @param markdownContent Markdown 内容
     * @return 保存后的 Markdown 内容
     * @author TellyJiang
     * @since 2026-04-15
     */
    String saveMemoryProfileMarkdown(Long userId, String markdownContent);

    /**
     * 追加用户 Memory 画像 Markdown
     *
     * @param userId 用户 ID
     * @param content 要追加的内容
     * @return 更新后的 Markdown 内容
     * @author TellyJiang
     * @since 2026-04-15
     */
    String appendMemoryProfileMarkdown(Long userId, String content);

    /**
     * 保存结构化记忆
     *
     * @param item 结构化记忆
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    MemoryStructuredItemDTO upsertMemoryStructuredItem(MemoryStructuredItemDTO item);

    /**
     * 查询结构化记忆
     *
     * @param userId 用户 ID
     * @param memoryType 记忆类型
     * @return 结构化记忆列表
     * @author TellyJiang
     * @since 2026-04-11
     */
    List<MemoryStructuredItemDTO> listMemoryStructuredItems(Long userId, String memoryType);

    /**
     * 保存向量记忆
     *
     * @param item 结构化记忆
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    MemoryStructuredItemDTO saveMemoryVector(MemoryStructuredItemDTO item);

    /**
     * 保存对话长期记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param content 记忆内容
     * @param importance 重要性
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-15
     */
    MemoryStructuredItemDTO saveConversationMemory(Long userId, Long sessionId, String content, BigDecimal importance);

    /**
     * 召回长期记忆
     *
     * @param userId 用户 ID
     * @param query 查询语句
     * @param topK 召回数量
     * @return 召回结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    List<MemoryRecallResultVO> recallMemoryVectors(Long userId, String query, Integer topK);

    /**
     * 查询可管理记忆
     *
     * @param userId 用户 ID
     * @return 可管理记忆列表
     * @author TellyJiang
     * @since 2026-04-15
     */
    List<MemoryManagedItemVO> listManagedMemories(Long userId);

    /**
     * 删除可管理记忆
     *
     * @param userId 用户 ID
     * @param memoryId 记忆主键
     * @author TellyJiang
     * @since 2026-04-15
     */
    void deleteManagedMemory(Long userId, Long memoryId);

    /**
     * 构建对话上下文
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param query 当前用户输入
     * @param topK 长期记忆召回数量
     * @return 上下文装配结果
     * @author TellyJiang
     * @since 2026-04-16
     */
    MemoryConversationContextDTO buildConversationContext(Long userId, String sessionId, String query, Integer topK);

    /**
     * 提交完整问答回合
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param userMessage 用户消息
     * @param assistantMessage 助手回复
     * @param modelName 模型名称
     * @return 最新会话状态
     * @author TellyJiang
     * @since 2026-04-16
     */
    MemorySessionDTO commitConversationRound(Long userId, String sessionId, String userMessage, String assistantMessage, String modelName);

    /**
     * 加载会话记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return 会话记忆
     * @author TellyJiang
     * @since 2026-04-11
     */
    MemorySessionDTO loadMemorySession(Long userId, String sessionId);

    /**
     * 追加会话记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @param message 消息内容
     * @param modelName 模型名称
     * @return 会话记忆
     * @author TellyJiang
     * @since 2026-04-11
     */
    MemorySessionDTO appendMemorySession(Long userId, String sessionId, String message, String modelName);

    /**
     * 提取会话记忆
     *
     * @param request 提取请求
     * @return 提取结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    MemoryExtractionResultDTO extractMemoryFromConversation(MemoryExtractionRequestDTO request);

    /**
     * 异步提取会话记忆
     *
     * @param request 提取请求
     * @author TellyJiang
     * @since 2026-04-15
     */
    void extractMemoryFromConversationAsync(MemoryExtractionRequestDTO request);
}
