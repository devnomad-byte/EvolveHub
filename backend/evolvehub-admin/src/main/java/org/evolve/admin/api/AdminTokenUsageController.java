package org.evolve.admin.api;

import jakarta.annotation.Resource;
import org.evolve.admin.response.TokenUsageRecordResponse;
import org.evolve.admin.response.TokenUsageSummaryResponse;
import org.evolve.admin.response.TokenUsageUserResponse;
import org.evolve.admin.service.TokenUsageExportManager;
import org.evolve.admin.service.TokenUsageRecordsManager;
import org.evolve.admin.service.TokenUsageSummaryManager;
import org.evolve.admin.service.TokenUsageUserListManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token-usage")
public class AdminTokenUsageController {

    @Resource
    private TokenUsageUserListManager userListManager;

    @Resource
    private TokenUsageRecordsManager recordsManager;

    @Resource
    private TokenUsageSummaryManager summaryManager;

    @Resource
    private TokenUsageExportManager exportManager;

    /**
     * 获取有Token用量记录的用户列表
     */
    @GetMapping("/users")
    public Result<PageResponse<TokenUsageUserResponse>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(userListManager.execute(
                new TokenUsageUserListManager.Request(keyword, deptId, startDate, endDate, pageNum, pageSize)));
    }

    /**
     * 获取Token用量记录列表
     */
    @GetMapping("/records")
    public Result<PageResponse<TokenUsageRecordResponse>> listRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long modelConfigId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(recordsManager.execute(
                new TokenUsageRecordsManager.Request(userId, modelConfigId, keyword, startDate, endDate, pageNum, pageSize)));
    }

    /**
     * 获取Token用量汇总
     */
    @GetMapping("/summary")
    public Result<TokenUsageSummaryResponse> getSummary(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(summaryManager.execute(
                new TokenUsageSummaryManager.Request(userId, startDate, endDate)));
    }

    /**
     * 导出Token用量
     */
    @PostMapping("/export")
    public Result<TokenUsageExportManager.ExportResult> exportTokenUsage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "md") String format) {
        var result = exportManager.execute(
                new TokenUsageExportManager.Request(userId, startDate, endDate, format));
        return Result.ok(result);
    }
}
