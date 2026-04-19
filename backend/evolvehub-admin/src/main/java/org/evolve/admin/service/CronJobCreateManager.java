package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.model.CronJobEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.admin.request.CreateCronJobRequest;
import org.evolve.admin.response.CronJobResponse;

@Slf4j
@Component
public class CronJobCreateManager extends BaseManager<CreateCronJobRequest, CronJobResponse> {

    @Resource
    private CronJobInfra cronJobInfra;

    @Override
    protected void check(CreateCronJobRequest req) {
        if (!StpUtil.hasPermission("cron:manage")) {
            throw new BusinessException("无权管理定时任务");
        }
    }

    @Override
    protected CronJobResponse process(CreateCronJobRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();

        CronJobEntity entity = new CronJobEntity();
        entity.setName(req.name());
        entity.setDescription(req.description());
        entity.setCronExpression(req.cronExpression());
        entity.setTimezone(req.timezone() != null ? req.timezone() : "Asia/Shanghai");
        entity.setTaskType(req.taskType() != null ? req.taskType() : "agent");
        entity.setPromptTemplate(req.promptTemplate());
        entity.setTargetUserId(req.targetUserId());
        entity.setTargetSessionId(req.targetSessionId());
        entity.setTimeoutSeconds(req.timeoutSeconds() != null ? req.timeoutSeconds() : 300);
        entity.setMaxRetries(req.maxRetries() != null ? req.maxRetries() : 3);
        entity.setMisfireGraceSeconds(req.misfireGraceSeconds() != null ? req.misfireGraceSeconds() : 60);
        entity.setMaxConcurrency(req.maxConcurrency() != null ? req.maxConcurrency() : 1);
        entity.setEnabled(1);
        entity.setCreateBy(userId);
        entity.setDeptId(req.deptId());

        cronJobInfra.save(entity);
        log.info("Created cron job: id={}, name={}, by user={}", entity.getId(), entity.getName(), userId);

        return CronJobResponse.from(entity);
    }
}
