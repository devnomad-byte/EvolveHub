package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.admin.response.SessionItemResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.evolve.domain.resource.infra.ChatSessionInfra;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理员查询用户的会话列表
 */
@Service
public class ChatHistorySessionListManager extends BaseManager<ChatHistorySessionListManager.Request, PageResponse<SessionItemResponse>> {

    public record Request(
            Long userId,
            String keyword,
            String startDate,
            String endDate,
            int pageNum,
            int pageSize
    ) {}

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(Request request) {}

    @Override
    protected PageResponse<SessionItemResponse> process(Request request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var scopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);

        Page<ChatSessionEntity> page = chatSessionInfra.listPageByDataScope(
                scopeInfo.dataScope(),
                scopeInfo.deptId(),
                scopeInfo.visibleDeptIds(),
                currentUserId,
                request.userId(),
                request.keyword(),
                request.startDate(),
                request.endDate(),
                request.pageNum(),
                request.pageSize());

        // 收集所有 userId 和 modelConfigId
        Set<Long> userIds = page.getRecords().stream()
                .map(ChatSessionEntity::getUserId)
                .filter(u -> u != null)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> modelIds = page.getRecords().stream()
                .map(ChatSessionEntity::getModelConfigId)
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

        List<SessionItemResponse> records = page.getRecords().stream().map(s -> {
            UsersEntity user = userMap.get(s.getUserId());
            ModelConfigEntity model = modelMap.get(s.getModelConfigId());
            return SessionItemResponse.builder()
                    .id(s.getId())
                    .userId(s.getUserId())
                    .username(user != null ? user.getUsername() : null)
                    .nickname(user != null ? user.getNickname() : null)
                    .title(s.getTitle())
                    .modelConfigId(s.getModelConfigId())
                    .modelName(model != null ? model.getName() : null)
                    .totalPromptTokens(s.getTotalPromptTokens())
                    .totalCompletionTokens(s.getTotalCompletionTokens())
                    .totalTokens(s.getTotalTokens())
                    .messageCount(s.getMessageCount())
                    .createTime(s.getCreateTime() != null ? s.getCreateTime().toString() : null)
                    .updateTime(s.getUpdateTime() != null ? s.getUpdateTime().toString() : null)
                    .build();
        }).toList();

        return new PageResponse<>(records, page.getTotal(), request.pageNum(), request.pageSize());
    }
}
