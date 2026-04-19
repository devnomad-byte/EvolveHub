package org.evolve.aiplatform.service.cron;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.cron.scheduler.CronExpression;
import lombok.extern.slf4j.Slf4j;
import org.evolve.aiplatform.config.ChatModelFactory;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.service.agent.ChatAgentFactory;
import org.evolve.aiplatform.service.agent.UserToolkitLoader;
import org.evolve.common.base.CurrentUserHolder;
import org.evolve.domain.resource.infra.ChatMessageInfra;
import org.evolve.domain.resource.infra.ChatSessionInfra;
import org.evolve.domain.resource.infra.ChatTokenUsageInfra;
import org.evolve.domain.resource.infra.CronJobHistoryInfra;
import org.evolve.domain.resource.infra.CronJobInfra;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.infra.UsersInfra;
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.evolve.domain.resource.model.CronJobEntity;
import org.evolve.domain.resource.model.CronJobHistoryEntity;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class CronJobExecutor {

    private final ConcurrentHashMap<Long, Semaphore> jobSemaphores = new ConcurrentHashMap<>();

    @lombok.Resource
    private CronJobInfra cronJobInfra;

    @lombok.Resource
    private CronJobHistoryInfra historyInfra;

    @lombok.Resource
    private ChatSessionInfra chatSessionInfra;

    @lombok.Resource
    private ChatMessageInfra chatMessageInfra;

    @lombok.Resource
    private ChatTokenUsageInfra tokenUsageInfra;

    @lombok.Resource
    private ModelConfigInfra modelConfigInfra;

    @lombok.Resource
    private UsersInfra usersInfra;

    @lombok.Resource
    private ChatAgentFactory chatAgentFactory;

    @lombok.Resource
    private UserToolkitLoader userToolkitLoader;

    @lombok.Resource
    private MemoryApi memoryApi;

    public void execute(Long jobId, boolean isManual) {
        CronJobEntity job = cronJobInfra.getById(jobId);
        if (job == null || job.getDeleted() == 1) {
            log.warn("Cron job not found or deleted: id={}", jobId);
            return;
        }

        Semaphore semaphore = jobSemaphores.computeIfAbsent(jobId,
                k -> new Semaphore(job.getMaxConcurrency()));
        if (!semaphore.tryAcquire()) {
            log.warn("Cron job {} is already running, skipping", jobId);
            return;
        }

        Long historyId = null;
        try {
            historyId = createHistory(job, isManual);
            Long targetUserId = job.getTargetUserId();
            String sessionId = isManual
                    ? "MANUAL:" + jobId + ":" + System.currentTimeMillis()
                    : "CRON:" + jobId;

            CurrentUserHolder.set(targetUserId);

            String prompt = buildPrompt(job);

            if ("agent".equals(job.getTaskType())) {
                executeAgentTask(job, targetUserId, sessionId, prompt);
            } else {
                executeTextTask(job, targetUserId, sessionId, prompt);
            }

            updateJobSuccess(jobId);
            updateHistorySuccess(historyId);
        } catch (Exception e) {
            log.error("Cron job execution failed: id={}", jobId, e);
            updateJobError(jobId, e.getMessage());
            updateHistoryError(historyId, e.getMessage());
        } finally {
            semaphore.release();
            CurrentUserHolder.clear();
        }
    }

    private Long createHistory(CronJobEntity job, boolean isManual) {
        CronJobHistoryEntity history = new CronJobHistoryEntity();
        history.setJobId(job.getId());
        history.setStartTime(LocalDateTime.now());
        history.setStatus("RUNNING");
        history.setTriggerType(isManual ? "MANUAL" : "SCHEDULED");
        history.setPromptContent(job.getPromptTemplate());
        historyInfra.save(history);
        return history.getId();
    }

    private String buildPrompt(CronJobEntity job) {
        String prompt = job.getPromptTemplate();
        if (prompt == null) {
            prompt = "请执行定时任务: " + job.getName();
        }
        prompt = prompt.replace("{{current_date}}", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        prompt = prompt.replace("{{current_time}}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        prompt = prompt.replace("{{job_name}}", job.getName());
        return prompt;
    }

    private void executeAgentTask(CronJobEntity job, Long targetUserId, String sessionId, String prompt) {
        ModelConfigEntity modelConfig = getDefaultModelConfig(targetUserId);
        if (modelConfig == null) {
            throw new RuntimeException("用户没有可用的模型配置");
        }

        ChatSessionEntity session = getOrCreateSession(targetUserId, modelConfig, sessionId);

        ChatMessageEntity userMsg = new ChatMessageEntity();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(prompt);
        chatMessageInfra.save(userMsg);

        List<Msg> messages = new ArrayList<>();
        messages.add(Msg.ofUser(prompt));

        String sysPrompt = buildSysPrompt(job);
        ReActAgent agent = chatAgentFactory.createAgent(modelConfig, targetUserId, session.getId(), sysPrompt);
        StreamOptions streamOptions = chatAgentFactory.buildStreamOptions();

        StringBuilder fullResponse = new StringBuilder();
        AtomicInteger promptTokens = new AtomicInteger(0);
        AtomicInteger completionTokens = new AtomicInteger(0);

        agent.stream(messages, streamOptions)
                .doOnNext(event -> {
                    if (event.getContent() != null) {
                        fullResponse.append(event.getContent().toString());
                    }
                })
                .blockLast();

        saveAssistantMessage(session, modelConfig, fullResponse.toString());
        chatSessionInfra.incrementTokenUsage(session.getId(), 0, 0, 0, 1);
    }

    private void executeTextTask(CronJobEntity job, Long targetUserId, String sessionId, String prompt) {
        ModelConfigEntity modelConfig = getDefaultModelConfig(targetUserId);
        if (modelConfig == null) {
            throw new RuntimeException("用户没有可用的模型配置");
        }

        ChatSessionEntity session = getOrCreateSession(targetUserId, modelConfig, sessionId);

        ChatMessageEntity userMsg = new ChatMessageEntity();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(prompt);
        chatMessageInfra.save(userMsg);

        String response = prompt + " - 任务已收到";
        saveAssistantMessage(session, modelConfig, response);
    }

    private ChatSessionEntity getOrCreateSession(Long userId, ModelConfigEntity modelConfig, String sessionId) {
        ChatSessionEntity session = chatSessionInfra.lambdaQuery()
                .eq(ChatSessionEntity::getName, sessionId)
                .eq(ChatSessionEntity::getUserId, userId)
                .one();

        if (session == null) {
            session = new ChatSessionEntity();
            session.setName(sessionId);
            session.setUserId(userId);
            session.setModelConfigId(modelConfig.getId());
            session.setDeptId(usersInfra.getUserById(userId).getDeptId());
            session.setTotalPromptTokens(0);
            session.setTotalCompletionTokens(0);
            session.setTotalTokens(0);
            session.setMessageCount(0);
            chatSessionInfra.save(session);
        }
        return session;
    }

    private ModelConfigEntity getDefaultModelConfig(Long userId) {
        return modelConfigInfra.lambdaQuery()
                .eq(ModelConfigEntity::getUserId, userId)
                .eq(ModelConfigEntity::getEnabled, 1)
                .one();
    }

    private String buildSysPrompt(CronJobEntity job) {
        return """
                你是 EvolveHub 智能助手，正在执行一个定时任务。

                任务名称: %s
                任务描述: %s

                请认真执行该任务，完成后给出简洁的报告。
                """.formatted(job.getName(), job.getDescription() != null ? job.getDescription() : "");
    }

    private void saveAssistantMessage(ChatSessionEntity session, ModelConfigEntity modelConfig, String content) {
        ChatMessageEntity assistantMsg = new ChatMessageEntity();
        assistantMsg.setSessionId(session.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(content);
        chatMessageInfra.save(assistantMsg);
    }

    private void updateJobSuccess(Long jobId) {
        try {
            CronExpression cron = CronExpression.parse(cronJobInfra.getById(jobId).getCronExpression());
            LocalDateTime nextRun = LocalDateTime.ofInstant(
                    cron.next(LocalDateTime.now()), ZoneId.systemDefault());

            cronJobInfra.lambdaUpdate()
                    .eq(org.evolve.domain.resource.model.CronJobEntity::getId, jobId)
                    .set(org.evolve.domain.resource.model.CronJobEntity::getLastRunStatus, "SUCCESS")
                    .set(org.evolve.domain.resource.model.CronJobEntity::getLastRunTime, LocalDateTime.now())
                    .set(org.evolve.domain.resource.model.CronJobEntity::getNextRunTime, nextRun)
                    .setNull(org.evolve.domain.resource.model.CronJobEntity::getLastRunError)
                    .update();
        } catch (Exception e) {
            log.warn("Failed to update next run time for job: {}", jobId);
        }
    }

    private void updateJobError(Long jobId, String error) {
        cronJobInfra.lambdaUpdate()
                .eq(org.evolve.domain.resource.model.CronJobEntity::getId, jobId)
                .set(org.evolve.domain.resource.model.CronJobEntity::getLastRunStatus, "ERROR")
                .set(org.evolve.domain.resource.model.CronJobEntity::getLastRunTime, LocalDateTime.now())
                .set(org.evolve.domain.resource.model.CronJobEntity::getLastRunError, error)
                .update();
    }

    private void updateHistorySuccess(Long historyId) {
        if (historyId == null) return;
        historyInfra.lambdaUpdate()
                .eq(CronJobHistoryEntity::getId, historyId)
                .set(CronJobHistoryEntity::getStatus, "SUCCESS")
                .set(CronJobHistoryEntity::getEndTime, LocalDateTime.now())
                .update();
    }

    private void updateHistoryError(Long historyId, String error) {
        if (historyId == null) return;
        historyInfra.lambdaUpdate()
                .eq(CronJobHistoryEntity::getId, historyId)
                .set(CronJobHistoryEntity::getStatus, "ERROR")
                .set(CronJobHistoryEntity::getEndTime, LocalDateTime.now())
                .set(CronJobHistoryEntity::getErrorMessage, error)
                .update();
    }
}
