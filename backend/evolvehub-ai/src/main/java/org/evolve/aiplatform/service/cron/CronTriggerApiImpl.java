package org.evolve.aiplatform.service.cron;

import org.evolve.ai.api.CronTriggerApi;
import org.springframework.stereotype.Component;

@Component
public class CronTriggerApiImpl implements CronTriggerApi {

    private final CronSchedulerService cronSchedulerService;

    public CronTriggerApiImpl(CronSchedulerService cronSchedulerService) {
        this.cronSchedulerService = cronSchedulerService;
    }

    @Override
    public void triggerNow(Long jobId) {
        cronSchedulerService.triggerNow(jobId);
    }
}
