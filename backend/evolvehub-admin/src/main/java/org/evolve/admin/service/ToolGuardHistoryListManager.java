package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.admin.response.ToolGuardHistoryResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.resource.infra.ToolGuardHistoryInfra;
import org.evolve.domain.resource.model.ToolGuardHistoryEntity;
import org.springframework.stereotype.Service;

/**
 * 获取工具守卫阻断历史
 */
@Service
public class ToolGuardHistoryListManager extends BaseManager<ToolGuardHistoryListManager.Request, PageResponse<ToolGuardHistoryResponse>> {

    public record Request(String severity, Long userId, int pageNum, int pageSize) {}

    @Resource
    private ToolGuardHistoryInfra toolGuardHistoryInfra;

    @Override
    protected void check(Request request) {}

    @Override
    protected PageResponse<ToolGuardHistoryResponse> process(Request request) {
        Page<ToolGuardHistoryEntity> page = toolGuardHistoryInfra.listPage(
                request.severity(), request.userId(), request.pageNum(), request.pageSize());
        return new PageResponse<>(
                page.getRecords().stream().map(ToolGuardHistoryResponse::from).toList(),
                page.getTotal(),
                request.pageNum(),
                request.pageSize()
        );
    }
}
