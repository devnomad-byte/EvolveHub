package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.admin.response.TokenUsageSummaryResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.evolve.domain.resource.infra.ChatTokenUsageInfra;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 管理员查询Token用量汇总
 */
@Service
public class TokenUsageSummaryManager extends BaseManager<TokenUsageSummaryManager.Request, TokenUsageSummaryResponse> {

    public record Request(
            Long userId,
            String startDate,
            String endDate
    ) {}

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(Request request) {}

    @Override
    protected TokenUsageSummaryResponse process(Request request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var scopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);

        // 构建用户ID集合
        Set<Long> visibleUserIds = null;
        if (scopeInfo.dataScope() == 4) {
            // 仅本人
            visibleUserIds = Set.of(currentUserId);
        } else if (request.userId() != null) {
            // 指定用户
            visibleUserIds = Set.of(request.userId());
        }

        List<ChatTokenUsageEntity> usageList;
        if (visibleUserIds != null && !visibleUserIds.isEmpty()) {
            usageList = chatTokenUsageInfra.listByUserIds(
                    visibleUserIds, request.startDate(), request.endDate());
        } else {
            // 使用分页查询获取所有可见记录
            var page = chatTokenUsageInfra.listPageByDataScope(
                    scopeInfo.dataScope(),
                    scopeInfo.deptId(),
                    scopeInfo.visibleDeptIds(),
                    currentUserId,
                    null, null,
                    request.startDate(),
                    request.endDate(),
                    1,
                    10000);
            usageList = page.getRecords();
        }

        long totalRequests = 0;
        long totalPromptTokens = 0;
        long totalCompletionTokens = 0;
        long totalTokens = 0;

        for (ChatTokenUsageEntity usage : usageList) {
            totalRequests += usage.getRequestCount() != null ? usage.getRequestCount() : 0;
            totalPromptTokens += usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
            totalCompletionTokens += usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
            totalTokens += usage.getTotalTokens() != null ? usage.getTotalTokens() : 0;
        }

        return TokenUsageSummaryResponse.builder()
                .totalRequests(totalRequests)
                .totalPromptTokens(totalPromptTokens)
                .totalCompletionTokens(totalCompletionTokens)
                .totalTokens(totalTokens)
                .build();
    }
}
