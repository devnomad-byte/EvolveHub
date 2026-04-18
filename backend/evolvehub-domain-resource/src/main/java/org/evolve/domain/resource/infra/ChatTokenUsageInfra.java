package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public class ChatTokenUsageInfra extends ServiceImpl<ChatTokenUsageInfra.ChatTokenUsageMapper, ChatTokenUsageEntity> {

    @Mapper
    interface ChatTokenUsageMapper extends BaseMapper<ChatTokenUsageEntity> {}

    public ChatTokenUsageEntity getByUserAndModelAndDate(Long userId, Long modelConfigId, LocalDate usageDate) {
        return this.lambdaQuery()
                .eq(ChatTokenUsageEntity::getUserId, userId)
                .eq(ChatTokenUsageEntity::getModelConfigId, modelConfigId)
                .eq(ChatTokenUsageEntity::getUsageDate, usageDate)
                .eq(ChatTokenUsageEntity::getDeleted, 0)
                .one();
    }

    public List<ChatTokenUsageEntity> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return this.lambdaQuery()
                .eq(ChatTokenUsageEntity::getUserId, userId)
                .ge(ChatTokenUsageEntity::getUsageDate, startDate)
                .le(ChatTokenUsageEntity::getUsageDate, endDate)
                .eq(ChatTokenUsageEntity::getDeleted, 0)
                .orderByDesc(ChatTokenUsageEntity::getUsageDate)
                .list();
    }

    public void incrementUsage(Long userId, Long modelConfigId, LocalDate usageDate, Long deptId,
                               int requestCount, int promptTokens, int completionTokens, int totalTokens) {
        ChatTokenUsageEntity existing = getByUserAndModelAndDate(userId, modelConfigId, usageDate);
        if (existing != null) {
            this.lambdaUpdate()
                    .eq(ChatTokenUsageEntity::getId, existing.getId())
                    .setSql("request_count = request_count + " + requestCount)
                    .setSql("prompt_tokens = prompt_tokens + " + promptTokens)
                    .setSql("completion_tokens = completion_tokens + " + completionTokens)
                    .setSql("total_tokens = total_tokens + " + totalTokens)
                    .update();
        } else {
            ChatTokenUsageEntity newRecord = new ChatTokenUsageEntity();
            newRecord.setUserId(userId);
            newRecord.setModelConfigId(modelConfigId);
            newRecord.setUsageDate(usageDate);
            newRecord.setDeptId(deptId);
            newRecord.setRequestCount(requestCount);
            newRecord.setPromptTokens(promptTokens);
            newRecord.setCompletionTokens(completionTokens);
            newRecord.setTotalTokens(totalTokens);
            this.save(newRecord);
        }
    }

    /**
     * 管理员分页查询用量记录（支持数据权限过滤 + 用户/日期过滤）
     */
    public Page<ChatTokenUsageEntity> listPageByDataScope(
            Integer dataScope, Long userDeptId, Set<Long> visibleDeptIds,
            Long currentUserId, Long userId, Long modelConfigId,
            String startDate, String endDate,
            int pageNum, int pageSize) {

        // 预处理日期参数
        LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;

        return this.lambdaQuery()
                // 数据权限过滤
                .eq(dataScope != null && dataScope == 4, ChatTokenUsageEntity::getCreateBy, currentUserId)
                .eq(dataScope != null && dataScope == 3, ChatTokenUsageEntity::getDeptId, userDeptId)
                .in(dataScope != null && dataScope == 2, ChatTokenUsageEntity::getDeptId,
                        visibleDeptIds != null ? visibleDeptIds : Set.of(userDeptId))
                .in(dataScope != null && dataScope == 5 && visibleDeptIds != null, ChatTokenUsageEntity::getDeptId, visibleDeptIds)
                // scope=1 不过滤
                // 用户过滤
                .eq(userId != null, ChatTokenUsageEntity::getUserId, userId)
                // 模型过滤
                .eq(modelConfigId != null, ChatTokenUsageEntity::getModelConfigId, modelConfigId)
                // 日期范围过滤
                .ge(start != null, ChatTokenUsageEntity::getUsageDate, start)
                .le(end != null, ChatTokenUsageEntity::getUsageDate, end)
                .orderByDesc(ChatTokenUsageEntity::getUsageDate)
                .orderByDesc(ChatTokenUsageEntity::getTotalTokens)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 统计指定用户的用量记录总数
     */
    public long countByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(ChatTokenUsageEntity::getUserId, userId)
                .count();
    }

    /**
     * 按用户ID列表查询用量记录
     */
    public List<ChatTokenUsageEntity> listByUserIds(Set<Long> userIds, String startDate, String endDate) {
        LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;

        return this.lambdaQuery()
                .in(userIds != null && !userIds.isEmpty(), ChatTokenUsageEntity::getUserId, userIds)
                .ge(start != null, ChatTokenUsageEntity::getUsageDate, start)
                .le(end != null, ChatTokenUsageEntity::getUsageDate, end)
                .orderByDesc(ChatTokenUsageEntity::getUsageDate)
                .list();
    }
}
