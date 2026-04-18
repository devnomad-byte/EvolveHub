package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.admin.response.TokenUsageUserResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.DeptEntity;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.evolve.domain.resource.infra.ChatTokenUsageInfra;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 管理员查询有Token用量记录的用户列表
 */
@Service
public class TokenUsageUserListManager extends BaseManager<TokenUsageUserListManager.Request, PageResponse<TokenUsageUserResponse>> {

    public record Request(
            String keyword,
            Long deptId,
            String startDate,
            String endDate,
            int pageNum,
            int pageSize
    ) {}

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(Request request) {}

    @Override
    protected PageResponse<TokenUsageUserResponse> process(Request request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var scopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);

        // 确定可见部门列表
        Set<Long> visibleDeptIds = null;
        if (scopeInfo.dataScope() == 1) {
            visibleDeptIds = null;
        } else if (scopeInfo.dataScope() == 2) {
            visibleDeptIds = scopeInfo.visibleDeptIds();
        } else if (scopeInfo.dataScope() == 3) {
            visibleDeptIds = scopeInfo.deptId() != null ? Set.of(scopeInfo.deptId()) : null;
        } else if (scopeInfo.dataScope() == 5) {
            visibleDeptIds = scopeInfo.visibleDeptIds();
        } else {
            visibleDeptIds = Set.of();
        }

        // 查询用户
        Page<UsersEntity> userPage = usersInfra.listByDeptIdsAndKeyword(
                visibleDeptIds, request.keyword(), request.pageNum(), request.pageSize());

        // 收集用户ID
        Set<Long> userIds = userPage.getRecords().stream()
                .map(UsersEntity::getId)
                .collect(java.util.stream.Collectors.toSet());

        // 查询这些用户的用量汇总
        Map<Long, long[]> userUsageMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<ChatTokenUsageEntity> usageList = chatTokenUsageInfra.listByUserIds(
                    userIds, request.startDate(), request.endDate());
            for (ChatTokenUsageEntity usage : usageList) {
                long[] stats = userUsageMap.computeIfAbsent(usage.getUserId(), k -> new long[4]);
                stats[0] += usage.getRequestCount();
                stats[1] += usage.getPromptTokens();
                stats[2] += usage.getCompletionTokens();
                stats[3] += usage.getTotalTokens();
            }
        }

        // 聚合响应
        List<TokenUsageUserResponse> records = new ArrayList<>();
        for (UsersEntity user : userPage.getRecords()) {
            DeptEntity dept = user.getDeptId() != null ? deptInfra.getDeptById(user.getDeptId()) : null;
            long[] usage = userUsageMap.getOrDefault(user.getId(), new long[4]);

            records.add(TokenUsageUserResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .deptId(user.getDeptId())
                    .deptName(dept != null ? dept.getDeptName() : null)
                    .totalRequests(usage[0])
                    .totalPromptTokens(usage[1])
                    .totalCompletionTokens(usage[2])
                    .totalTokens(usage[3])
                    .build());
        }

        // 按总Tokens降序
        records.sort((a, b) -> Long.compare(b.getTotalTokens(), a.getTotalTokens()));

        return new PageResponse<>(records, userPage.getTotal(), request.pageNum(), request.pageSize());
    }
}
