package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.model.CronJobEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.admin.response.CronJobResponse;

@Slf4j
@Component
public class CronJobPauseManager extends BaseManager<Long, CronJobResponse> {

    @Resource
    private CronJobInfra cronJobInfra;

    @Override
    protected void check(Long jobId) {
        if (!StpUtil.hasPermission("cron:manage")) {
            throw new BusinessException("无权管理定时任务");
        }
    }

    @Override
    protected CronJobResponse process(Long jobId) {
        CronJobEntity entity = cronJobInfra.getById(jobId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException("任务不存在");
        }

        entity.setEnabled(0);
        cronJobInfra.updateById(entity);
        log.info("Paused cron job: id={}", jobId);

        return CronJobResponse.from(entity);
    }
}
