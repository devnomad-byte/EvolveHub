package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.model.CronJobEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.admin.response.CronJobResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CronJobRunManager {

    @Resource
    private CronJobInfra cronJobInfra;

    private static final String AI_SERVICE_URL = "http://localhost:8082/api/ai/cron/jobs/%d/run";

    public CronJobResponse run(Long jobId) {
        if (!StpUtil.hasPermission("cron:execute")) {
            throw new BusinessException("无权执行定时任务");
        }

        CronJobEntity entity = cronJobInfra.getById(jobId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException("任务不存在");
        }

        log.info("Manual trigger cron job: id={}, name={}", jobId, entity.getName());

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(String.format(AI_SERVICE_URL, jobId), null, Void.class);
            return CronJobResponse.from(entity);
        } catch (Exception e) {
            log.error("Failed to trigger cron job: id={}", jobId, e);
            throw new BusinessException("触发执行失败: " + e.getMessage());
        }
    }
}
