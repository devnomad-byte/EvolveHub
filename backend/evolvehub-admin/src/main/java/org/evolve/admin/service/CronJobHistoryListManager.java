package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.CronJobHistoryInfra;
import org.evolve.domain.resource.model.CronJobHistoryEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.page.PageResponse;
import org.evolve.admin.response.CronJobHistoryResponse;

@Slf4j
@Component
public class CronJobHistoryListManager extends BaseManager<CronJobHistoryPageRequest, PageResponse<CronJobHistoryResponse>> {

    @Resource
    private CronJobHistoryInfra historyInfra;

    @Override
    protected void check(CronJobHistoryPageRequest req) {
        if (!StpUtil.hasPermission("cron:list")) {
            throw new BusinessException("无权查看任务历史");
        }
    }

    @Override
    protected PageResponse<CronJobHistoryResponse> process(CronJobHistoryPageRequest req) {
        int pageNum = req.pageNum() != null ? req.pageNum() : 1;
        int pageSize = req.pageSize() != null ? Math.min(req.pageSize(), 100) : 10;

        Page<CronJobHistoryEntity> page;
        if (req.jobId() != null) {
            page = historyInfra.listByJobId(req.jobId(), pageNum, pageSize);
        } else {
            page = historyInfra.listPage(pageNum, pageSize);
        }

        return new PageResponse<>(
            page.getRecords().stream().map(CronJobHistoryResponse::from).toList(),
            page.getTotal(),
            (int) page.getCurrent(),
            (int) page.getSize()
        );
    }
}
