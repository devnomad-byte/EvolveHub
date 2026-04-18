package org.evolve.aiplatform.memory.application.service.impl;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryConversationContextDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemorySessionDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryManagedItemVO;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;
import org.evolve.aiplatform.memory.application.service.MemoryExtractionService;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.memory.application.service.MemoryProfileService;
import org.evolve.aiplatform.memory.application.service.MemorySessionService;
import org.evolve.aiplatform.memory.application.service.MemoryStructuredService;
import org.evolve.aiplatform.memory.application.service.MemoryVectorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Memory 模块统一门面实现
 * 
 * 该类作为 memory 子系统的总入口，统一编排访问控制、画像、结构化记忆、向量记忆、
 * 会话记忆与记忆提取等核心能力。外部调用方只依赖 MemoryApi，即可隔离具体实现细节。
 * 
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Service
public class MemoryApiImpl implements MemoryApi {

    @Resource
    private MemoryAccessGuard memoryAccessGuard;

    @Resource(name = "memoryProfileServiceImpl")
    private MemoryProfileService memoryProfileService;

    @Resource(name = "memoryStructuredServiceImpl")
    private MemoryStructuredService memoryStructuredService;

    @Resource(name = "memoryVectorServiceImpl")
    private MemoryVectorService memoryVectorService;

    @Resource(name = "memorySessionServiceImpl")
    private MemorySessionService memorySessionService;

    @Resource(name = "memoryExtractionServiceImpl")
    private MemoryExtractionService memoryExtractionService;

    /**
     * 初始化用户 Memory 画像
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public String initializeMemoryProfile(Long userId) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memoryProfileService.initializeMemoryProfile(userId);
    }

    /**
     * 获取用户 Memory 画像
     *
     * @param userId 用户 ID
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public String getMemoryProfile(Long userId) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memoryProfileService.getMemoryProfile(userId);
    }

    /**
     * 更新用户 Memory 画像
     *
     * @param profile 画像对象
     * @return Markdown 内容
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public String updateMemoryProfile(MemoryProfileDTO profile) {
        memoryAccessGuard.assertCanUpdateProfile(profile);
        return memoryProfileService.updateMemoryProfile(profile);
    }

    /**
     * 直接保存用户画像 Markdown
     *
     * @param userId 用户 ID
     * @param markdownContent Markdown 内容
     * @return 保存后的 Markdown 内容
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public String saveMemoryProfileMarkdown(Long userId, String markdownContent) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memoryProfileService.saveMemoryProfileMarkdown(userId, markdownContent);
    }

    /**
     * 追加用户画像 Markdown
     *
     * @param userId 用户 ID
     * @param content 要追加的内容
     * @return 更新后的 Markdown 内容
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public String appendMemoryProfileMarkdown(Long userId, String content) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memoryProfileService.appendMemoryProfileMarkdown(userId, content);
    }

    /**
     * 保存结构化记忆
     *
     * @param item 结构化记忆
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public MemoryStructuredItemDTO upsertMemoryStructuredItem(MemoryStructuredItemDTO item) {
        memoryAccessGuard.assertCanWriteStructuredItem(item);
        return memoryStructuredService.upsertMemoryStructuredItem(item);
    }

    /**
     * 查询结构化记忆
     *
     * @param userId 用户 ID
     * @param memoryType 记忆类型
     * @return 结构化记忆列表
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public List<MemoryStructuredItemDTO> listMemoryStructuredItems(Long userId, String memoryType) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memoryStructuredService.listMemoryStructuredItems(userId, memoryType);
    }

    /**
     * 保存向量记忆
     *
     * @param item 结构化记忆
     * @return 保存结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public MemoryStructuredItemDTO saveMemoryVector(MemoryStructuredItemDTO item) {
        memoryAccessGuard.assertCanWriteStructuredItem(item);
        return memoryVectorService.saveMemoryVector(item);
    }

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
    @Override
    public MemoryStructuredItemDTO saveConversationMemory(Long userId, Long sessionId, String content, BigDecimal importance) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memoryVectorService.saveConversationMemory(userId, sessionId, content, importance);
    }

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
    @Override
    public List<MemoryRecallResultVO> recallMemoryVectors(Long userId, String query, Integer topK) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memoryVectorService.recallMemoryVectors(userId, query, topK);
    }

    /**
     * 查询可管理记忆
     *
     * @param userId 用户 ID
     * @return 可管理记忆列表
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public List<MemoryManagedItemVO> listManagedMemories(Long userId) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memoryVectorService.listManagedMemories(userId);
    }

    /**
     * 删除可管理记忆
     *
     * @param userId 用户 ID
     * @param memoryId 记忆主键
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public void deleteManagedMemory(Long userId, Long memoryId) {
        memoryAccessGuard.assertCanAccessUser(userId);
        memoryVectorService.deleteManagedMemory(userId, memoryId);
    }

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
    @Override
    public MemoryConversationContextDTO buildConversationContext(Long userId, String sessionId, String query, Integer topK) {
        memoryAccessGuard.assertCanAccessUser(userId);
        List<MemoryRecallResultVO> recalledMemories = memoryVectorService.recallMemoryVectors(userId, query, topK);
        MemoryConversationContextDTO context = memorySessionService.buildConversationContext(
                userId,
                sessionId,
                query,
                recalledMemories
        );
        context.setProfileMarkdown(memoryProfileService.getMemoryProfile(userId));
        context.setRecalledMemories(recalledMemories);
        return context;
    }

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
    @Override
    public MemorySessionDTO commitConversationRound(Long userId, String sessionId, String userMessage, String assistantMessage, String modelName) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memorySessionService.commitConversationRound(userId, sessionId, userMessage, assistantMessage, modelName);
    }

    /**
     * 加载会话记忆
     *
     * @param userId 用户 ID
     * @param sessionId 会话 ID
     * @return 会话记忆
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public MemorySessionDTO loadMemorySession(Long userId, String sessionId) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memorySessionService.loadMemorySession(userId, sessionId);
    }

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
    @Override
    public MemorySessionDTO appendMemorySession(Long userId, String sessionId, String message, String modelName) {
        memoryAccessGuard.assertCanAccessUser(userId);
        return memorySessionService.appendMemorySession(userId, sessionId, message, modelName);
    }

    /**
     * 提取会话记忆
     *
     * @param request 提取请求
     * @return 提取结果
     * @author TellyJiang
     * @since 2026-04-11
     */
    @Override
    public MemoryExtractionResultDTO extractMemoryFromConversation(MemoryExtractionRequestDTO request) {
        memoryAccessGuard.assertCanExtract(request);
        return memoryExtractionService.extractMemoryFromConversation(request);
    }

    /**
     * 异步提取会话记忆
     *
     * @param request 提取请求
     * @author TellyJiang
     * @since 2026-04-15
     */
    @Override
    public void extractMemoryFromConversationAsync(MemoryExtractionRequestDTO request) {
        memoryAccessGuard.assertCanExtract(request);
        memoryExtractionService.extractMemoryFromConversationAsync(request);
    }
}
