package org.evolve.admin.api;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.admin.service.TokenUsageService;
import org.evolve.common.web.response.Result;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/user/token-usage")
public class TokenUsageController {

    @Resource
    private TokenUsageService tokenUsageService;

    @GetMapping("/records")
    public Result<List<ChatTokenUsageEntity>> getMyUsage(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ChatTokenUsageEntity> records = tokenUsageService.getUserUsage(userId, startDate, endDate);
        return Result.ok(records);
    }

    @GetMapping("/summary")
    public Result<Object> getMyUsageSummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Long userId = StpUtil.getLoginIdAsLong();
        int[] summary = tokenUsageService.getUserUsageSummary(userId, startDate, endDate);
        return Result.ok(new Object(){
            public final int totalRequests = summary[0];
            public final int totalPromptTokens = summary[1];
            public final int totalCompletionTokens = summary[2];
            public final int totalTokens = summary[3];
        });
    }
}
