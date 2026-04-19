package org.evolve.aiplatform.service.cron;


import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.evolve.ai.api.CronTriggerApi;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.model.CronJobEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CronTools {

    @Resource
    private CronJobInfra cronJobInfra;

    @Resource
    private CronTriggerApi cronTriggerApi;

    @Tool(name = "create_cron_job", description = "创建一个新的定时任务")
    public String createCronJob(
            @ToolParam(name = "name", description = "任务名称") String name,
            @ToolParam(name = "cron", description = "cron表达式，如 0 9 * * * 表示每天9点执行") String cron,
            @ToolParam(name = "prompt", description = "执行时的prompt内容，支持{{current_date}}等变量") String prompt,
            @ToolParam(name = "description", description = "任务描述，可选") String description
    ) {
        Long userId = org.evolve.common.base.CurrentUserHolder.getUserId();
        if (userId == null) {
            return "错误: 无法获取当前用户";
        }

        CronJobEntity entity = new CronJobEntity();
        entity.setName(name);
        entity.setCronExpression(cron);
        entity.setPromptTemplate(prompt);
        entity.setDescription(description);
        entity.setTaskType("agent");
        entity.setTimezone("Asia/Shanghai");
        entity.setTimeoutSeconds(300);
        entity.setMaxRetries(3);
        entity.setMisfireGraceSeconds(60);
        entity.setMaxConcurrency(1);
        entity.setEnabled(1);
        entity.setCreateBy(userId);

        cronJobInfra.save(entity);
        log.info("Created cron job via AI: id={}, name={}", entity.getId(), name);

        return "定时任务创建成功，任务ID: " + entity.getId() + "，任务名称: " + name;
    }

    @Tool(name = "list_cron_jobs", description = "列出当前用户的所有定时任务")
    public String listCronJobs() {
        Long userId = org.evolve.common.base.CurrentUserHolder.getUserId();
        if (userId == null) {
            return "错误: 无法获取当前用户";
        }

        List<CronJobEntity> jobs = cronJobInfra.lambdaQuery()
                .eq(CronJobEntity::getCreateBy, userId)
                .eq(CronJobEntity::getDeleted, 0)
                .orderByDesc(CronJobEntity::getCreateTime)
                .list();

        if (jobs.isEmpty()) {
            return "暂无定时任务";
        }

        return jobs.stream()
                .map(j -> String.format("- [%s] %s (cron: %s, 状态: %s, 下次执行: %s)",
                        j.getId(), j.getName(), j.getCronExpression(),
                        j.getEnabled() == 1 ? "启用" : "禁用",
                        j.getNextRunTime() != null ? j.getNextRunTime().toString() : "N/A"))
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "delete_cron_job", description = "删除指定的定时任务")
    public String deleteCronJob(
            @ToolParam(name = "job_id", description = "要删除的任务ID") Long jobId
    ) {
        Long userId = org.evolve.common.base.CurrentUserHolder.getUserId();
        if (userId == null) {
            return "错误: 无法获取当前用户";
        }

        CronJobEntity job = cronJobInfra.getById(jobId);
        if (job == null || job.getDeleted() == 1) {
            return "错误: 任务不存在";
        }
        if (!job.getCreateBy().equals(userId)) {
            return "错误: 无权删除该任务";
        }

        job.setDeleted(1);
        cronJobInfra.updateById(job);
        log.info("Deleted cron job via AI: id={}", jobId);

        return "定时任务删除成功";
    }

    @Tool(name = "trigger_cron_now", description = "立即触发执行指定的定时任务")
    public String triggerCronNow(
            @ToolParam(name = "job_id", description = "要触发的任务ID") Long jobId
    ) {
        Long userId = org.evolve.common.base.CurrentUserHolder.getUserId();
        if (userId == null) {
            return "错误: 无法获取当前用户";
        }

        CronJobEntity job = cronJobInfra.getById(jobId);
        if (job == null || job.getDeleted() == 1) {
            return "错误: 任务不存在";
        }
        if (!job.getCreateBy().equals(userId)) {
            return "错误: 无权执行该任务";
        }

        try {
            cronTriggerApi.triggerNow(jobId);
            return "任务已触发执行，请稍后查看执行结果";
        } catch (Exception e) {
            log.error("Failed to trigger cron job via AI: id={}", jobId, e);
            return "触发失败: " + e.getMessage();
        }
    }
}
