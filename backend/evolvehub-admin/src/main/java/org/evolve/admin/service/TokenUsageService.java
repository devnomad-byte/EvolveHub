package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.resource.infra.ChatTokenUsageInfra;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TokenUsageService {

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    public void recordUsage(Long userId, Long modelConfigId, Long deptId,
                            int requestCount, int promptTokens, int completionTokens, int totalTokens) {
        LocalDate today = LocalDate.now();
        chatTokenUsageInfra.incrementUsage(userId, modelConfigId, today, deptId,
                requestCount, promptTokens, completionTokens, totalTokens);
    }

    public List<ChatTokenUsageEntity> getUserUsage(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        return chatTokenUsageInfra.listByUserAndDateRange(userId, startDate, endDate);
    }

    public int[] getUserUsageSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        List<ChatTokenUsageEntity> records = getUserUsage(userId, startDate, endDate);
        int totalRequests = 0, totalPrompt = 0, totalCompletion = 0, totalTokens = 0;
        for (ChatTokenUsageEntity record : records) {
            totalRequests += record.getRequestCount();
            totalPrompt += record.getPromptTokens();
            totalCompletion += record.getCompletionTokens();
            totalTokens += record.getTotalTokens();
        }
        return new int[]{totalRequests, totalPrompt, totalCompletion, totalTokens};
    }
}
