package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.model.CronJobEntity;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.page.PageResponse;
import org.evolve.admin.response.CronJobResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CronJobListManager extends BaseManager<PageCronJobRequest, PageResponse<CronJobResponse>> {

    @Resource
    private CronJobInfra cronJobInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(PageCronJobRequest req) {
        if (!StpUtil.hasPermission("cron:list")) {
            throw new BusinessException("无权查看定时任务");
        }
    }

    @Override
    protected PageResponse<CronJobResponse> process(PageCronJobRequest req) {
        int pageNum = req.pageNum() != null ? req.pageNum() : 1;
        int pageSize = req.pageSize() != null ? Math.min(req.pageSize(), 100) : 10;

        Long userId = StpUtil.getLoginIdAsLong();
        UserDataScopeService.DataScopeInfo dataScopeInfo = userDataScopeService.getDataScopeInfo(userId);

        List<CronJobEntity> jobs;
        if (dataScopeInfo.dataScope() == 1) {
            jobs = cronJobInfra.lambdaQuery()
                    .eq(req.enabled() != null, CronJobEntity::getEnabled, req.enabled())
                    .eq(req.targetUserId() != null, CronJobEntity::getTargetUserId, req.targetUserId())
                    .eq(CronJobEntity::getDeleted, 0)
                    .orderByDesc(CronJobEntity::getCreateTime)
                    .list();
        } else {
            jobs = cronJobInfra.lambdaQuery()
                    .eq(CronJobEntity::getCreateBy, userId)
                    .eq(req.enabled() != null, CronJobEntity::getEnabled, req.enabled())
                    .eq(req.targetUserId() != null, CronJobEntity::getTargetUserId, req.targetUserId())
                    .eq(CronJobEntity::getDeleted, 0)
                    .orderByDesc(CronJobEntity::getCreateTime)
                    .list();
        }

        int total = jobs.size();
        int from = (pageNum - 1) * pageSize;
        List<CronJobResponse> paged = jobs.stream()
                .skip(from)
                .limit(pageSize)
                .map(CronJobResponse::from)
                .collect(Collectors.toList());

        return new PageResponse<>(paged, total, pageNum, pageSize);
    }
}
