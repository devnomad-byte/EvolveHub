package org.evolve.aiplatform.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.bean.entity.ChatTokenUsageEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Token 消费日报数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Repository
public class ChatTokenUsageInfra extends ServiceImpl<ChatTokenUsageInfra.ChatTokenUsageMapper, ChatTokenUsageEntity> {

    @Mapper
    interface ChatTokenUsageMapper extends BaseMapper<ChatTokenUsageEntity> {}

    /**
     * 查询指定用户、模型、日期的消费记录
     *
     * @param userId        用户 ID
     * @param modelConfigId 模型配置 ID
     * @param usageDate     日期
     * @return 消费记录，不存在返回 null
     */
    public ChatTokenUsageEntity getByUserAndModelAndDate(Long userId, Long modelConfigId, LocalDate usageDate) {
        return this.lambdaQuery()
                .eq(ChatTokenUsageEntity::getUserId, userId)
                .eq(ChatTokenUsageEntity::getModelConfigId, modelConfigId)
                .eq(ChatTokenUsageEntity::getUsageDate, usageDate)
                .one();
    }

    /**
     * 累加消费记录（已存在的记录增量更新）
     *
     * @param id               记录 ID
     * @param promptTokens     prompt token 增量
     * @param completionTokens completion token 增量
     * @param totalTokens      总 token 增量
     */
    public void incrementUsage(Long id, int promptTokens, int completionTokens, int totalTokens) {
        this.lambdaUpdate()
                .eq(ChatTokenUsageEntity::getId, id)
                .setSql("request_count = request_count + 1")
                .setSql("prompt_tokens = prompt_tokens + " + promptTokens)
                .setSql("completion_tokens = completion_tokens + " + completionTokens)
                .setSql("total_tokens = total_tokens + " + totalTokens)
                .update();
    }

    /**
     * 查询指定用户在日期范围内的消费记录
     *
     * @param userId    用户 ID
     * @param startDate 起始日期（含）
     * @param endDate   结束日期（含）
     * @return 消费记录列表
     */
    public List<ChatTokenUsageEntity> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return this.lambdaQuery()
                .eq(ChatTokenUsageEntity::getUserId, userId)
                .ge(ChatTokenUsageEntity::getUsageDate, startDate)
                .le(ChatTokenUsageEntity::getUsageDate, endDate)
                .orderByAsc(ChatTokenUsageEntity::getUsageDate)
                .list();
    }
}
