package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.admin.response.UserChatActivityResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.DeptEntity;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.evolve.domain.resource.infra.ChatSessionInfra;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 管理员查询有聊天记录的用户列表
 */
@Service
public class ChatHistoryUserListManager extends BaseManager<ChatHistoryUserListManager.Request, PageResponse<UserChatActivityResponse>> {

    public record Request(
            String keyword,
            Long deptId,
            int pageNum,
            int pageSize
    ) {}

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(Request request) {}

    @Override
    protected PageResponse<UserChatActivityResponse> process(Request request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var scopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);

        // 确定可见部门列表
        Set<Long> visibleDeptIds = null;
        if (scopeInfo.dataScope() == 1) {
            // SUPER_ADMIN: 不过滤
            visibleDeptIds = null;
        } else if (scopeInfo.dataScope() == 2) {
            // 部门及子部门
            visibleDeptIds = scopeInfo.visibleDeptIds();
        } else if (scopeInfo.dataScope() == 3) {
            // 仅本部门
            visibleDeptIds = scopeInfo.deptId() != null ? Set.of(scopeInfo.deptId()) : null;
        } else if (scopeInfo.dataScope() == 5) {
            visibleDeptIds = scopeInfo.visibleDeptIds();
        } else {
            // scope=4 (本人) 或无角色：只能看到自己
            visibleDeptIds = Set.of();
        }

        // 查询用户
        Page<UsersEntity> userPage = usersInfra.listByDeptIdsAndKeyword(
                visibleDeptIds, request.keyword(), request.pageNum(), request.pageSize());

        // 聚合会话数据
        List<UserChatActivityResponse> records = new ArrayList<>();
        for (UsersEntity user : userPage.getRecords()) {
            DeptEntity dept = user.getDeptId() != null ? deptInfra.getDeptById(user.getDeptId()) : null;
            long sessionCount = chatSessionInfra.countByUserId(user.getId());

            records.add(UserChatActivityResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .deptId(user.getDeptId())
                    .deptName(dept != null ? dept.getDeptName() : null)
                    .sessionCount((int) sessionCount)
                    .build());
        }

        // 按会话数降序
        records.sort((a, b) -> Integer.compare(b.getSessionCount(), a.getSessionCount()));

        return new PageResponse<>(records, userPage.getTotal(), request.pageNum(), request.pageSize());
    }
}
