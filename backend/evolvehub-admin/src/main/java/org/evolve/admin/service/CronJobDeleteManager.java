package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.model.CronJobEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;

@Slf4j
@Component
public class CronJobDeleteManager extends BaseManager<Long, Boolean> {

    @Resource
    private CronJobInfra cronJobInfra;

    @Override
    protected void check(Long jobId) {
        if (!StpUtil.hasPermission("cron:manage")) {
            throw new BusinessException("无权管理定时任务");
        }
    }

    @Override
    protected Boolean process(Long jobId) {
        CronJobEntity entity = cronJobInfra.getById(jobId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException("任务不存在");
        }

        cronJobInfra.lambdaUpdate()
            .eq(CronJobEntity::getId, jobId)
            .eq(CronJobEntity::getDeleted, 0)
            .set(CronJobEntity::getDeleted, 1)
            .update();
        log.info("Deleted cron job: id={}", jobId);
        return true;
    }
}
