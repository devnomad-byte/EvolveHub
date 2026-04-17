package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.resource.infra.ChatSessionInfra;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.springframework.stereotype.Service;

@Service
public class AdminListSessionsManager extends BaseManager<PageRequest, PageResponse<ChatSessionEntity>> {

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(PageRequest request) {}

    @Override
    protected PageResponse<ChatSessionEntity> process(PageRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var dataScopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);
        Page<ChatSessionEntity> page = chatSessionInfra.listPageByDataScope(
                dataScopeInfo.dataScope(), dataScopeInfo.deptId(),
                dataScopeInfo.visibleDeptIds(), currentUserId,
                request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
