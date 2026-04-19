package org.evolve.aiplatform.service.cron;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.model.CronJobEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class CronSchedulerService {

    @Resource
    private CronJobInfra cronJobInfra;

    @Resource
    private CronJobExecutor cronJobExecutor;

    private TaskScheduler taskScheduler;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        taskScheduler = new ThreadPoolTaskScheduler();
        ((ThreadPoolTaskScheduler) taskScheduler).setPoolSize(10);
        ((ThreadPoolTaskScheduler) taskScheduler).setThreadNamePrefix("cron-scheduler-");
        ((ThreadPoolTaskScheduler) taskScheduler).initialize();

        syncJobs();
    }

    public void syncJobs() {
        List<CronJobEntity> jobs = cronJobInfra.listEnabled();
        log.info("Syncing cron jobs, enabled count: {}", jobs.size());

        for (CronJobEntity job : jobs) {
            scheduleJob(job);
        }
    }

    public void scheduleJob(CronJobEntity job) {
        if (scheduledTasks.containsKey(job.getId())) {
            cancelJob(job.getId());
        }

        try {
            CronTrigger trigger = new CronTrigger(job.getCronExpression(), ZoneId.of(job.getTimezone()));
            ScheduledFuture<?> future = taskScheduler.schedule(() -> executeJob(job.getId()), trigger);
            scheduledTasks.put(job.getId(), future);
            log.info("Scheduled cron job: id={}, name={}, cron={}", job.getId(), job.getName(), job.getCronExpression());
        } catch (Exception e) {
            log.error("Failed to schedule cron job: id={}, error={}", job.getId(), e.getMessage());
        }
    }

    public void cancelJob(Long jobId) {
        ScheduledFuture<?> future = scheduledTasks.remove(jobId);
        if (future != null) {
            future.cancel(false);
            log.info("Cancelled cron job: id={}", jobId);
        }
    }

    public void executeJob(Long jobId) {
        cronJobExecutor.execute(jobId, false);
    }

    public void triggerNow(Long jobId) {
        cronJobExecutor.execute(jobId, true);
    }

    public void rescheduleJob(CronJobEntity job) {
        cancelJob(job.getId());
        if (job.getEnabled() == 1) {
            scheduleJob(job);
        }
    }
}
