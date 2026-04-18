package org.evolve.admin.service;

import lombok.Builder;
import org.evolve.admin.response.SecurityScannerBlockedHistoryResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.resource.infra.SecurityScannerBlockedHistoryInfra;
import org.evolve.domain.resource.model.SecurityScannerBlockedHistoryEntity;
import org.springframework.stereotype.Service;

/**
 * 获取安全扫描阻断历史列表
 */
@Service
public class SecurityScannerBlockedHistoryListManager extends BaseManager<SecurityScannerBlockedHistoryListManager.Request, PageResponse<SecurityScannerBlockedHistoryResponse>> {

    @jakarta.annotation.Resource
    private SecurityScannerBlockedHistoryInfra historyInfra;

    @Builder
    public record Request(String action, Long userId, int pageNum, int pageSize) {}

    @Override
    protected void check(Request request) {}

    @Override
    protected PageResponse<SecurityScannerBlockedHistoryResponse> process(Request request) {
        var page = historyInfra.listPage(request.pageNum(), request.pageSize(), request.action(), request.userId());
        return new PageResponse<>(
                page.getRecords().stream().map(SecurityScannerBlockedHistoryResponse::from).toList(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
    }
}
