package org.evolve.aiplatform.api;

import lombok.extern.slf4j.Slf4j;
import org.evolve.aiplatform.service.cron.CronSchedulerService;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cron")
public class AiCronController {

    private final CronSchedulerService cronSchedulerService;

    public AiCronController(CronSchedulerService cronSchedulerService) {
        this.cronSchedulerService = cronSchedulerService;
    }

    @PostMapping("/jobs/{id}/run")
    public Result<Void> triggerJob(@PathVariable Long id) {
        log.info("Received trigger request for cron job: id={}", id);
        cronSchedulerService.triggerNow(id);
        return Result.ok();
    }
}
