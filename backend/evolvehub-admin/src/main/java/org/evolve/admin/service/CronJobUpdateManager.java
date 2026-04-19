package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.model.CronJobEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.admin.request.UpdateCronJobRequest;
import org.evolve.admin.response.CronJobResponse;

@Slf4j
@Component
public class CronJobUpdateManager extends BaseManager<UpdateCronJobRequest, CronJobResponse> {

    @Resource
    private CronJobInfra cronJobInfra;

    @Override
    protected void check(UpdateCronJobRequest req) {
        if (!StpUtil.hasPermission("cron:manage")) {
            throw new BusinessException("无权管理定时任务");
        }
        if (req.id() == null) {
            throw new BusinessException("任务ID不能为空");
        }
        CronJobEntity existing = cronJobInfra.getById(req.id());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("任务不存在");
        }
    }

    @Override
    protected CronJobResponse process(UpdateCronJobRequest req) {
        CronJobEntity entity = cronJobInfra.getById(req.id());

        if (req.name() != null) entity.setName(req.name());
        if (req.description() != null) entity.setDescription(req.description());
        if (req.cronExpression() != null) entity.setCronExpression(req.cronExpression());
        if (req.timezone() != null) entity.setTimezone(req.timezone());
        if (req.taskType() != null) entity.setTaskType(req.taskType());
        if (req.promptTemplate() != null) entity.setPromptTemplate(req.promptTemplate());
        if (req.targetUserId() != null) entity.setTargetUserId(req.targetUserId());
        if (req.targetSessionId() != null) entity.setTargetSessionId(req.targetSessionId());
        if (req.timeoutSeconds() != null) entity.setTimeoutSeconds(req.timeoutSeconds());
        if (req.maxRetries() != null) entity.setMaxRetries(req.maxRetries());
        if (req.misfireGraceSeconds() != null) entity.setMisfireGraceSeconds(req.misfireGraceSeconds());
        if (req.maxConcurrency() != null) entity.setMaxConcurrency(req.maxConcurrency());
        if (req.enabled() != null) entity.setEnabled(req.enabled());
        if (req.deptId() != null) entity.setDeptId(req.deptId());

        cronJobInfra.updateById(entity);
        log.info("Updated cron job: id={}", req.id());

        return CronJobResponse.from(entity);
    }
}
