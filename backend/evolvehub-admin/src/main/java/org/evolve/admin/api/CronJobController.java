package org.evolve.admin.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.evolve.common.web.response.Result;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.infra.CronJobHistoryInfra;
import org.evolve.admin.request.CreateCronJobRequest;
import org.evolve.admin.request.UpdateCronJobRequest;
import org.evolve.admin.response.CronJobResponse;
import org.evolve.admin.response.CronJobHistoryResponse;
import org.evolve.admin.service.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cron")
public class CronJobController {

    private final CronJobInfra cronJobInfra;
    private final CronJobHistoryInfra historyInfra;
    private final CronJobListManager listManager;
    private final CronJobCreateManager createManager;
    private final CronJobUpdateManager updateManager;
    private final CronJobDeleteManager deleteManager;
    private final CronJobPauseManager pauseManager;
    private final CronJobResumeManager resumeManager;
    private final CronJobRunManager runManager;
    private final CronJobHistoryListManager historyListManager;

    public CronJobController(
            CronJobInfra cronJobInfra,
            CronJobHistoryInfra historyInfra,
            CronJobListManager listManager,
            CronJobCreateManager createManager,
            CronJobUpdateManager updateManager,
            CronJobDeleteManager deleteManager,
            CronJobPauseManager pauseManager,
            CronJobResumeManager resumeManager,
            CronJobRunManager runManager,
            CronJobHistoryListManager historyListManager
    ) {
        this.cronJobInfra = cronJobInfra;
        this.historyInfra = historyInfra;
        this.listManager = listManager;
        this.createManager = createManager;
        this.updateManager = updateManager;
        this.deleteManager = deleteManager;
        this.pauseManager = pauseManager;
        this.resumeManager = resumeManager;
        this.runManager = runManager;
        this.historyListManager = historyListManager;
    }

    @GetMapping("/jobs")
    public Result<PageResponse<CronJobResponse>> listJobs(PageCronJobRequest req) {
        return Result.ok(listManager.execute(req));
    }

    @PostMapping("/jobs")
    public Result<CronJobResponse> createJob(@Valid @RequestBody CreateCronJobRequest req) {
        return Result.ok(createManager.execute(req));
    }

    @PutMapping("/jobs/{id}")
    public Result<CronJobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody UpdateCronJobRequest req) {
        return Result.ok(updateManager.execute(req));
    }

    @DeleteMapping("/jobs/{id}")
    public Result<Boolean> deleteJob(@PathVariable Long id) {
        return Result.ok(deleteManager.execute(id));
    }

    @PostMapping("/jobs/{id}/pause")
    public Result<CronJobResponse> pauseJob(@PathVariable Long id) {
        return Result.ok(pauseManager.execute(id));
    }

    @PostMapping("/jobs/{id}/resume")
    public Result<CronJobResponse> resumeJob(@PathVariable Long id) {
        return Result.ok(resumeManager.execute(id));
    }

    @PostMapping("/jobs/{id}/run")
    public Result<CronJobResponse> runJob(@PathVariable Long id) {
        return Result.ok(runManager.run(id));
    }

    @GetMapping("/jobs/{id}/history")
    public Result<PageResponse<CronJobHistoryResponse>> listJobHistory(
            @PathVariable Long id,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.ok(historyListManager.execute(new CronJobHistoryPageRequest(id, pageNum, pageSize)));
    }

    @GetMapping("/history")
    public Result<PageResponse<CronJobHistoryResponse>> listHistory(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.ok(historyListManager.execute(new CronJobHistoryPageRequest(null, pageNum, pageSize)));
    }
}
