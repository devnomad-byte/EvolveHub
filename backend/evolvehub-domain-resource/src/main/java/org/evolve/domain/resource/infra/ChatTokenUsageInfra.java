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

    public Page<ChatTokenUsageEntity> listPageByDataScope(Integer dataScope, Long userDeptId,
                                                          Set<Long> visibleDeptIds,
                                                          Long currentUserId,
                                                          int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(dataScope == 4, ChatTokenUsageEntity::getCreateBy, currentUserId)
                .eq(dataScope == 3, ChatTokenUsageEntity::getDeptId, userDeptId)
                .in(dataScope == 2, ChatTokenUsageEntity::getDeptId,
                        visibleDeptIds != null ? visibleDeptIds : Set.of(userDeptId))
                .in(dataScope == 5 && visibleDeptIds != null, ChatTokenUsageEntity::getDeptId, visibleDeptIds)
                .orderByDesc(ChatTokenUsageEntity::getUsageDate)
                .orderByDesc(ChatTokenUsageEntity::getTotalTokens)
                .page(new Page<>(pageNum, pageSize));
    }
}
