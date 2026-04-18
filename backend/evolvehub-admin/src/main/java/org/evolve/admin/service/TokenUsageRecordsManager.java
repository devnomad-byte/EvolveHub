package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.admin.response.TokenUsageRecordResponse;
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
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理员查询Token用量记录
 */
@Service
public class TokenUsageRecordsManager extends BaseManager<TokenUsageRecordsManager.Request, PageResponse<TokenUsageRecordResponse>> {

    public record Request(
            Long userId,
            Long modelConfigId,
            String keyword,
            String startDate,
            String endDate,
            int pageNum,
            int pageSize
    ) {}

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(Request request) {}

    @Override
    protected PageResponse<TokenUsageRecordResponse> process(Request request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var scopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);

        Page<ChatTokenUsageEntity> page = chatTokenUsageInfra.listPageByDataScope(
                scopeInfo.dataScope(),
                scopeInfo.deptId(),
                scopeInfo.visibleDeptIds(),
                currentUserId,
                request.userId(),
                request.modelConfigId(),
                request.startDate(),
                request.endDate(),
                request.pageNum(),
                request.pageSize());

        // 收集用户ID和模型ID
        Set<Long> userIds = page.getRecords().stream()
                .map(ChatTokenUsageEntity::getUserId)
                .filter(u -> u != null)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> modelIds = page.getRecords().stream()
                .map(ChatTokenUsageEntity::getModelConfigId)
                .filter(m -> m != null)
                .collect(java.util.stream.Collectors.toSet());

        Map<Long, UsersEntity> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UsersEntity u : usersInfra.listByIds(userIds)) {
                userMap.put(u.getId(), u);
            }
        }

        Map<Long, ModelConfigEntity> modelMap = new HashMap<>();
        if (!modelIds.isEmpty()) {
            for (ModelConfigEntity m : modelConfigInfra.listByIds(modelIds)) {
                modelMap.put(m.getId(), m);
            }
        }

        List<TokenUsageRecordResponse> records = page.getRecords().stream().map(r -> {
            UsersEntity user = userMap.get(r.getUserId());
            ModelConfigEntity model = modelMap.get(r.getModelConfigId());
            DeptEntity dept = user != null && user.getDeptId() != null
                    ? deptInfra.getDeptById(user.getDeptId()) : null;

            return TokenUsageRecordResponse.builder()
                    .id(r.getId())
                    .userId(r.getUserId())
                    .username(user != null ? user.getUsername() : null)
                    .nickname(user != null ? user.getNickname() : null)
                    .deptId(r.getDeptId())
                    .deptName(dept != null ? dept.getDeptName() : null)
                    .modelConfigId(r.getModelConfigId())
                    .modelName(model != null ? model.getName() : null)
                    .usageDate(r.getUsageDate() != null ? r.getUsageDate().toString() : null)
                    .requestCount(r.getRequestCount())
                    .promptTokens(r.getPromptTokens())
                    .completionTokens(r.getCompletionTokens())
                    .totalTokens(r.getTotalTokens())
                    .build();
        }).toList();

        return new PageResponse<>(records, page.getTotal(), request.pageNum(), request.pageSize());
    }
}
