package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;

@Repository
public class ChatSessionInfra extends ServiceImpl<ChatSessionInfra.ChatSessionMapper, ChatSessionEntity> {

    @Mapper
    interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {}

    public Page<ChatSessionEntity> listPageByUserId(Long userId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(ChatSessionEntity::getUserId, userId)
                .orderByDesc(ChatSessionEntity::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    public ChatSessionEntity getByIdAndUserId(Long id, Long userId) {
        return this.lambdaQuery()
                .eq(ChatSessionEntity::getId, id)
                .eq(ChatSessionEntity::getUserId, userId)
                .one();
    }

    public void incrementTokenUsage(Long sessionId, int promptTokens, int completionTokens,
                                     int totalTokens, int messageIncrement) {
        this.lambdaUpdate()
                .eq(ChatSessionEntity::getId, sessionId)
                .setSql("total_prompt_tokens = total_prompt_tokens + " + promptTokens)
                .setSql("total_completion_tokens = total_completion_tokens + " + completionTokens)
                .setSql("total_tokens = total_tokens + " + totalTokens)
                .setSql("message_count = message_count + " + messageIncrement)
                .update();
    }

    /**
     * 管理员分页查询会话列表（支持数据权限过滤 + 关键字搜索 + 日期范围）
     *
     * scope=1 (SUPER_ADMIN): 不过滤，直接查全部
     * scope=4: 仅本人数据
     * scope=3: 仅本部门
     * scope=2: 本部门及子部门
     * scope=5: 自定义部门列表
     *
     * @param dataScope 数据权限类型
     * @param userDeptId 当前用户所属部门ID
     * @param visibleDeptIds 可见部门ID列表（scope=2/5时使用）
     * @param currentUserId 当前用户ID（scope=4时使用）
     * @param userId 指定用户ID（可选，传null表示查所有用户）
     * @param keyword 会话标题关键字（可选）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param pageNum 页码
     * @param pageSize 每页条数
     */
    public Page<ChatSessionEntity> listPageByDataScope(
            Integer dataScope, Long userDeptId, Set<Long> visibleDeptIds,
            Long currentUserId, Long userId, String keyword,
            String startDate, String endDate,
            int pageNum, int pageSize) {

        // 预处理日期参数，避免在 lambda 中解析导致异常
        LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;

        return this.lambdaQuery()
                // 数据权限过滤
                .eq(dataScope != null && dataScope == 4, ChatSessionEntity::getCreateBy, currentUserId)
                .eq(dataScope != null && dataScope == 3, ChatSessionEntity::getDeptId, userDeptId)
                .in(dataScope != null && dataScope == 2, ChatSessionEntity::getDeptId,
                        visibleDeptIds != null ? visibleDeptIds : Set.of(userDeptId))
                .in(dataScope != null && dataScope == 5 && visibleDeptIds != null, ChatSessionEntity::getDeptId, visibleDeptIds)
                // scope=1 不过滤
                // 用户过滤
                .eq(userId != null, ChatSessionEntity::getUserId, userId)
                // 关键字搜索
                .like(keyword != null && !keyword.isBlank(), ChatSessionEntity::getTitle, keyword)
                // 日期范围过滤（按创建时间）
                .ge(start != null, ChatSessionEntity::getCreateTime, start != null ? start.atStartOfDay() : null)
                .le(end != null, ChatSessionEntity::getCreateTime, end != null ? end.atTime(23, 59, 59) : null)
                .orderByDesc(ChatSessionEntity::getUpdateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 统计指定用户的会话数量
     */
    public long countByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(ChatSessionEntity::getUserId, userId)
                .count();
    }
}
